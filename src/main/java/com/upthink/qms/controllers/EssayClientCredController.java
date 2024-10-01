package com.upthink.qms.controllers;

import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.EssayClientCredService;
import com.upthink.qms.service.JwtService;
import com.upthink.qms.service.request.CreateEssayClientCredRequest;
import com.upthink.qms.service.request.UpdateEssayClientCredRequest;
import com.upthink.qms.service.request.CreateEssayClientRequest;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.EssayClientCredResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qm")
public class EssayClientCredController {

//    @Autowired
    private AuthService authService;

//    @Autowired
    private JwtService jwtService;

    private Authentication authentication;

    private EssayClientCredService essayClientCredService;


    @Autowired
    public EssayClientCredController(EssayClientCredService essayClientCredService,
                                     AuthService authService,
                                     JwtService jwtService) {
        this.essayClientCredService = essayClientCredService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/addClientCred")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse addEssayClientCred(@RequestBody CreateEssayClientCredRequest request) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = authService.handleAuthenticatedUser(authentication);
        String email = jwtService.extractEmail(token);
        System.out.println("Email " + email);
        essayClientCredService.createEssayClientCredential(
                request.getName(),
                request.getPassword(),
                request.getClientName(),
                request.getDownloadLimit(),
                email
        );
        return new BaseResponse(true, null);
    }


    @PostMapping("/updateClientCred")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse updateEssayClientCred(@RequestBody UpdateEssayClientCredRequest request) {
        return essayClientCredService.updateEssayClientCredential(
                request.getClientName(),
                request.getName(),
                request.getPassword(),
                request.getDownloadLimit(),
                request.getDownloadLimit(),
                request.getClientId());
    }


    @PostMapping("/deleteClientCreds")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse deleteEssayClientCred(@RequestBody UpdateEssayClientCredRequest request) {
        try{
            int count = essayClientCredService.deleteEssayClientCredential(request.getClientId());
            return new BaseResponse(false, "Failed to delete the credentials");
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }


    @GetMapping("/listClientCreds")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse listEssayClientCred() {
        try {
            essayClientCredService.listClientCreds().toPrettyString();
            return new BaseResponse(true, null);
        } catch (Exception e) {
            return new BaseResponse(false, e.toString());
        }
    }


//    public BaseResponse listCredsForEssayClient(@RequestBody CreateEssayClientCredRequest request) {
//        try {
//            essayClientCredService
//                    .listClientCredsForAClient(request.getClientName())
//                            .toPrettyString();
//            return new BaseResponse(true, null);
//        } catch (Exception e){
//            return new BaseResponse(false, e.toString());
//        }
//    }









}
