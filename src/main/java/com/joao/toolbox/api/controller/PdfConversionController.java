package com.joao.toolbox.api.controller;

import com.joao.toolbox.api.domain.PdfConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/conversion/image-to-pdf")
public class PdfConversionController {
    private final PdfConversionService pdfConversionService;

    // Injeção de dependência via construtor
    public PdfConversionController(PdfConversionService pdfConversionService) {
        this.pdfConversionService = pdfConversionService;
    }

    @PostMapping(value = "/image-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertImageToPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Executa a conversão no service e obtém os bytes do PDF
            byte[] pdfBytes = pdfConversionService.convertImageToPdf(file);
            // Retorna 200 OK com o binário e os headers apropriados de download
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"converted.pdf\"")
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
