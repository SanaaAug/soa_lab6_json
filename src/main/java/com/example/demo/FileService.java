package com.example.demo;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

// DigitalOcean Spaces (S3-тэй нийцтэй) руу файл байршуулах сервис
@Service
public class FileService {

    @Value("${s3.access.key}")
    private String accessKey;

    @Value("${s3.secret.key}")
    private String secretKey;

    // DigitalOcean Spaces endpoint хаяг
    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.bucket}")
    private String bucketName;

    private S3Client s3;

    // Application эхлэхэд S3 клиент үүсгэнэ
    @PostConstruct
    public void init() {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3 = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.of("sgp1"))
            // DigitalOcean Spaces path-style URL ашиглана
            .forcePathStyle(true)
            .build();
    }

    // Файлыг S3-д байршуулж нийтийн URL буцаана
    public String uploadFile(MultipartFile file) throws IOException {
        // Нэр давхцахаас сэргийлж timestamp нэмнэ
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            // Нийтэд харагдахаар тохируулна
            .acl("public-read")
            .build();

        s3.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Файлын нийтийн URL буцаана
        return endpoint + "/" + bucketName + "/" + fileName;
    }
}
