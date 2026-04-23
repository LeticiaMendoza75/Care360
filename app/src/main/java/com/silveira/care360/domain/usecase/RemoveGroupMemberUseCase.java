package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.GroupRepository;

import java.util.List;

import javax.inject.Inject;

public class RemoveGroupMemberUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public RemoveGroupMemberUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String currentUserId,
                        String groupId,
                        String ownerUserId,
                        GroupMember targetMember,
                        List<GroupMember> members,
                        ResultCallback<Void> callback) {
        if (targetMember == null || targetMember.getUserId() == null || targetMember.getUserId().trim().isEmpty()) {
            callback.onError("No se pudo identificar el miembro");
            return;
        }

        if (!isAdmin(currentUserId, ownerUserId, members)) {
            callback.onError("Solo un admin puede eliminar miembros");
            return;
        }

        if (currentUserId != null && currentUserId.equals(targetMember.getUserId())) {
            callback.onError("No puedes eliminarte a ti mismo desde aqui");
            return;
        }

        if (ownerUserId != null && ownerUserId.equals(targetMember.getUserId())) {
            callback.onError("No se puede eliminar al creador del grupo");
            return;
        }

        if ("admin".equalsIgnoreCase(targetMember.getRole()) && countAdmins(members) <= 1) {
            callback.onError("El grupo debe tener al menos un admin");
            return;
        }

        groupRepository.removeMember(groupId, targetMember.getUserId(), callback);
    }

    private boolean isAdmin(String currentUserId, String ownerUserId, List<GroupMember> members) {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return false;
        }

        if (ownerUserId != null && ownerUserId.equals(currentUserId)) {
            return true;
        }

        if (members == null) {
            return false;
        }

        for (GroupMember member : members) {
            if (member != null
                    && currentUserId.equals(member.getUserId())
                    && "admin".equalsIgnoreCase(member.getRole())) {
                return true;
            }
        }

        return false;
    }

    private int countAdmins(List<GroupMember> members) {
        if (members == null) {
            return 0;
        }

        int count = 0;
        for (GroupMember member : members) {
            if (member != null && "admin".equalsIgnoreCase(member.getRole())) {
                count++;
            }
        }
        return count;
    }
}
