package com.upthink.qms.repository;

import com.upthink.qms.domain.EssayClientCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EssayClientCredRepository extends JpaRepository<EssayClientCred, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO essay_client_credential (name, password, created_by, essay_client_id, download_limit, download_remaining) " +
            "VALUES (:name, :password, :emailId, :essayClientId, :downloadLimit, :remainingDownloads)", nativeQuery = true)
    int insertEssayClientCred(@Param("name") String name,
                              @Param("password") String password,
                              @Param("emailId") String emailId,
                              @Param("essayClientId") int essayClientId,
                              @Param("downloadLimit") int downloadLimit,
                              @Param("remainingDownloads") int remainingDownloads);


    @Query(value = "SELECT * FROM essay_client_credential  WHERE name = :name", nativeQuery = true)
    Optional<EssayClientCred> findByName(@Param("name") String name);

    @Query(value = "SELECT * FROM essay_client_credential WHERE name=:name AND deleted_at IS NULL", nativeQuery = true)
    Optional<EssayClientCred> getEssayClientCredByName(String name);

    @Query(value = "SELECT * FROM essay_client_credential WHERE essay_client_id = :clientId AND deleted_at IS NULL", nativeQuery = true)
    List<EssayClientCred> getEssayClientCredsByClientId(Integer clientId);

    @Transactional
    @Query(value = "SELECT * FROM essay_client_credential WHERE deleted_at IS NULL ORDER By essay_client_id", nativeQuery = true)
    List<EssayClientCred> listEssayClientCreds();

    @Modifying
    @Transactional
    @Query(value = "UPDATE essay_client_credential SET name= :name, password= :password, download_limit= :downloadLimit, " +
            "download_remaining=:downloadRemaining WHERE id= :id", nativeQuery = true)
    int updateEssayClientCred(@Param("id")Integer id, @Param("name")String name, @Param("password")String password,
                              @Param("downloadLimit")Integer downloadLimit, @Param("downloadRemaining")Integer downloadRemaining);

    @Modifying
    @Transactional
    @Query(value = "UPDATE essay_client_credential SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id AND deleted_at IS NULL", nativeQuery = true)
    int deleteEssayClientCred(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "update essay_client_credential set download_remaining=download_remaining+(:count)"
            + " where id=:id", nativeQuery=true)
    void updateDownloadCounter(@Param("id") Integer id, @Param("count") Integer count);
}
