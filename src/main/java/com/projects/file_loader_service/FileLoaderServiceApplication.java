package com.projects.file_loader_service;

import com.projects.file_loader_service.service.FileProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Path;

@EnableScheduling
@SpringBootApplication
public class FileLoaderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileLoaderServiceApplication.class, args);
	}

}
