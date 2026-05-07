package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

// Файл upload хийх REST controller
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    // Token шалгахад ашиглах SOAP клиент
    @Autowired
    private SoapAuthValidator soapAuth;

    // POST /upload — зураг болон файл S3-д хадгална
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestHeader("Authorization") String authHeader,
                                    @RequestParam("file") MultipartFile file) {

        // Bearer token авна
        String token = authHeader.replace("Bearer ", "");

        // SOAP сервисээр token шалгана
        if (!soapAuth.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid or expired token"));
        }

        try {
            // S3-д файл хадгалж URL буцаана
            String url = fileService.uploadFile(file);
            return ResponseEntity.ok(Map.of("success", true, "url", url));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to upload"));
        }
    }
}
