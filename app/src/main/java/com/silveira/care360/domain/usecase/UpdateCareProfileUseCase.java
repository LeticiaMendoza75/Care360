package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.GroupRepository;

import javax.inject.Inject;

public class UpdateCareProfileUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public UpdateCareProfileUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String groupId,
                        String careName,
                        Integer careAge,
                        String carePhotoUri,
                        String carePhotoUrl,
                        String carePhone,
                        String careAddress,
                        String emergencyContactName,
                        String emergencyContactPhone,
                        String careAllergies,
                        String careConditions,
                        ResultCallback<Void> callback) {
        groupRepository.updateCareProfile(
                groupId,
                careName,
                careAge,
                carePhotoUri,
                carePhotoUrl,
                carePhone,
                careAddress,
                emergencyContactName,
                emergencyContactPhone,
                careAllergies,
                careConditions,
                callback
        );
    }
}
