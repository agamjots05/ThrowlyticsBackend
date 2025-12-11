package dev.throwlytics.ThrowlyticsBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuration for serving static files (videos and thumbnails)
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve videos
        String videosPath = Paths.get(uploadDir, "videos").toAbsolutePath().toString();
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + videosPath + "/");
        
        // Serve thumbnails
        String thumbnailsPath = Paths.get(uploadDir, "thumbnails").toAbsolutePath().toString();
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + thumbnailsPath + "/");
        
        // Also serve from uploads root for backward compatibility
        String uploadsPath = Paths.get(uploadDir).toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath + "/");
    }
}

