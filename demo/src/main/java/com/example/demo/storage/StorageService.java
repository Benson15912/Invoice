package com.example.demo.storage;

import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorageService {

    // Define a directory where PDFs will be stored
    private final Path storageLocation = Paths.get("pdf-storage");

    public StorageService() throws IOException {
        // Create the directory if it does not exist
        if (!Files.exists(storageLocation)) {
            Files.createDirectories(storageLocation);
        }
    }

    public void saveFile(byte[] pdfContent, String fileName) throws IOException {
        // Ensure the directory exists before saving the file
        if (!Files.exists(storageLocation)) {
            Files.createDirectories(storageLocation);
        }

        Path targetLocation = storageLocation.resolve(fileName);

        // Check if file already exists and handle accordingly
        if (Files.exists(targetLocation)) {
            System.out.println("File already exists, overwriting: " + targetLocation);
        }

        // Save the file, using StandardOpenOption to make it more robust in a multi-threaded environment
        Files.write(targetLocation, pdfContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Log success
        System.out.println("File saved successfully to: " + targetLocation.toAbsolutePath().toString());
    }

    public void saveInvoice(byte[] pdfContent, String fileName, String date) throws IOException {
        Path targetFolder = createSubdirectory(date);

        Path targetLocation = targetFolder.resolve(fileName);
        // Check if file already exists and handle accordingly
        if (Files.exists(targetLocation)) {
            System.out.println("File already exists, overwriting: " + targetLocation);
        }

        // Save the file, using StandardOpenOption to make it more robust in a multi-threaded environment
        Files.write(targetLocation, pdfContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Log success
        System.out.println("File saved successfully to: " + targetLocation.toAbsolutePath().toString());
    }



    // Load a PDF by its file name (for downloading/viewing)
    public byte[] loadFile(String relativePath) throws IOException {
        Path filePath = storageLocation.resolve(relativePath).normalize();
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return Files.readAllBytes(filePath);
    }


    // List all stored PDF files
    public List<String> listAllFiles() throws IOException {
        try (Stream<Path> fileStream = Files.walk(storageLocation)) {
            return fileStream
                    .filter(Files::isRegularFile)
                    .map(storageLocation::relativize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    // Delete a file from storage
    public void deleteFile(String fileName) throws IOException {
        Path targetLocation = storageLocation.resolve(fileName);
        Files.deleteIfExists(targetLocation);
    }

    public Path createSubdirectory(String date) throws IOException {
        // Parse year and month from date (expected format: yyyy-MM-dd)
        String[] parts = date.split("-");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Date must be in format yyyy-MM-dd");
        }
        String year = parts[0];
        String month = parts[1];

        // Construct the subdirectory path
        Path subDir = storageLocation.resolve(Paths.get(year, month));

        // Create the directories if they don't exist
        if (!Files.exists(subDir)) {
            Files.createDirectories(subDir);
            System.out.println("Created subdirectory: " + subDir.toAbsolutePath());
        } else {
            System.out.println("Subdirectory already exists: " + subDir.toAbsolutePath());
        }

        return subDir;
    }
}
