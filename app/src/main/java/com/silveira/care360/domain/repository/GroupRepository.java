package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Group;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.GroupUserInput;
import com.silveira.care360.domain.model.User;

import java.util.List;

/**
 * Interfaz de repositorio para grupos sin filtrar detalles de Firebase.
 */
public interface GroupRepository {

    void createGroupForUser(String groupName, String careName, User user, ResultCallback<Void> callback);

    void joinGroupByCode(String joinCode, User user, ResultCallback<JoinGroupResult> callback);

    void createGroupForUser(String groupName, String careName, GroupUserInput user, ResultCallback<Void> callback);

    void joinGroupByCode(String joinCode, GroupUserInput user, ResultCallback<JoinGroupResult> callback);

    void getGroupMembers(String groupId, ResultCallback<List<GroupMember>> callback);

    void getGroupById(String groupId, ResultCallback<Group> callback);

    void updateMemberRole(String groupId, String userId, String newRole, ResultCallback<Void> callback);

    void removeMember(String groupId, String userId, ResultCallback<Void> callback);

    void leaveGroup(String groupId, String userId, String newOwnerUserId, ResultCallback<Void> callback);

    void deleteGroup(String groupId, List<GroupMember> members, ResultCallback<Void> callback);

    void updateCareProfile(String groupId,
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
                           ResultCallback<Void> callback);

    class JoinGroupResult {
        public enum Type {
            JOINED
        }

        private final Type type;
        private final String message;

        public JoinGroupResult(Type type, String message) {
            this.type = type;
            this.message = message;
        }

        public static JoinGroupResult joined() {
            return new JoinGroupResult(Type.JOINED, "Te has unido al grupo");
        }

        public Type getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public boolean isJoined() {
            return type == Type.JOINED;
        }
    }
}
