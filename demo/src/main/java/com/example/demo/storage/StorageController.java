package com.example.demo.storage;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    // Endpoint to download a PDF by its filename
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable String filename) {
        try {
            // Load the file content from storage
            byte[] pdfContent = storageService.loadFile(filename);

            // Set the HTTP headers for PDF file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);  // Set content type as PDF
            headers.setContentDisposition(ContentDisposition.builder("attachment")  // Set as attachment
                    .filename(filename)  // The file's name to be saved on the client-side
                    .build());

            // Return the file content as a response with the correct headers
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            // If the file is not found, return 404 NOT FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Endpoint to list all stored PDF files
    @GetMapping("/list")
    public ResponseEntity<?> listFiles() {
        try {
            return ResponseEntity.ok(storageService.listAllFiles());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error listing files");
        }
    }

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

    // Endpoint to save a new PDF (file content received as a byte array)
    @PostMapping("/save")
    public ResponseEntity<String> savePDF(@RequestParam byte[] fileContent, @RequestParam String fileName) {
        try {
            storageService.saveFile(fileContent, fileName);
            return ResponseEntity.ok("File saved successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file");
        }
    }

    // Endpoint to delete a file from storage
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try {
            storageService.deleteFile(filename);
            return ResponseEntity.ok("File deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file");
        }
    }
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
}
