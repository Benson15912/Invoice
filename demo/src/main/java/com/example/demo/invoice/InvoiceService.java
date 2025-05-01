package com.example.demo.invoice;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.borders.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import java.io.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.LocalDate;
import java.time.Month;


@Service
public class InvoiceService {

    public byte[] generateInvoice(Long studentId, String name, double rate, String date, String day) throws IOException {
        Month month = getMonth(date); // returns Month
        int year = getYear(date);
        int numberOfLessons = countDaysInMonth(year, month, day);

        return toPDF(
                studentId,
                name,
                rate,
                month.toString(), // pass String to PDF
                year,
                numberOfLessons
        );
    }

    public byte[] toPDF(Long studentId, String name, double rate, String month, int year, int numberOfLessons) throws IOException {
        double amount = rate * numberOfLessons;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        // Logo
        InputStream logoStream = new ClassPathResource("images/logo.png").getInputStream();
        byte[] logoBytes = logoStream.readAllBytes();
        Image logo = new Image(ImageDataFactory.create(logoBytes));
        logo.setHeight(80);
        logo.setAutoScale(true);

        // Title section
        Paragraph title = new Paragraph("OFFICIAL INVOICE\nFOR " + month)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.RIGHT);
        Paragraph invoiceDetails = new Paragraph(String.format(
                "# %d/TJLC\nDate: 2 %s %d\nDue Date: 15 %s %d", year, month, year, month, year))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        Paragraph balanceDue = new Paragraph("Balance Due: SGD " + String.format("%.2f", amount))
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT);

        // Header table (logo left, title right)
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
        headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
        Cell rightCell = new Cell().add(title).add(invoiceDetails).add(balanceDue);
        rightCell.setBorder(Border.NO_BORDER);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Bill To
        document.add(new Paragraph("\nBill To:\n").setBold());
        document.add(new Paragraph(name).setBold());

        // Item Table
        Table itemTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2, 2})).useAllAvailableWidth();
        itemTable.addHeaderCell(createHeaderCell("Item"));
        itemTable.addHeaderCell(createHeaderCell("Quantity"));
        itemTable.addHeaderCell(createHeaderCell("Rate"));
        itemTable.addHeaderCell(createHeaderCell("Amount"));

        itemTable.addCell(String.format("%s (%d Lessons)", month, numberOfLessons));
        itemTable.addCell(String.format("%d", numberOfLessons));
        itemTable.addCell(String.format("SGD %.2f", rate));
        itemTable.addCell(String.format("SGD %.2f", amount));

        document.add(itemTable);

        // Payment Terms
        Paragraph terms = new Paragraph("\nTerms:\nPayment Details\n\n" +
                "1) OCBC Current Account 609-300942-001\n" +
                "Or\n" +
                "2) PayNOW to TUTORJOHN\nUEN no: 53373300J\n\n" +
                "Please send us a screenshot once the payment is made. Thank you! 😊")
                .setFontSize(10);

        // QR Code Image
        InputStream qrStream = new ClassPathResource("images/paynow_qr.png").getInputStream();
        byte[] qrBytes = qrStream.readAllBytes();
        Image qr = new Image(ImageDataFactory.create(qrBytes));
        qr.setWidth(100);

        // Bottom table (terms left, QR right)
        Table bottomTable = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        bottomTable.addCell(new Cell().add(terms).setBorder(Border.NO_BORDER));
        bottomTable.addCell(new Cell().add(new Paragraph(String.format("Total: SGD %.2f", amount)).setBold()).add(qr).setBorder(Border.NO_BORDER));

        document.add(bottomTable);

        document.close();
        return out.toByteArray();
    }

    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
    private Month getMonth(String date) {
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        return parsedDate.getMonth();
    }

    private int getYear(String date) {
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);

        int year = parsedDate.getYear();
        return year;
    }

    private int countDaysInMonth(int year, Month month, String day) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth());

        DayOfWeek targetDay = DayOfWeek.valueOf(day.toUpperCase()); // e.g., "monday" → MONDAY

        int count = 0;
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == targetDay) {
                count++;
            }
        }
        return count;
    }
}
