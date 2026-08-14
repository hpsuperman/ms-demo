package com.example.ms.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;

/**
 * 启动时确保默认桶存在（不存在自动创建），避免去控制台手动建桶。
 * 由 MinioConfig 通过 @Bean(initMethod="ensureBucket") 触发。
 */
@RequiredArgsConstructor
public class BucketInitializer {

  private final MinioClient minioClient;
  private final String bucket;

  public void ensureBucket() throws Exception {
    boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }
}
