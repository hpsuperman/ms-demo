package com.example.ms.announcement.converter;


import com.example.ms.announcement.dto.AnnouncementRequest;
import com.example.ms.announcement.dto.AnnouncementResponse;
import com.example.ms.announcement.entity.Announcement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AnnouncementConverter {
    Announcement toEntity(AnnouncementRequest request);

    AnnouncementResponse toResponse(Announcement announcement);

    //忽略不该改动的字段
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publisherId", ignore = true)
    @Mapping(target = "publisherName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "pinnedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(AnnouncementRequest request, @MappingTarget Announcement announcement);
}
