package com.openclassrooms.chatop.config;

import com.openclassrooms.chatop.service.RentalPictureStorage;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadedFilesConfiguration implements WebMvcConfigurer {

    private final RentalPictureStorage pictureStorage;

    public UploadedFilesConfiguration(RentalPictureStorage pictureStorage) {
        this.pictureStorage = pictureStorage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(pictureStorage.getUploadDirectory().toUri() + "/");
    }
}
