package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.GroupRepository;

import java.util.List;

import javax.inject.Inject;

public class UpdateGroupMemberRoleUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public UpdateGroupMemberRoleUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String currentUserId,
                        String groupId,
                        String ownerUserId,
                        GroupMember targetMember,
                        String newRole,
                        List<GroupMember> members,
                        ResultCallback<Void> callback) {
        if (targetMember == null || targetMember.getUserId() == null || targetMember.getUserId().trim().isEmpty()) {
            callback.onError("No se pudo identificar el miembro");
            return;
        }

        if (!isAdmin(currentUserId, ownerUserId, members)) {
            callback.onError("Solo un admin puede cambiar roles");
            return;
        }

        if (currentUserId != null && currentUserId.equals(targetMember.getUserId())) {
            callback.onError("No puedes cambiar tu propio rol");
            return;
        }

        if (ownerUserId != null && ownerUserId.equals(targetMember.getUserId())) {
            callback.onError("No se puede modificar el rol del creador");
            return;
        }

        if (newRole == null || newRole.trim().isEmpty()) {
            callback.onError("Rol no valido");
            return;
        }

        if ("member".equalsIgnoreCase(newRole) && "admin".equalsIgnoreCase(targetMember.getRole()) && countAdmins(members) <= 1) {
            callback.onError("El grupo debe tener al menos un admin");
            return;
        }

        groupRepository.updateMemberRole(groupId, targetMember.getUserId(), newRole, callback);
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
