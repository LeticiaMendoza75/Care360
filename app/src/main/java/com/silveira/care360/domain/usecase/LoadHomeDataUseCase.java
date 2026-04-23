package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class LoadHomeDataUseCase {

    private final UserRepository userRepository;

    @Inject
    public LoadHomeDataUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String userId, boolean mandatory, boolean fromCreateGroup, ResultCallback<Result> callback) {
        userRepository.getUserMemberships(userId, new ResultCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> memberships) {
                List<GroupMember> safeMemberships = memberships != null ? memberships : new ArrayList<>();

                if (safeMemberships.isEmpty()) {
                    callback.onSuccess(Result.navigateToGroupEntry());
                    return;
                }

                if (fromCreateGroup) {
                    resolveSelectedGroupForCreate(userId, safeMemberships, callback);
                    return;
                }

                callback.onSuccess(Result.openSelector(safeMemberships, mandatory));
            }

            @Override
            public void onError(String message) {
                callback.onError("Error cargando grupos");
            }
        });
    }

    private void resolveSelectedGroupForCreate(String userId,
                                               List<GroupMember> memberships,
                                               ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String activeGroupId) {
                GroupMember selected = findMembershipByGroupId(memberships, activeGroupId);
                if (selected == null && !memberships.isEmpty()) {
                    selected = memberships.get(0);
                }

                if (selected == null) {
                    callback.onSuccess(Result.navigateToGroupEntry());
                    return;
                }

                callback.onSuccess(Result.selectedGroup(
                        memberships,
                        selected.getGroupName(),
                        selected.getCareName(),
                        selected.getGroupId()
                ));
            }

            @Override
            public void onError(String message) {
                GroupMember selected = memberships.isEmpty() ? null : memberships.get(0);
                if (selected == null) {
                    callback.onSuccess(Result.navigateToGroupEntry());
                    return;
                }
                callback.onSuccess(Result.selectedGroup(
                        memberships,
                        selected.getGroupName(),
                        selected.getCareName(),
                        selected.getGroupId()
                ));
            }
        });
    }

    private GroupMember findMembershipByGroupId(List<GroupMember> memberships, String groupId) {
        if (memberships == null || groupId == null || groupId.trim().isEmpty()) {
            return null;
        }
        for (GroupMember member : memberships) {
            if (member != null && groupId.equals(member.getGroupId())) {
                return member;
            }
        }
        return null;
    }

    public static class Result {
        public enum Type {
            NAVIGATE_GROUP_ENTRY,
            OPEN_SELECTOR,
            SELECTED_GROUP
        }

        private final Type type;
        private final List<GroupMember> memberships;
        private final boolean mandatory;
        private final String groupName;
        private final String careName;
        private final String groupId;

        private Result(Type type,
                       List<GroupMember> memberships,
                       boolean mandatory,
                       String groupName,
                       String careName,
                       String groupId) {
            this.type = type;
            this.memberships = memberships != null ? memberships : new ArrayList<>();
            this.mandatory = mandatory;
            this.groupName = groupName;
            this.careName = careName;
            this.groupId = groupId;
        }

        public static Result navigateToGroupEntry() {
            return new Result(Type.NAVIGATE_GROUP_ENTRY, new ArrayList<>(), false, null, null, null);
        }

        public static Result openSelector(List<GroupMember> memberships, boolean mandatory) {
            return new Result(Type.OPEN_SELECTOR, memberships, mandatory, null, null, null);
        }

        public static Result selectedGroup(List<GroupMember> memberships, String groupName, String careName, String groupId) {
            return new Result(Type.SELECTED_GROUP, memberships, false, groupName, careName, groupId);
        }

        public Type getType() {
            return type;
        }

        public List<GroupMember> getMemberships() {
            return memberships;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getCareName() {
            return careName;
        }

        public String getGroupId() {
            return groupId;
        }
    }
}
