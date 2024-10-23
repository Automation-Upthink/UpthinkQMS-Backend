package com.upthink.qms.service;

import com.upthink.qms.domain.EssayDetails;
import com.upthink.qms.domain.QmAnalytics;
import com.upthink.qms.repository.EssayDetailsRepository;
import com.upthink.qms.service.response.PersonAnalyticsResponse;
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
                .map(this::mapToEssayExportDetails)
                .collect(Collectors.toList());
    }


    public List<EssayDetails> getAllEssayDetails() {
        List<Object[]> results = essayDetailsRepository.getAllEssayDetails();
        return results.stream()
                .map(this::mapToEssayExportDetails)
                .collect(Collectors.toList());

    }


    public List<QmAnalytics> getGetUserActionCounts(String startDate, String endDate, boolean yesterday){
        List<Map<String, Object>> results = yesterday ? essayDetailsRepository.getUserActionCountsByGroupYesterday(startDate, endDate) :
                essayDetailsRepository.getUserActionCountsByGroup(startDate, endDate);
        System.out.println("Service layer results " + results);
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

    public List<QmAnalytics> getUserActionAnalytics(String startDate, String endDate) {
        List<Map<String, Object>> results = essayDetailsRepository
                .getPersonAnalyticsByDate(startDate, endDate);
        return results.stream()
                .map(this::mapToQmAnalyticsDTO)
                .collect(Collectors.toList());
    }

    public Optional<QmAnalytics> getAvgGradeTimeForPersonDownload(String startDate,
                                                                  String endDate,
                                                                  String personId) {
        List<Map<String, Object>> results = essayDetailsRepository
                .getAvgGradeTimeByPersonId(startDate, endDate, personId);
        return results.stream()
                .map(this::mapToQmAnalyticsAvgGradeTime)
                .findFirst();
    }








    // *********************************************
    // Mapper Functions


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
        int checkedInCount = ((Number) result.get("checkedInCount")).intValue();
        int checkedOutCount = ((Number) result.get("checkedOutCount")).intValue();
        int availableCount = ((Number) result.get("availableCount")).intValue();

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


    private EssayDetails mapToEssayExportDetails(Object[] result) {
        int id = (int) result[0];
        String personId = (String) result[1];
        String essayId = (String) result[2];
        String userAction = (String) result[3];
        Timestamp createdAt = (Timestamp) result[4];
        Timestamp updatedAt = (Timestamp) result[5];  // if needed
        Timestamp deletedAt = (Timestamp) result[6];  // if needed

        Timestamp downloadedAt = (Timestamp) result[7]; // e.created_at as downloaded_at
        String clientName = (String) result[8];         // c.name as client_name
        String credName = (String) result[9];           // cc.name as cred_name

        return new EssayDetails(
                id,
                personId,
                essayId,
                userAction,
                createdAt,
                clientName,
                credName,
                downloadedAt);
    }

}
