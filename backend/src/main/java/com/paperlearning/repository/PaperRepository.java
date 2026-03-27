package com.paperlearning.repository;

import com.paperlearning.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {
    Page<Paper> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Paper p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.paperAbstract) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.authors) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Paper> search(@Param("query") String query, Pageable pageable);

    List<Paper> findByParsedStatus(Paper.ParsedStatus status);

    Paper findByArxivId(String arxivId);
}
