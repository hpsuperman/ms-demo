package com.example.ms.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResponse<T> {

  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;

  public static <T> PageResponse<T> from(IPage<T> page) {
    long current = page.getCurrent();
    long pages = page.getPages();
    return new PageResponse<>(
        page.getRecords(),
        (int) current,
        (int) page.getSize(),
        page.getTotal(),
        (int) pages,
        current == 0,
        pages == 0 || current >= pages - 1);
  }

  public static <T, R> PageResponse<R> from(IPage<T> page, Function<T, R> mapper) {
    long current = page.getCurrent();
    long pages = page.getPages();
    List<R> content = page.getRecords().stream().map(mapper).toList();
    return new PageResponse<>(
        content,
        (int) current,
        (int) page.getSize(),
        page.getTotal(),
        (int) pages,
        current == 0,
        pages == 0 || current >= pages - 1);
  }

  public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
    int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    return new PageResponse<>(
        content, page, size, totalElements, totalPages, page == 0, totalPages == 0 || page >= totalPages - 1);
  }

  public static <T> PageResponse<T> empty() {
    return new PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
  }
}
