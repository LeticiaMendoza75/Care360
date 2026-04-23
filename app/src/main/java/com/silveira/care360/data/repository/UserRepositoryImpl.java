package com.silveira.care360.data.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.silveira.care360.data.mapper.GroupMemberMapper;
import com.silveira.care360.data.mapper.UserMapper;
import com.silveira.care360.data.remote.dto.GroupMemberDocumentDto;
import com.silveira.care360.data.remote.dto.UserDocumentDto;
import com.silveira.care360.data.remote.firestore.FirebaseUserDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {

    private final FirebaseUserDataSource userDataSource;

    @Inject
    public UserRepositoryImpl(FirebaseUserDataSource userDataSource) {
        this.userDataSource = userDataSource;
    }

    @Override
    public void getUserProfile(String uid, ResultCallback<User> callback) {
        userDataSource.getUserProfileTask(uid).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo cargar el perfil de usuario");
                return;
            }

            callback.onSuccess(UserMapper.fromDto(UserMapper.fromFirestore(task.getResult())));
        });
    }

    @Override
    public void createMinimalUserProfile(User user, String authProvider, ResultCallback<Void> callback) {
        UserDocumentDto userDto = UserMapper.toDocumentDto(user, authProvider);

        userDataSource.createMinimalUserProfile(userDto).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo guardar el perfil de usuario");
                return;
            }

            callback.onSuccess(null);
        });
    }

    @Override
    public void searchUserByEmail(String email, ResultCallback<List<User>> callback) {
        userDataSource.searchUserByEmail(email).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo comprobar el email");
                return;
            }

            callback.onSuccess(mapUsers(task.getResult()));
        });
    }

    @Override
    public void setActiveGroup(String uid, String groupId, ResultCallback<Void> callback) {
        userDataSource.setActiveGroup(uid, groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo actualizar el grupo activo");
                return;
            }

            callback.onSuccess(null);
        });
    }

    @Override
    public void getActiveGroupId(String uid, ResultCallback<String> callback) {
        userDataSource.getUserProfileTask(uid).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo obtener el grupo activo");
                return;
            }

            DocumentSnapshot document = task.getResult();
            if (document == null || !document.exists()) {
                callback.onSuccess(null);
                return;
            }

            callback.onSuccess(document.getString("activeGroupId"));
        });
    }

    @Override
    public void getUserMemberships(String uid, ResultCallback<List<GroupMember>> callback) {
        userDataSource.getUserMemberships(uid).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudieron cargar los grupos");
                return;
            }

            callback.onSuccess(mapMemberships(task.getResult()));
        });
    }

    private List<GroupMember> mapMemberships(List<GroupMemberDocumentDto> dtos) {
        List<GroupMember> members = new ArrayList<>();
        if (dtos == null) {
            return members;
        }

        for (GroupMemberDocumentDto dto : dtos) {
            GroupMember member = GroupMemberMapper.fromDto(dto);
            if (member != null) {
                members.add(member);
            }
        }

        return members;
    }

    private List<User> mapUsers(QuerySnapshot snapshot) {
        List<User> users = new ArrayList<>();
        if (snapshot == null) {
            return users;
        }

        for (QueryDocumentSnapshot doc : snapshot) {
            users.add(UserMapper.fromDto(UserMapper.fromFirestore(doc)));
        }

        return users;
    }
}
