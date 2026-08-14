package com.example.ms.exception;

import com.example.ms.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(
      BusinessException ex, HttpServletRequest request) {
    ErrorCode errorCode = ex.getErrorCode();
    log.warn(
        "Business exception: code={}, message={}, path={}",
        errorCode.getCode(),
        ex.getMessage(),
        request.getRequestURI());
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(ApiResponse.error(errorCode.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    log.warn("Validation failed: errors={}, path={}", errors, request.getRequestURI());
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
        .body(
            ApiResponse.error(
                ErrorCode.BAD_REQUEST.getCode(),
                ErrorCode.BAD_REQUEST.getDefaultMessage(),
                errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String path = violation.getPropertyPath().toString();
      errors.put(path.substring(path.lastIndexOf('.') + 1), violation.getMessage());
    }
    log.warn("Constraint violation: errors={}, path={}", errors, request.getRequestURI());
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
        .body(
            ApiResponse.error(
                ErrorCode.BAD_REQUEST.getCode(),
                ErrorCode.BAD_REQUEST.getDefaultMessage(),
                errors));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    log.warn("No handler found: method={}, path={}", ex.getHttpMethod(), request.getRequestURI());
    return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
        .body(
            ApiResponse.error(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "No handler found for " + ex.getHttpMethod() + " " + request.getRequestURI()));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
        .body(
            ApiResponse.error(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "No resource found: " + request.getRequestURI()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {
    log.warn("Upload size exceeded: path={}", request.getRequestURI());
    return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
        .body(
            ApiResponse.error(
                ErrorCode.SYSTEM_ERROR.getCode(), "File size exceeds maximum allowed"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<String>> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    String msg = ex.getMostSpecificCause().getMessage();
    log.warn("Data integrity violation: {} path={}", msg, request.getRequestURI());

    if (msg != null && msg.contains("Duplicate entry")) {
      String field = extractDuplicateField(msg);
      String value = extractDuplicateValue(msg);
      return ResponseEntity.status(ErrorCode.DUPLICATE_RESOURCE.getHttpStatus())
          .body(
              ApiResponse.error(
                  ErrorCode.DUPLICATE_RESOURCE.getCode(),
                  value != null && field != null
                      ? "「" + value + "」与已有数据重复，请更换" + field
                      : "数据重复，请检查后重试"));
    }
    return ResponseEntity.status(ErrorCode.DUPLICATE_RESOURCE.getHttpStatus())
        .body(ApiResponse.error(ErrorCode.DUPLICATE_RESOURCE.getCode(), "数据约束冲突，请检查输入"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<String>> handleMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn("Malformed request body: {} path={}", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
        .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "请求数据格式错误，请检查输入内容"));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    log.warn("Method not allowed: {} {}", ex.getMethod(), request.getRequestURI());
    return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
        .body(
            ApiResponse.error(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(), "不支持 " + ex.getMethod() + " 方法"));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    log.warn("Missing parameter: {} path={}", ex.getParameterName(), request.getRequestURI());
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
        .body(
            ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "缺少必要参数: " + ex.getParameterName()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    log.warn("Type mismatch: {} path={}", ex.getName(), request.getRequestURI());
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
        .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "参数类型错误: " + ex.getName()));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
    log.warn("Media type not supported: path={}", request.getRequestURI());
    return ResponseEntity.status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatus())
        .body(ApiResponse.error(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(), "不支持的请求格式"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error: path={}", request.getRequestURI(), ex);
    return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
        .body(
            ApiResponse.error(
                ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getDefaultMessage()));
  }

  private String extractDuplicateValue(String msg) {
    int start = msg.indexOf('\'');
    int end = msg.indexOf('\'', start + 1);
    return start >= 0 && end > start ? msg.substring(start + 1, end) : null;
  }

  private String extractDuplicateField(String msg) {
    int idx = msg.lastIndexOf('.');
    return idx >= 0 && idx < msg.length() - 1
        ? msg.substring(idx + 1).replace("'", "").trim()
        : null;
  }
}
