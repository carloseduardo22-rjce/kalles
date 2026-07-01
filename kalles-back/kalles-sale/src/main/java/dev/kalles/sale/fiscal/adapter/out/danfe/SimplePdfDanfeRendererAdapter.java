package dev.kalles.sale.fiscal.adapter.out.danfe;

import dev.kalles.sale.fiscal.application.port.out.DanfeRendererPort;
import dev.kalles.sale.fiscal.domain.FiscalDocument;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SimplePdfDanfeRendererAdapter implements DanfeRendererPort {

    @Override
    public byte[] render(FiscalDocument document) {
        String text = "DANFE NFC-e\nChave: " + document.accessKey() + "\nProtocolo: " + document.authorizationProtocol();
        String pdf = """
                %%PDF-1.4
                1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
                2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
                3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 300 200] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj
                4 0 obj << /Length %d >> stream
                BT /F1 12 Tf 20 160 Td (%s) Tj ET
                endstream endobj
                5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj
                xref
                0 6
                0000000000 65535 f
                trailer << /Root 1 0 R /Size 6 >>
                startxref
                0
                %%%%EOF
                """.formatted(text.length() + 40, escapePdf(text));
        return pdf.getBytes(StandardCharsets.UTF_8);
    }

    private String escapePdf(String value) {
        return value.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\n", " ");
    }
}
