package com.advancedjava.interview.frameworks.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA + record 投影：
 * 使用 JPQL constructor expression 直接映射到 record。
 */
public interface InterviewResultRepository extends JpaRepository<InterviewResultEntity, Long> {

    @Query("""
            select new com.advancedjava.interview.frameworks.springdata.InterviewSummary(
                r.id, r.candidateName, r.score
            )
            from InterviewResultEntity r
            where r.score >= :minScore
            order by r.score desc
            """)
    List<InterviewSummary> findSummaryByMinScore(@Param("minScore") Integer minScore);
}
