package com.upthink.qms.repository;

import com.upthink.qms.domain.EssayScriptLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EssayScriptLogRepository extends JpaRepository<EssayScriptLog, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO essay_script_log(person_id, action_performed, type, client_name, uuid) " +
            "VALUES (:personId, CAST(:actionPerformed AS action), :type, :clientName, :uuid)", nativeQuery = true)
    int insertScriptLog(
            @Param("personId") String personId,
            @Param("actionPerformed") String actionPerformed,
            @Param("type") String type,
            @Param("clientName") String clientName,
            @Param("uuid") String uuid
    );

    @Query(value = "SELECT * FROM essay_script_log WHERE person_id = :personId AND type = :type " +
            "AND action_performed = :actionPerformed ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    EssayScriptLog getEssayScriptLog(
            @Param("personId") String personId,
            @Param("actionPerformed") String actionPerformed,
            @Param("type") String type
    );
}
