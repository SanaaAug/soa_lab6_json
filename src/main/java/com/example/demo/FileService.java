package com.example.demo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;

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

    private AmazonS3 s3;

    @PostConstruct
    public void init() {
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        this.s3 = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint, "sgp1"))
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .withPathStyleAccessEnabled(true)
            .build();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        s3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead));

        return s3.getUrl(bucketName, fileName).toString();
    }
}