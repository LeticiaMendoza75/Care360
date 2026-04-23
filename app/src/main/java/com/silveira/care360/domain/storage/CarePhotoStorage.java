package com.silveira.care360.domain.storage;

import com.silveira.care360.domain.common.ResultCallback;

public interface CarePhotoStorage {

    void uploadCarePhoto(String groupId, String localPhotoUri, ResultCallback<String> callback);
}
