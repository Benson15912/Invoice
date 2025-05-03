package com.example.demo.invoice;

import com.example.demo.storage.StorageService;
import com.example.demo.invoice.InvoiceService;
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

    @GetMapping
    public ResponseEntity<byte[]> getInvoice(@RequestParam Long studentId,
                                             @RequestParam String name,
                                             @RequestParam double rate,
                                             @RequestParam String date,     // e.g., "2025-05-01"
                                             @RequestParam String day) { //monday, tuesday etc
        try {
            byte[] pdf = invoiceService.generateInvoice(studentId, name, rate, date, day);
            storageService.saveInvoice(pdf, name+".pdf", date);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDisposition(ContentDisposition.builder("attachment")
//                    .filename("invoice_" + studentId + ".pdf").build());

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Invalid input, return 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Error: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            // Internal server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}