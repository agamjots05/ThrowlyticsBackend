package dev.throwlytics.ThrowlyticsBackend.config;

import dev.throwlytics.ThrowlyticsBackend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to initialize file storage directories on startup
 */
@Configuration
public class FileStorageConfig implements CommandLineRunner {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Override
    public void run(String... args) throws Exception {
        fileStorageService.initializeDirectories();
        System.out.println("✓ Upload directories initialized");
    }
}

