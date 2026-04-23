package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class LeaveGroupUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public LeaveGroupUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String currentUserId,
                        String groupId,
                        String ownerUserId,
                        String newOwnerUserId,
                        List<GroupMember> members,
                        ResultCallback<Void> callback) {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            callback.onError("No se pudo identificar al usuario");
            return;
        }

        if (groupId == null || groupId.trim().isEmpty()) {
            callback.onError("No se pudo identificar el grupo");
            return;
        }

        List<GroupMember> safeMembers = members != null ? members : new ArrayList<>();
        GroupMember currentMember = findMember(currentUserId, safeMembers);
        if (currentMember == null) {
            callback.onError("No se pudo identificar tu membresia");
            return;
        }

        boolean isOwner = ownerUserId != null && ownerUserId.equals(currentUserId);
        boolean isAdmin = "admin".equalsIgnoreCase(currentMember.getRole()) || isOwner;
        int adminCount = countAdmins(safeMembers);

        if (isOwner) {
            if (adminCount <= 1) {
                callback.onError("Antes de salir debe quedar otro admin en el grupo");
                return;
            }

            if (newOwnerUserId == null || newOwnerUserId.trim().isEmpty()) {
                callback.onError("Debes elegir a que admin transferir la responsabilidad");
                return;
            }

            GroupMember newOwner = findMember(newOwnerUserId, safeMembers);
            if (newOwner == null || !"admin".equalsIgnoreCase(newOwner.getRole()) || currentUserId.equals(newOwnerUserId)) {
                callback.onError("Selecciona un admin valido para transferir la responsabilidad");
                return;
            }

            groupRepository.leaveGroup(groupId, currentUserId, newOwnerUserId, callback);
            return;
        }

        if (isAdmin && adminCount <= 1) {
            callback.onError("El grupo debe tener al menos un admin");
            return;
        }

        groupRepository.leaveGroup(groupId, currentUserId, null, callback);
    }

    public List<GroupMember> getTransferCandidates(String currentUserId,
                                                   String ownerUserId,
                                                   List<GroupMember> members) {
        List<GroupMember> candidates = new ArrayList<>();
        String safeCurrentUserId = normalize(currentUserId);
        String safeOwnerUserId = normalize(ownerUserId);
        if (safeCurrentUserId == null || safeOwnerUserId == null || !safeOwnerUserId.equals(safeCurrentUserId) || members == null) {
            return candidates;
        }

        for (GroupMember member : members) {
            String memberUserId = resolveUserId(member);
            if (member == null || memberUserId == null) {
                continue;
            }
            if (!safeCurrentUserId.equals(memberUserId) && isAdmin(member)) {
                candidates.add(member);
            }
        }

        return candidates;
    }

    private GroupMember findMember(String userId, List<GroupMember> members) {
        String safeUserId = normalize(userId);
        if (safeUserId == null) {
            return null;
        }

        for (GroupMember member : members) {
            if (member != null && safeUserId.equals(resolveUserId(member))) {
                return member;
            }
        }
        return null;
    }

    private int countAdmins(List<GroupMember> members) {
        int count = 0;
        for (GroupMember member : members) {
            if (isAdmin(member)) {
                count++;
            }
        }
        return count;
    }

    private boolean isAdmin(GroupMember member) {
        return member != null && "admin".equalsIgnoreCase(normalize(member.getRole()));
    }

    private String resolveUserId(GroupMember member) {
        if (member == null) {
            return null;
        }

        String userId = normalize(member.getUserId());
        if (userId != null) {
            return userId;
        }

        return normalize(member.getId());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
