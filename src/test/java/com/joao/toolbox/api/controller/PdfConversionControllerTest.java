package com.joao.toolbox.api.controller;

import com.joao.toolbox.api.domain.PdfConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PdfConversionController.class)
class PdfConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PdfConversionService pdfConversionService;

    @Test
    @DisplayName("POST /api/v1/conversions/image-to-pdf - Deve retornar 200 OK com PDF quando upload for válido")
    void shouldReturnPdfWhenUploadIsValid() throws Exception {
        byte[] fakePdfBytes = "%PDF-1.4 fake content".getBytes();
        when(pdfConversionService.convertImageToPdf(any())).thenReturn(fakePdfBytes);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4}
        );

        mockMvc.perform(multipart("/api/v1/conversions/image-to-pdf").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"converted.pdf\""))
                .andExpect(content().bytes(fakePdfBytes));
    }

    @Test
    @DisplayName("POST /api/v1/conversions/image-to-pdf - Deve retornar 400 Bad Request quando Service lançar IllegalArgumentException")
    void shouldReturnBadRequestWhenServiceThrowsIllegalArgumentException() throws Exception {
        when(pdfConversionService.convertImageToPdf(any()))
                .thenThrow(new IllegalArgumentException("Formato inválido."));

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "documento.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "texto simples".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/conversions/image-to-pdf").file(invalidFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/conversions/image-to-pdf - Deve retornar 400 Bad Request quando parâmetro 'file' não for enviado")
    void shouldReturnBadRequestWhenFileParameterIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/conversions/image-to-pdf"))
                .andExpect(status().isBadRequest());
    }
}