package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

public class ChangeActiveGroupUseCase {

    private final UserRepository userRepository;

    @Inject
    public ChangeActiveGroupUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String userId,
                        String groupId,
                        List<GroupMember> memberships,
                        ResultCallback<Result> callback) {
        userRepository.setActiveGroup(userId, groupId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                GroupMember selected = findMembershipByGroupId(memberships, groupId);
                if (selected == null) {
                    callback.onError("No se encontro el grupo seleccionado");
                    return;
                }

                callback.onSuccess(new Result(
                        selected.getGroupName(),
                        selected.getCareName()
                ));
            }

            @Override
            public void onError(String message) {
                callback.onError("Error cambiando de grupo");
            }
        });
    }

    private GroupMember findMembershipByGroupId(List<GroupMember> memberships, String groupId) {
        if (memberships == null) {
            return null;
        }

        for (GroupMember member : memberships) {
            if (member != null && groupId.equals(member.getGroupId())) {
                return member;
            }
        }

        return null;
    }

    public static class Result {
        private final String groupName;
        private final String careName;

        public Result(String groupName, String careName) {
            this.groupName = groupName;
            this.careName = careName;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getCareName() {
            return careName;
        }
    }
}
