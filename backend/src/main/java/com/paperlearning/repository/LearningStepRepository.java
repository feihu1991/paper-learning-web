package com.paperlearning.repository;

import com.paperlearning.entity.LearningStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningStepRepository extends JpaRepository<LearningStep, Long> {
    List<LearningStep> findByPaperIdOrderByStepOrderAsc(Long paperId);
    void deleteByPaperId(Long paperId);
    int countByPaperId(Long paperId);
}
