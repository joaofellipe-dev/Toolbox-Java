package com.joao.toolbox.api.domain;

import com.joao.toolbox.api.audit.ProcessingAudit;
import com.joao.toolbox.api.audit.ProcessingAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;import com.joao.toolbox.api.audit.ProcessingAudit;
import com.joao.toolbox.api.audit.ProcessingAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PdfConversionServiceTest {

    @Mock
    private ProcessingAuditRepository auditRepository;

    @InjectMocks
    private PdfConversionService pdfConversionService;

    private byte[] sampleImageBytes;

    @BeforeEach
    void setUp() throws IOException{
        // Gera uma imagem PNG válida 100x100 em memória para alimentar os testes
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        sampleImageBytes = baos.toByteArray();
    }
    @Test
    @DisplayName("Deve converter imagem PNG para PDF com sucesso e registrar auditoria")
    void shouldConvertImageToPdfSuccessfully() {
        // Arrange (Preparação)
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                sampleImageBytes
        );

        // Act (Ação)
        byte[] pdfResult = pdfConversionService.convertImageToPdf(file);

        // Assert (Verificações)
        assertNotNull(pdfResult, "O PDF retornado não deve ser nulo");
        assertTrue(pdfResult.length > 0, "O PDF gerado deve conter bytes");

        // Verifica a assinatura inicial de um arquivo PDF real (%PDF)
        String pdfHeader = new String(pdfResult, 0, Math.min(pdfResult.length, 4));
        assertEquals("%PDF", pdfHeader, "O binário retornado deve ter o cabeçalho válido de um arquivo PDF");

        // Captura o objeto salvo na auditoria para validar se os dados bateram
        ArgumentCaptor<ProcessingAudit> auditCaptor = ArgumentCaptor.forClass(ProcessingAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());

        ProcessingAudit savedAudit = auditCaptor.getValue();
        assertEquals("IMAGE_TO_PDF", savedAudit.getOperationType());
        assertEquals("test-image.png", savedAudit.getFileName());
        assertEquals("SUCCESS", savedAudit.getStatus());
        assertTrue(savedAudit.getDurationMs() >= 0);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o arquivo for vazio")
    void shouldThrowExceptionWhenFileIsEmpty() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pdfConversionService.convertImageToPdf(emptyFile)
        );

        assertEquals("O arquivo enviado está vazio.", exception.getMessage());
        verify(auditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o formato não for imagem permitida")
    void shouldThrowExceptionWhenFileTypeIsInvalid() {
        // Arrange
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "documento.txt",
                "text/plain",
                "conteudo texto".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pdfConversionService.convertImageToPdf(textFile)
        );

        assertTrue(exception.getMessage().contains("Formato inválido"));
        verify(auditRepository, never()).save(any());
    }
}