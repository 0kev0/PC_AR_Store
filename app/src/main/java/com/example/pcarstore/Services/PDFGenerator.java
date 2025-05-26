package com.example.pcarstore.Services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.example.pcarstore.ModelsDB.Transaction;
import com.example.pcarstore.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    // Colores corporativos
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(56, 142, 60); // Verde corporativo
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(211, 47, 47); // Rojo
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(33, 150, 243); // Azul
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(33, 33, 33); // Negro
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);

    public static File generateFinancialReport(Context context,
                                               double income,
                                               double expenses,
                                               double netBalance,
                                               List<Transaction> transactions) {

        // 1. Validación de almacenamiento
        if (!isExternalStorageWritable()) {
            Log.e("PDFGenerator", "Error: Almacenamiento no disponible");
            return null;
        }

        // 2. Crear directorio de reportes
        File reportsDir = createReportsDirectory(context);
        if (reportsDir == null) return null;

        // 3. Generar nombre de archivo único
        String fileName = generateFileName();
        File pdfFile = new File(reportsDir, fileName);

        try {
            // 4. Configurar documento PDF
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 5. Configurar fuentes
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // 6. Añadir contenido al PDF
            addLogoHeader(document, context); // Logo corporativo
            addReportTitle(document, boldFont);
            addReportDate(document, font);
            addExecutiveSummary(document, font, income, expenses, netBalance);
            addBalanceTable(document, font, boldFont, income, expenses, netBalance);

            if (transactions != null && !transactions.isEmpty()) {
                addTransactionsTable(document, font, boldFont, transactions);
            }

            addFooter(document, font);

            document.close();
            return pdfFile;

        } catch (Exception e) {
            Log.e("PDFGenerator", "Error generando PDF: " + e.getMessage());
            return null;
        }
    }

    private static File createReportsDirectory(Context context) {
        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "FinancialReports");
        if (!folder.exists() && !folder.mkdirs()) {
            Log.e("PDFGenerator", "Error creando directorio de reportes");
            return null;
        }
        return folder;
    }

    private static String generateFileName() {
        return "ReporteFinanciero_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date()) + ".pdf";
    }

    private static void addLogoHeader(Document document, Context context) throws IOException {
        try {
            // 1. Cargar el bitmap del logo desde recursos
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.azamoz_logo2);

            // 2. Optimizar el tamaño para PDF
            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 80, true);

            // 3. Convertir a formato compatible con iText
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapData = stream.toByteArray();
            ImageData imageData = ImageDataFactory.create(bitmapData);

            // 4. Crear y configurar la imagen en el PDF
            Image logo = new Image(imageData)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginBottom(15);

            document.add(logo);

        } catch (Exception e) {
            Log.w("PDFGenerator", "No se pudo cargar el logo, continuando sin él", e);
            // Puedes añadir un texto alternativo si lo prefieres
            document.add(new Paragraph("AZAMOZ SHOPPING")
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
        }
    }

    private static void addReportTitle(Document document, PdfFont font) throws IOException {
        document.add(new Paragraph("REPORTE FINANCIERO")
                .setFont(font)
                .setFontSize(18)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));
    }

    private static void addReportDate(Document document, PdfFont font) throws IOException {
        String dateStr = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault())
                .format(new Date());

        document.add(new Paragraph(dateStr)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(TEXT_COLOR)
                .setMarginBottom(20));
    }

    private static void addExecutiveSummary(Document document, PdfFont font,
                                            double income, double expenses, double netBalance) throws IOException {

        String performance = netBalance >= 0 ?
                "El balance neto positivo indica un desempeño financiero favorable." :
                "El balance negativo sugiere necesidad de revisar estrategias operativas.";

        String summary = "Este reporte detalla la situación financiera de Azamoz Shopping, incluyendo:\n\n" +
                "• Ingresos totales: " + formatCurrency(income) + " en ventas y servicios\n" +
                "• Egresos totales: " + formatCurrency(expenses) + " en costos operativos\n" +
                "• Balance neto: " + formatCurrency(netBalance) + "\n\n" +
                performance + " Los detalles completos se presentan a continuación.";

        document.add(new Paragraph("RESUMEN EJECUTIVO")
                .setFontSize(12)
                .setBold()
                .setFontColor(ACCENT_COLOR)
                .setMarginTop(15)
                .setMarginBottom(10));

        document.add(new Paragraph(summary)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginBottom(20)
                .setPadding(10)
                .setBackgroundColor(LIGHT_GRAY));
    }

    private static void addBalanceTable(Document document, PdfFont font, PdfFont boldFont,
                                        double income, double expenses, double netBalance) throws IOException {

        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(90))
                .setMarginTop(20)
                .setMarginBottom(30)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Encabezado
        addTableHeaderCell(table, "CONCEPTO", PRIMARY_COLOR, boldFont);
        addTableHeaderCell(table, "VALOR", PRIMARY_COLOR, boldFont);

        // Datos
        addTableRow(table, "INGRESOS TOTALES", formatCurrency(income), PRIMARY_COLOR, boldFont);
        addTableRow(table, "EGRESOS TOTALES", formatCurrency(expenses), SECONDARY_COLOR, boldFont);

        DeviceRgb balanceColor = netBalance >= 0 ? PRIMARY_COLOR : SECONDARY_COLOR;
        addTableRow(table, "BALANCE NETO", formatCurrency(netBalance), balanceColor, boldFont);

        document.add(table);
    }

    private static void addTransactionsTable(Document document, PdfFont font, PdfFont boldFont,
                                             List<Transaction> transactions) throws IOException {

        document.add(new Paragraph("DETALLE DE TRANSACCIONES")
                .setFontSize(12)
                .setBold()
                .setFontColor(ACCENT_COLOR)
                .setMarginTop(25)
                .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(30);

        // Encabezados
        addTableHeaderCell(table, "FECHA", ACCENT_COLOR, boldFont);
        addTableHeaderCell(table, "DESCRIPCIÓN", ACCENT_COLOR, boldFont);
        addTableHeaderCell(table, "CATEGORÍA", ACCENT_COLOR, boldFont);
        addTableHeaderCell(table, "TIPO", ACCENT_COLOR, boldFont);
        addTableHeaderCell(table, "MONTO", ACCENT_COLOR, boldFont);

        // Datos
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Transaction t : transactions) {
            addTransactionRow(table, font,
                    dateFormat.format(t.getDate()),
                    t.getDescription(),
                    t.getDescription(),
                    t.getType(),
                    t.getAmount());
        }

        document.add(table);
    }

    private static void addTransactionRow(Table table, PdfFont font,
                                          String date, String description,
                                          String category, String type, double amount) {

        DeviceRgb amountColor = "INGRESO".equals(type) ? PRIMARY_COLOR : SECONDARY_COLOR;

        table.addCell(createCell(date, font, TEXT_COLOR));
        table.addCell(createCell(description, font, TEXT_COLOR));
        table.addCell(createCell(category, font, TEXT_COLOR));
        table.addCell(createCell(type, font, TEXT_COLOR));
        table.addCell(createCell(formatCurrency(amount), font, amountColor)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private static void addFooter(Document document, PdfFont font) throws IOException {
        String footer = "Documento generado automáticamente por el sistema de Azamoz Shopping\n" +
                "Fecha de generación: " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date()) + "\n" +
                "Para validaciones oficiales, consulte el sistema de gestión financiera.";

        document.add(new Paragraph(footer)
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setItalic()
                .setFontColor(TEXT_COLOR));
    }

    private static Cell createCell(String text, PdfFont font, DeviceRgb color) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(10)
                        .setFontColor(color))
                .setPadding(5);
    }

    private static void addTableHeaderCell(Table table, String text, DeviceRgb color, PdfFont font) {
        table.addCell(new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(11)
                        .setBold()
                        .setFontColor(DeviceRgb.WHITE))
                .setBackgroundColor(color)
                .setPadding(7));
    }

    private static void addTableRow(Table table, String label, String value,
                                    DeviceRgb color, PdfFont font) throws IOException {
        table.addCell(createCell(label, font, color));
        table.addCell(createCell(value, font, color)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private static String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "$%,.2f", Math.abs(amount));
    }

    private static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}