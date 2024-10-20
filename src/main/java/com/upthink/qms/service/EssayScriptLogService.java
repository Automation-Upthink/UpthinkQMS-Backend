package com.upthink.qms.service;

import com.upthink.qms.domain.EssayScriptLog;
import com.upthink.qms.repository.EssayScriptLogRepository;
import com.upthink.qms.service.response.EssayScriptLogResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EssayScriptLogService {

    @Autowired
    private EssayScriptLogRepository essayScriptLogRepository;

    public int insertScriptLog(
            String personId,
            String actionPerformed,
            String type,
            String clientName,
            String uuid) {

        return essayScriptLogRepository.insertScriptLog(personId, actionPerformed, type, clientName, uuid);
    }

    public List<EssayScriptLogResponse.EssayScriptLogDTO> getLatestScriptLog(
            String personId,
            String actionPerformed,
            String type) {
        EssayScriptLog essayScriptLog = essayScriptLogRepository.getEssayScriptLog(personId, actionPerformed, type);
        if (essayScriptLog == null) {
            return new ArrayList<>();
        }
        Timestamp createdAt = essayScriptLog.getCreatedAt();
        LocalDateTime timestampLocalDateTime = createdAt.toLocalDateTime();
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        Duration duration = Duration.between(timestampLocalDateTime, currentLocalDateTime);
        long secondsDifference = duration.toSeconds();
        System.out.println("Time diff in SECONDS  " + secondsDifference);

        EssayScriptLogResponse.EssayScriptLogDTO essayScriptLogDTO = new EssayScriptLogResponse.EssayScriptLogDTO(
                essayScriptLog.getId(),
                essayScriptLog.getPersonId(),
                essayScriptLog.getClientName(),
                essayScriptLog.getActionPerformed(),
                essayScriptLog.getType(),
                essayScriptLog.getCreatedAt(),
                (int) secondsDifference
        );

        List<EssayScriptLogResponse.EssayScriptLogDTO> result = new ArrayList<>();
        result.add(essayScriptLogDTO);
        return result;
    }
}
