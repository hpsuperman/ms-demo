package com.example.ms.file.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.file.dto.FileUploadVO;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
  // 扩展名白名单（小写），过滤掉脚本等危险文件
  private static final Set<String> ALLOWED_EXTENSIONS =
      Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip");
  // objectKey 只允许字母数字和 / _ . -（下载入参防路径穿越）
  private static final String OBJECT_KEY_PATTERN = "^[a-zA-Z0-9/_.-]+$";
  // 返回给前端的下载地址走网关统一入口
  private static final String GATEWAY_BASE = "http://localhost:3080";

  private final MinioClient minioClient;

  @Value("${minio.bucket}")
  private String bucket;

  // 上传：multipart/form-data，字段名 file
  @PostMapping("/upload")
  public ApiResponse<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
    }
    String objectKey = buildObjectKey(file.getOriginalFilename());
    try (InputStream in = file.getInputStream()) {
      minioClient.putObject(PutObjectArgs.builder()
          .bucket(bucket)
          .object(objectKey)
          .stream(in, file.getSize(), -1)
          .contentType(file.getContentType())
          .build());
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
    }
    String url = GATEWAY_BASE + "/file/download/" + objectKey;
    return ApiResponse.success(new FileUploadVO(objectKey, url, file.getSize()));
  }

  // 下载：按 objectKey 取流返回。objectKey 含日期层级斜杠，用 {*objectKey} 捕获整段剩余路径
  @GetMapping("/download/{*objectKey}")
  public ResponseEntity<InputStreamResource> download(@PathVariable String objectKey) {
    if (!objectKey.matches(OBJECT_KEY_PATTERN) || objectKey.contains("..")) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的文件标识");
    }
    GetObjectResponse object = null;
    try {
      object = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
      GetObjectResponse finalObject = object;
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileNameOf(objectKey) + "\"")
          .body(new InputStreamResource(finalObject));
    } catch (Exception e) {
      if (object != null) {
        try {
          object.close();
        } catch (IOException ignored) {
        }
      }
      throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文件不存在");
    }
  }

  // objectKey 形如 2026/08/13/uuid.png
  private String buildObjectKey(String originalFilename) {
    String ext = extensionOf(originalFilename);
    if (!ALLOWED_EXTENSIONS.contains(ext)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的文件类型: ." + ext);
    }
    return LocalDate.now().format(DATE_FORMAT) + "/" + UUID.randomUUID() + "." + ext;
  }

  private String extensionOf(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
  }

  private String fileNameOf(String objectKey) {
    return objectKey.substring(objectKey.lastIndexOf('/') + 1);
  }
}
