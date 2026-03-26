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

@Service
public class FileService {

    @Value("${s3.access.key}")
    private String accessKey;

    @Value("${s3.secret.key}")
    private String secretKey;

    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.bucket}")
    private String bucketName;

    private S3Client s3;

    @PostConstruct
    public void init() {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3 = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.of("sgp1"))
            .forcePathStyle(true)
            .build();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .acl("public-read")
            .build();

        s3.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return endpoint + "/" + bucketName + "/" + fileName;
    }
}