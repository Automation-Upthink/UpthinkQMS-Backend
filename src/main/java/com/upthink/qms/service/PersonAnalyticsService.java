package com.upthink.qms.service;

import com.upthink.qms.domain.PersonAnalytics;
import com.upthink.qms.repository.PersonAnalyticsRepository;
import com.upthink.qms.service.response.PersonAnalyticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonAnalyticsService {

    @Autowired
    private PersonAnalyticsRepository personAnalyticsRepository;

    /**
     * Inserts a new PersonAnalytics entry into the database.
     *
     * @param personId The ID of the person for whom analytics are being created.
     */
    public void createPersonAnalytics(String personId) {
        personAnalyticsRepository.insertPersonAnalytics(personId);
    }

    /**
     * Loads PersonAnalytics for a specific person by their ID.
     *
     * @param personId The ID of the person whose analytics should be loaded.
     * @return An Optional containing the PersonAnalytics if found, or empty if not.
     */
    public Optional<PersonAnalytics> loadPersonAnalytics(String personId) {
        return Optional.ofNullable(personAnalyticsRepository.loadPersonAnalyticsByPersonId(personId))
                .orElse(Optional.empty());
    }

    /**
     * Updates an existing PersonAnalytics entry.
     *
     * @param id The ID of the PersonAnalytics entry to update.
     * @param checkOutNum The number of checkouts to add.
     * @param checkInNum The number of check-ins to add.
     * @param reuploadNum The number of reuploads to add.
     * @param avgGradeTime The new average grade time.
     */
    public void updatePersonAnalytics(Integer id, Integer checkOutNum, Integer checkInNum, Integer reuploadNum, long avgGradeTime) {
        personAnalyticsRepository.updatePersonAnalytics(id, checkOutNum, checkInNum, reuploadNum, (long)avgGradeTime);
    }

    /**
     * Loads all PersonAnalytics entries, along with additional user information.
     *
     * @return A list of PersonAnalytics entries with associated user data.
     */
    public List<PersonAnalytics> loadAllPersonAnalytics() {
        List<Object[]> results = personAnalyticsRepository.loadPersonAnalytics();
        return mapToPersonAnalytics(results);
    }


    public List<PersonAnalytics> mapToPersonAnalytics(List<Object[]> results) {
        return results.stream()
                .map(result -> new PersonAnalytics(
                        ((Number) result[0]).intValue(),  // id
                        ((Number) result[1]).intValue(),  // checkedInNum
                        ((Number) result[2]).intValue(),  // checkedOutNum
                        ((Number) result[3]).intValue(),  // reuploadNum
                        result[4] != null ? ((Number) result[4]).intValue() : null,  // avgGradeTime
                        (String) result[5],  // personId
                        (String) result[6],  // personName
                        (String) result[7]   // personEmail
                ))
                .collect(Collectors.toList());
    }





}
