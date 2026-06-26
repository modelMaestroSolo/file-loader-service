package com.projects.file_loader_service;

import com.projects.file_loader_service.service.FileProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

@SpringBootApplication
public class FileLoaderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileLoaderServiceApplication.class, args);
	}

    @Bean
    public CommandLineRunner runFileLoader(
            FileProcessorService fileProcessorService
            ){
        Path file = Path.of("C:\\Users\\yebso\\Documents\\PAIC project files\\cdr.log.20230818_10_reducted");
        return args -> fileProcessorService.process(file);
    }

}
