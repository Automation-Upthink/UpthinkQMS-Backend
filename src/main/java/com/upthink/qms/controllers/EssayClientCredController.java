package com.upthink.qms.controllers;

import com.upthink.qms.domain.PersonPrincipal;
import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.CustomUserDetailsService;
import com.upthink.qms.service.EssayClientCredService;
import com.upthink.qms.service.JwtService;
import com.upthink.qms.service.request.AuthenticatedRequest;
import com.upthink.qms.service.request.CreateEssayClientCredRequest;
import com.upthink.qms.service.request.UpdateEssayClientCredRequest;
import com.upthink.qms.service.request.CreateEssayClientRequest;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.EssayClientCredResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    public BaseResponse addEssayClientCred(CreateEssayClientCredRequest  request) {
        try {
            int count  = essayClientCredService.createEssayClientCredential(
                    request.getName(),
                    request.getPassword(),
                    request.getClientName(),
                    request.getDownloadLimit(),
                    request.getId().getEmail()
            );
            if (count == 1) {
                return new BaseResponse(true, null);
            } else {
                return new BaseResponse(false, "Credential for the client not created");
            }
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }

    }


    @PostMapping("/updateClientCred")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse updateEssayClientCred(UpdateEssayClientCredRequest request) {
        try {
            return essayClientCredService.updateEssayClientCredential(
                    request.getClientName(),
                    request.getName(),
                    request.getPassword(),
                    request.getDownloadLimit(),
                    request.getDownloadLimit(),
                    request.getClientId());
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }


    @DeleteMapping("/deleteClientCred")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse deleteEssayClientCred(UpdateEssayClientCredRequest request) {
        try{
            int count = essayClientCredService.deleteEssayClientCredential(request.getClientId());
            if (count == 0) {
                return new BaseResponse(false, "Failed to delete the credentials");
            } else {
                return new BaseResponse(true, null);
            }

        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }


    @PostMapping("/listClientCreds")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public EssayClientCredResponse listEssayClientCred(AuthenticatedRequest request) {
        return essayClientCredService.listClientCreds();
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
