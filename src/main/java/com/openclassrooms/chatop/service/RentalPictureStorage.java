package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class RentalPictureStorage {

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif"
    );

    private final Path uploadDirectory;

    public RentalPictureStorage(@Value("${app.upload.dir:uploads}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile picture) {
        if (picture == null || picture.isEmpty()) {
            throw new InvalidRequestException("A rental picture is required");
        }

        String extension = IMAGE_EXTENSIONS.get(picture.getContentType());
        if (extension == null) {
            throw new InvalidRequestException("Only JPEG, PNG, and GIF images are supported");
        }

        byte[] content;
        try {
            content = picture.getBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read rental picture", exception);
        }

        try {
            if (ImageIO.read(new ByteArrayInputStream(content)) == null) {
                throw new InvalidRequestException("The uploaded file is not a valid image");
            }
        } catch (IOException exception) {
            throw new InvalidRequestException("The uploaded file is not a valid image");
        }

        try {
            Files.createDirectories(uploadDirectory);
            String filename = UUID.randomUUID() + extension;
            Files.write(uploadDirectory.resolve(filename), content);
            return "/uploads/" + filename;
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to store rental picture", exception);
        }
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }
}
