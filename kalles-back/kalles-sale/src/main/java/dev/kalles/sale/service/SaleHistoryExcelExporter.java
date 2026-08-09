package dev.kalles.sale.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

import dev.kalles.sale.dto.SaleHistoryItemResponse;
import dev.kalles.sale.dto.SaleHistoryPaymentResponse;
import dev.kalles.sale.dto.SaleHistoryResponse;

@Component
public class SaleHistoryExcelExporter {

    public byte[] export(List<SaleHistoryResponse> sales) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output)) {
                add(zip, "[Content_Types].xml", contentTypes());
                add(zip, "_rels/.rels", packageRels());
                add(zip, "xl/workbook.xml", workbook());
                add(zip, "xl/_rels/workbook.xml.rels", workbookRels());
                add(zip, "xl/worksheets/sheet1.xml", worksheet(salesHeader(), salesRows(sales)));
                add(zip, "xl/worksheets/sheet2.xml", worksheet(itemsHeader(), itemsRows(sales)));
                add(zip, "xl/worksheets/sheet3.xml", worksheet(paymentsHeader(), paymentsRows(sales)));
            }
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel gerar o arquivo de vendas.", ex);
        }
    }

    private List<String> salesHeader() {
        return List.of(
                "id",
                "version",
                "session_token",
                "company_id",
                "state",
                "client_id",
                "subtotal",
                "total",
                "amount_due",
                "fidelity_discount_applied",
                "points_earned"
        );
    }

    private List<String> itemsHeader() {
        return List.of(
                "id",
                "sale_id",
                "product_id",
                "quantity",
                "unit_price",
                "discount"
        );
    }

    private List<String> paymentsHeader() {
        return List.of(
                "id",
                "sale_id",
                "method",
                "amount",
                "change_amount",
                "transaction_id",
                "confirmed",
                "created_at",
                "updated_at"
        );
    }

    private List<List<String>> salesRows(List<SaleHistoryResponse> sales) {
        return sales.stream()
                .map(sale -> List.of(
                        text(sale.id()),
                        text(sale.version()),
                        text(sale.sessionToken()),
                        text(sale.companyId()),
                        text(sale.state()),
                        text(sale.clientId()),
                        text(sale.subtotal()),
                        text(sale.total()),
                        text(sale.amountDue()),
                        text(sale.fidelityDiscountApplied()),
                        text(sale.pointsEarned())
                ))
                .toList();
    }

    private List<List<String>> itemsRows(List<SaleHistoryResponse> sales) {
        List<List<String>> rows = new ArrayList<>();
        for (SaleHistoryResponse sale : sales) {
            for (SaleHistoryItemResponse item : sale.items()) {
                rows.add(List.of(
                        text(item.id()),
                        text(item.saleId()),
                        text(item.productId()),
                        text(item.quantity()),
                        text(item.unitPrice()),
                        text(item.discount())
                ));
            }
        }
        return rows;
    }

    private List<List<String>> paymentsRows(List<SaleHistoryResponse> sales) {
        List<List<String>> rows = new ArrayList<>();
        for (SaleHistoryResponse sale : sales) {
            for (SaleHistoryPaymentResponse payment : sale.payments()) {
                rows.add(List.of(
                        text(payment.id()),
                        text(payment.saleId()),
                        text(payment.method()),
                        text(payment.amount()),
                        text(payment.changeAmount()),
                        text(payment.transactionId()),
                        text(payment.confirmed()),
                        text(payment.createdAt()),
                        text(payment.updatedAt())
                ));
            }
        }
        return rows;
    }

    private String worksheet(List<String> header, List<List<String>> rows) {
        StringBuilder xml = new StringBuilder();
        xml.append("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
                """);
        appendRow(xml, 1, header);
        for (int i = 0; i < rows.size(); i++) {
            appendRow(xml, i + 2, rows.get(i));
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private void appendRow(StringBuilder xml, int rowNumber, List<String> values) {
        xml.append("<row r=\"").append(rowNumber).append("\">");
        for (int i = 0; i < values.size(); i++) {
            xml.append("<c r=\"")
                    .append(columnName(i + 1))
                    .append(rowNumber)
                    .append("\" t=\"inlineStr\"><is><t>")
                    .append(escape(values.get(i)))
                    .append("</t></is></c>");
        }
        xml.append("</row>");
    }

    private String columnName(int index) {
        StringBuilder name = new StringBuilder();
        int current = index;
        while (current > 0) {
            current--;
            name.insert(0, (char) ('A' + current % 26));
            current /= 26;
        }
        return name.toString();
    }

    private void add(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String text(Object value) {
        return switch (value) {
            case null -> "";
            case UUID uuid -> uuid.toString();
            case BigDecimal number -> number.toPlainString();
            case LocalDateTime dateTime -> dateTime.toString();
            default -> String.valueOf(value);
        };
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String contentTypes() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                  <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """;
    }

    private String packageRels() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>
                """;
    }

    private String workbook() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets>
                    <sheet name="sales" sheetId="1" r:id="rId1"/>
                    <sheet name="sale_items" sheetId="2" r:id="rId2"/>
                    <sheet name="payments" sheetId="3" r:id="rId3"/>
                  </sheets>
                </workbook>
                """;
    }

    private String workbookRels() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
                  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
                </Relationships>
                """;
    }
}
