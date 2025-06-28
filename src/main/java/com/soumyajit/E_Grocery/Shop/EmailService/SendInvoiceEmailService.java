package com.soumyajit.E_Grocery.Shop.EmailService;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderItemDTO;

import com.soumyajit.E_Grocery.Shop.Entities.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;


import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class SendInvoiceEmailService {

    @Autowired
    private JavaMailSender mailSender;


    public void sendInvoice(OrderDTO orderDTO) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedDate = orderDTO.getPlacedAt().format(formatter);


        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 🛍️ Logo
            String imageUrl = "https://res.cloudinary.com/dek6gftbb/image/upload/v1751108759/grocery-store-logo-template-in-flat-design-style-vector-removebg-preview_hcbtaz.png";
            Image logo = new Image(ImageDataFactory.create(imageUrl)).scaleToFit(100, 100).setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(logo);

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

//            infoTable.addCell(new Cell().add(new Paragraph("👤 Customer Name: " + orderDTO.getCustomerName())).setBorder(Border.NO_BORDER));
//            infoTable.addCell(new Cell().add(new Paragraph("📦 Order ID: " + orderDTO.getOrderId())).setBorder(Border.NO_BORDER));
//            infoTable.addCell(new Cell().add(new Paragraph("✉️ Email: " + orderDTO.getCustomerEmail())).setBorder(Border.NO_BORDER));
//            infoTable.addCell(new Cell().add(new Paragraph("🗓️ Placed At: " + orderDTO.getPlacedAt())).setBorder(Border.NO_BORDER));

            infoTable.addCell(new Cell().add(new Paragraph()
                            .add(new Text("👤 Customer Name: ").setBold())
                            .add(orderDTO.getCustomerName()))
                    .setBorder(Border.NO_BORDER));

            infoTable.addCell(new Cell().add(new Paragraph()
                            .add(new Text("📦 Order ID: ").setBold())
                            .add(String.valueOf(orderDTO.getOrderId())))
                    .setBorder(Border.NO_BORDER));

            infoTable.addCell(new Cell().add(new Paragraph()
                            .add(new Text("✉️ Email: ").setBold())
                            .add(orderDTO.getCustomerEmail()))
                    .setBorder(Border.NO_BORDER));

            infoTable.addCell(new Cell().add(new Paragraph()
                            .add(new Text("🗓️ Placed At: ").setBold())
                            .add(formattedDate))
                    .setBorder(Border.NO_BORDER));




            if (orderDTO.getAddresses() != null && !orderDTO.getAddresses().isEmpty()) {
                StringBuilder addressBuilder = new StringBuilder();
                for (Address addr : orderDTO.getAddresses()) {
                    addressBuilder.append(addr.getHouseNumber()).append(", ")
                            .append(addr.getStreet()).append(", ")
                            .append(addr.getCity()).append(", ")
                            .append(addr.getDistrict()).append(", ")
                            .append(addr.getState()).append(", ")
                            .append(addr.getPinCode()).append(", ")
                            .append(addr.getCountry()).append("\n");
                }

                Paragraph addressPara = new Paragraph()
                        .add(new Text("🏠 Delivery Address:\n").setBold())  // Bold label
                        .add(addressBuilder.toString().trim());            // Regular address

                infoTable.addCell(new Cell(1, 2)
                        .add(addressPara)
                        .setBorder(Border.NO_BORDER));

            }


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
                BigDecimal subtotal = item.getPrice(); // Could be item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
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

            // 📝 Footer Note
            document.add(new Paragraph("\nThis is an autogenerated invoice and does not require any signature.\nFor any queries, contact the shop owner or email us at xyz@gmail.com.")
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

            // 🙏 Thanks Footer
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

    private void sendEmailWithAttachment(String to, String subject, String plainBody, byte[] attachmentBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("newssocialmedia2025@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);

        String htmlBody = """
        <html>
        <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                <div style="text-align: center;">
                    <img src="https://res.cloudinary.com/dek6gftbb/image/upload/v1751108759/grocery-store-logo-template-in-flat-design-style-vector-removebg-preview_hcbtaz.png" width="80" alt="E-Grocery Logo">
                    <h2 style="color: #4CAF50;">🧾 E-Grocery Store</h2>
                </div>

                <p style="font-size: 16px;">Hi <strong>%s</strong>,</p>
                <p style="font-size: 15px;">Thank you for shopping with us! Please find attached the invoice for your order <strong># %s</strong>.</p>

                <div style="background-color: #f1f1f1; padding: 10px 15px; border-radius: 5px; margin: 20px 0;">
                    <p><strong>📅 Placed At:</strong> %s</p>
                    <p><strong>📄 Total Amount:</strong> ₹%s</p>
                </div>

                <p style="font-size: 14px; margin-bottom: 30px;">
                    If you have any questions, feel free to reply to this email or contact our support team.
                </p>

                <div style="text-align: center;">
                    <p style="font-size: 13px; color: #888;">📲 Want order updates on WhatsApp?<br>
                    Send <code style="background-color:#eee; padding: 2px 5px; border-radius: 3px;">join exclaimed-call</code> to <strong>+1 (415) 523-8886</strong></p>
                </div>

                <p style="text-align: center; font-size: 12px; color: #aaa;">🙏 Thank you for shopping with <strong>E-Grocery Store</strong>.</p>
            </div>
        </body>
        </html>
        """.formatted(
                plainBody.split(",")[0].replace("Dear ", ""), // Extract name from plainBody
                plainBody.replaceAll("\\D+", ""), // Extract order number from subject
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                "Check PDF" // Optional - you can pass orderDTO.getTotalAmount() instead
        );

        helper.setText(htmlBody, true); // true for HTML body

        InputStreamSource attachment = new ByteArrayResource(attachmentBytes);
        helper.addAttachment("invoice.pdf", attachment);

        mailSender.send(message);
    }

}
