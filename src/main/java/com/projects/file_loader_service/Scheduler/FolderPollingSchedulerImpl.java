package com.projects.file_loader_service.Scheduler;

import com.projects.file_loader_service.service.FileProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FolderPollingSchedulerImpl implements FolderPollingScheduler{

    private static final Logger log = LoggerFactory.getLogger(FolderPollingSchedulerImpl.class);

    private final FileProcessorService processorService;

    public FolderPollingSchedulerImpl(FileProcessorService processorService) {
        this.processorService = processorService;
    }

    @Value("${app.loader.watch-folder}")
    private String watchFolder;

    @Scheduled(fixedDelayString = "${app.loader.poll-interval-ms}")
    public void poll() {
        Path folder = Path.of(watchFolder);
        if (!Files.isDirectory(folder)) {
            log.warn("Watch folder is not a directory: {}", watchFolder);
            return;
        }

        List<Path> files;

        try(Stream<Path> fileStream = Files.list(folder) ){
            files = fileStream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .toList();

        } catch (IOException e) {
            log.error("Error listing watch folder {}: {}", watchFolder, e.getMessage(), e);
            return;
        }

        if (files.isEmpty()){
            return;
        }

        log.debug("Found {} file(s) to process in {}", files.size(), watchFolder);

        for (Path file: files){
            try{
                processorService.process(file);
            } catch (Exception e) {
                log.error("unexpected error processing {}: {}", file.getFileName(), e.getMessage(), e);
            }
        }
    }
}
