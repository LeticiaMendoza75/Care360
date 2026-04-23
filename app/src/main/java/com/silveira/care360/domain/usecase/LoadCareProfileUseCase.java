package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Group;
import com.silveira.care360.domain.repository.GroupRepository;

import javax.inject.Inject;

public class LoadCareProfileUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public LoadCareProfileUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String groupId, ResultCallback<Group> callback) {
        groupRepository.getGroupById(groupId, callback);
    }
}
