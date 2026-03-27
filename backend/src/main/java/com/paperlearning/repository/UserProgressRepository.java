package com.paperlearning.repository;

import com.paperlearning.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByPaperId(Long paperId);
    void deleteByPaperId(Long paperId);
}
