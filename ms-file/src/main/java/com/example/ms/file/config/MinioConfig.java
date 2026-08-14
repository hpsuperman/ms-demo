package com.example.ms.file.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

  @Bean
  public MinioClient minioClient(MinioProperties props) {
    return MinioClient.builder()
        .endpoint(props.getEndpoint())
        .credentials(props.getAccessKey(), props.getSecretKey())
        .build();
  }

  @Bean(initMethod = "ensureBucket")
  public BucketInitializer bucketInitializer(MinioClient minioClient, MinioProperties props) {
    return new BucketInitializer(minioClient, props.getBucket());
  }
}
