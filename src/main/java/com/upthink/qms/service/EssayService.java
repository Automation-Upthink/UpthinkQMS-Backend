package com.upthink.qms.service;

import com.upthink.qms.domain.*;
import com.upthink.qms.repository.*;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.EssayResponse;
import com.upthink.qms.service.request.ListEssayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;



@Service
public class EssayService {

    private static EssayRepository essayRepository;
    private final EssayDetailsRepository essayDetailsRepository;
    private final PersonRepository personRepository;

    @Autowired
    public EssayService(EssayRepository essayRepository,
                        EssayDetailsRepository essayDetailsRepository,
                        PersonRepository personRepository) {
        this.essayRepository = essayRepository;
        this.essayDetailsRepository = essayDetailsRepository;
        this.personRepository = personRepository;
    }


    // MAYBE CHANGE THIS ACCORDING TO JPA STANDARDS
    public void createEssay(String taskId, String name, int clientId, int credId, String fileLink, String fetchedFor, String dueDate) {
        essayRepository.createEssay(taskId, taskId, name, clientId, credId, fileLink, fetchedFor, dueDate);

    }

    public static Optional<Essay> loadEssayById(String id) {
        Essay essay = essayRepository.findByEssayId(id)
                .orElseThrow(() -> new IllegalArgumentException("No essay found with the provided id."));
        return Optional.ofNullable(essay);
    }

    public void updateEssayStatus(String essayId, String newStatus) {
        essayRepository.updateEssayStatus(essayId, newStatus);
    }

    public void updateEssayGradeTime(String essayId, long gradeTime){
        essayRepository.updateEssayGradeTime(essayId, gradeTime);
    }


    public EssayResponse listEssays(int clientId, List<String> essayStatusList) {
        try {
            List<Essay> essayList;
            if(clientId == 0) {
                essayList = essayRepository.listByEssayStatus(essayStatusList)
                        .orElseThrow(() -> new IllegalArgumentException("No essays found with the provided status"));
            } else {
                essayList = essayRepository.listByClientIdAndStatusIn(clientId, essayStatusList)
                        .orElseThrow(() -> new IllegalArgumentException("No essays found for the provided client"));
            }

            // Map each essay, client name, and credential name to DTOs
            List<EssayResponse.EssayDTO> essayDTOS = essayList.stream().map(essay -> {
                if (essay.getStatus().toString().equals("checked_out")) {
                    return buildCheckedOutEssayDTO(essay);
                } else {
                    return buildDefaultEssayDTO(essay);
                }
            }).collect(Collectors.toList());

            return new EssayResponse(essayDTOS, true, null);
        } catch (Exception e) {
            return new EssayResponse(null, false, e.getMessage());
        }
    }

    private EssayResponse.EssayDTO buildDefaultEssayDTO(Essay essay) {
        return new EssayResponse.EssayDTO(
                essay.getClientName(),   // This comes directly from the repository query
                essay.getName(),
                essay.getEssayId(),
                essay.getCredName(),     // This also comes from the repository query
                essay.getStatus(),
                essay.getDownloadTime(),
                essay.getFileLink(),
                essay.getDueDate()
        );
    }

    private EssayResponse.EssayDTO buildCheckedOutEssayDTO(Essay essay) {
        Optional<EssayDetails> latestEssayDetails = essayDetailsRepository.getLatestEssayDetailByEssayId(essay.getEssayId());
        if(latestEssayDetails.isPresent()) {
            Optional<Person> checkOutPerson = personRepository.findById(latestEssayDetails.get().getPersonId());
            String checkOutBy = checkOutPerson.map(Person::getEmail).orElse("Unknown Person");

            return new EssayResponse.EssayDTO(
                    essay.getClientName(),
                    essay.getName(),
                    essay.getEssayId(),
                    essay.getCredName(),
                    checkOutBy,
                    essay.getStatus().toString(),
                    essay.getFileLink(),
                    essay.getDueDate(),
                    essay.getDownloadTime(),
                    latestEssayDetails.get().getCreatedAt()
            );
        }
        return buildDefaultEssayDTO(essay);
    }


    public static long getDueDateInISTEpoch(String estDueDate) {
        if (estDueDate == null) {
            return 0;
        }
        String[] components = estDueDate.split("[/:\\s]");

        // Extract components
        int month = Integer.parseInt(components[0]);
        int day = Integer.parseInt(components[1]);
        int year = Integer.parseInt(components[2]);
        int hour = Integer.parseInt(components[3]);
        int minute = Integer.parseInt(components[4]);
        int second = Integer.parseInt(components[5].substring(0, 2)); // Extract seconds
        String amPm = components[6]; // Extract AM/PM indicator

        // Adjust hour for AM/PM
        if (amPm.equalsIgnoreCase("PM") && hour < 12) {
            hour += 12;
        } else if (amPm.equalsIgnoreCase("AM") && hour == 12) {
            hour = 0;
        }
        // Create LocalDateTime object
        LocalDateTime estDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
        // Define EST time zone
        ZoneId estTimeZone = ZoneId.of("America/New_York");
        // Convert EST due date to ZonedDateTime
        ZonedDateTime estZonedDateTime = estDateTime.atZone(estTimeZone);
        System.out.println("Est Time " + estZonedDateTime);
        // Define IST time zone
        ZoneId istTimeZone = ZoneId.of("Asia/Kolkata");
        // Convert EST due date to IST
        ZonedDateTime istZonedDateTime = estZonedDateTime.withZoneSameInstant(istTimeZone);
        long istTimestampMillis = istZonedDateTime.toInstant().toEpochMilli();
        // Return the converted due date timestamp in IST
        return istTimestampMillis;
    }


}




