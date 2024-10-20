package com.upthink.qms.controllers;

import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.EssayClientService;
import com.upthink.qms.service.EssayScriptLogService;
import com.upthink.qms.service.JwtService;
import com.upthink.qms.service.request.AuthenticatedRequest;
import com.upthink.qms.service.request.ChangeEventRequest;
import com.upthink.qms.service.request.CreateEssayClientRequest;
import com.upthink.qms.service.request.UpdateEssayClientRequest;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.EssayClientResponse;
import com.upthink.qms.service.response.EssayScriptLogResponse;
import org.jdbi.v3.core.Handle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.ListRulesRequest;
import software.amazon.awssdk.services.eventbridge.model.ListRulesResponse;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.Rule;
import software.amazon.awssdk.services.eventbridge.model.EventBridgeException;

@RestController
@RequestMapping("/qm")
public class EssayClientController {

    private final EssayClientService essayClientService;
    private final AuthService authService;
    private final EssayScriptLogService essayScriptLogService;
    private final JwtService jwtService;


    @Autowired
    public EssayClientController(EssayClientService essayClientService,
                                 EssayScriptLogService essayScriptLogService,
                                 AuthService authService,
                                 JwtService jwtService) {
        this.essayClientService = essayClientService;
        this.essayScriptLogService =essayScriptLogService;
        this.authService =authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/addClient")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse addEssayClient(CreateEssayClientRequest request) {
        essayClientService.createEssayClient(request.getName(), request.getDownloadCap());
        return new BaseResponse(true, null);
    }

    @DeleteMapping("/deleteClient")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse deleteEssayClient(UpdateEssayClientRequest request) {
        try {
            int deleteCount = essayClientService.deleteEssayClient(request.getClientId());
            if (deleteCount == 0) {
                return new BaseResponse(false, "The client has already been deleted previously");
            } else {
                return new BaseResponse(true, null);
            }
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }

    @PostMapping("/updateClient")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse updateEssayClient(UpdateEssayClientRequest request) {
        try {
            int updatedRows = essayClientService
                    .updateEssayClient(request.getClientId(), request.getName(), request.getDownloadCap());
            if (updatedRows <= 0)
                return new BaseResponse(false, "Failed to update, please check the client id or check if the client is deleted");
            else
                return new BaseResponse(true, null);
        } catch(Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }


    @PostMapping("/listClients")
    @PreAuthorize("hasRole('QM_ADMIN') or hasRole ('QM_AUTHOR')")
    public EssayClientResponse listActiveClients(AuthenticatedRequest request) {
        return essayClientService.listActiveEssayClients();
    }


    @PostMapping("/changeEvent")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse changeEvent(ChangeEventRequest request) {
        try {
            String cognitoSub = request.getId().getSub();
            // Create the EventBridge client
            EventBridgeClient ebClient = EventBridgeClient.builder().build();

            // List rules and check if the rule is present in AWS
            ListRulesRequest listRulesRequest = ListRulesRequest.builder().build();
            ListRulesResponse listRulesResponse = ebClient.listRules(listRulesRequest);

            String state = request.isStart() ? "ENABLED" : "DISABLED";

            for (Rule rule : listRulesResponse.rules()) {
                if (rule.name().matches(request.getRuleName())) {
                    String existingScheduleExpression = rule.scheduleExpression();

                    PutRuleRequest ruleRequest = PutRuleRequest.builder()
                            .name(request.getRuleName())
                            .scheduleExpression(existingScheduleExpression)
                            .state(state)
                            .build();

                    ebClient.putRule(ruleRequest);

                    try {
                        essayScriptLogService.insertScriptLog(
                                cognitoSub,
                                state,
                                "ALL",
                                request.getClientName(),
                                "allFetch"
                        );
                        return new BaseResponse(true, null);
                    } catch (Exception e) {
                        return new BaseResponse(false, e.getMessage());
                    }
                }
            }
        return new BaseResponse(false, "No rule with given name.");
        } catch (EventBridgeException e) {
            return new BaseResponse(false, e.getMessage());
        }
    }



}
