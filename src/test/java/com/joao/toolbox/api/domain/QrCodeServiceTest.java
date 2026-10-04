package com.joao.toolbox.api.domain;

import com.joao.toolbox.api.audit.ProcessingAudit;
import com.joao.toolbox.api.audit.ProcessingAuditRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    private ProcessingAuditRepository auditRepository;

    @InjectMocks
    private QrCodeService qrCodeService;

    @Test
    @DisplayName("Deve gerar QR Code com sucesso e salvar log de auditoria")
    void shouldGenerateQrCodeSuccessfully() {
        // Arrange
        String content = "https://github.com";
        int width = 300;
        int height = 300;

        // Act
        byte[] resultBytes = qrCodeService.generateQrCode(content, width, height);

        // Assert
        assertNotNull(resultBytes, "Os bytes do QR Code não devem ser nulos");
        assertTrue(resultBytes.length > 0, "O array de bytes gerado deve ser maior que zero");

        // Assinatura PNG: Primeiros bytes são sempre 0x89, 'P', 'N', 'G'
        assertEquals((byte) 0x89, resultBytes[0]);
        assertEquals((byte) 'P', resultBytes[1]);
        assertEquals((byte) 'N', resultBytes[2]);
        assertEquals((byte) 'G', resultBytes[3]);

        // Validação da chamada no repositório de auditoria
        ArgumentCaptor<ProcessingAudit> captor = ArgumentCaptor.forClass(ProcessingAudit.class);
        verify(auditRepository, times(1)).save(captor.capture());

        ProcessingAudit savedAudit = captor.getValue();
        assertEquals("QRCODE_GENERATION", savedAudit.getOperationType());
        assertEquals("qrcode-300x300.png", savedAudit.getFileName());
        assertEquals("SUCCESS", savedAudit.getStatus());
        assertTrue(savedAudit.getInputSizeBytes() > 0);
        assertTrue(savedAudit.getDurationMs() >= 0);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve lançar IllegalArgumentException quando o conteúdo for nulo ou em branco")
    void shouldThrowExceptionWhenContentIsBlank(String invalidContent) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> qrCodeService.generateQrCode(invalidContent, 300, 300)
        );

        assertEquals("O conteúdo para o QR Code não pode ser vazio.", exception.getMessage());
        verify(auditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando as dimensões forem menores ou iguais a zero")
    void shouldThrowExceptionWhenDimensionsAreZeroOrNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> qrCodeService.generateQrCode("teste", 0, 300)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> qrCodeService.generateQrCode("teste", 300, -10)
        );

        verify(auditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando as dimensões ultrapassarem o limite máximo")
    void shouldThrowExceptionWhenDimensionsExceedLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> qrCodeService.generateQrCode("teste", 2001, 300)
        );

        verify(auditRepository, never()).save(any());
    }
}