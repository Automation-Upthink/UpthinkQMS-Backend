package com.upthink.qms.repository;

import com.upthink.qms.domain.PersonAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonAnalyticsRepository extends JpaRepository<PersonAnalytics, Integer> {

    @Query(value = "SELECT * FROM person_analytics WHERE person_id = :personId", nativeQuery = true)
    Optional<PersonAnalytics> loadPersonAnalyticsByPersonId(@Param("personId") String personId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO person_analytics (person_id, avg_grade_time, checked_out_num, checked_in_num, reupload_num) " +
            "VALUES (:personId, 1, 0, 0, 0)", nativeQuery = true)
    int insertPersonAnalytics(@Param("personId") String personId);

    @Query(value = "SELECT person_analytics.*, person.name AS person_name, person.email AS person_email " +
            "FROM person_analytics " +
            "JOIN person ON person_analytics.person_id = person.id", nativeQuery = true)
    List<PersonAnalytics> loadPersonAnalytics();

    @Modifying
    @Transactional
    @Query(value = "UPDATE person_analytics SET checked_out_num = checked_out_num + :checkOutNum, " +
            "checked_in_num = checked_in_num + :checkInNum, " +
            "reupload_num = reupload_num + :reuploadNum, " +
            "avg_grade_time = :avgGradeTime WHERE id = :id", nativeQuery = true)
    int updatePersonAnalytics(
            @Param("id") Integer id,
            @Param("checkOutNum") Integer checkOutNum,
            @Param("checkInNum") Integer checkInNum,
            @Param("reuploadNum") Integer reuploadNum,
            @Param("avgGradeTime") long avgGradeTime
    );

}
