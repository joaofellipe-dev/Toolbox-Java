package com.joao.toolbox.api.domain;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.joao.toolbox.api.audit.ProcessingAudit;
import com.joao.toolbox.api.audit.ProcessingAuditRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;

@Service
public class QrCodeService {

    private final ProcessingAuditRepository auditRepository;

    public QrCodeService(ProcessingAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public byte[] generateQrCode(String text, int width, int height) {
        long startTime = System.currentTimeMillis();

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("O conteúdo para o QR Code não pode ser vazio.");
        }

        if (width <= 0 || height <= 0 || width > 2000 || height > 2000) {
            throw new IllegalArgumentException("Dimensões inválidas. Largura e altura devem estar entre 1 e 2000 pixels.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] qrCodeBytes = outputStream.toByteArray();

            long duration = System.currentTimeMillis() - startTime;
            auditRepository.save(new ProcessingAudit(
                    "QRCODE_GENERATION",
                    "qrcode-" + width + "x" + height + ".png",
                    qrCodeBytes.length,
                    duration,
                    "SUCCESS"
            ));

            return qrCodeBytes;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            auditRepository.save(new ProcessingAudit(
                    "QRCODE_GENERATION",
                    "failed-qrcode.png",
                    0L,
                    duration,
                    "FAILED"
            ));
            throw new RuntimeException("Erro ao gerar QR Code: " + e.getMessage(), e);
        }
    }
}