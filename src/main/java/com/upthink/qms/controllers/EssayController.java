package com.upthink.qms.controllers;

import com.google.gson.JsonObject;
import com.nimbusds.oauth2.sdk.Request;
import com.upthink.qms.domain.*;
import com.upthink.qms.service.*;
import com.upthink.qms.service.request.CreateEssayRequest;
import com.upthink.qms.service.request.EssayStatusChangeRequest;
import com.upthink.qms.service.request.FetchEssayRequest;
import com.upthink.qms.service.request.ListEssayRequest;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.EssayResponse;
import org.json.JSONObject;
//import org.junit.platform.commons.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;


import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/qm")
public class EssayController {

//    private static final Logger logger = (Logger) LoggerFactory.getLogger(EssayController.class);

    @Value("${functionName}")
    private String functionName;

    @Value("$qmBucket")
    private String qmBucket;

    @Autowired
    AuthService authService;

    @Autowired
    EssayDetailsService essayDetailsService;

    @Autowired
    EssayClientCredService essayClientCredService;

    @Autowired
    PersonAnalyticsService personAnalyticsService;

    @Autowired
    JwtService jwtService;

    @Autowired
    private EssayClientService essayClientService;

    @Autowired
    private PersonService personService;

    @Autowired
    private EssayService essayService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public EssayController(EssayService essayService) {
        this.essayService = essayService;
    }

    final Object insertLock = new Object();

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private Optional<Timestamp> getLastEntryTimestamp(String taskId) {
        String sql = "SELECT created_at FROM essay WHERE id LIKE :taskId ORDER BY created_at DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("taskId", "%" + taskId);

        List<Timestamp> timestamps = namedParameterJdbcTemplate.query(sql, params,
                (rs, rowNum) -> rs.getTimestamp("created_at"));

        return timestamps.stream().findFirst();
    }


    @PostMapping("/addEssay")
    @PreAuthorize("hasRole('QM_AUTHOR')")
    public BaseResponse addEssay(CreateEssayRequest request) {
        try {

            Optional<EssayClient> essayClient = essayClientService.loadEssayClient(request.getName());
            Optional<EssayClientCred> essayClientCred =
                    essayClientCredService.loadCredWithId(request.getCredId());
            if (!essayClient.isPresent()) {
                return new BaseResponse(
                        false, String.format("%s : no such client found", request.getName()));
            }
            if (!essayClientCred.isPresent()) {
                return new BaseResponse(
                        false,
                        String.format("%s : no such cred account found", request.getCredId()));
            }
            List<EssayClientCred> essayClientCredList = essayClientCredService.listEssayClientCredsByClientId(essayClient.get().getId());
            List<Integer> credIds =
                    essayClientCredList.stream().map(c -> c.getId()).collect(Collectors.toList());
            System.out.println(essayClientCredList);
            if (!credIds.contains(request.getCredId())) {
                return new BaseResponse(
                        false,
                        String.format(
                                "cred %s does not belong to client %s",
                                request.getCredId(), request.getName()));
            }
            String fileLink =
                    String.format(
                            "https://%s.s3.ap-south-1.amazonaws.com/essays/%s/file/%s",
                            qmBucket, request.getTaskId(), request.getName());
            System.out.println("file Link " + fileLink);
            synchronized (insertLock) {
                Optional<Timestamp> lastEntryTimestamp = getLastEntryTimestamp(request.getTaskId());
                String taskId = request.getTaskId();
                if (lastEntryTimestamp.isPresent()) {
                    long currentTime = System.currentTimeMillis();
                    long twoHoursAgo = currentTime - (2 * 60 * 60 * 1000);
                    if (lastEntryTimestamp.get().getTime() < twoHoursAgo) {
                        UUID uuid = UUID.randomUUID();

                        // Convert UUID to a String
                        String uuidString = uuid.toString();
                        // Generate a new unique ID with a UUID and retry
                        String first6Chars = uuidString.substring(0, 6);
                        int random4 = 1000 + new Random().nextInt(9000);
                        taskId = first6Chars + random4 + "_" + taskId;
                    } else {
                        System.out.println(
                                "Last entry was created within the last 2 hours. Cannot insert.");
                        return new BaseResponse(false, "Looks like a duplicate entry");
                    }
                }
                essayService.createEssay(taskId,
                        request.getName(),
                        essayClient.get().getId(),
                        request.getCredId(),
                        fileLink,
                        request.getUuid(),
                        request.getDueDate());
                essayClientCredService.updateDownloadCounter(essayClientCred.get().getId(), -1);
                return new BaseResponse(true, null);
            }
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }



    @PostMapping("/updateEssayStatus")
    @PreAuthorize("hasRole('QM_AUTHOR')")
    public BaseResponse essayCheckIn(@RequestBody EssayStatusChangeRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String token = authService.handleAuthenticatedUser(authentication);
            String cognitoSub = jwtService.extractCognitoSub(token);
//            String cognitoSub = request.getId().getSub();
            // Load person by ID (userId is usually from authentication)
            Optional<Person> qmAuthor = personService.findPersonById(cognitoSub);
            if (!qmAuthor.isPresent()) {
                return new BaseResponse(false, "Person does not exist");
            }
            Optional<Essay> essay = essayService.loadEssayById(request.getEssayId());

            if(!essay.isPresent()){
                return new BaseResponse(false, String.format("Essay does not exist"));
            }

            Optional<EssayDetails> latestEssayDetails = essayDetailsService
                    .getLatestEssayDetailForEssay(essay.get().getId());
            System.out.println(latestEssayDetails);
            Optional<PersonAnalytics> personAnalytics = personAnalyticsService.loadPersonAnalytics(qmAuthor.get().getId());
            System.out.println("Person " + personAnalytics);
            String newStatus;
            long gradeTime = 0;
            switch(request.getStatus()) {
                case "checked_out":
                    newStatus = "checked_out";
                    if(latestEssayDetails.isPresent()){
                        String latestEssayStatus = latestEssayDetails.get().getUserAction();
                        switch(latestEssayStatus){
                            // check out only if the essay is made available by someone or no
                            // one has already if it's already checked in or essay expired
                            case "available":
                                essayDetailsService.createEssayDetails(request.getEssayId(), qmAuthor.get().getId(), newStatus);
                                essayService.updateEssayStatus(essay.get().getId(), newStatus);
                                break;
                            case "checked_out":
                                return new BaseResponse(false, "Essay has been already checked out");
                            default:
                                return new BaseResponse(false, "Essay cannot be checked out");
                        }
                    } else {
                        essayDetailsService.createEssayDetails(request.getEssayId(), qmAuthor.get().getId(), newStatus);
                        essayService.updateEssayStatus(essay.get().getId(), newStatus);
                        break;
                    }
                    break;
                case "available":
                    newStatus = "available";
                    // can only be made available if the essay is checked out
                    if (latestEssayDetails.isEmpty()
                            || !latestEssayDetails.get().getUserAction().equals("checked_out")) {
                        return new BaseResponse(false, "Essay is not in checked out state");
                    }
                    // only be made available by the person who checked it out
                    if(!latestEssayDetails.get().getPersonId().equals(qmAuthor.get().getId())) {
                        return new BaseResponse(
                                false, "You are not authorized to make essay available");
                    }
                    essayDetailsService.createEssayDetails(request.getEssayId(), qmAuthor.get().getId(), newStatus);
                    essayService.updateEssayStatus(essay.get().getId(), newStatus);
                    break;
                case "checked_in":
                    newStatus = "checked_in";
                    if (latestEssayDetails.isEmpty()
                            || !latestEssayDetails.get().getUserAction().toString().equals("checked_out")) {
                        return new BaseResponse(false, "Essay is not checked out");
                    }

                    if (!latestEssayDetails.get().getPersonId().equals(request.getId().getSub())) {
                        return new BaseResponse(false, "You are not authorized to check in");
                    }
                    essayDetailsService.createEssayDetails(request.getEssayId(), qmAuthor.get().getId(), newStatus);
                    essayService.updateEssayStatus(essay.get().getId(), newStatus);

                    Timestamp checkOutTime = latestEssayDetails.get().getCreatedAt();
                    Date checkedInDate = new Date();
                    Timestamp checkedInTime = new Timestamp(checkedInDate.getTime());
                    gradeTime = checkedInTime.getTime() - checkOutTime.getTime();
                    System.out.println("grade duration " + gradeTime);
                    essayService.updateEssayGradeTime(essay.get().getId(), gradeTime);
                    essayClientCredService.updateDownloadCounter(essay.get().getCredId(), 1);
                    break;
                default:
                    return new BaseResponse(false, "Not valid status input");
            }

            if (personAnalytics.isPresent()) {
                System.out.println("If person analytics present");
                int updateCheckOutCount = newStatus.equals("checked_out") ? 1 : 0;
                int updateCheckInCount = newStatus.equals("checked_in") ? 1 : 0;
                int updatereuploadCount = newStatus.equals("available") ? 1 : 0;
                long avgGradeTime = personAnalytics.get().getAvgGradeTime();

                if (newStatus.equals("checked_in")) {
                    int totalCheckIn = personAnalytics.get().getCheckInNum();
                    totalCheckIn++;
                    avgGradeTime = (avgGradeTime * (totalCheckIn - 1) + gradeTime) / totalCheckIn;
                }
                personAnalyticsService.updatePersonAnalytics(
                        personAnalytics.get().getId(),
                        updateCheckOutCount,
                        updateCheckInCount,
                        updatereuploadCount,
                        avgGradeTime);
            } else {
                personAnalyticsService.createPersonAnalytics(qmAuthor.get().getId());
            }
            return new BaseResponse(true, null);
        } catch(Exception e) {
            return new BaseResponse(false, "Person does not exist " + e);
        }
    }


    @PostMapping("/listEssays")
    @PreAuthorize("hasRole('QM_AUTHOR')")
    public EssayResponse listEssayRequest(@RequestBody ListEssayRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = authService.handleAuthenticatedUser(authentication);
        String cognitoSub = jwtService.extractCognitoSub(token);
        // Load person by ID (userId is usually from authentication)
        Optional<Person> qmAuthor = personService.findPersonById(cognitoSub);
//        Optional<Essay> essay = essayService.loadEssayById(request.getEssayId());
        return essayService.listEssays(request.getClientId(), request.getEssayStatus());

    }



    @PostMapping("/fetchSingleEssay")
    @PreAuthorize("hasRole('QM_AUTHOR')")
    public ResponseEntity<BaseResponse> fetchSingleEssay (@RequestBody FetchEssayRequest fetchEssayRequest) {
        try {
            if (fetchEssayRequest.getClientName() == null || fetchEssayRequest.getClientName().isEmpty()) {
                return null;
            }
            // Generate uuid
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();
            String eventBody = new JSONObject()
                    .put("clientName", fetchEssayRequest.getClientName())
                    .put("singleDownload", true)
                    .put("uuid", uuidString)
                    .toString();

            System.out.println("Event body : " + eventBody);

            invokeLambdaAsynchronously(uuidString, functionName, eventBody);
            return ResponseEntity.ok(new BaseResponse(true, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new BaseResponse(false, e.getMessage()));
        }
    }

    private final Map<String, CompletableFuture<Void>> pendingRequests = new ConcurrentHashMap<>();

    private CompletableFuture<Void> invokeLambdaAsynchronously(String requestUUID, String functionName, String eventBody) {
        long threadId = Thread.currentThread().threadId();

        CompletableFuture<Void> newFuture = new CompletableFuture<>();
        System.out.println(functionName);
        if(!pendingRequests.containsKey(requestUUID)) {
            // The uuid was not in the map, indicating a new request
            CompletableFuture<Void> finalNewFuture = newFuture;
            newFuture = CompletableFuture.supplyAsync(
                    () -> {
                        final CompletableFuture<Void> runningFuture =
                                pendingRequests.putIfAbsent(requestUUID, finalNewFuture);
                        if (runningFuture == null) {
                            try {
                                invokeLambda(functionName, eventBody);
                            } catch(Exception e) {
                                System.out.println("Invoke lambda asynchronously " + e.getMessage());
                            }
                        }
                        return null;
                    });
        } else {
            // A request with the same UUID is already being processed
            newFuture = pendingRequests.get(requestUUID);
        }
        return newFuture;
    }


    private void invokeLambda(String functionName, String eventBody) {
        LambdaClient lambdaClient = LambdaClient.create();
        InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromUtf8String(eventBody))
                .build();
        System.out.println("Invoke Request " + invokeRequest);
        InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
        // Handle the response
        String responsePayload = invokeResponse.payload().asUtf8String();
        System.out.println("Lambda response: " + responsePayload);
    }


}
