package com.projects.file_loader_service.repository;

import com.projects.file_loader_service.model.CallDetailRecord;

public interface CdrRepository {
    int insert(CallDetailRecord r);
}