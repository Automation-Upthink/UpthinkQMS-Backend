package com.upthink.qms.service;

import com.upthink.qms.domain.*;
import com.upthink.qms.repository.*;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.EssayResponse;
import com.upthink.qms.service.request.ListEssayRequest;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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


    public int createEssay(String taskId, String name, int clientId, int credId, String fileLink, String fetchedFor, String dueDate) {
        return essayRepository.createEssay(taskId, taskId, name, clientId, credId, fileLink, fetchedFor, dueDate);

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

    public List<Essay> listEssaysByStatus(List<String> statusList) {
        List<Object[]> essayList = essayRepository.listByEssayStatus(statusList);

        // If the result is empty, return an empty list
        return essayList.stream()
                .map(this::mapRowToEssay)  // Use the mapper function to convert Object[] to Essay
                .collect(Collectors.toList());
    }


//    public EssayResponse listEssays(int clientId, List<String> essayStatusList) {
//        try {
//            List<Essay> essayList;
//            if(clientId == 0) {
//                essayList = essayRepository.listByEssayStatus(essayStatusList)
//                        .orElseThrow(() -> new IllegalArgumentException("No essays found with the provided status"));
//            } else {
//                essayList = essayRepository.listByClientIdAndStatusIn(clientId, essayStatusList)
//                        .orElseThrow(() -> new IllegalArgumentException("No essays found for the provided client"));
//            }
//            // Map each essay, client name, and credential name to DTOs
//            List<EssayResponse.EssayDTO> essayDTOS = essayList.stream().map(essay -> {
//                if (essay.getStatus().toString().equals("checked_out")) {
//                    return buildCheckedOutEssayDTO(essay);
//                } else {
//                    return buildDefaultEssayDTO(essay);
//                }
//            }).collect(Collectors.toList());
//            System.out.println(essayDTOS);
//            return new EssayResponse(essayDTOS, true, null);
//        } catch (Exception e) {
//            return new EssayResponse(null, false, e.getMessage());
//        }
//    }

    // List essays by client ID and status
    public EssayResponse listEssays(int clientId, List<String> essayStatusList) {
        try {
            List<Essay> essayList;
            if (clientId == 0) {
                // Fetching all essays based on status
                essayList = essayRepository.listByEssayStatus(essayStatusList)
                        .stream()
                        .map(this::mapRowToEssay) // Use the function as a mapper
                        .collect(Collectors.toList());
            } else {
                // Fetching essays filtered by client ID and status
                essayList = essayRepository.listByClientIdAndStatusIn(clientId, essayStatusList)
                        .stream()
                        .map(this::mapRowToEssay) // Use the function as a mapper
                        .collect(Collectors.toList());
            }

            // Convert essays to DTOs for the response
            List<EssayResponse.EssayDTO> essayDTOS = essayList.stream()
                    .map(essay -> {
                        if ("checked_out".equals(essay.getStatus())) {
                            return buildCheckedOutEssayDTO(essay); // Handle checked out essays
                        } else {
                            return buildDefaultEssayDTO(essay); // Handle other essays
                        }
                    })
                    .collect(Collectors.toList());

            return new EssayResponse(essayDTOS, true, null);
        } catch (Exception e) {
            return new EssayResponse(null, false, e.getMessage());
        }
    }


    // Essay Mapper Function (formerly EssayListMapper class)
    private Essay mapRowToEssay(Object[] row) {
        return new Essay(
                (String) row[0],  // id
                (String) row[1],  // essayId
                (String) row[2],  // name
                (String) row[3],  // filelink
                (String) row[4],  // status
                (String) row[11], // client_name
                (String) row[12], // cred_name
                (int) row[6],     // essay_client_id
                (int) row[7],     // essay_cred_id
                (Timestamp) row[9], // created_at
                getDueDateInISTEpoch((String) row[5]) // due_date
        );
    }



    private EssayResponse.EssayDTO buildDefaultEssayDTO(Essay essay) {
        return new EssayResponse.EssayDTO(
                essay.getClientName(),   // This comes directly from the repository query
                essay.getName(),
                essay.getEssayId(),
                essay.getCredName(),     // This also comes from the repository query
                essay.getStatus(),
                convertTimestampToIST(essay.getDownloadTime()),
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
                    convertTimestampToIST(essay.getDownloadTime()),
                    convertTimestampToIST(latestEssayDetails.get().getCreatedAt())
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

    public static String convertTimestampToIST(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        // Define the formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Convert Timestamp to Instant, then to ZonedDateTime in UTC
        ZonedDateTime utcDateTime = timestamp.toInstant().atZone(ZoneId.of("UTC"));
        // Convert UTC ZonedDateTime to IST
        ZonedDateTime istDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        // Format the ZonedDateTime to string
        return istDateTime.format(formatter);
    }
}




