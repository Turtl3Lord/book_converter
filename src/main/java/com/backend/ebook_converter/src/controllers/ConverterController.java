package com.backend.ebook_converter.src.controllers;

import com.backend.ebook_converter.src.Responses.SendToConversionRespondeModel;
import com.backend.ebook_converter.src.interfaces.ISendBookToConversion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class ConverterController {

    private final ISendBookToConversion sendBookToConversion;

    public ConverterController(ISendBookToConversion sendBookToConversion) {
        this.sendBookToConversion = sendBookToConversion;
    }

    @PostMapping("/convert")
    public ResponseEntity<SendToConversionRespondeModel> convertEbook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("originalFormat") String originalFormat,
            @RequestParam("targetFormat") String targetFormat
    ) throws Exception {

        // 1. Ler bytes do arquivo enviado
        byte[] bookData = file.getBytes();

        // 2. Chamar o serviço de conversão (retorna apenas o ID)
        SendToConversionRespondeModel response = sendBookToConversion.sendBookToConversion(bookData, originalFormat, targetFormat);

        // 3. Retornar JSON com o ID
        return ResponseEntity.ok(response);
    }
}