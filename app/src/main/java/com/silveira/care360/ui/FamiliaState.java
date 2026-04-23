package com.silveira.care360.ui;

import com.silveira.care360.domain.model.GroupMember;

import java.util.ArrayList;
import java.util.List;

public class FamiliaState {

    public final boolean isLoading;
    public final String groupId;
    public final String groupName;
    public final String careName;
    public final String joinCode;
    public final String currentUserId;
    public final String ownerUserId;
    public final boolean canManageMembers;
    public final boolean canDeleteGroup;
    public final boolean canLeaveGroup;
    public final List<GroupMember> members;
    public final String errorMessage;

    public FamiliaState() {
        this(false, null, null, null, null, null, null, false, false, false, new ArrayList<>(), null);
    }

    public FamiliaState(boolean isLoading,
                        String groupId,
                        String groupName,
                        String careName,
                        String joinCode,
                        String currentUserId,
                        String ownerUserId,
                        boolean canManageMembers,
                        boolean canDeleteGroup,
                        boolean canLeaveGroup,
                        List<GroupMember> members,
                        String errorMessage) {
        this.isLoading = isLoading;
        this.groupId = groupId;
        this.groupName = groupName;
        this.careName = careName;
        this.joinCode = joinCode;
        this.currentUserId = currentUserId;
        this.ownerUserId = ownerUserId;
        this.canManageMembers = canManageMembers;
        this.canDeleteGroup = canDeleteGroup;
        this.canLeaveGroup = canLeaveGroup;
        this.members = members != null ? members : new ArrayList<>();
        this.errorMessage = errorMessage;
    }
}
