package com.silveira.care360.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Group;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.ChangeActiveGroupUseCase;
import com.silveira.care360.domain.usecase.DeleteGroupUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadFamilyDataUseCase;
import com.silveira.care360.domain.usecase.LoadHomeDataUseCase;
import com.silveira.care360.domain.usecase.LeaveGroupUseCase;
import com.silveira.care360.domain.usecase.RemoveGroupMemberUseCase;
import com.silveira.care360.domain.usecase.SignOutUseCase;
import com.silveira.care360.domain.usecase.UpdateGroupMemberRoleUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FamiliaViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final SignOutUseCase signOutUseCase;
    private final LoadFamilyDataUseCase loadFamilyDataUseCase;
    private final LoadHomeDataUseCase loadHomeDataUseCase;
    private final ChangeActiveGroupUseCase changeActiveGroupUseCase;
    private final DeleteGroupUseCase deleteGroupUseCase;
    private final UpdateGroupMemberRoleUseCase updateGroupMemberRoleUseCase;
    private final RemoveGroupMemberUseCase removeGroupMemberUseCase;
    private final LeaveGroupUseCase leaveGroupUseCase;

    private final MutableLiveData<FamiliaState> _state = new MutableLiveData<>(new FamiliaState());
    public LiveData<FamiliaState> state = _state;

    private final MutableLiveData<FamiliaAction> _action = new MutableLiveData<>();
    public LiveData<FamiliaAction> action = _action;

    private final MutableLiveData<GroupSelectorState> _groupSelectorEvent = new MutableLiveData<>();
    public LiveData<GroupSelectorState> groupSelectorEvent = _groupSelectorEvent;

    private List<GroupMember> cachedMemberships = new ArrayList<>();

    @Inject
    public FamiliaViewModel(GetCurrentUserUseCase getCurrentUserUseCase,
                            SignOutUseCase signOutUseCase,
                            LoadFamilyDataUseCase loadFamilyDataUseCase,
                            LoadHomeDataUseCase loadHomeDataUseCase,
                            ChangeActiveGroupUseCase changeActiveGroupUseCase,
                            DeleteGroupUseCase deleteGroupUseCase,
                            UpdateGroupMemberRoleUseCase updateGroupMemberRoleUseCase,
                            RemoveGroupMemberUseCase removeGroupMemberUseCase,
                            LeaveGroupUseCase leaveGroupUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.signOutUseCase = signOutUseCase;
        this.loadFamilyDataUseCase = loadFamilyDataUseCase;
        this.loadHomeDataUseCase = loadHomeDataUseCase;
        this.changeActiveGroupUseCase = changeActiveGroupUseCase;
        this.deleteGroupUseCase = deleteGroupUseCase;
        this.updateGroupMemberRoleUseCase = updateGroupMemberRoleUseCase;
        this.removeGroupMemberUseCase = removeGroupMemberUseCase;
        this.leaveGroupUseCase = leaveGroupUseCase;
    }

    public String getLoggedUserEmail() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().trim().isEmpty()) {
            return currentUser.getEmail();
        }
        return "Usuario";
    }

    public void loadFamilyData() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }

        FamiliaState current = _state.getValue();
        _state.setValue(new FamiliaState(
                true,
                current != null ? current.groupId : null,
                current != null ? current.groupName : null,
                current != null ? current.careName : null,
                current != null ? current.joinCode : null,
                currentUser.getId(),
                current != null ? current.ownerUserId : null,
                current != null && current.canManageMembers,
                current != null && current.canDeleteGroup,
                current != null && current.canLeaveGroup,
                current != null ? current.members : new ArrayList<>(),
                null
        ));

        loadFamilyDataUseCase.execute(currentUser.getId(), new ResultCallback<LoadFamilyDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadFamilyDataUseCase.Result result) {
                if (result == null || !result.hasActiveGroup()) {
                    _state.postValue(new FamiliaState(false, null, null, null, null, currentUser.getId(), null, false, false, false, new ArrayList<>(), null));
                    _action.postValue(new NavigateToGroupEntryAction());
                    return;
                }

                Group group = result.getGroup();
                List<GroupMember> members = result.getMembers() != null ? result.getMembers() : new ArrayList<>();
                String ownerUserId = group != null ? group.getCreatedBy() : null;
                boolean canDeleteGroup = canDeleteGroup(currentUser.getId(), ownerUserId);
                boolean canManageMembers = canManageMembers(currentUser.getId(), ownerUserId, members);
                boolean canLeaveGroup = isCurrentUserMember(currentUser.getId(), members);

                _state.postValue(new FamiliaState(
                        false,
                        result.getGroupId(),
                        group != null ? group.getName() : null,
                        group != null ? group.getCareName() : null,
                        group != null ? group.getJoinCode() : null,
                        currentUser.getId(),
                        ownerUserId,
                        canManageMembers,
                        canDeleteGroup,
                        canLeaveGroup,
                        members,
                        result.getMembersError()
                ));

                cacheCurrentMembership(result.getGroupId(), group);
            }

            @Override
            public void onError(String message) {
                emitLoadError("Error cargando el grupo activo");
            }
        });
    }

    public void onChangeGroupClicked() {
        if (cachedMemberships != null && cachedMemberships.size() > 1) {
            _groupSelectorEvent.setValue(new GroupSelectorState(cachedMemberships));
            return;
        }

        loadMembershipsForGroupChange();
    }

    private void loadMembershipsForGroupChange() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }

        loadHomeDataUseCase.execute(currentUser.getId(), false, false, new ResultCallback<LoadHomeDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadHomeDataUseCase.Result result) {
                cachedMemberships = result != null ? result.getMemberships() : new ArrayList<>();

                if (result == null || cachedMemberships.isEmpty()
                        || result.getType() == LoadHomeDataUseCase.Result.Type.NAVIGATE_GROUP_ENTRY) {
                    _action.postValue(new NavigateToGroupEntryAction());
                    return;
                }

                if (cachedMemberships.size() == 1) {
                    _action.postValue(new ShowMessageAction("Solo tienes un grupo disponible"));
                    return;
                }

                _groupSelectorEvent.postValue(new GroupSelectorState(cachedMemberships));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction("No se pudieron cargar tus grupos"));
            }
        });
    }

    public void onGroupSelected(String groupId) {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }

        changeActiveGroupUseCase.execute(currentUser.getId(), groupId, cachedMemberships, new ResultCallback<ChangeActiveGroupUseCase.Result>() {
            @Override
            public void onSuccess(ChangeActiveGroupUseCase.Result result) {
                loadFamilyData();
                _action.postValue(new ShowMessageAction("Grupo cambiado"));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "Error cambiando de grupo"));
            }
        });
    }

    public void onCopyCodeClicked() {
        FamiliaState current = _state.getValue();
        String joinCode = current != null ? current.joinCode : null;

        if (joinCode == null || joinCode.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("Codigo no disponible aun"));
            return;
        }

        _action.setValue(new CopyCodeAction(joinCode));
    }

    public void onShareCodeClicked() {
        FamiliaState current = _state.getValue();
        String joinCode = current != null ? current.joinCode : null;

        if (joinCode == null || joinCode.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("Codigo no disponible aun"));
            return;
        }

        _action.setValue(new ShareTextAction(buildInviteMessage(joinCode, null)));
    }

    public void onInviteContactsClicked() {
        FamiliaState current = _state.getValue();
        String joinCode = current != null ? current.joinCode : null;

        if (joinCode == null || joinCode.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("Codigo no disponible aun"));
            return;
        }

        _action.setValue(new OpenContactPickerAction());
    }

    public void onContactSelected(String contactName, String phoneNumber) {
        FamiliaState current = _state.getValue();
        String joinCode = current != null ? current.joinCode : null;

        if (joinCode == null || joinCode.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("Codigo no disponible aun"));
            return;
        }

        String inviteMessage = buildInviteMessage(joinCode, contactName);
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            _action.setValue(new OpenSmsInviteAction(phoneNumber, inviteMessage));
            return;
        }

        _action.setValue(new ShareTextAction(inviteMessage));
    }

    public void onCreateNewGroupClicked() {
        _action.setValue(new NavigateToGroupEntryAction());
    }

    public void onViewMembersClicked() {
        FamiliaState current = _state.getValue();
        List<GroupMember> members = current != null ? current.members : new ArrayList<>();

        if (members.isEmpty()) {
            _action.setValue(new ShowMessageAction("No hay miembros para mostrar"));
            return;
        }

        _action.setValue(new OpenMembersDialogAction(
                members,
                current.canManageMembers,
                current.currentUserId,
                current.ownerUserId
        ));
    }

    public void onManageMemberClicked(GroupMember member) {
        FamiliaState current = _state.getValue();
        if (current == null || !current.canManageMembers) {
            _action.setValue(new ShowMessageAction("Solo un admin puede gestionar miembros"));
            return;
        }

        if (member == null || member.getUserId() == null || member.getUserId().trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("No se pudo identificar el miembro"));
            return;
        }

        if (current.currentUserId != null && current.currentUserId.equals(member.getUserId())) {
            _action.setValue(new ShowMessageAction("No puedes gestionarte a ti mismo desde aqui"));
            return;
        }

        if (current.ownerUserId != null && current.ownerUserId.equals(member.getUserId())) {
            _action.setValue(new ShowMessageAction("No se puede gestionar al creador del grupo"));
            return;
        }

        _action.setValue(new ShowManageMemberActionsAction(member));
    }

    public void onChangeMemberRoleRequested(GroupMember member) {
        User currentUser = getCurrentUserUseCase.execute();
        FamiliaState current = _state.getValue();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }
        if (current == null) {
            _action.setValue(new ShowMessageAction("No se pudo cargar el grupo"));
            return;
        }

        String nextRole = "admin".equalsIgnoreCase(member != null ? member.getRole() : null) ? "member" : "admin";

        updateGroupMemberRoleUseCase.execute(
                currentUser.getId(),
                current.groupId,
                current.ownerUserId,
                member,
                nextRole,
                current.members,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        _action.postValue(new ShowMessageAction(
                                "admin".equalsIgnoreCase(nextRole) ? "Rol actualizado a admin" : "Rol actualizado a miembro"
                        ));
                        loadFamilyData();
                    }

                    @Override
                    public void onError(String message) {
                        _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo actualizar el rol"));
                    }
                }
        );
    }

    public void onRemoveMemberRequested(GroupMember member) {
        FamiliaState current = _state.getValue();
        if (current == null || !current.canManageMembers) {
            _action.setValue(new ShowMessageAction("Solo un admin puede eliminar miembros"));
            return;
        }

        _action.setValue(new ConfirmRemoveMemberAction(member));
    }

    public void onRemoveMemberConfirmed(GroupMember member) {
        User currentUser = getCurrentUserUseCase.execute();
        FamiliaState current = _state.getValue();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }
        if (current == null) {
            _action.setValue(new ShowMessageAction("No se pudo cargar el grupo"));
            return;
        }

        removeGroupMemberUseCase.execute(
                currentUser.getId(),
                current.groupId,
                current.ownerUserId,
                member,
                current.members,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        _action.postValue(new ShowMessageAction("Miembro eliminado del grupo"));
                        loadFamilyData();
                    }

                    @Override
                    public void onError(String message) {
                        _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo eliminar al miembro"));
                    }
                }
        );
    }

    public void onDeleteGroupClicked() {
        FamiliaState current = _state.getValue();
        if (current == null || current.groupId == null || current.groupId.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("No se pudo identificar el grupo"));
            return;
        }

        if (!current.canDeleteGroup) {
            _action.setValue(new ShowMessageAction("Solo el responsable del grupo puede eliminarlo"));
            return;
        }

        _action.setValue(new ConfirmDeleteGroupAction(
                current.groupName != null && !current.groupName.trim().isEmpty() ? current.groupName : "este grupo"
        ));
    }

    public void onLeaveGroupClicked() {
        User currentUser = getCurrentUserUseCase.execute();
        FamiliaState current = _state.getValue();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }
        if (current == null || current.groupId == null || current.groupId.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("No se pudo salir del grupo"));
            return;
        }

        // Reload latest members/roles before deciding leave flow to avoid stale state.
        loadFamilyDataUseCase.execute(currentUser.getId(), new ResultCallback<LoadFamilyDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadFamilyDataUseCase.Result result) {
                if (result == null || !result.hasActiveGroup() || result.getGroup() == null) {
                    _action.postValue(new ShowMessageAction("No se pudo cargar el grupo"));
                    return;
                }

                List<GroupMember> freshMembers = result.getMembers() != null ? result.getMembers() : new ArrayList<>();
                String freshOwnerUserId = result.getGroup().getCreatedBy();

                if (currentUser.getId() != null
                        && freshOwnerUserId != null
                        && currentUser.getId().equals(freshOwnerUserId)) {
                    List<GroupMember> candidates = leaveGroupUseCase.getTransferCandidates(
                            currentUser.getId(),
                            freshOwnerUserId,
                            freshMembers
                    );

                    if (candidates.isEmpty() && current.members != null && !current.members.isEmpty()) {
                        candidates = leaveGroupUseCase.getTransferCandidates(
                                currentUser.getId(),
                                current.ownerUserId,
                                current.members
                        );
                    }

                    if (candidates.isEmpty()) {
                        _action.postValue(new ShowMessageAction("Antes de salir debe quedar otro admin en el grupo"));
                        return;
                    }

                    _action.postValue(new SelectNewOwnerAction(candidates));
                    return;
                }

                _action.postValue(new ConfirmLeaveGroupAction(null));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction("No se pudo cargar el grupo"));
            }
        });
    }

    public void onNewOwnerSelectedForLeave(String newOwnerUserId) {
        _action.setValue(new ConfirmLeaveGroupAction(newOwnerUserId));
    }

    public void onLeaveGroupConfirmed(String newOwnerUserId) {
        User currentUser = getCurrentUserUseCase.execute();
        FamiliaState current = _state.getValue();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }
        if (current == null || current.groupId == null || current.groupId.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("No se pudo identificar el grupo"));
            return;
        }

        leaveGroupUseCase.execute(
                currentUser.getId(),
                current.groupId,
                current.ownerUserId,
                newOwnerUserId,
                current.members,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        _action.postValue(new LeaveGroupCompletedAction("Has salido del grupo"));
                    }

                    @Override
                    public void onError(String message) {
                        _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo salir del grupo"));
                    }
                }
        );
    }

    public void onDeleteGroupConfirmed() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _action.setValue(new NavigateToWelcomeAction());
            return;
        }

        FamiliaState current = _state.getValue();
        if (current == null || current.groupId == null || current.groupId.trim().isEmpty()) {
            _action.setValue(new ShowMessageAction("No se pudo identificar el grupo"));
            return;
        }

        deleteGroupUseCase.execute(
                currentUser.getId(),
                current.groupId,
                current.ownerUserId,
                current.members,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                    _action.postValue(new DeleteGroupCompletedAction("Grupo eliminado"));
                }

                    @Override
                    public void onError(String message) {
                        _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo eliminar el grupo"));
                    }
                }
        );
    }

    public void performLogout() {
        signOutUseCase.execute(null);
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    public void onGroupSelectorHandled() {
        _groupSelectorEvent.setValue(null);
    }

    private void cacheCurrentMembership(String groupId, Group group) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return;
        }

        if (cachedMemberships == null) {
            cachedMemberships = new ArrayList<>();
        }

        for (GroupMember member : cachedMemberships) {
            if (member != null && groupId.equals(member.getGroupId())) {
                if ((member.getGroupName() == null || member.getGroupName().trim().isEmpty()) && group != null) {
                    member.setGroupName(group.getName());
                }
                if ((member.getCareName() == null || member.getCareName().trim().isEmpty()) && group != null) {
                    member.setCareName(group.getCareName());
                }
                return;
            }
        }

        GroupMember currentMembership = new GroupMember();
        currentMembership.setGroupId(groupId);
        if (group != null) {
            currentMembership.setGroupName(group.getName());
            currentMembership.setCareName(group.getCareName());
        }
        cachedMemberships.add(currentMembership);
    }

    private void emitLoadError(String message) {
        FamiliaState current = _state.getValue();
        _state.postValue(new FamiliaState(
                false,
                current != null ? current.groupId : null,
                current != null ? current.groupName : null,
                current != null ? current.careName : null,
                current != null ? current.joinCode : null,
                current != null ? current.currentUserId : null,
                current != null ? current.ownerUserId : null,
                current != null && current.canManageMembers,
                current != null && current.canDeleteGroup,
                current != null && current.canLeaveGroup,
                current != null ? current.members : new ArrayList<>(),
                message
        ));
    }

    private boolean isCurrentUserMember(String currentUserId, List<GroupMember> members) {
        if (currentUserId == null || members == null) {
            return false;
        }

        for (GroupMember member : members) {
            if (member != null && currentUserId.equals(member.getUserId())) {
                return true;
            }
        }

        return false;
    }

    private boolean canDeleteGroup(String currentUserId, String ownerUserId) {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return false;
        }

        return ownerUserId != null && currentUserId.equals(ownerUserId);
    }

    private boolean canManageMembers(String currentUserId, String ownerUserId, List<GroupMember> members) {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return false;
        }

        if (ownerUserId != null && currentUserId.equals(ownerUserId)) {
            return true;
        }

        if (members == null) {
            return false;
        }

        for (GroupMember member : members) {
            if (member != null
                    && currentUserId.equals(member.getUserId())
                    && "admin".equalsIgnoreCase(member.getRole())) {
                return true;
            }
        }

        return false;
    }

    private String buildInviteMessage(String joinCode, String contactName) {
        String saludo = (contactName != null && !contactName.trim().isEmpty())
                ? "Hola " + contactName + "\n\n"
                : "Hola\n\n";

        return saludo
                + "Te invito a unirte a nuestro grupo familiar en Care360.\n"
                + "Entra en la app y pulsa \"Unirme con codigo\".\n\n"
                + "Codigo: " + joinCode;
    }

    public static abstract class FamiliaAction { }

    public static class CopyCodeAction extends FamiliaAction {
        public final String code;

        public CopyCodeAction(String code) {
            this.code = code;
        }
    }

    public static class ShareTextAction extends FamiliaAction {
        public final String text;

        public ShareTextAction(String text) {
            this.text = text;
        }
    }

    public static class OpenContactPickerAction extends FamiliaAction { }

    public static class OpenSmsInviteAction extends FamiliaAction {
        public final String phoneNumber;
        public final String text;

        public OpenSmsInviteAction(String phoneNumber, String text) {
            this.phoneNumber = phoneNumber;
            this.text = text;
        }
    }

    public static class OpenMembersDialogAction extends FamiliaAction {
        public final List<GroupMember> members;
        public final boolean canManageMembers;
        public final String currentUserId;
        public final String ownerUserId;

        public OpenMembersDialogAction(List<GroupMember> members) {
            this(members, false, null, null);
        }

        public OpenMembersDialogAction(List<GroupMember> members,
                                       boolean canManageMembers,
                                       String currentUserId,
                                       String ownerUserId) {
            this.members = members != null ? members : new ArrayList<>();
            this.canManageMembers = canManageMembers;
            this.currentUserId = currentUserId;
            this.ownerUserId = ownerUserId;
        }
    }

    public static class ShowManageMemberActionsAction extends FamiliaAction {
        public final GroupMember member;

        public ShowManageMemberActionsAction(GroupMember member) {
            this.member = member;
        }
    }

    public static class ConfirmRemoveMemberAction extends FamiliaAction {
        public final GroupMember member;

        public ConfirmRemoveMemberAction(GroupMember member) {
            this.member = member;
        }
    }

    public static class ConfirmDeleteGroupAction extends FamiliaAction {
        public final String groupName;

        public ConfirmDeleteGroupAction(String groupName) {
            this.groupName = groupName;
        }
    }

    public static class SelectNewOwnerAction extends FamiliaAction {
        public final List<GroupMember> candidates;

        public SelectNewOwnerAction(List<GroupMember> candidates) {
            this.candidates = candidates != null ? candidates : new ArrayList<>();
        }
    }

    public static class ConfirmLeaveGroupAction extends FamiliaAction {
        public final String newOwnerUserId;

        public ConfirmLeaveGroupAction(String newOwnerUserId) {
            this.newOwnerUserId = newOwnerUserId;
        }
    }

    public static class LeaveGroupCompletedAction extends FamiliaAction {
        public final String message;

        public LeaveGroupCompletedAction(String message) {
            this.message = message;
        }
    }

    public static class DeleteGroupCompletedAction extends FamiliaAction {
        public final String message;

        public DeleteGroupCompletedAction(String message) {
            this.message = message;
        }
    }

    public static class NavigateToGroupEntryAction extends FamiliaAction { }

    public static class NavigateToWelcomeAction extends FamiliaAction { }

    public static class ShowMessageAction extends FamiliaAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }

    public static class GroupSelectorState {
        public final List<GroupMember> memberships;

        public GroupSelectorState(List<GroupMember> memberships) {
            this.memberships = memberships != null ? memberships : new ArrayList<>();
        }
    }
}
