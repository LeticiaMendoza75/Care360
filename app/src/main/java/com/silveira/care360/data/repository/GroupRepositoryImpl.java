package com.silveira.care360.data.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.mapper.GroupMapper;
import com.silveira.care360.data.mapper.GroupMemberMapper;
import com.silveira.care360.data.mapper.UserMapper;
import com.silveira.care360.data.remote.dto.GroupDocumentDto;
import com.silveira.care360.data.remote.dto.GroupMemberDocumentDto;
import com.silveira.care360.data.remote.dto.UserDocumentDto;
import com.silveira.care360.data.remote.firestore.FirebaseGroupDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Group;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.GroupUserInput;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GroupRepositoryImpl implements GroupRepository {

    private final FirebaseGroupDataSource groupDataSource;

    @Inject
    public GroupRepositoryImpl(FirebaseGroupDataSource groupDataSource) {
        this.groupDataSource = groupDataSource;
    }

    @Override
    public void createGroupForUser(String groupName, String careName, User user, ResultCallback<Void> callback) {
        createGroupForUser(groupName, careName, toGroupUserInput(user), callback);
    }

    @Override
    public void joinGroupByCode(String joinCode, User user, ResultCallback<JoinGroupResult> callback) {
        joinGroupByCode(joinCode, toGroupUserInput(user), callback);
    }

    @Override
    public void createGroupForUser(String groupName, String careName, GroupUserInput user, ResultCallback<Void> callback) {
        UserDocumentDto userDto = toUserDocumentDto(user);
        groupDataSource.createGroupForUser(groupName, careName, userDto, callback);
    }

    @Override
    public void joinGroupByCode(String joinCode, GroupUserInput user, ResultCallback<JoinGroupResult> callback) {
        UserDocumentDto userDto = toUserDocumentDto(user);
        groupDataSource.joinGroupByCode(joinCode, userDto, callback);
    }

    @Override
    public void getGroupMembers(String groupId, ResultCallback<List<GroupMember>> callback) {
        groupDataSource.getGroupMembers(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudieron cargar los miembros del grupo");
                return;
            }

            List<GroupMember> members = new ArrayList<>();
            List<GroupMemberDocumentDto> dtos = task.getResult();
            if (dtos != null) {
                for (GroupMemberDocumentDto dto : dtos) {
                    GroupMember member = GroupMemberMapper.fromDto(dto);
                    if (member != null) {
                        members.add(member);
                    }
                }
            }

            callback.onSuccess(members);
        });
    }

    @Override
    public void getGroupById(String groupId, ResultCallback<Group> callback) {
        groupDataSource.getGroupById(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo cargar el grupo");
                return;
            }

            DocumentSnapshot doc = task.getResult();
            if (doc == null || !doc.exists()) {
                callback.onSuccess(null);
                return;
            }

            GroupDocumentDto dto = GroupMapper.fromFirestore(doc);
            callback.onSuccess(GroupMapper.fromDto(dto));
        });
    }

    @Override
    public void updateMemberRole(String groupId, String userId, String newRole, ResultCallback<Void> callback) {
        groupDataSource.updateMemberRole(groupId, userId, newRole, callback);
    }

    @Override
    public void removeMember(String groupId, String userId, ResultCallback<Void> callback) {
        groupDataSource.removeMember(groupId, userId, callback);
    }

    @Override
    public void leaveGroup(String groupId, String userId, String newOwnerUserId, ResultCallback<Void> callback) {
        groupDataSource.leaveGroup(groupId, userId, newOwnerUserId, callback);
    }

    @Override
    public void deleteGroup(String groupId, List<GroupMember> members, ResultCallback<Void> callback) {
        groupDataSource.deleteGroup(groupId, members, callback);
    }

    @Override
    public void updateCareProfile(String groupId,
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
        groupDataSource.updateCareProfile(
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

    private UserDocumentDto toUserDocumentDto(GroupUserInput user) {
        if (user == null) {
            return null;
        }

        UserDocumentDto userDto = new UserDocumentDto();
        userDto.setId(user.getUserId());
        userDto.setEmail(user.getEmail());
        userDto.setDisplayName(user.getDisplayName());
        return userDto;
    }
}
