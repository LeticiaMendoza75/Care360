package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupUserInput;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.GroupRepository;

import javax.inject.Inject;

public class CreateGroupUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public CreateGroupUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String groupName, String careName, User user, ResultCallback<Void> callback) {
        groupRepository.createGroupForUser(groupName, careName, toGroupUserInput(user), callback);
    }

    private GroupUserInput toGroupUserInput(User user) {
        if (user == null) {
            return null;
        }

        return new GroupUserInput(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }
}
