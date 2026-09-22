package com.joao.toolbox.api.domain;

import com.joao.toolbox.api.audit.ProcessingAudit;
import com.joao.toolbox.api.audit.ProcessingAuditRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfConversionService {
    private final ProcessingAuditRepository auditRepository;

    public PdfConversionService(ProcessingAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public byte[] convertImageToPdf(MultipartFile file) {
        long startTime = System.currentTimeMillis();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Formato inválido. Apenas imagens JPEG e PNG são aceitas.");
        }

        // try-with-resources garante que o PDDocument fecha a memória mesmo se estourar erro
        try (PDDocument document = new PDDocument()) {
            // 1. Cria folha tamanho A4
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 2. Transforma os bytes do arquivo em um objeto de imagem do PDFBox
            PDImageXObject image = PDImageXObject.createFromByteArray(document,file.getBytes(),file.getOriginalFilename());

            // 3. Cálculos de proporção para não estourar a folha A4
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            float imgWidth = image.getWidth();
            float imgHeight = image.getHeight();

            // Deixa uma margem de 40 unidades (20 de cada lado)
            float scale = Math.min((pageWidth - 40) / imgWidth, (pageHeight - 40) / imgHeight);
            if (scale > 1.0f) {
                scale = 1.0f; // Mantém o tamanho original se for menor que a página
            }

            float finalWidth = imgWidth * scale;
            float finalHeight = imgHeight * scale;

            // Centraliza na página
            float x = (pageWidth - finalWidth) / 2;
            float y = (pageHeight - finalHeight) / 2;

            // 4. Desenha a imagem na página
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(image, x, y, finalWidth, finalHeight);
            }

            // 5. Escreve o PDF em um fluxo de memória
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);

            // 6. Grava log de sucesso
            long duration = System.currentTimeMillis() - startTime;
            auditRepository.save(new ProcessingAudit(
                    "IMAGE_TO_PDF",
                    file.getOriginalFilename(),
                    file.getSize(),
                    duration,
                    "SUCCESS"
            ));

            return outputStream.toByteArray();

        } catch (IOException e) {
            // Em caso de falha de I/O, registra a falha na auditoria antes de lançar a exceção
            long duration = System.currentTimeMillis() - startTime;
            auditRepository.save(new ProcessingAudit(
                    "IMAGE_TO_PDF",
                    file.getOriginalFilename(),
                    file.getSize(),
                    duration,
                    "FAILED"
            ));
            throw new RuntimeException("Erro ao processar conversão para PDF: " + e.getMessage(), e);
        }
    }
}
