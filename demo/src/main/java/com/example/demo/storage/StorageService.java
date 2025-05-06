package com.example.demo.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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

    /**
     * Constructs a new instance of the StorageService class.
     * This constructor initializes the storage service by ensuring that the
     * directory specified by the `storageLocation` field exists.
     * If the directory does not exist, it is created.
     *
     * @throws IOException if an I/O error occurs while creating the directory
     */
    public StorageService() throws IOException {
        // Create the directory if it does not exist
        if (!Files.exists(storageLocation)) {
            Files.createDirectories(storageLocation);
        }
    }

    /**
     * Lists all files and folders within a given directory path, organizing them as FileNode objects.
     * Each folder is further populated with its children in a hierarchical structure.
     *
     * @param rootDir the root directory path from which the files and folders are to be listed
     * @return a list of FileNode objects, where each object represents a folder with its children
     * @throws IOException if an I/O error occurs accessing the directory or its contents
     */
    public List<FileNode> listFilesAndFolders(Path rootDir) throws IOException {
        List<FileNode> result = new ArrayList<>();

        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    paths.add(path);
                }
            }
        }

        // Sort descending by folder name
        paths.sort((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()));

        for (Path path : paths) {
            String folderName = path.getFileName().toString();
            FileNode folderNode = new FileNode(folderName, true, folderName);
            addChildren(folderNode, path, folderName);
            result.add(folderNode);
        }

        return result;
    }


    /**
     * Recursively adds child nodes to the given parent node by exploring the
     * directory structure at the specified path.
     *
     * @param parentNode      The parent FileNode to which child nodes will be added.
     * @param currentPath     The Path object representing the current directory being explored.
     * @param parentPathString The string representation of the parent directory's path.
     */
    private void addChildren(FileNode parentNode, Path currentPath, String parentPathString) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) {
            List<Path> childrenPaths = new ArrayList<>();
            for (Path path : stream) {
                childrenPaths.add(path);
            }

            for (Path path : childrenPaths) {
                String name = path.getFileName().toString();
                String fullPath = parentPathString + "/" + name;

                if (Files.isDirectory(path)) {
                    FileNode folderNode = new FileNode(name, true, fullPath);
                    parentNode.addChild(folderNode);
                    addChildren(folderNode, path, fullPath);
                } else {
                    FileNode fileNode = new FileNode(name, false, fullPath);
                    parentNode.addChild(fileNode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // or log
        }
    }

    /**
     * Loads a file from the specified relative path within the storage location.
     * This method reads the bytes of the file and returns them as a byte array.
     * If the file does not exist at the resolved path, a FileNotFoundException is thrown.
     *
     * @param relativePath the relative path to the file within the storage directory
     * @return a byte array containing the contents of the loaded file
     * @throws IOException if an I/O error occurs while accessing the file
     */
    // Load a PDF by its file name (for downloading/viewing)
    public byte[] loadFile(String relativePath) throws IOException {
        Path filePath = storageLocation.resolve(relativePath).normalize();
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return Files.readAllBytes(filePath);
    }


/**
 * Deletes a file or directory from the storage location.
 * If it's a directory, recursively deletes all its contents first.
 *
 * @param filePath the relative path of the file/directory to be deleted
 * @return true if deleted successfully, false if path doesn't exist
 * @throws IOException if an I/O error occurs during deletion
 * @throws SecurityException if path traversal is attempted
 */
public boolean deleteFile(String filePath) throws IOException {
    Path targetLocation = storageLocation.resolve(filePath).normalize();
    // Security check to prevent path traversal
    if (!targetLocation.startsWith(storageLocation)) {
        throw new SecurityException("Path traversal outside storage location is not allowed");
    }

    if (!Files.exists(targetLocation)) {
        return false;
    }

    if (Files.isDirectory(targetLocation)) {
        try (Stream<Path> files = Files.walk(targetLocation)) {
            files.sorted(Comparator.reverseOrder()) // Delete children before parents
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException e) {
                         throw new UncheckedIOException(e);
                     }
                 });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    } else {
        Files.delete(targetLocation);
    }
    
    return true;
}

    /**
     * Creates a subdirectory at the specified path within the storage location.
     * If the directory does not already exist, it will be created, including any necessary but nonexistent parent directories.
     *
     * @param targetDir the target directory within the storage location where the subdirectory is to be created
     * @param folderName the subdirectory name to be created under the target directory
     * @throws IOException if an I/O error occurs during the directory creation process
     */
    public boolean createDirectory(String targetDir, String folderName) throws IOException {
        Path targetLocation = storageLocation.resolve(targetDir).resolve(folderName);
        if (!Files.exists(targetLocation)) {
            Files.createDirectories(targetLocation);
            System.out.println("Created subdirectory: " + targetLocation.toAbsolutePath());
            return true;
        } else {
            System.out.println("Subdirectory already exists: " + targetLocation.toAbsolutePath());
            return false;
        }
    }


//    // List all stored PDF files
//    public List<String> listAllFiles() throws IOException {
//        try (Stream<Path> fileStream = Files.walk(storageLocation)) {
//            return fileStream
//                    .filter(Files::isRegularFile)
//                    .map(storageLocation::relativize)
//                    .map(Path::toString)
//                    .collect(Collectors.toList());
//        }
//    }


//    public void saveFile(byte[] pdfContent, String fileName) throws IOException {
//        // Ensure the directory exists before saving the file
//        if (!Files.exists(storageLocation)) {
//            Files.createDirectories(storageLocation);
//        }
//
//        Path targetLocation = storageLocation.resolve(fileName);
//
//        // Check if file already exists and handle accordingly
//        if (Files.exists(targetLocation)) {
//            System.out.println("File already exists, overwriting: " + targetLocation);
//        }
//
//        // Save the file, using StandardOpenOption to make it more robust in a multi-threaded environment
//        Files.write(targetLocation, pdfContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//
//        // Log success
//        System.out.println("File saved successfully to: " + targetLocation.toAbsolutePath().toString());
//    }
//


    public void saveInvoice(byte[] pdfContent, String fileName, String date) throws IOException {
        Path targetFolder = createSubdirectory(date);

        Path targetLocation = targetFolder.resolve(fileName);
        // Check if a file already exists and handle accordingly
        if (Files.exists(targetLocation)) {
            System.out.println("File already exists, overwriting: " + targetLocation);
        }

        // Save the file, using StandardOpenOption to make it more robust in a multi-threaded environment
        Files.write(targetLocation, pdfContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Log success
        System.out.println("File saved successfully to: " + targetLocation.toAbsolutePath().toString());
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