package com.soumyajit.E_Grocery.Shop.InvoiceGenerator;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderItemDTO;

import java.io.File;
import java.io.FileOutputStream;

public class InvoicePdfGenerator {

    public static File generate(OrderDTO order) throws Exception {
        Document document = new Document();
        String filename = "invoice_order_" + order.getOrderId() + ".pdf";
        File pdfFile = new File(filename);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        Font bold = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font normal = new Font(Font.HELVETICA, 12, Font.NORMAL);

        document.add(new Paragraph("INVOICE", bold));
        document.add(new Paragraph("Order ID: " + order.getOrderId()));
        document.add(new Paragraph("Customer: " + order.getCustomerName()));
        document.add(new Paragraph("Email: " + order.getCustomerEmail()));
        document.add(new Paragraph("Status: " + order.getStatus()));
        document.add(new Paragraph("Placed At: " + order.getPlacedAt()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.addCell("Product");
        table.addCell("Qty");
        table.addCell("Weight");
        table.addCell("Price");

        for (OrderItemDTO item : order.getItems()) {
            table.addCell(item.getProductName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(item.getWeight());
            table.addCell("₹" + item.getPrice());
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total: ₹" + order.getTotalAmount(), bold));
        document.add(new Paragraph("Thank you!", normal));

        document.close();
        return pdfFile;
    }
}
