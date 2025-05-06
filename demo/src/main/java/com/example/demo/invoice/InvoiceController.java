package com.example.demo.invoice;

import com.example.demo.storage.StorageService;
import com.example.demo.invoice.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private StorageService storageService;

    /**
     * Generates an invoice for a student, automatically calculating the number of lessons for a given month
     * based on the specified day of the week. The invoice is saved as a PDF file.
     *
     * @param studentId The unique identifier of the student for whom the invoice is generated.
     * @param name The name of the student for whom the invoice is generated.
     * @param rate The hourly rate or cost per lesson.
     * @param date The date representing the target month and year in the format "yyyy-MM-dd".
     * @param day The day of the week (e.g., "Monday", "Tuesday") used to calculate the lessons in the specified month.
     * @return A ResponseEntity containing an HTTP status and message. HTTP 200 OK if successful,
     *         HTTP 400 BAD REQUEST if the input is invalid, or HTTP 500 INTERNAL SERVER ERROR for other exceptions.
     */
    @Operation(summary = "Generate invoice",
            description = "Generates invoices by automatically counting number of lessons for that month")
    @PostMapping("/generateinvoice/auto")
    public ResponseEntity<String> getInvoice(@RequestParam Long studentId,
                                             @RequestParam String name,
                                             @RequestParam double rate,
                                             @RequestParam String date,     // e.g., "2025-05-01"
                                             @RequestParam String day) { //monday, tuesday etc
        try {
            byte[] pdf = invoiceService.generateInvoice(studentId, name, rate, date, day);
            storageService.saveInvoice(pdf, name+".pdf", date);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException e) {
            // Invalid date format
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Invalid date format. Please use yyyy-MM-dd format");
        } catch (IllegalArgumentException e) {
            // Another invalid input
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Illegal Argument" + e.getMessage());
        } catch (Exception e) {
            // Internal server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
    }

    /**
     * Generates an invoice for a student based on manual input, such as the number of lessons.
     * The invoice is output as a PDF file and stored in the system.
     *
     * @param studentId The unique identifier of the student for whom the invoice is generated.
     * @param name The name of the student for whom the invoice is generated.
     * @param rate The hourly rate or cost per lesson.
     * @param date The date representing the target month and year for the invoice in the format "yyyy-MM-dd".
     * @param numberOfLessons The total number of lessons to be included in the invoice.
     * @return A ResponseEntity containing an HTTP status and message. HTTP 200 OK if the invoice is generated successfully,
     *         HTTP 400 BAD REQUEST if the input is invalid (e.g., incorrect date format),
     *         or HTTP 500 INTERNAL SERVER ERROR for other exceptions.
     */
    @Operation(summary = "Generates Invoice", description = "Generates invoices based on manual input")
    @PostMapping("/generateinvoice/manual")
    public ResponseEntity<String> getInvoice(@RequestParam Long studentId,
                                             @RequestParam String name,
                                             @RequestParam double rate,
                                             @RequestParam String date,     // e.g., "2025-05-01"
                                             @RequestParam int numberOfLessons) {
        try {
            int year = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).getYear();
            String month = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).getMonth().toString();
            byte[] pdf = invoiceService.toPDF(studentId,name,rate,month,year,numberOfLessons);
            storageService.saveInvoice(pdf, name+".pdf", date);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException e) {
            // Invalid date format
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Invalid date format. Please use yyyy-MM-dd format");
        } catch (IllegalArgumentException e) {
            // Another invalid input
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Illegal Argument" + e.getMessage());
        } catch (Exception e) {
            // Internal server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
    }

}