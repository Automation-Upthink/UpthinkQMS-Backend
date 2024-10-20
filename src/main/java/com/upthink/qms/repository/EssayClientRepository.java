package com.upthink.qms.repository;

import com.upthink.qms.domain.EssayClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EssayClientRepository extends JpaRepository<EssayClient, Integer> {

    Optional<EssayClient> findById(Integer id);

    @Query(value = "SELECT * FROM essay_client WHERE name= :name", nativeQuery = true)
    Optional <EssayClient> findByName(String name);

    @Query(value = "SELECT FROM essay_client WHERE id = :id AND deleted_at IS NULL", nativeQuery = true)
    Optional<EssayClient> findByIdAndActive(Integer id);

    @Transactional
    @Query(value = "SELECT id, name, download_capacity FROM essay_client WHERE deleted_at IS NULL", nativeQuery = true)
    List<EssayClient> findAllActive();

    @Modifying
    @Transactional
    @Query(value = "UPDATE essay_client SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id AND deleted_at IS NULL", nativeQuery = true)
    int softDeleteById(Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE essay_client SET name = :name, download_capacity = :downloadCapacity WHERE id = :id AND deleted_at IS NULL", nativeQuery = true)
    int updateClient(@Param("id") int id, @Param("name") String name, @Param("downloadCapacity") int downloadCapacity);

    List<EssayClient> findByIdIn(Set<Integer> ids);

}
