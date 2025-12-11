package dev.throwlytics.ThrowlyticsBackend.repository;

import dev.throwlytics.ThrowlyticsBackend.model.ThowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ThrowHistory entity
 */
@Repository
public interface ThrowHistoryRepository extends JpaRepository<ThowHistory, Long> {
    
    /**
     * Find all throws for a specific user, ordered by most recent first
     */
    List<ThowHistory> findByUserUserIdOrderByUploadDateDesc(Long userId);
}

