package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.User;

import java.util.List;

/**
 * Interfaz de repositorio definida en el dominio.
 * Utiliza unicamente modelos de dominio para mantener la independencia tecnologica.
 */
public interface UserRepository {
    void getUserProfile(String uid, ResultCallback<User> callback);
    void createMinimalUserProfile(User user, String authProvider, ResultCallback<Void> callback);
    void searchUserByEmail(String email, ResultCallback<List<User>> callback);
    void setActiveGroup(String uid, String groupId, ResultCallback<Void> callback);
    void getActiveGroupId(String uid, ResultCallback<String> callback);
    void getUserMemberships(String uid, ResultCallback<List<GroupMember>> callback);
}
