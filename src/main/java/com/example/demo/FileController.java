package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private SoapAuthValidator soapAuth;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestHeader("Authorization") String authHeader,
                                    @RequestParam("file") MultipartFile file) {

        String token = authHeader.replace("Bearer ", "");

        if (!soapAuth.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid or expired token"));
        }

        try {
            String url = fileService.uploadFile(file);
            return ResponseEntity.ok(Map.of("success", true, "url", url));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to upload"));
        }
    }
}