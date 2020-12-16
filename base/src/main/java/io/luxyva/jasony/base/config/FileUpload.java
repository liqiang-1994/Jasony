package io.luxyva.jasony.base.config;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class FileUpload {
    
    @Autowired
    private ApplicationProperties applicationProperties;
    
    public void upload(String bucketName, String objectName, String fileName) {
        try {
            MinioClient minioClient = MinioClient.builder()
                                          .endpoint(applicationProperties.getFileUpAddress())
                                          .credentials(applicationProperties.getUpAccessKey(),
                                              applicationProperties.getUpSecretKey())
                                          .build();
            boolean isExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            minioClient.uploadObject(
                UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(fileName)
                    .build());
        } catch (Exception e) {
            log.error("file upload error, bucketName:{}, objectName:{}, fileName:{}", bucketName, objectName, fileName);
        }
    }
    
    public String getpreSigned(String bucketName, String objectName) {
        try {
            MinioClient minioClient = MinioClient.builder()
                                          .endpoint(applicationProperties.getFileUpAddress())
                                          .credentials(applicationProperties.getUpAccessKey(),
                                              applicationProperties.getUpSecretKey())
                                          .build();
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(3, TimeUnit.MINUTES)
                    .build());
            return url;
        } catch (Exception e) {
        
        }
        return null;
    }
}
