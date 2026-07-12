package pe.edu.uni.firma.signature.patterns.behavioral;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import pe.edu.uni.firma.signature.dto.CitizenPrivateDto;

/**
 * Strategy concreto:
 * agrega un sello visual vertical en el lado izquierdo de cada pagina del PDF
 * antes de calcular hash y firma.
 */
@Component
@Order(1)
public class PdfStampPreprocessor implements DocumentPreprocessor {

    @Override
    public boolean supports(String fileName, String contentType) {
        String name = fileName == null ? "" : fileName.toLowerCase();
        String type = contentType == null ? "" : contentType.toLowerCase();

        return name.endsWith(".pdf") || type.contains("pdf");
    }

    @Override
    public PreparedDocument prepare(
            String fileName,
            String contentType,
            byte[] content,
            CitizenPrivateDto signer
    ) {
        try (
                PDDocument document = PDDocument.load(content);
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            for (PDPage page : document.getPages()) {
                insertarSelloVerticalIzquierdo(document, page, signer);
            }

            document.save(output);

            return new PreparedDocument(
                    fileName,
                    MediaType.APPLICATION_PDF_VALUE,
                    output.toByteArray(),
                    true
            );

        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "No se pudo insertar el sello visible en el PDF. "
                            + "Verifique que el archivo PDF no este corrupto o protegido.",
                    ex
            );
        }
    }

    private void insertarSelloVerticalIzquierdo(
            PDDocument document,
            PDPage page,
            CitizenPrivateDto signer
    ) throws IOException {
        PDRectangle box = page.getMediaBox();

        float pageLeft = box.getLowerLeftX();
        float pageBottom = box.getLowerLeftY();
        float pageHeight = box.getHeight();

        float marginLeft = 12f;
        float stampWidth = 50f;
        float stampHeight = 260f;

        float x = pageLeft + marginLeft;
        float y = pageBottom + Math.max(32f, (pageHeight - stampHeight) / 2f);

        try (
                PDPageContentStream cs = new PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                )
        ) {
            // Rectangulo del sello al lado izquierdo
            cs.setStrokingColor(147, 0, 10);
            cs.setNonStrokingColor(255, 246, 246);
            cs.addRect(x, y, stampWidth, stampHeight);
            cs.fillAndStroke();

            // Texto vertical. Se rota 90 grados dentro del rectangulo izquierdo.
            float textStartX = x + 17f;
            float textStartY = y + 10f;

            cs.beginText();
            cs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(90), textStartX, textStartY));
            cs.setNonStrokingColor(147, 0, 10);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 8);
            cs.showText("Firmado digitalmente por DNI: " + safeText(signer.dni(), 20));
            cs.endText();

            cs.beginText();
            cs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(90), textStartX + 11f, textStartY));
            cs.setNonStrokingColor(60, 40, 40);
            cs.setFont(PDType1Font.HELVETICA, 7);
            cs.showText(safeText(shortText(signer.apellidosNombres(), 42), 42));
            cs.endText();

            cs.beginText();
            cs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(90), textStartX + 22f, textStartY));
            cs.setNonStrokingColor(60, 40, 40);
            cs.setFont(PDType1Font.HELVETICA, 6);
            cs.showText("Servicio de Firma Digital UNI - " + safeText(Instant.now().toString(), 32));
            cs.endText();
        }
    }

    private String shortText(String value, int max) {
        if (value == null) {
            return "";
        }

        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private String safeText(String value, int max) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String cleaned = normalized.replaceAll("[^\\x20-\\x7E]", "");

        return shortText(cleaned, max);
    }
}
