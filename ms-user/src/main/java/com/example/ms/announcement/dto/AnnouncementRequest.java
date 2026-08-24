package com.example.ms.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementRequest {
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100,message = "最小2,最大100")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;
    private Boolean pinned;
}
