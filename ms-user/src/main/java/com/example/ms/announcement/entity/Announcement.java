package com.example.ms.announcement.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_announcement")
public class Announcement extends BaseEntity {
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    private AnnouncementStatus status;
    private LocalDateTime publishedAt;
    private Boolean pinned;
    private LocalDateTime pinnedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
