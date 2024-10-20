package com.upthink.qms.service;


import com.upthink.qms.domain.EssayClient;
import com.upthink.qms.repository.EssayClientRepository;
import com.upthink.qms.service.request.UpdateEssayClientRequest;
import com.upthink.qms.service.response.EssayClientResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EssayClientService {
    private final EssayClientRepository essayClientRepository;

    @Autowired
    public EssayClientService(EssayClientRepository essayClientRepository) {
        this.essayClientRepository = essayClientRepository;
    }

    @Transactional
    public void createEssayClient(String name, int downloadCapacity) {
        // If an essay client with the same name is already present then dont add the new essay client
        if(essayClientRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Client by the name " + name + " is already present");
        }
        EssayClient client = new EssayClient(name, downloadCapacity);
        essayClientRepository.save(client);
    }

    public EssayClientResponse loadEssayClientByName(String name) {
        Optional<EssayClient> clientOpt = essayClientRepository.findByName(name);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Client by the name " + name + " not found");
        }
        EssayClient essayClient = clientOpt.get();
        EssayClientResponse.EssayClientDTO essayClientDTO = new EssayClientResponse
                .EssayClientDTO(
                        essayClient.getId(),
                        essayClient.getName(),
                        essayClient.getDownloadCapacity());
        return new EssayClientResponse(List.of(essayClientDTO));
    }


    public Optional<EssayClient> loadEssayClient(String name) {
        return essayClientRepository.findByName(name);
    }

    @Transactional
    public EssayClientResponse listActiveEssayClients() {
        List<EssayClient> essayClients = essayClientRepository.findAllActive();
        if (essayClients.isEmpty()) {
            throw new IllegalArgumentException("No clients found");
        }
        List<EssayClientResponse.EssayClientDTO> essayClientDTOList = essayClients
                .stream()
                .map(
                        essayClient ->  {
                            return new EssayClientResponse.EssayClientDTO(
                                    essayClient.getId(),
                                    essayClient.getName(),
                                    essayClient.getDownloadCapacity()
                            );
                        })
                .collect(Collectors.toList());
        return new EssayClientResponse(essayClientDTOList);
    }

    @Transactional
    public int updateEssayClient(int clientId, String name, int downloadCapacity) {
        return essayClientRepository.updateClient(clientId, name, downloadCapacity);  // updated_at handled by the repository
    }

    @Transactional
    public int deleteEssayClient(int clientId) {
        return essayClientRepository.softDeleteById(clientId);  // deleted_at handled by the repository
    }

    public Map<Integer, String> mapNameToId(Set<Integer> ids) {
        List<EssayClient> essayClientList = essayClientRepository.findByIdIn(ids);
        Map<Integer, String> clientNameToIdMap = new HashMap<>();
        essayClientList.forEach(client -> clientNameToIdMap.put(client.getId(), client.getName()));
        return clientNameToIdMap;
    }
}
