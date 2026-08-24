package com.example.ms.announcement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.announcement.converter.AnnouncementConverter;
import com.example.ms.announcement.dto.AnnouncementRequest;
import com.example.ms.announcement.dto.AnnouncementResponse;
import com.example.ms.announcement.entity.Announcement;
import com.example.ms.announcement.entity.AnnouncementStatus;
import com.example.ms.announcement.mapper.AnnouncementMapper;
import com.example.ms.common.PageResponse;
import com.example.ms.common.context.UserContext;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.user.entity.User;
import com.example.ms.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementConverter announcementConverter;
    private final UserMapper userMapper;
    private final AnnouncementMapper announcementMapper;

    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request) {
        Long userId = UserContext.getUserId();
        Announcement announcement = announcementConverter.toEntity(request);
        User user = userMapper.selectById(userId);
        String name = user.getNickname() != null ? user.getNickname() : user.getPhone();
        announcement.setPublisherId(userId);
        announcement.setPublisherName(name);
        announcement.setStatus(AnnouncementStatus.PUBLISHED);
        announcement.setPublishedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(request.getPinned())) {
            announcement.setPinnedAt(LocalDateTime.now());
        }
        announcementMapper.insert(announcement);
        return announcementConverter.toResponse(announcement);
    }

    @Transactional(readOnly = true)
    public PageResponse<AnnouncementResponse> page(String keyword, Pageable pageable) {
        IPage<Announcement> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getPinned).orderByDesc(Announcement::getPinnedAt).orderByDesc(Announcement::getPublishedAt);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Announcement::getTitle, keyword);
        }
        IPage<Announcement> page = announcementMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, announcementConverter::toResponse);
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse detail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公告不存在");
        }
        return announcementConverter.toResponse(announcement);
    }

    @Transactional
    public void delete(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公告不存在");
        }
        announcementMapper.deleteById(id);
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公告不存在");
        }
        announcementConverter.updateEntity(request, announcement);
        announcementMapper.updateById(announcement);
        return announcementConverter.toResponse(announcement);
    }

}
