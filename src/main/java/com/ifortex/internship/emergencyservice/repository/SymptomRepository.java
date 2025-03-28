package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.Symptom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, UUID> {

    Page<Symptom> findByParentIsNull(Pageable pageable);

    Page<Symptom> findByParentId(UUID parentId, Pageable pageable);

    List<Symptom> findByParentId(UUID parentId);

    Page<Symptom> findByIdNotIn(Set<UUID> ids, Pageable pageable);

    Optional<Symptom> findByName(String name);

    @Query(value = """
        WITH RECURSIVE symptom_tree AS (
            SELECT * FROM symptoms WHERE id IN (:ids)
            UNION
            SELECT s.* FROM symptoms s
            JOIN symptom_tree st ON s.id = st.parent_id
        )
        SELECT * FROM symptom_tree
        """, nativeQuery = true)
    List<Symptom> findAllWithParentsRecursively(@Param("ids") Set<UUID> ids);

    @Query(value = """
    WITH RECURSIVE symptom_tree AS (
        SELECT * FROM symptoms WHERE id IN (:ids)
        UNION
        SELECT s.* FROM symptoms s
        JOIN symptom_tree st ON s.parent_id = st.id
    )
    SELECT * FROM symptom_tree
    """, nativeQuery = true)
    List<Symptom> findAllChildrenRecursively(@Param("ids") Set<UUID> ids);
}
