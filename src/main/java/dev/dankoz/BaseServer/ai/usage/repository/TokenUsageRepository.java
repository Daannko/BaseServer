package dev.dankoz.BaseServer.ai.usage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.dankoz.BaseServer.ai.usage.model.TokenUsageRecord;

@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsageRecord, Long> {

    /** Single aggregate row: [sumTotal, sumPrompt, sumCompletion, requestCount]. */
    @Query("""
            select coalesce(sum(t.totalTokens), 0),
                   coalesce(sum(t.promptTokens), 0),
                   coalesce(sum(t.completionTokens), 0),
                   count(t)
            from TokenUsageRecord t
            where t.userId = :userId
            """)
    List<Object[]> totalsForUser(@Param("userId") Integer userId);

    /** Rows of [feature, sumTotal, requestCount]. */
    @Query("""
            select t.feature, coalesce(sum(t.totalTokens), 0), count(t)
            from TokenUsageRecord t
            where t.userId = :userId
            group by t.feature
            order by sum(t.totalTokens) desc
            """)
    List<Object[]> sumByFeature(@Param("userId") Integer userId);

    /** Rows of [model, sumTotal, requestCount]. */
    @Query("""
            select t.model, coalesce(sum(t.totalTokens), 0), count(t)
            from TokenUsageRecord t
            where t.userId = :userId
            group by t.model
            order by sum(t.totalTokens) desc
            """)
    List<Object[]> sumByModel(@Param("userId") Integer userId);
}
