package com.example.ms.announcement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ms.announcement.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
