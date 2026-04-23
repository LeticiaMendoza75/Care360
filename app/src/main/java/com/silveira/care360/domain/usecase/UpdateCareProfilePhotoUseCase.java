package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.storage.CarePhotoStorage;

import javax.inject.Inject;

public class UpdateCareProfilePhotoUseCase {

    private final CarePhotoStorage carePhotoStorage;

    @Inject
    public UpdateCareProfilePhotoUseCase(CarePhotoStorage carePhotoStorage) {
        this.carePhotoStorage = carePhotoStorage;
    }

    public void execute(String groupId, String localPhotoUri, ResultCallback<String> callback) {
        carePhotoStorage.uploadCarePhoto(groupId, localPhotoUri, callback);
    }
}
