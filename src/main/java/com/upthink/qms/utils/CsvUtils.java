package com.upthink.qms.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.upthink.qms.domain.EssayDetails;
import com.upthink.qms.service.response.PersonAnalyticsResponse;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CsvUtils {

    public static void personAnalyticsToSheet(
            String spreadsheetId,
            String sheetName,
            List<PersonAnalyticsResponse.PersonAnalyticsDTO> personAnalyticsDTOSList)
            throws IOException, GeneralSecurityException {
        // Prepare the data
        List<List<Object>> data = new ArrayList<>();
        data.add(
                Arrays.asList(
                        "Person Id",
                        "Person Name",
                        "Avg. Grade Time",
                        "Total Check-out",
                        "Total Check-in",
                        "Total Re-upload"));

        for (PersonAnalyticsResponse.PersonAnalyticsDTO personAnalyticsDTO :
                personAnalyticsDTOSList) {
            long totalSeconds = personAnalyticsDTO.getAvgGradeTime() / 1000;
            int hours = (int) (totalSeconds / 3600);
            int minutes = (int) (totalSeconds % 3600) / 60;
            int seconds = (int) (totalSeconds % 60);

            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            data.add(
                    Arrays.asList(
                            personAnalyticsDTO.getPersonId(),
                            personAnalyticsDTO.getPersonName(),
                            formattedTime,
                            personAnalyticsDTO.getTotalCheckOuts(),
                            personAnalyticsDTO.getTotalCheckIns(),
                            personAnalyticsDTO.getTotalReuploads()));
        }
        writeToGSheet(spreadsheetId, sheetName, data);
    }

    public static void essayAnalyticsToSheet(
            String spreadsheetId, String sheetName, List<EssayDetails> essayDetailsList)
            throws IOException, GeneralSecurityException {
        // Prepare the data
        List<List<Object>> data = new ArrayList<>();
        data.add(
                Arrays.asList(
                        "Essay Id",
                        "Downloaded At (UTC)",
                        "Downloaded At (IST)",
                        "Downloaded At (EST)",
                        "Person Id",
                        "User Action",
                        "Client",
                        "Credential",
                        "Created At (UTC)",
                        "Created At (IST)",
                        "Created At (EST)"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (EssayDetails essayDetails : essayDetailsList) {
            ZonedDateTime createdAtUTC =
                    ZonedDateTime.ofInstant(
                            essayDetails.getCreatedAt().toInstant(), ZoneId.of("UTC"));
            ZonedDateTime downloadedAtUTC =
                    ZonedDateTime.ofInstant(
                            essayDetails.getDownloadedAt().toInstant(), ZoneId.of("UTC"));

            // Converting to different time zones
            ZonedDateTime createdAtIST =
                    createdAtUTC.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime createdAtEST =
                    createdAtUTC.withZoneSameInstant(ZoneId.of("America/New_York"));
            ZonedDateTime downloadedAtIST =
                    downloadedAtUTC.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime downloadedAtEST =
                    downloadedAtUTC.withZoneSameInstant(ZoneId.of("America/New_York"));

            data.add(
                    Arrays.asList(
                            essayDetails.getEssayId(),
                            downloadedAtUTC.format(formatter),
                            downloadedAtIST.format(formatter),
                            downloadedAtEST.format(formatter),
                            essayDetails.getPersonId(),
                            essayDetails.getUserAction(),
                            essayDetails.getClientName(),
                            essayDetails.getCredsName(),
                            createdAtUTC.format(formatter),
                            createdAtIST.format(formatter),
                            createdAtEST.format(formatter)));
        }
        writeToGSheet(spreadsheetId, sheetName, data);
    }

//    private static void writeToGSheet(
//            String spreadsheetId, String sheetName, List<List<Object>> data)
//            throws IOException, GeneralSecurityException {
//        // Load the credentials file for your service account
//        InputStream inputStream = CsvUtils.class.getResourceAsStream("/serviceCreds.json");
//        if (inputStream != null) {
//            try {
//                // Create a temporary file
//                File tempFile = File.createTempFile("serviceCreds", ".json");
//
//                // Write the input stream to the temporary file
//                try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                        outputStream.write(buffer, 0, bytesRead);
//                    }
//                }
//
//                // Load credentials from the temporary file with required scopes
//                FileInputStream credentialsStream = new FileInputStream(tempFile);
//                GoogleCredentials credentials =
//                        ServiceAccountCredentials.fromStream(credentialsStream)
//                                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"));
//
//                // Create a Sheets service
//                Sheets sheetsService =
//                        new Sheets.Builder(
//                                GoogleNetHttpTransport.newTrustedTransport(),
//                                GsonFactory.getDefaultInstance(),
//                                new HttpCredentialsAdapter(credentials))
//                                .setApplicationName("QM Analytics")
//                                .build();
//
//                // Prepare the request to clear the sheet
//                ClearValuesRequest clearRequest = new ClearValuesRequest();
//
//                sheetsService
//                        .spreadsheets()
//                        .values()
//                        .clear(spreadsheetId, sheetName, clearRequest)
//                        .execute();
//
//                // Write the data to the Google Sheet
//                ValueRange body = new ValueRange().setValues(data);
//                String range = sheetName + "!A1";
//                UpdateValuesResponse result =
//                        sheetsService
//                                .spreadsheets()
//                                .values()
//                                .update(spreadsheetId, range, body)
//                                .setValueInputOption("RAW")
//                                .execute();
//
//                System.out.printf("Updated %d cells.%n", result.getUpdatedCells());
//
//                // Clean up
//                inputStream.close();
//                credentialsStream.close();
//                tempFile.delete();
//            } catch (IOException e) {
//                throw e;
//            }
//        } else {
//            System.err.println("Resource not found.");
//            throw new FileNotFoundException("serviceCreds.json not found");
//        }
//    }


    private static void writeToGSheet(
            String spreadsheetId, String sheetName, List<List<Object>> data)
            throws IOException, GeneralSecurityException {
        // Load the credentials file for your service account
        InputStream inputStream = CsvUtils.class.getResourceAsStream("/serviceCreds.json");
        if (inputStream != null) {
            try {
                // Create a temporary file
                File tempFile = File.createTempFile("serviceCreds", ".json");

                // Open a FileOutputStream to write the data to the temporary file
                try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                // Now you have a FileInputStream for the temporary file
                FileInputStream credentialsStream = new FileInputStream(tempFile);
                GoogleCredentials credentials =
                        ServiceAccountCredentials.fromStream(credentialsStream);
//                // Create a Sheets service
                Sheets sheetsService =
                        new Sheets.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                GsonFactory.getDefaultInstance(),
                                new HttpCredentialsAdapter(credentials))
                                .setApplicationName("QM Analytics")
                                .build();

                // Create a ValueRange object
                ValueRange body = new ValueRange().setValues(data);

                // Define the range where you want to write the data (e.g., "Sheet1!A1")
                String range = sheetName + "!A1";
                ClearValuesRequest clearRequest = new ClearValuesRequest();

                sheetsService
                        .spreadsheets()
                        .values()
                        .clear(spreadsheetId, sheetName, clearRequest)
                        .execute();
                // Write the data to the Google Sheet
                UpdateValuesResponse result =
                        sheetsService
                                .spreadsheets()
                                .values()
                                .update(spreadsheetId, range, body)
                                .setValueInputOption("RAW") // Use "RAW" for unformatted text
                                .execute();

                System.out.printf("Updated %d cells.%n", result.getUpdatedCells());
                // You can use fileInputStream for your further processing

                // Don't forget to close the input streams and delete the temporary file when done
                inputStream.close();
                credentialsStream.close();
                tempFile.delete();
            } catch (IOException e) {
                throw e;
            }
        } else {
            System.err.println("Resource not found.");
            throw new FileNotFoundException("serviceCreds not found");
        }
    }
}