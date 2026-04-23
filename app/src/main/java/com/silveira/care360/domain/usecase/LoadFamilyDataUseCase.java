package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Group;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.GroupRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class LoadFamilyDataUseCase {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Inject
    public LoadFamilyDataUseCase(UserRepository userRepository,
                                 GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String activeGroupId) {
                if (activeGroupId == null || activeGroupId.trim().isEmpty()) {
                    callback.onSuccess(Result.noActiveGroup());
                    return;
                }

                loadGroup(activeGroupId, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError("Error cargando el grupo activo");
            }
        });
    }

    private void loadGroup(String activeGroupId, ResultCallback<Result> callback) {
        groupRepository.getGroupById(activeGroupId, new ResultCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group == null) {
                    callback.onError("No se encontro el grupo activo");
                    return;
                }

                groupRepository.getGroupMembers(activeGroupId, new ResultCallback<List<GroupMember>>() {
                    @Override
                    public void onSuccess(List<GroupMember> members) {
                        callback.onSuccess(Result.withGroup(
                                activeGroupId,
                                group,
                                members != null ? members : new ArrayList<>(),
                                null
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onSuccess(Result.withGroup(
                                activeGroupId,
                                group,
                                new ArrayList<>(),
                                "No se pudieron cargar los miembros del grupo"
                        ));
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError("Error cargando los datos del grupo");
            }
        });
    }

    public static class Result {
        private final boolean hasActiveGroup;
        private final String groupId;
        private final Group group;
        private final List<GroupMember> members;
        private final String membersError;

        private Result(boolean hasActiveGroup,
                       String groupId,
                       Group group,
                       List<GroupMember> members,
                       String membersError) {
            this.hasActiveGroup = hasActiveGroup;
            this.groupId = groupId;
            this.group = group;
            this.members = members != null ? members : new ArrayList<>();
            this.membersError = membersError;
        }

        public static Result noActiveGroup() {
            return new Result(false, null, null, new ArrayList<>(), null);
        }

        public static Result withGroup(String groupId,
                                       Group group,
                                       List<GroupMember> members,
                                       String membersError) {
            return new Result(true, groupId, group, members, membersError);
        }

        public boolean hasActiveGroup() {
            return hasActiveGroup;
        }

        public String getGroupId() {
            return groupId;
        }

        public Group getGroup() {
            return group;
        }

        public List<GroupMember> getMembers() {
            return members;
        }

        public String getMembersError() {
            return membersError;
        }
    }
}
