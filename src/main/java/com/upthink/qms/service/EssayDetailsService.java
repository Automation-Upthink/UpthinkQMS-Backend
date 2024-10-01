package com.upthink.qms.service;

import com.upthink.qms.domain.EssayDetails;
import com.upthink.qms.domain.QmAnalytics;
import com.upthink.qms.repository.EssayDetailsRepository;
import org.jdbi.v3.core.Handle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EssayDetailsService {

    @Autowired
    private final EssayDetailsRepository essayDetailsRepository;

    public EssayDetailsService(EssayDetailsRepository essayDetailsRepository) {
        this.essayDetailsRepository = essayDetailsRepository;
    }

    public void createEssayDetails(String essayId, String personId, String userAction){
        int row = essayDetailsRepository.insertEssayDetails(essayId, personId, userAction);
    }


    public Optional<EssayDetails> getLatestEssayDetailForEssay(String essayId) {
        return Optional.ofNullable(
                essayDetailsRepository.getLatestEssayDetailByEssayId(essayId)
        ).orElse(Optional.empty());
    }


    public List<EssayDetails> getEssayLogsByPersonId(String personId, String startDate, String endDate, boolean singleDay){
        return singleDay ?
                essayDetailsRepository.getEssayLogsByPersonIdSingleDay(personId, startDate, endDate).stream()
                        .map(this::mapToEssayDetails)
                        .collect(Collectors.toList()) :
                essayDetailsRepository.getEssayLogsByPersonId(personId, startDate, endDate).stream()
                        .map(this::mapToEssayDetails)
                        .collect(Collectors.toList());
    }



    public List<EssayDetails> getAllEssayDetailsByTime(String startDate, String endDate) {
        List<Object[]> essayDetails = essayDetailsRepository.getAllEssayDetailsByTime(startDate, endDate);
        return essayDetails.stream()
                .map(this::mapToEssayDetails)
                .collect(Collectors.toList());
    }


    public List<EssayDetails> getAllEssayDetails() {
        List<Object[]> results = essayDetailsRepository.getAllEssayDetails();
        return results.stream()
                .map(this::mapToEssayDetails)
                .collect(Collectors.toList());

    }


    public List<QmAnalytics> getGetUserActionCounts(String startDate, String endDate, boolean yesterday){
        List<Map<String, Object>> results = yesterday ? essayDetailsRepository.getUserActionCountsByGroupYesterday(startDate, endDate) :
                essayDetailsRepository.getUserActionCountsByGroup(startDate, endDate);

        return results.stream()
                .map(this::mapToQmAnalyticsDTO)
                .collect(Collectors.toList());
    }


    public Optional<QmAnalytics> getAvgGradeTimeForPerson(String startDate, String endDate, String personId, boolean yesterday){
        List<Map<String, Object>> results = yesterday ? essayDetailsRepository
                .getAvgGradeTimeYesterday(startDate, endDate, personId) :
        essayDetailsRepository.getAvgGradeTime(startDate, endDate, personId);

        return results.stream()
                .map(this::mapToQmAnalyticsAvgGradeTime)
                .findFirst();
    }


    public List<QmAnalytics> getUserActionCountsByClientId(String startDate,
                                                                     String endDate,
                                                                     int clientId,
                                                                     boolean yesterday){
        List<Map<String, Object>> results = yesterday ? essayDetailsRepository
                .getUserActionCountsByGroupAndClientIdYesterday(startDate, endDate, clientId) :
                essayDetailsRepository.getUserActionCountsByGroupAndClientId(startDate, endDate, clientId);

        return results.stream()
                .map(this::mapToQmAnalyticsDTO)
                .collect(Collectors.toList());
    }


    public Optional<QmAnalytics> getAvgGradeTimeForPersonAndClient(
            String startDate,
            String endDate,
            String personId,
            int clientId,
            boolean yesterday) {
        List<Map<String, Object>> results = yesterday ? essayDetailsRepository
                .getAvgGradeTimeByClientIdYesterday(startDate, endDate, personId, clientId) :
                essayDetailsRepository.getAvgGradeTimeByClientId(startDate, endDate, personId, clientId);
        return results.stream()
                .map(this::mapToQmAnalyticsAvgGradeTime)
                .findFirst();
    }


    private QmAnalytics mapToQmAnalyticsAvgGradeTime(Map<String, Object> result) {
        String personId = (String) result.get("personId");
        int avgGradeTime = ((Number) result.get("avgGradeTime")).intValue();

        // Assuming QmAnalytics has a constructor that accepts personId and avgGradeTime
        return new QmAnalytics(personId, 0, 0, 0, avgGradeTime);
    }

    private QmAnalytics mapToQmAnalyticsDTO(Map<String, Object> result) {
        String personId = (String) result.get("personId");
        String personName = (String) result.get("personName");
        String personEmail = (String) result.get("personEmail");
        int checkedInCount = ((Number) result.get("checkInNum")).intValue();
        int checkedOutCount = ((Number) result.get("checkOutNum")).intValue();
        int availableCount = ((Number) result.get("reuploadNum")).intValue();

        return new QmAnalytics(personId, personName, personEmail, checkedInCount, checkedOutCount, availableCount, 0);
    }




    private EssayDetails mapToEssayDetails(Object[] result) {
        int id = (int) result[0];
        String personId = (String) result[1];
        String essayId = (String) result[2];
        String userAction = (String) result[3];
        Timestamp createdAt = (Timestamp) result[4];

        return new EssayDetails(id, personId, essayId, userAction, createdAt);
    }
}
