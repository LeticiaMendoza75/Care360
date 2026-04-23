package com.silveira.care360.data.mapper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.domain.model.Group;

/**
 * Mapper para transformar documentos de Firestore al modelo de dominio Group.
 */
public class GroupMapper {

    public static Group fromFirestore(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Group group = new Group();
        group.setId(document.getId());
        group.setName(document.getString("name"));
        group.setInviteCode(document.getString("inviteCode"));
        group.setCreatedBy(document.getString("createdBy"));
        
        Long createdAt = document.getLong("createdAt");
        if (createdAt != null) group.setCreatedAt(createdAt);

        Long updatedAt = document.getLong("updatedAt");
        if (updatedAt != null) group.setUpdatedAt(updatedAt);

        Boolean active = document.getBoolean("active");
        if (active != null) group.setActive(active);

        return group;
    }
}