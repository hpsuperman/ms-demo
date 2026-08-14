package com.example.ms.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private int code;
  private String message;
  private T data;
  private LocalDateTime timestamp;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(200, "success", data, LocalDateTime.now());
  }

  public static <T> ApiResponse<T> success() {
    return success(null);
  }

  public static <T> ApiResponse<T> error(int code, String message) {
    return new ApiResponse<>(code, message, null, LocalDateTime.now());
  }

  public static <T> ApiResponse<T> error(int code, String message, T data) {
    return new ApiResponse<>(code, message, data, LocalDateTime.now());
  }
}
