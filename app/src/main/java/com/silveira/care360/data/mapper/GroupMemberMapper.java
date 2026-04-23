package com.silveira.care360.data.mapper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.local.entity.GroupMemberEntity;
import com.silveira.care360.data.remote.dto.GroupMemberDocumentDto;
import com.silveira.care360.domain.model.GroupMember;

import java.util.HashMap;
import java.util.Map;

public class GroupMemberMapper {

    public static GroupMember fromEntity(GroupMemberEntity entity) {
        if (entity == null) return null;

        GroupMember member = new GroupMember();
        member.setId(entity.getId());
        member.setGroupId(entity.getGroupId());
        member.setUserId(entity.getUserId());
        member.setRole(entity.getRole());
        member.setJoinedAt(entity.getJoinedAt());
        member.setInvitedBy(entity.getInvitedBy());
        member.setStatus(entity.getStatus());
        member.setGroupName(entity.getGroupName());
        member.setCareName(entity.getCareName());
        member.setName(entity.getName());
        member.setEmail(entity.getEmail());
        return member;
    }

    public static GroupMemberEntity toEntity(GroupMember member) {
        if (member == null) return null;

        GroupMemberEntity entity = new GroupMemberEntity();
        entity.setId(member.getId());
        entity.setGroupId(member.getGroupId());
        entity.setUserId(member.getUserId());
        entity.setRole(member.getRole());
        entity.setJoinedAt(member.getJoinedAt());
        entity.setInvitedBy(member.getInvitedBy());
        entity.setStatus(member.getStatus());
        entity.setGroupName(member.getGroupName());
        entity.setCareName(member.getCareName());
        entity.setName(member.getName());
        entity.setEmail(member.getEmail());
        return entity;
    }

    public static GroupMember fromDto(GroupMemberDocumentDto dto) {
        if (dto == null) return null;

        GroupMember member = new GroupMember();
        member.setId(dto.getId() != null ? dto.getId() : "");
        member.setGroupId(dto.getGroupId());
        member.setUserId(dto.getUserId());
        member.setRole(dto.getRole());
        member.setJoinedAt(dto.getJoinedAt());
        member.setInvitedBy(dto.getInvitedBy());
        member.setStatus(dto.getStatus());
        member.setGroupName(dto.getGroupName());
        member.setCareName(dto.getCareName());
        member.setName(dto.getName());
        member.setEmail(dto.getEmail());
        return member;
    }

    public static GroupMemberDocumentDto fromFirestore(DocumentSnapshot doc, String groupId) {
        if (doc == null || !doc.exists()) return null;

        GroupMemberDocumentDto dto = new GroupMemberDocumentDto();
        dto.setId(doc.getId());
        dto.setGroupId(groupId);

        String uid = doc.getString("uid");
        if (uid == null || uid.trim().isEmpty()) {
            uid = doc.getId();
        }
        dto.setUserId(uid);

        dto.setName(doc.getString("name"));
        dto.setEmail(doc.getString("email"));
        dto.setRole(doc.getString("role"));
        dto.setInvitedBy(doc.getString("invitedBy"));
        dto.setStatus(doc.getString("status"));
        dto.setGroupName(doc.getString("groupName"));
        dto.setCareName(doc.getString("careName"));

        Object joinedAtObj = doc.get("joinedAt");
        long joinedAt = 0L;

        if (joinedAtObj instanceof Long) {
            joinedAt = (Long) joinedAtObj;
        } else if (joinedAtObj instanceof com.google.firebase.Timestamp) {
            joinedAt = ((com.google.firebase.Timestamp) joinedAtObj).toDate().getTime();
        }

        dto.setJoinedAt(joinedAt);
        return dto;
    }

    public static Map<String, Object> toFirestoreMemberMap(GroupMemberDocumentDto dto, Object joinedAtValue, Object updatedAtValue) {
        Map<String, Object> data = new HashMap<>();
        if (dto == null) {
            return data;
        }

        data.put("uid", dto.getUserId());
        data.put("name", dto.getName());
        data.put("email", dto.getEmail());
        data.put("role", dto.getRole());
        data.put("joinedAt", joinedAtValue);
        data.put("updatedAt", updatedAtValue);
        return data;
    }

    public static Map<String, Object> toFirestoreMembershipMap(GroupMemberDocumentDto dto, Object joinedAtValue, Object updatedAtValue) {
        Map<String, Object> data = new HashMap<>();
        if (dto == null) {
            return data;
        }

        data.put("groupId", dto.getGroupId());
        data.put("groupName", dto.getGroupName());
        data.put("careName", dto.getCareName());
        data.put("role", dto.getRole());
        data.put("joinedAt", joinedAtValue);
        data.put("updatedAt", updatedAtValue);
        return data;
    }
}
