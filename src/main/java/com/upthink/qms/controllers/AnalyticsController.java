package com.upthink.qms.controllers;

import com.upthink.qms.domain.EssayDetails;
import com.upthink.qms.domain.Person;
import com.upthink.qms.domain.PersonAnalytics;
import com.upthink.qms.domain.QmAnalytics;
import com.upthink.qms.service.EssayDetailsService;
import com.upthink.qms.service.PersonAnalyticsService;
import com.upthink.qms.service.PersonService;
import com.upthink.qms.service.request.*;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.PersonAnalyticsResponse;
import com.upthink.qms.service.response.QMAnalyticsDownloadResponse;
import com.upthink.qms.service.response.QmAnalyticsResponse;
import com.upthink.qms.utils.CsvUtils;
import org.jdbi.v3.core.Handle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/qm")
public class AnalyticsController {

    @Autowired
    private PersonAnalyticsService personAnalyticsService;

    @Autowired
    private PersonService personService;

    @Autowired
    private EssayDetailsService essayDetailsService;

    @Value("${gSheetId}")
    private String gSheetId;

    @Value("${yesterdaySheetName}")
    private String yesterdaySheetName;

    @Value("${personSheetName}")
    private String personSheetName;

    @Value("${essaySheetName}")
    private String essaySheetName;


    @PostMapping("/getPersonAnalytics")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public PersonAnalyticsResponse getPersonAnalytics(PersonAnalyticsRequest request) {
        try{
            Optional<Person> person = personService.findPersonById(request.getPersonId());
            if(!person.isPresent()){
                return new PersonAnalyticsResponse(null, null, "Person is not present");
            }
            String clientTimeZone = request.getTimezone();
            int startDay = 7;
            boolean singleDay = false;
            switch (request.getTimespan().toString()){
                case "monthly":
                    startDay = 30;
                    break;
                case "daily":
                    startDay = 1;
                    singleDay = true;
                default:
                    startDay = 7;
            }
            Optional<PersonAnalytics> personAnalytics = personAnalyticsService
                    .loadPersonAnalytics(request.getPersonId());
            if (!personAnalytics.isPresent()) {
                return new PersonAnalyticsResponse(null, null, "No analytics on this person");
            }
            String[] formattedDates = getStartEndDate(startDay, clientTimeZone);

            List<EssayDetails> userEssayLogs = essayDetailsService.getEssayLogsByPersonId(
                    request.getPersonId(),
                    formattedDates[0],
                    formattedDates[1],
                    singleDay);

            PersonAnalyticsResponse.PersonAnalyticsDTO personAnalyticsDTO =
                    new PersonAnalyticsResponse.PersonAnalyticsDTO(
                            request.getPersonId(),
                            person.get().getName(),
                            person.get().getEmail(),
                            personAnalytics.get().getCheckOutNum(),
                            personAnalytics.get().getCheckInNum(),
                            personAnalytics.get().getReuploadNum(),
                            personAnalytics.get().getAvgGradeTime());

            PersonAnalyticsResponse.EssayDetailsDTO essayDetailsDTO =
                    new PersonAnalyticsResponse.EssayDetailsDTO(userEssayLogs);

            return new PersonAnalyticsResponse(personAnalyticsDTO, essayDetailsDTO, null);

        } catch(Exception e) {
            return new PersonAnalyticsResponse(null, null, e.getMessage());
        }
    }


    @PostMapping("/getQMAnalytics")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public QmAnalyticsResponse getQmAnalytics(QmAnalyticsRequest request){  // Should return
        try{
            List<PersonAnalyticsResponse.PersonAnalyticsDTO> qmAnalyticsDTOS = new ArrayList<>();
            String clientTimeZone = request.getTimezone();
            int startDay = 7;
            boolean yesterday = false;
            switch(request.getTimespan().toString()) {
                case "overall":
                    qmAnalyticsDTOS = getAllPersonAnalytics();
                    return new QmAnalyticsResponse(qmAnalyticsDTOS, null);
                case "monthly":
                    startDay = 30;
                    break;
                case "daily":
                    startDay = 1;
                    yesterday = true;
                    break;
                default:
                    startDay = 7;
            }
            String[] formattedDates = getStartEndDate(startDay, clientTimeZone);
            System.out.printf("Start day %s , End Day %s\n", formattedDates[0], formattedDates[1]);
            List<QmAnalytics> qmAnalyticsList = new ArrayList<>();
            List<QmAnalytics> finalAnalyticsList = new ArrayList<>();
            if(request.getClientId() == 0) {
                finalAnalyticsList = getQmAnalyticsList(formattedDates[0], formattedDates[1], yesterday);
                System.out.println(finalAnalyticsList);
                // To be filled tomorrow!!!!!
            } else {
                System.out.println("Before in else block");
                qmAnalyticsList = essayDetailsService.getUserActionCountsByClientId(
                        formattedDates[0],
                        formattedDates[1],
                        request.getClientId(),
                        yesterday);
                System.out.println("QM Analytics else part " + qmAnalyticsList);
                boolean finalYesterday1 = yesterday;
                finalAnalyticsList =
                        qmAnalyticsList.stream()
                                .map(
                                        elem -> {
                                            String personId = elem.getPersonId();
                                            if (elem.getCheckInNum() > 0) {
                                                Optional<QmAnalytics> averageGradeTimeForPerson =
                                                        essayDetailsService
                                                                .getAvgGradeTimeForPersonAndClient(
                                                                        formattedDates[0],
                                                                        formattedDates[1],
                                                                        personId,
                                                                        request.getClientId(),
                                                                        finalYesterday1);
                                                return new QmAnalytics(
                                                        personId,
                                                        elem.getPersonName(),
                                                        elem.getPersonEmail(),
                                                        elem.getCheckInNum(),
                                                        elem.getCheckOutNum(),
                                                        elem.getReuploadNum(),
                                                        averageGradeTimeForPerson
                                                                .get()
                                                                .getAvgGradeTime());
                                            }
                                            return new QmAnalytics(
                                                    personId,
                                                    elem.getPersonName(),
                                                    elem.getPersonEmail(),
                                                    elem.getCheckInNum(),
                                                    elem.getCheckOutNum(),
                                                    elem.getReuploadNum(),
                                                    0);
                                        })
                                .collect(Collectors.toList());
            }

            System.out.printf("now list %s", finalAnalyticsList);
            qmAnalyticsDTOS =
                    finalAnalyticsList.stream()
                            .map(
                                    personAnalytics -> {
                                        return new PersonAnalyticsResponse.PersonAnalyticsDTO(
                                                personAnalytics.getPersonId(),
                                                personAnalytics.getPersonName(),
                                                personAnalytics.getPersonEmail(),
                                                personAnalytics.getCheckOutNum(),
                                                personAnalytics.getCheckInNum(),
                                                personAnalytics.getReuploadNum(),
                                                personAnalytics.getAvgGradeTime());
                                    })
                            .collect(Collectors.toList());

            return new QmAnalyticsResponse(qmAnalyticsDTOS, null);
        } catch (Exception e) {
            return new QmAnalyticsResponse(null, e.getMessage());
        }
    }


    public List<PersonAnalyticsResponse.PersonAnalyticsDTO> getAllPersonAnalytics() {
        List<PersonAnalytics> qmAnalyticsList = personAnalyticsService.loadAllPersonAnalytics();
        System.out.println(qmAnalyticsList);
        return qmAnalyticsList.stream()
                .map(
                        personAnalytics -> {
                            return new PersonAnalyticsResponse.PersonAnalyticsDTO(
                                    personAnalytics.getPersonId(),
                                    personAnalytics.getPersonName(),
                                    personAnalytics.getPersonEmail(),
                                    personAnalytics.getCheckOutNum(),
                                    personAnalytics.getCheckInNum(),
                                    personAnalytics.getReuploadNum(),
                                    personAnalytics.getAvgGradeTime());
                        })
                .collect(Collectors.toList());
    }


    public List<QmAnalytics> getQmAnalyticsList(String startDate, String endDate, boolean yesterday) {
        System.out.println("*******************************************************");
        System.out.println("startDate " + startDate);
        System.out.println("endDate " + endDate);
        List<QmAnalytics> qmAnalyticsListYesterday =
                essayDetailsService.getGetUserActionCounts(startDate, endDate, yesterday);
        System.out.println("Qm analytics list " + qmAnalyticsListYesterday);
        return qmAnalyticsListYesterday.stream()
                .map(
                        elem -> {
                            String personId = elem.getPersonId();
                            if (elem.getCheckInNum() > 0) {
                                Optional<QmAnalytics> averageGradeTimeForPerson =
                                        essayDetailsService.getAvgGradeTimeForPerson(
                                                startDate, endDate, personId, yesterday);
                                return new QmAnalytics(
                                        personId,
                                        elem.getPersonName(),
                                        elem.getPersonEmail(),
                                        elem.getCheckInNum(),
                                        elem.getCheckOutNum(),
                                        elem.getReuploadNum(),
                                        averageGradeTimeForPerson.get().getAvgGradeTime());
                            }
                            return new QmAnalytics(
                                    personId,
                                    elem.getPersonName(),
                                    elem.getPersonEmail(),
                                    elem.getCheckInNum(),
                                    elem.getCheckOutNum(),
                                    elem.getReuploadNum(),
                                    0);
                        })
                .collect(Collectors.toList());
    }

    @PostMapping("/exportAllPersonAnalytics")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public BaseResponse exportAllPersonAnalytics(ExportPersonAnalyticsRequest request) throws GeneralSecurityException, IOException {
        try{
            List<PersonAnalyticsResponse.PersonAnalyticsDTO> qmAnalyticsDTOS = getAllPersonAnalytics();
            CsvUtils.personAnalyticsToSheet(
                    gSheetId, personSheetName, qmAnalyticsDTOS);
            // yesterday analytics
            String clientTimeZone = request.getTimezone();
            String[] formattedDates = getStartEndDate(1, clientTimeZone);
            List<QmAnalytics> finalAnalyticsList =
                    getQMAnalyticsList(formattedDates[0], formattedDates[1], true);
            List<PersonAnalyticsResponse.PersonAnalyticsDTO> qmAnalyticsDTOSYesterday =
                    finalAnalyticsList.stream()
                            .map(
                                    personAnalytics -> {
                                        return new PersonAnalyticsResponse.PersonAnalyticsDTO(
                                                personAnalytics.getPersonId(),
                                                personAnalytics.getPersonName(),
                                                personAnalytics.getPersonEmail(),
                                                personAnalytics.getCheckOutNum(),
                                                personAnalytics.getCheckInNum(),
                                                personAnalytics.getReuploadNum(),
                                                personAnalytics.getAvgGradeTime());
                                    })
                            .collect(Collectors.toList());

            CsvUtils.personAnalyticsToSheet(
                    gSheetId,
                    yesterdaySheetName,
                    qmAnalyticsDTOSYesterday);
            return new BaseResponse(true, null);
        } catch(Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }


    @PostMapping("/exportAllEssayAnalytics")
    @PreAuthorize("hasRole('QM_ADMIN")
    public BaseResponse exportAllEssayAnalytics(DownloadAnalyticsRequest request) {
        try{
            List<EssayDetails> essayDetailsList = essayDetailsService
                    .getAllEssayDetailsByTime(request.getStartTime(), request.getEndTime());
            CsvUtils.essayAnalyticsToSheet(
                    gSheetId, essaySheetName, essayDetailsList);
            return new BaseResponse(true, null);
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage());
        }
    }

    @PostMapping("/downloadAllEssayAnalytics")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public QMAnalyticsDownloadResponse downloadAllEssayAnalytics(DownloadAnalyticsRequest request) {
        try{
            List<EssayDetails> essayDetailsList =
                    essayDetailsService.getAllEssayDetailsByTime(request.getStartTime(), request.getEndTime());
            List<QMAnalyticsDownloadResponse.QMAnalyticsDownloadDTO> finalListDTO =
                essayDetailsList.stream()
                        .map(
                                essayDetail -> {
                                    return new QMAnalyticsDownloadResponse
                                            .QMAnalyticsDownloadDTO(
                                            essayDetail.getPersonId(),
                                            essayDetail.getEssayId(),
                                            essayDetail.getUserAction(),
                                            essayDetail.getCreatedAt(),
                                            essayDetail.getClientName(),
                                            essayDetail.getCredsName(),
                                            essayDetail.getDownloadedAt());
                                })
                        .collect(Collectors.toList());
            return new QMAnalyticsDownloadResponse(finalListDTO, null);
        } catch (Exception e) {
            return new QMAnalyticsDownloadResponse(null, e.getMessage());
        }
    }


    @PostMapping("/downloadAllPersonAnalytics")
    @PreAuthorize("hasRole('QM_ADMIN')")
    public QmAnalyticsResponse downloadAllPersonAnalytics(DownloadAnalyticsRequest request) {
        try {
            List<QmAnalytics> finalAnalyticsList = new ArrayList<>();
            List<PersonAnalyticsResponse.PersonAnalyticsDTO> qmAnalyticsDTOS = new ArrayList<>();
            String startDate = request.getStartTime();
            String endDate = request.getEndTime();
            System.out.printf("start Date %s End Date %s\n", startDate, endDate);
            List<QmAnalytics> analyticsList =
                    essayDetailsService.getUserActionAnalytics(startDate, endDate);
            System.out.println("Analyticvs list : " + analyticsList);
            finalAnalyticsList =
                    analyticsList.stream()
                            .map(
                                    elem -> {
                                        String personId = elem.getPersonId();
                                        if (elem.getCheckInNum() > 0) {
                                            Optional<QmAnalytics> averageGradeTimeForPerson =
                                                    essayDetailsService.getAvgGradeTimeForPersonDownload(
                                                            startDate, endDate, personId);
                                            return new QmAnalytics(
                                                    personId,
                                                    elem.getPersonName(),
                                                    elem.getPersonEmail(),
                                                    elem.getCheckInNum(),
                                                    elem.getCheckOutNum(),
                                                    elem.getReuploadNum(),
                                                    averageGradeTimeForPerson
                                                            .get()
                                                            .getAvgGradeTime());
                                        }
                                        return new QmAnalytics(
                                                personId,
                                                elem.getPersonName(),
                                                elem.getPersonEmail(),
                                                elem.getCheckInNum(),
                                                elem.getCheckOutNum(),
                                                elem.getReuploadNum(),
                                                0);
                                    })
                            .collect(Collectors.toList());

            qmAnalyticsDTOS =
                    finalAnalyticsList.stream()
                            .map(
                                    personAnalytics -> {
                                        return new PersonAnalyticsResponse.PersonAnalyticsDTO(
                                                personAnalytics.getPersonId(),
                                                personAnalytics.getPersonName(),
                                                personAnalytics.getPersonEmail(),
                                                personAnalytics.getCheckOutNum(),
                                                personAnalytics.getCheckInNum(),
                                                personAnalytics.getReuploadNum(),
                                                personAnalytics.getAvgGradeTime());
                                    })
                            .collect(Collectors.toList());

            return new QmAnalyticsResponse(qmAnalyticsDTOS, null);
        } catch (Exception e) {
            return new QmAnalyticsResponse(null, e.getMessage());
        }
    }


    public List<QmAnalytics> getQMAnalyticsList(
            String startDate, String endDate, boolean yesterday) {
        System.out.println("Here in getQMAnalyticsList");
        System.out.println("startDate " + startDate);
        System.out.println("endDate " + endDate);
        List<QmAnalytics> qmAnalyticsListYesterday =
                essayDetailsService.getGetUserActionCounts(startDate, endDate, yesterday);
        return qmAnalyticsListYesterday.stream()
                .map(
                        elem -> {
                            String personId = elem.getPersonId();
                            if (elem.getCheckInNum() > 0) {
                                Optional<QmAnalytics> averageGradeTimeForPerson =
                                        essayDetailsService.getAvgGradeTimeForPerson(
                                                startDate, endDate, personId, yesterday);
                                return new QmAnalytics(
                                        personId,
                                        elem.getPersonName(),
                                        elem.getPersonEmail(),
                                        elem.getCheckInNum(),
                                        elem.getCheckOutNum(),
                                        elem.getReuploadNum(),
                                        averageGradeTimeForPerson.get().getAvgGradeTime());
                            }
                            return new QmAnalytics(
                                    personId,
                                    elem.getPersonName(),
                                    elem.getPersonEmail(),
                                    elem.getCheckInNum(),
                                    elem.getCheckOutNum(),
                                    elem.getReuploadNum(),
                                    0);
                        })
                .collect(Collectors.toList());
    }


    public String[] getStartEndDate(int startDay, String timeZone) {
        ZoneId clientZone = ZoneId.of(timeZone);
        ZonedDateTime clientNow = ZonedDateTime.now(clientZone);

        ZonedDateTime startDate;
        ZonedDateTime endDate;

        switch (startDay) {
            case 30: // Monthly
                ZonedDateTime firstDayOfCurrentMonth =
                        clientNow.with(TemporalAdjusters.firstDayOfMonth());
                endDate =
                        firstDayOfCurrentMonth
                                .minusDays(1)
                                .with(LocalTime.MAX); // Last day of previous month
                startDate =
                        endDate.with(TemporalAdjusters.firstDayOfMonth())
                                .with(LocalTime.MIN); // First day of previous month
                break;
            case 7: // Weekly
                ZonedDateTime lastSunday =
                        clientNow.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                endDate = lastSunday.with(LocalTime.MAX); // Last Sunday
                startDate = lastSunday.minusDays(6).with(LocalTime.MIN); // Previous Monday
                break;
            default: // Daily or other
                startDate = clientNow.minusDays(1).truncatedTo(ChronoUnit.DAYS);
                endDate = startDate.plusDays(1).minusNanos(1);
                break;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String formattedStart = startDate.withZoneSameInstant(ZoneId.of("UTC")).format(formatter);
        String formattedEnd = endDate.withZoneSameInstant(ZoneId.of("UTC")).format(formatter);
        String[] result = {formattedStart, formattedEnd};
        return result;
    }

}
