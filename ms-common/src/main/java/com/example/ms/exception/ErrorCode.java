package com.example.ms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  SUCCESS(200, "操作成功", HttpStatus.OK),

  BAD_REQUEST(400, "请求参数错误", HttpStatus.BAD_REQUEST),

  UNAUTHORIZED(401, "未登录或登录已过期", HttpStatus.UNAUTHORIZED),

  FORBIDDEN(403, "无权限访问", HttpStatus.FORBIDDEN),

  RESOURCE_NOT_FOUND(404, "请求的资源不存在", HttpStatus.NOT_FOUND),

  METHOD_NOT_ALLOWED(405, "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),

  UNSUPPORTED_MEDIA_TYPE(415, "不支持的请求格式", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

  DUPLICATE_RESOURCE(409, "数据已存在或冲突", HttpStatus.CONFLICT),

  RATE_LIMITED(429, "请求过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS),

  SYSTEM_ERROR(500, "系统开小差了，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);

  private final int code;
  private final String defaultMessage;
  private final HttpStatus httpStatus;

  ErrorCode(int code, String defaultMessage, HttpStatus httpStatus) {
    this.code = code;
    this.defaultMessage = defaultMessage;
    this.httpStatus = httpStatus;
  }
}
