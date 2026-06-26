package com.projects.file_loader_service.repository;

import com.projects.file_loader_service.model.CrdLog;

import java.sql.Timestamp;
import java.util.Optional;

public interface CrdLogRepository {
    Optional<CrdLog> findByFileName(String fileName);
    long insert(CrdLog log);
    void complete(long id, Timestamp endTime, int successCount, int failCount, String status);
}