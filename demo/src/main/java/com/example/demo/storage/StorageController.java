package com.example.demo.storage;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


import java.io.IOException;

@RestController
@RequestMapping("/api/storage")
@Tag(name = "Storage Controller", description = "Endpoints for managing file storage operations")
public class StorageController {

    @Autowired
    private StorageService storageService;

    /**
     * Handles the HTTP GET request to retrieve the hierarchical structure of files and folders
     * from the specified storage directory. The method constructs the directory tree
     * starting from the root location and returns it as a response.
     *
     * @return ResponseEntity containing a list of {@link FileNode} objects representing
     *         the file/folder structure if successful, or an error message in case of an
     *         internal server error.
     */
    @Operation(summary = "Get file tree", description = "Retrieves the hierarchical structure of files and folders from the storage directory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved file tree"),
            @ApiResponse(responseCode = "500", description = "Server error while retrieving file tree")
    })

    @GetMapping("/listfoldertree")
    public ResponseEntity<?> getFiletree() {
        try {
            Path location = Paths.get("pdf-storage");
            List<FileNode> tree = storageService.listFilesAndFolders(location);
            return ResponseEntity.ok(tree);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error listing files: " + e.getMessage());
        }
    }

    /**
     * Handles the HTTP GET request to view a PDF file from the specified file path.
     * The method retrieves the PDF content from the storage and returns it as a response
     * with appropriate headers for displaying the file inline in the browser.
     *
     * @param filepath the relative file path of the PDF to be viewed
     * @return ResponseEntity containing the byte array of the PDF content with proper
     *         HTTP headers if successful, or a 404 NOT FOUND response if the file is not found
     */
    @Operation(summary = "View PDF file", description = "Retrieves and displays a PDF file from the storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF file retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "PDF file not found")
    })
    @GetMapping("/view")
    public ResponseEntity<byte[]> viewPDF(@RequestParam String filepath) {
        try {
            byte[] pdfContent = storageService.loadFile(filepath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(Paths.get(filepath).getFileName().toString()).build());
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Deletes a file from the storage system using the specified file path.
     * Handles file deletion and returns an appropriate HTTP response.
     *
     * @param filePath the relative file path of the file to be deleted
     * @return ResponseEntity containing a success message if the file is deleted successfully,
     *         or an error message with the appropriate HTTP status if the file deletion fails
     */
    // Endpoint to delete a file from storage
    @Operation(summary = "Delete file", description = "Deletes a file from the storage system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Server error while deleting file")
    })

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String filePath) {
        try {
            boolean deleted = storageService.deleteFile(filePath);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File not found: " + filePath);
            }
            return ResponseEntity.ok("File deleted successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Invalid file path");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting file");
        }

    }

    /**
     * Handles the HTTP POST request to create a new folder within the specified directory.
     * The method uses the provided target directory and folder name to create a new folder
     * and returns an appropriate HTTP response.
     *
     * @param targetDir the path of the directory where the new folder is to be created
     * @param folderName the name of the folder to be created
     * @return ResponseEntity containing a success message if the folder is created successfully,
     *         or an error message with the appropriate HTTP status if folder creation fails
     */
    @Operation(summary = "Create folder", description = "Creates a new folder in the specified directory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Folder created successfully"),
            @ApiResponse(responseCode = "400", description = "Folder already exists"),
            @ApiResponse(responseCode = "500", description = "Server error while creating folder")
    })

    @PostMapping("/addfolder")
    public ResponseEntity<String> makeFolder(@RequestParam String targetDir, @RequestParam String folderName) {
        try {
            boolean created = storageService.createDirectory(targetDir, folderName);
            if (!created) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Folder already exists: " + folderName);
            }
            else {
                return ResponseEntity.status(HttpStatus.CREATED).build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating folder");
        }
    }


//    // Endpoint to download a PDF by its filename
//    @GetMapping("/download/{filename}")
//    public ResponseEntity<byte[]> downloadPDF(@PathVariable String filename) {
//        try {
//            // Load the file content from storage
//            byte[] pdfContent = storageService.loadFile(filename);
//
//            // Set the HTTP headers for PDF file download
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);  // Set content type as PDF
//            headers.setContentDisposition(ContentDisposition.builder("attachment")  // Set as attachment
//                    .filename(filename)  // The file's name to be saved on the client-side
//                    .build());
//
//            // Return the file content as a response with the correct headers
//            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
//        } catch (IOException e) {
//            // If the file is not found, return 404 NOT FOUND
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//    }
//
//    // Endpoint to list all stored PDF files
//    @GetMapping("/list")
//    public ResponseEntity<?> listFiles() {
//        try {
//            return ResponseEntity.ok(storageService.listAllFiles());
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error listing files");
//        }
//    }
//

//
//    // Endpoint to save a new PDF (file content received as a byte array)
//    @PostMapping("/save")
//    public ResponseEntity<String> savePDF(@RequestParam byte[] fileContent, @RequestParam String fileName) {
//        try {
//            storageService.saveFile(fileContent, fileName);
//            return ResponseEntity.ok("File saved successfully!");
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file");
//        }
//    }
//


}
