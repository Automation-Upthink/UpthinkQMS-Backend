package com.upthink.qms.repository;

import com.upthink.qms.domain.Essay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EssayRepository extends JpaRepository<Essay, String> {

    // Load essay by essay id
    Optional<Essay> findById(String id);


    @Query(value = "SELECT * FROM essay WHERE essay_id=:essayId", nativeQuery = true)
    Optional<Essay> findByEssayId(@Param("essayId") String essayId);



    // Find essay by status
    @Query(value = "SELECT * FROM essay WHERE status::text IN (:statusList)", nativeQuery = true)
    List<Essay> findByEssayStatus(@Param("statusList") List<String> statusList);

    // Create essay
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO essay (id, essay_id, name, essay_client_id, essay_cred_id, filelink, fetched_for, due_date) " +
            "VALUES (:id, :essayId, :name, :clientId, :credId, :fileLink, :fetchedFor, :dueDate)", nativeQuery = true)
    int createEssay(String id, String essayId, String name, int clientId, int credId, String fileLink, String fetchedFor, String dueDate);

//    // List by client id and essay status
//    @Query(value = "SELECT e.*, ec.name as client_name, ecc.name as cred_name "
//            + "FROM essay e "
//            + "JOIN essay_client ec ON e.essay_client_id = ec.id "
//            + "JOIN essay_client_credential ecc ON e.essay_cred_id = ecc.id "
//            + "WHERE e.essay_client_id = :clientId AND e.status::text IN (:statusList) "
//            + "ORDER BY e.status, e.created_at", nativeQuery = true)
//    Optional<List<Essay>> listByClientIdAndStatusIn(@Param("clientId") Integer clientId, @Param("statusList") List<String> statusList);
//
//    // List by essay status
//    @Query(value = "SELECT e.*, ec.name as client_name, ecc.name as cred_name "
//            + "FROM essay e "
//            + "JOIN essay_client ec ON e.essay_client_id = ec.id "
//            + "JOIN essay_client_credential ecc ON e.essay_cred_id = ecc.id "
//            + "WHERE e.status::text IN (:statusList) "
//            + "ORDER BY e.status, e.created_at", nativeQuery = true)
//    Optional<List<Essay>> listByEssayStatus(@Param("statusList") List<String> statusList);

    // List by client id and essay status
    @Query(value = "SELECT e.id, e.essay_id, e.name, e.filelink, e.status, e.due_date, e.essay_client_id, e.essay_cred_id, e.grade_time, e.created_at, e.fetched_for, ec.name as client_name, ecc.name as cred_name "
            + "FROM essay e "
            + "JOIN essay_client ec ON e.essay_client_id = ec.id "
            + "JOIN essay_client_credential ecc ON e.essay_cred_id = ecc.id "
            + "WHERE e.essay_client_id = :clientId AND e.status::text IN (:statusList) "
            + "ORDER BY e.status, e.created_at", nativeQuery = true)
    List<Object[]> listByClientIdAndStatusIn(@Param("clientId") Integer clientId, @Param("statusList") List<String> statusList);

    // List by essay status
    @Query(value = "SELECT e.id, e.essay_id, e.name, e.filelink, e.status, e.due_date, e.essay_client_id, e.essay_cred_id, e.grade_time, e.created_at, e.fetched_for, ec.name as client_name, ecc.name as cred_name "
            + "FROM essay e "
            + "JOIN essay_client ec ON e.essay_client_id = ec.id "
            + "JOIN essay_client_credential ecc ON e.essay_cred_id = ecc.id "
            + "WHERE e.status::text IN (:statusList) "
            + "ORDER BY e.status, e.created_at", nativeQuery = true)
    List<Object[]> listByEssayStatus(@Param("statusList") List<String> statusList);


    // Update essay status and the time when the status of the essay changed
    @Modifying
    @Transactional
    @Query(value = "UPDATE essay SET status = CAST(:newStatus AS status), updated_at = NOW() WHERE id = :id", nativeQuery = true)
    int updateEssayStatus(@Param("id") String id, @Param("newStatus") String newStatus);


    @Modifying
    @Transactional
    @Query(value = "UPDATE essay SET grade_time = :gradeTime WHERE id= :id", nativeQuery = true)
    void updateEssayGradeTime(@Param("id") String id, @Param("gradeTime")Long gradeTime);

}













