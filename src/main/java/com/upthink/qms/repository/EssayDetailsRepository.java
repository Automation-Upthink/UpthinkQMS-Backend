package com.upthink.qms.repository;

import com.upthink.qms.domain.Essay;
import com.upthink.qms.domain.EssayDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.security.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EssayDetailsRepository extends JpaRepository<EssayDetails, Integer> {

    // Custom query to find the latest EssayDetails for a given essayId
    @Query(value="SELECT * FROM essay_details WHERE essay_id = :essayId ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<EssayDetails> getLatestEssayDetailByEssayId(@Param("essayId") String essayId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO essay_details (essay_id, person_id, user_action) " +
            "VALUES (:essayId, :personId, CAST(:userAction AS status))", nativeQuery = true)
    int insertEssayDetails(@Param("essayId") String essayId,
                            @Param("personId") String personId,
                            @Param("userAction") String userAction);

    @Query(value = "SELECT ed.*, e.created_at as downloaded_at, c.name as client_name, cc.name as cred_name " +
            "FROM essay_details AS ed " +
            "INNER JOIN essay AS e ON ed.essay_id = e.essay_id " +
            "INNER JOIN essay_client AS c ON e.essay_client_id = c.id " +
            "INNER JOIN essay_client_credential AS cc ON e.essay_cred_id = cc.id", nativeQuery = true)
    List<Object[]> getAllEssayDetails();

    @Query(value = "SELECT ed.*, e.created_at as downloaded_at, c.name as client_name, cc.name as cred_name " +
            "FROM essay_details AS ed " +
            "INNER JOIN essay AS e ON ed.essay_id = e.essay_id " +
            "INNER JOIN essay_client AS c ON e.essay_client_id = c.id " +
            "INNER JOIN essay_client_credential AS cc ON e.essay_cred_id = cc.id " +
            "WHERE ed.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND ed.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6')", nativeQuery = true)
    List<Object[]> getAllEssayDetailsByTime(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate);


    @Query(value = "SELECT * FROM essay_details WHERE person_id = :personId " +
            "AND created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "ORDER BY created_at DESC", nativeQuery = true)
    List<Object[]> getEssayLogsByPersonId(@Param("personId") String personId,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);

    @Query(value = "SELECT * FROM essay_details WHERE person_id = :personId " +
            "AND created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "ORDER BY created_at DESC",
            nativeQuery = true)
    List<Object[]> getEssayLogsByPersonIdSingleDay(@Param("personId")String personId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);


    @Query(value = "SELECT " +
            "ed.person_id AS personId, " +
            "p.email AS personEmail, " +
            "p.name AS personName, " +
            "SUM(CASE WHEN ed.user_action = 'available' THEN 1 ELSE 0 END) AS availableCount, " +
            "SUM(CASE WHEN ed.user_action = 'checked_out' THEN 1 ELSE 0 END) AS checkedOutCount, " +
            "SUM(CASE WHEN ed.user_action = 'checked_in' THEN 1 ELSE 0 END) AS checkedInCount " +
            "FROM essay_details ed " +
            "JOIN person p ON ed.person_id = p.id " +
            "WHERE ed.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND ed.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "GROUP BY ed.person_id, p.email, p.name",
            nativeQuery = true)
    List<Map<String, Object>> getUserActionCountsByGroupYesterday(@Param("startDate") String startDate,
                                                                  @Param("endDate") String endDate);


    @Query(value = "SELECT " +
            "ed.person_id AS personId, " +
            "p.email AS personEmail, " +
            "p.name AS personName, " +
            "SUM(CASE WHEN ed.user_action = 'available' THEN 1 ELSE 0 END) AS availableCount, " +
            "SUM(CASE WHEN ed.user_action = 'checked_out' THEN 1 ELSE 0 END) AS checkedOutCount, " +
            "SUM(CASE WHEN ed.user_action = 'checked_in' THEN 1 ELSE 0 END) AS checkedInCount " +
            "FROM essay_details ed " +
            "JOIN person p ON ed.person_id = p.id " +
            "WHERE ed.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND ed.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "GROUP BY ed.person_id, p.email, p.name",
            nativeQuery = true)
    List<Map<String, Object>> getUserActionCountsByGroup(@Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);



    @Query(value = "SELECT essay_details.person_id AS personId, AVG(essay.grade_time) AS avgGradeTime " +
            "FROM essay_details " +
            "JOIN essay ON essay_details.essay_id = essay.id " +
            "WHERE essay_details.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.user_action = 'checked_in' " +
            "AND essay_details.person_id = :personId " +
            "GROUP BY essay_details.person_id", nativeQuery = true)
    List<Map<String, Object>> getAvgGradeTime(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate,
                                              @Param("personId") String personId);

    @Query(value = "SELECT essay_details.person_id AS personId, AVG(essay.grade_time) AS avgGradeTime " +
            "FROM essay_details " +
            "JOIN essay ON essay_details.essay_id = essay.id " +
            "WHERE essay_details.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.user_action = 'checked_in' " +
            "AND essay_details.person_id = :personId " +
            "GROUP BY essay_details.person_id", nativeQuery = true)
    List<Map<String, Object>> getAvgGradeTimeYesterday(@Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("personId") String personId);



    @Query(value = "SELECT " +
            "essay_details.person_id AS personId, " +
            "person.email AS personEmail, " +
            "person.name AS personName, " +
            "SUM(CASE WHEN essay_details.user_action = 'available' THEN 1 ELSE 0 END) AS availableCount, " +
            "SUM(CASE WHEN essay_details.user_action = 'checked_out' THEN 1 ELSE 0 END) AS checkedOutCount, " +
            "SUM(CASE WHEN essay_details.user_action = 'checked_in' THEN 1 ELSE 0 END) AS checkedInCount " +
            "FROM essay_details " +
            "JOIN essay ON essay_details.essay_id = essay.id " +
            "JOIN person ON essay_details.person_id = person.id " +
            "WHERE essay_details.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay.essay_client_id = :clientId " +
            "GROUP BY essay_details.person_id, person.email, person.name", nativeQuery = true)
    List<Map<String, Object>> getUserActionCountsByGroupAndClientId(@Param("startDate") String startDate,
                                                                    @Param("endDate") String endDate,
                                                                    @Param("clientId") int clientId);

    @Query(value = "SELECT " +
            "essay_details.person_id AS personId, " +
            "person.email AS personEmail, " +
            "person.name AS personName, " +
            "SUM(CASE WHEN essay_details.user_action = 'available' THEN 1 ELSE 0 END) AS availableCount, " +
            "SUM(CASE WHEN essay_details.user_action = 'checked_out' THEN 1 ELSE 0 END) AS checkedOutCount, " +
            "SUM(CASE WHEN essay_details.user_action = 'checked_in' THEN 1 ELSE 0 END) AS checkedInCount " +
            "FROM essay_details " +
            "JOIN essay ON essay_details.essay_id = essay.id " +
            "JOIN person ON essay_details.person_id = person.id " +
            "WHERE essay_details.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay.essay_client_id = :clientId " +
            "GROUP BY essay_details.person_id, person.email, person.name", nativeQuery = true)
    List<Map<String, Object>> getUserActionCountsByGroupAndClientIdYesterday(@Param("startDate") String startDate,
                                                                             @Param("endDate") String endDate,
                                                                             @Param("clientId") int clientId);


    @Query(value = "SELECT essay_details.person_id AS personId, AVG(essay.grade_time) AS avgGradeTime " +
            "FROM essay_details " +
            "JOIN essay ON essay_details.essay_id = essay.id " +
            "WHERE essay_details.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.user_action = 'checked_in' " +
            "AND essay_details.person_id = :personId " +
            "AND essay.essay_client_id = :clientId " +
            "GROUP BY essay_details.person_id", nativeQuery = true)
    List<Map<String, Object>> getAvgGradeTimeByClientId(@Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("personId") String personId,
                                                        @Param("clientId") int clientId);

    @Query(value = "SELECT essay_details.person_id AS personId, AVG(essay.grade_time) AS avgGradeTime " +
            "FROM essay_details " +
            "JOIN essay ON essay_details.essay_id = essay.id " +
            "WHERE essay_details.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.user_action = 'checked_in' " +
            "AND essay_details.person_id = :personId " +
            "AND essay.essay_client_id = :clientId " +
            "GROUP BY essay_details.person_id", nativeQuery = true)
    List<Map<String, Object>> getAvgGradeTimeByClientIdYesterday(@Param("startDate") String startDate,
                                                                 @Param("endDate") String endDate,
                                                                 @Param("personId") String personId,
                                                                 @Param("clientId") int clientId);


    @Query(value = "SELECT essay_details.person_id AS personId, person.email AS personEmail, person.name AS personName, " +
            "    SUM(CASE WHEN essay_details.user_action = 'available' THEN 1 ELSE 0 END) AS availableCount, " +
            "    SUM(CASE WHEN essay_details.user_action = 'checked_out' THEN 1 ELSE 0 END) AS checkedOutCount, " +
            "    SUM(CASE WHEN essay_details.user_action = 'checked_in' THEN 1 ELSE 0 END) AS checkedInCount " +
            "FROM essay_details " +
            "JOIN person ON essay_details.person_id = person.id " +
            "WHERE essay_details.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND essay_details.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "GROUP BY essay_details.person_id, person.email, person.name",
            nativeQuery = true)
    List<Map<String, Object>> getPersonAnalyticsByDate(@Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);


    @Query(value = "SELECT ed.person_id AS personId, AVG(e.grade_time) AS avgGradeTime " +
            "FROM essay_details ed " +
            "JOIN essay e ON ed.essay_id = e.id " +
            "WHERE ed.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND ed.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS.FF6') " +
            "AND ed.user_action = 'checked_in' " +
            "AND ed.person_id = :personId " +
            "GROUP BY ed.person_id", nativeQuery = true)
    List<Map<String, Object>> getAvgGradeTimeByPersonId(@Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("personId") String personId);

}






















