package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.GroupRepository;

import java.util.List;

import javax.inject.Inject;

public class DeleteGroupUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public DeleteGroupUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String currentUserId,
                        String groupId,
                        String ownerUserId,
                        List<GroupMember> members,
                        ResultCallback<Void> callback) {
        if (groupId == null || groupId.trim().isEmpty()) {
            callback.onError("No se pudo identificar el grupo");
            return;
        }

        if (!canDeleteGroup(currentUserId, ownerUserId)) {
            callback.onError("Solo el responsable del grupo puede eliminarlo");
            return;
        }

        groupRepository.deleteGroup(groupId, members, callback);
    }

    private boolean canDeleteGroup(String currentUserId, String ownerUserId) {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return false;
        }

        return ownerUserId != null && currentUserId.equals(ownerUserId);
    }
}
