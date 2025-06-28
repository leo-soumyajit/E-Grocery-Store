package com.soumyajit.E_Grocery.Shop.EmailService;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderItemDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class SendInvoiceEmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendInvoice(OrderDTO orderDTO) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 🏪 Header
            Paragraph header = new Paragraph("🛒 E-Grocery Store")
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph contact = new Paragraph("📞 123-456-7890   ✉️ support@egrocery.com   📍 Kolkata, India")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(contact);

            // 👤 Customer and Order Info
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);

            infoTable.addCell(new Cell().add(new Paragraph("👤 Customer Name: " + orderDTO.getCustomerName())).setBorder(Border.NO_BORDER));
            infoTable.addCell(new Cell().add(new Paragraph("📦 Order ID: " + orderDTO.getOrderId())).setBorder(Border.NO_BORDER));
            infoTable.addCell(new Cell().add(new Paragraph("✉️ Email: " + orderDTO.getCustomerEmail())).setBorder(Border.NO_BORDER));
            infoTable.addCell(new Cell().add(new Paragraph("🗓️ Placed At: " + orderDTO.getPlacedAt())).setBorder(Border.NO_BORDER));
            document.add(infoTable);

            // 📦 Order Details Heading
            document.add(new Paragraph("📦 Order Details")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(5));

            // 📋 Items Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2}))
                    .useAllAvailableWidth()
                    .setBorder(new SolidBorder(1));

            String[] headers = {"Product", "Qty", "Price", "Weight", "Subtotal"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setBold());
            }

            BigDecimal grandTotal = BigDecimal.ZERO;
            for (OrderItemDTO item : orderDTO.getItems()) {
                BigDecimal subtotal = item.getPrice(); // You can multiply with quantity if needed
                grandTotal = grandTotal.add(subtotal);

                table.addCell(new Cell().add(new Paragraph(item.getProductName())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity()))));
                table.addCell(new Cell().add(new Paragraph("₹" + item.getPrice())));
                table.addCell(new Cell().add(new Paragraph(item.getWeight())));
                table.addCell(new Cell().add(new Paragraph("₹" + subtotal)));
            }
            document.add(table);

            // 💰 Total Section
            document.add(new Paragraph("\n💰 Total Amount: ₹" + grandTotal)
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT));

            // 🙏 Footer
            document.add(new Paragraph("\n🙏 Thank you for shopping with E-Grocery Store!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10));

            document.close();

            // 📧 Email with PDF
            sendEmailWithAttachment(orderDTO.getCustomerEmail(),
                    "🧾 Your Invoice for Order #" + orderDTO.getOrderId(),
                    "Dear " + orderDTO.getCustomerName() + ",\n\nPlease find attached your invoice.\n\nThanks for ordering with E-Grocery!",
                    out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmailWithAttachment(String to, String subject, String body, byte[] attachmentBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("newssocialmedia2025@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);

        InputStreamSource attachment = new ByteArrayResource(attachmentBytes);
        helper.addAttachment("invoice.pdf", attachment);

        mailSender.send(message);
    }
}
