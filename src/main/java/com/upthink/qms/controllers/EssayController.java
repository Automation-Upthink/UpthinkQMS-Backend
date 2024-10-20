package com.upthink.qms.controllers;

import com.google.gson.JsonObject;
import com.nimbusds.oauth2.sdk.Request;
import com.upthink.qms.domain.*;
import com.upthink.qms.service.*;
import com.upthink.qms.service.request.*;
import com.upthink.qms.service.response.*;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


import java.net.URL;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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

    @Value("${qmBucket}")
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
    EssayScriptLogService essayScriptLogService;

    @Autowired
    JwtService jwtService;

    @Autowired
    private EssayClientService essayClientService;

    @Autowired
    private PersonService personService;

    @Value("${aws.region}")
    private String awsRegion;

    @Autowired
    private EssayService essayService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${aws.accessKeyId}")
    private String awsAccessKeyId;

    @Value("${aws.secretKey}")
    private String awsSecretKey;


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


    @PostMapping("/checkExpiry")
    public BaseResponse checkExpiry(@RequestBody EmptyRequest request) {
        // get all the essay that are available
        List<String> statusList = new ArrayList<String>(Arrays.asList("available", "checked_out"));
        List<Essay> essayList = essayService.listEssaysByStatus(statusList);

        for (Essay essay : essayList) {
            Timestamp downloadAt = essay.getDownloadTime();
            Long downloadedAtTime = essay.getDownloadTime().getTime();
            Long timeToExpire = Long.valueOf(80 * 60 * 1000);
            Long expiresAtTime = downloadedAtTime + timeToExpire;
            Timestamp willExpireAt = new Timestamp(expiresAtTime);
            Timestamp currentTimestamp = new Timestamp(Instant.now().toEpochMilli());
            // check if the difference between current time and created_at is less then 80 mins
            if (currentTimestamp.after(willExpireAt) || currentTimestamp.equals(willExpireAt)) {
                // if it has crossed the threshold, mark the status as expired
                essayService.updateEssayStatus(essay.getId(), "expired");
                // update available download count for the same client cred
                essayClientCredService.updateDownloadCounter(essay.getCredId(), 1);
            }
        }
        return new BaseResponse(true, null);
    }



    @PostMapping("/presignUrl")
    public PresignedURLResponse essayPresignUrl(@RequestBody EssayPresignedURLRequest request) {
        // Set up AWS credentials and region
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey);
        String jobBucketKey = String.format("essays/%s/%s/%s", request.getFileId(), request.getFileType(), request.getFileName());

        // Use Date for expiration - 10 minutes from now
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plus(10, ChronoUnit.MINUTES)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());

        // Create an S3 presigner
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        PresignedURLResponse presignedURLResponse;

        // Handle GET action for presigned URL
        if (request.getFileAction() != null && request.getFileAction() == EssayPresignedURLRequest.FileAction.GET) {
            // Build GET request for S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(qmBucket)
                    .key(jobBucketKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10)) // Directly use Duration for simplicity
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            presignedURLResponse = new PresignedURLResponse(presignedRequest.url().toString(), request.getFileId(), request.getFileName());

            // Handle PUT action for presigned URL
        } else {
            // Build PUT request for S3
            PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                    .bucket(qmBucket)
                    .key(jobBucketKey);

            // Set content type if applicable
            if (request.getFileType() == EssayPresignedURLRequest.FileType.screenshot) {
                putObjectRequestBuilder.contentType("image/png");
            }
            // Additional file types can be handled here if needed

            PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10)) // Use Duration directly
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            presignedURLResponse = new PresignedURLResponse(presignedRequest.url().toString(), request.getFileId(), request.getFileName());
        }
        // Close the presigner to free resources
        presigner.close();
        return presignedURLResponse;
    }








    @PostMapping("/addEssay")
    public BaseResponse addEssay(@RequestBody CreateEssayRequest request) {
        try {
            Optional<EssayClient> essayClient = essayClientService.loadEssayClient(request.getClientName());
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
                int essayCount = essayService.createEssay(taskId,
                        request.getName(),
                        essayClient.get().getId(),
                        request.getCredId(),
                        fileLink,
                        request.getUuid(),
                        request.getDueDate());
                essayClientCredService.updateDownloadCounter(essayClientCred.get().getId(), -1);
                if (essayCount <= 0){
                    return new BaseResponse(true, "No essay added");
                } else {
                    return new BaseResponse(true, null);
                }
            }
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }

    @PostMapping("/listCredsForClient")
    public EssayClientCredResponse listCredentialsForClient(@RequestBody GetEssayCredRequest request) {
        Optional<EssayClient> essayClientOpt = essayClientService.loadEssayClient(request.getClientName());
        if (essayClientOpt.isPresent()) {
            EssayClient essayClient = essayClientOpt.get();
            int clientId = essayClient.getId();
            return essayClientCredService.listCredsForClient(clientId);
        } else{
            return null;
        }
    }




    @PostMapping("/updateEssayStatus")
    @PreAuthorize("hasRole('QM_AUTHOR')")
    public BaseResponse essayCheckIn(EssayStatusChangeRequest request) {
        try {
            String cognitoSub = request.getId().getSub();
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
            Optional<PersonAnalytics> personAnalytics = personAnalyticsService.loadPersonAnalytics(qmAuthor.get().getId());
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
                    essayService.updateEssayGradeTime(essay.get().getId(), gradeTime);
                    essayClientCredService.updateDownloadCounter(essay.get().getCredId(), 1);
                    break;
                default:
                    return new BaseResponse(false, "Not valid status input");
            }

            if (personAnalytics.isPresent()) {
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
    public EssayResponse listEssayRequest(ListEssayRequest request) {
        return essayService.listEssays(request.getClientId(), request.getEssayStatus());

    }


    @PostMapping("/fetchSingleEssay")
    @PreAuthorize("hasRole('QM_AUTHOR')")
    public BaseResponse fetchSingleEssay (FetchEssayRequest fetchEssayRequest) {
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
            List<EssayScriptLogResponse.EssayScriptLogDTO> essayScriptLogDTOS =
                    essayScriptLogService.getLatestScriptLog(fetchEssayRequest.getId().getSub(), "ENABLED","SINGLE");
            System.out.println(essayScriptLogDTOS);
            // its possible that the user didnt performed any fetch earlier so proceed
            if (essayScriptLogDTOS.size() > 0) {
                if (essayScriptLogDTOS.get(0).getTimeDiff() < 30) {
                    return new BaseResponse(false, "Wait till previous request to complete");
                }
            }
            int response =
                    essayScriptLogService.insertScriptLog(
                            fetchEssayRequest.getId().getSub(),
                            "ENABLED",
                            "SINGLE",
                            fetchEssayRequest.getClientName(),
                            uuidString);
            if (response == 1){
                invokeLambdaAsynchronously(uuidString, functionName, eventBody);
                return new BaseResponse(true, null);
            } else {
                return new BaseResponse(false, "Something went wrong, please try again");
            }
        } catch (Exception e) {
            return new BaseResponse(false, "False " + e.getMessage());
        }
    }


    @PostMapping("/latestSingleEntry")
    @PreAuthorize("hasRole('QM_AUTHOR')")
    public EssayScriptLogResponse latestSingleEntry(AuthenticatedRequest request) {
        try {
            List<EssayScriptLogResponse.EssayScriptLogDTO> essayScriptLogDTOS =
                    essayScriptLogService.getLatestScriptLog(
                            request.getId().getSub(), "ENABLED", "SINGLE");
            return new EssayScriptLogResponse(essayScriptLogDTOS, true, null);
        } catch (Exception e) {
            return new EssayScriptLogResponse(null, false, e.getMessage());
        }
    }


    private final Map<String, CompletableFuture<Void>> pendingRequests = new ConcurrentHashMap<>();

    private CompletableFuture<Void> invokeLambdaAsynchronously(String requestUUID, String functionName, String eventBody) {
        long threadId = Thread.currentThread().threadId();

        CompletableFuture<Void> newFuture = new CompletableFuture<>();
        System.out.println("" + functionName);
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
                                System.out.println("After invocation try block");
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
        InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
        // Handle the response
        String responsePayload = invokeResponse.payload().asUtf8String();
        System.out.println("Invoke Request " + invokeRequest);
        System.out.println("Response payload " + responsePayload);
    }


}
