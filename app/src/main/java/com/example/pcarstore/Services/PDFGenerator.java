package com.example.pcarstore.Services;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PDFGenerator {

    public static File generateBalancePDF(Context context, double income, double expenses, double netBalance) {
        // Verificar permisos de almacenamiento primero
        if (!isExternalStorageWritable()) {
            Log.e("PDFGenerator", "El almacenamiento externo no está disponible");
            return null;
        }

        // Crear directorio si no existe
        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "FinancialReports");
        if (!folder.exists() && !folder.mkdirs()) {
            Log.e("PDFGenerator", "Error al crear el directorio");
            return null;
        }

        // Nombre del archivo con fecha
        String fileName = "BalanceReport_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date()) + ".pdf";

        File pdfFile = new File(folder, fileName);

        try {
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            document.add(new Paragraph("Reporte de Balance Financiero - Azamoz Shopping")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date());
            document.add(new Paragraph("Generado el: " + currentDate)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            String descripcion = "Este documento presenta un análisis completo del balance financiero, reflejando los ingresos, egresos y el resultado neto de las operaciones comerciales.\n\n" +
                    "• Ingresos Totales: Representan todos los fondos generados por ventas, servicios u otras fuentes de rentabilidad. Un valor positivo indica un flujo de capital saludable que sustenta el crecimiento del negocio.\n\n" +
                    "• Egresos Totales: Incluyen todos los gastos operativos, compras de inventario, inversiones y costos fijos. Un control adecuado de estos rubros es esencial para mantener la sostenibilidad financiera.\n\n" +
                    "• Balance Neto: Resultado clave que determina la rentabilidad del período. Un valor positivo (indicado en verde) refleja ganancias, mientras que un valor negativo (en rojo) señala pérdidas, requiriendo ajustes en la estrategia financiera.\n\n"+ "\n\n\n";
            document.add(new Paragraph(descripcion)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginBottom(20)
                    .setPadding(10f));

            Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .setWidth(UnitValue.createPercentValue(90))
                    .setMarginBottom(20)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);


            DeviceRgb greenColor = new DeviceRgb(56, 142, 60);  // Verde más oscuro
            DeviceRgb blackColor = new DeviceRgb(0, 0, 0);
            DeviceRgb redColor = new DeviceRgb(211, 47, 47);     // Rojo más oscuro
            DeviceRgb balanceColor = netBalance >= 0 ? greenColor : redColor;

            addTableRow(table, "INGRESOS TOTALES:", formatCurrency(income), greenColor, boldFont);
            addTableRow(table, "EGRESOS TOTALES:", formatCurrency(expenses), redColor, boldFont);
            addTableRow(table, "BALANCE NETO:", formatCurrency(netBalance), balanceColor, boldFont);

            document.add(table);

            String pie = "\n\nEste reporte automatizado ofrece una visión clara del desempeño económico, facilitando la toma de decisiones informadas para optimizar recursos y maximizar utilidades.\n\n" +
                    "Los valores monetarios se expresan en dólares con formato estándar para precisión contable.";
            document.add(new Paragraph(pie)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginBottom(20)
                    .setPadding(10f));
            document.close();

            return pdfFile;

        } catch (IOException e) {
            Log.e("PDFGenerator", "Error al generar PDF: " + e.getMessage());
            return null;
        }
    }

    private static void addTableRow(Table table, String label, String value,
                                    DeviceRgb color, PdfFont font) {
        table.addCell(new Paragraph(label)
                .setFont(font)
                .setFontColor(color, 1)
                .setPaddingBottom(5f));

        table.addCell(new Paragraph(value)
                .setFont(font)
                .setFontColor(color, 1)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddingBottom(5f));
    }

    private static String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "$%,.2f", amount);
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}