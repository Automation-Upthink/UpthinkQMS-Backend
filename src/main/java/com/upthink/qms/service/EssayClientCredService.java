package com.upthink.qms.service;

import com.upthink.qms.domain.EssayClient;
import com.upthink.qms.domain.EssayClientCred;
import com.upthink.qms.repository.EssayClientCredRepository;
import com.upthink.qms.repository.EssayClientRepository;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.EssayClientCredResponse;
import com.upthink.qms.service.response.EssayClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EssayClientCredService {

    private final EssayClientCredRepository essayClientCredRepository;
    private final EssayClientRepository essayClientRepository;
    private final EssayClientService essayClientService;

    @Autowired
    public EssayClientCredService(EssayClientCredRepository essayClientCredRepository,
                                  EssayClientRepository essayClientRepository,
                                  EssayClientService essayClientService) {
        this.essayClientCredRepository = essayClientCredRepository;
        this.essayClientRepository = essayClientRepository;
        this.essayClientService = essayClientService;
    }

    @Transactional
    public int createEssayClientCredential(String name, String password, String clientName, int downloadLimit, String emailId) {
        // check if the essay client exists or not
        Optional<EssayClient> essayClientOpt = essayClientRepository.findByName(clientName);
        System.out.println("Essay client " + essayClientOpt.get().getId());
        if (!essayClientOpt.isPresent()) {
            throw new IllegalArgumentException("Client with name " + clientName + " not found");
        }
        EssayClient essayClient = essayClientOpt.get();
        int essayClientDownloadCapacity = essayClient.getDownloadCapacity();
        // Check if the essay client cred download limit is more than client download capacity
        if(downloadLimit > essayClientDownloadCapacity) {
            throw new IllegalArgumentException("Cannot assign download capacity more than that of the client");
        }
//        EssayClientCred essayClientCred = new EssayClientCred(
//                name,
//                password,
//                emailId,
//                essayClient.getId(),
//                downloadLimit,
//                downloadLimit
//                );

//        int count = essayClientCredRepository.save(essayClientCred);

        // Insert using native query
        int count = essayClientCredRepository.insertEssayClientCred(
                name, password, emailId, essayClient.getId(), downloadLimit, downloadLimit
        );
        return count;
    }


    public Optional<EssayClientCred> loadCredWithId(int id) {
        return essayClientCredRepository.findById(id)
                .or(() -> {
                    throw new IllegalArgumentException("Client credential by the name " + id + " not found");
                });
    }


    @Transactional
    public BaseResponse updateEssayClientCredential(String clientName, String name, String password, int downloadLimit, int downloadRemaining, int id) {
        try {
            Optional<EssayClient> essayClient = essayClientRepository.findByName(clientName);
            if (!essayClient.isPresent()) {
                return new BaseResponse(false, "Client not found");
            }

            int totalClientDownloadLimit = essayClient.get().getDownloadCapacity();
            if (downloadLimit > totalClientDownloadLimit) {
                return new BaseResponse(
                        false,
                        String.format(
                                "Can't set download limit greater than client download capacity - %s",
                                totalClientDownloadLimit));
            }

            Optional<EssayClientCred> essayClientCred = essayClientCredRepository.findById(id);
            if (!essayClientCred.isPresent()) {
                return new BaseResponse(false, "Credential not found");
            }

            // Adjust download remaining based on the new download limit
            int diffInDL = essayClientCred.get().getDownloadLimit() - downloadLimit;
            int newDR = essayClientCred.get().getDownloadRemaining() - diffInDL;
            if (newDR < 0) {
                newDR = 0; // Ensure remaining downloads do not go below 0
            }

            int count = essayClientCredRepository.updateEssayClientCred(id, name, password, downloadLimit, newDR);
            if (count > 0) {
                return new BaseResponse(true, "Credentials updated successfully");
            }
            return new BaseResponse(false, "Failed to update the credentials");
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }



    @Transactional
    public int deleteEssayClientCredential(int id) {
        int count = essayClientCredRepository.deleteEssayClientCred(id);
        return count;
    }


    public List<EssayClientCred> listEssayClientCredsByClientId(int clientId){
        List<EssayClientCred> essayClientCredsList = essayClientCredRepository
                .getEssayClientCredsByClientId(clientId);
        return essayClientCredsList;
    }



    public EssayClientCredResponse listCredsForClient(int clientId) {
        Optional<EssayClient> essayClient = essayClientRepository.findById(clientId);
        String clientName = essayClient.get().getName();
        List<EssayClientCredResponse.EssayClientCredDTO> essayClientCredDTOList = new ArrayList<>();
        List<EssayClientCred> essayClientCredsList = essayClientCredRepository
                .getEssayClientCredsByClientId(clientId);
        essayClientCredDTOList = essayClientCredsList.stream()
                .map(
                        cred -> {
                            return new EssayClientCredResponse.EssayClientCredDTO(
                                    cred.getId(),
                                    cred.getName(),
                                    clientName,
                                    cred.getPassword(),
                                    cred.getCreatedBy(),
                                    cred.getDownloadLimit(),
                                    cred.getDownloadRemaining(),
                                    cred.getCreatedAt()
                            );
                        })
                .collect(Collectors.toList());
        return new EssayClientCredResponse(essayClientCredDTOList);

    }



    public EssayClientCredResponse listClientCreds(){
        List<EssayClientCred> essayClientCredList = essayClientCredRepository.listEssayClientCreds();
        List<EssayClientCredResponse.EssayClientCredDTO> essayClientCredDTOList = new ArrayList<>();
        if (essayClientCredList.isEmpty()) {
            return new EssayClientCredResponse(essayClientCredDTOList);
        }
        Set<Integer> uniqueClientIDs = essayClientCredList.stream()
                .map(EssayClientCred::getClientId)
                .collect(Collectors.toSet());

        Map<Integer, String> mapClientNameId = essayClientService.mapNameToId(uniqueClientIDs);

        essayClientCredDTOList = essayClientCredList.stream()
                .map(essayClientCred ->
                    {
                        return new EssayClientCredResponse.EssayClientCredDTO(
                                essayClientCred.getId(),
                                essayClientCred.getName(),
                                // Way to know the client name
                                mapClientNameId.get(essayClientCred.getClientId()),
                                essayClientCred.getPassword(),
                                essayClientCred.getCreatedBy(),
                                essayClientCred.getDownloadLimit(),
                                essayClientCred.getDownloadRemaining(),
                                essayClientCred.getCreatedAt()
                                );
                    })
                .collect(Collectors.toList());

        return new EssayClientCredResponse(essayClientCredDTOList);
    }

    public void updateDownloadCounter(int id, int count) {
        essayClientCredRepository.updateDownloadCounter(id, count);
    }


}
