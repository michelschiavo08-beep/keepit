package com.keepit.repository;

import com.keepit.model.MonitoringLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoringLogRepository extends JpaRepository<MonitoringLog, Long> {
    List<MonitoringLog> findByAssetIdOrderByReadingDateDesc(Long assetId);
}
