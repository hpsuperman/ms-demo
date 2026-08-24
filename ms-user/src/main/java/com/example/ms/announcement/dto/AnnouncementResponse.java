package com.example.ms.announcement.dto;

import com.example.ms.announcement.entity.AnnouncementStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    private AnnouncementStatus status;
    private LocalDateTime publishedAt;
    private Boolean pinned;
    private LocalDateTime pinnedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
