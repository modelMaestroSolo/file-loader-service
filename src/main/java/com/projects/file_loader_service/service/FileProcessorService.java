package com.projects.file_loader_service.service;

import java.nio.file.Path;

public interface FileProcessorService {
    void process(Path file);
}