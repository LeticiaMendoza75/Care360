package com.silveira.care360.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.ActividadItem;
import com.silveira.care360.domain.model.Group;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.domain.model.Patologia;
import com.silveira.care360.domain.model.SeguimientoRegistro;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.ChangeActiveGroupUseCase;
import com.silveira.care360.domain.usecase.DeleteIncidenciaUseCase;
import com.silveira.care360.domain.usecase.ExportIncidenciasPdfUseCase;
import com.silveira.care360.domain.usecase.GetActiveGroupIdUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadHomeDataUseCase;
import com.silveira.care360.domain.usecase.LoadPatologiasUseCase;
import com.silveira.care360.domain.usecase.LoadIncidenciasUseCase;
import com.silveira.care360.domain.usecase.LoadLatestIncidenciaUseCase;
import com.silveira.care360.domain.usecase.LoadNextCitaUseCase;
import com.silveira.care360.domain.usecase.LoadCareProfileUseCase;
import com.silveira.care360.domain.usecase.LoadActividadDataUseCase;
import com.silveira.care360.domain.usecase.LoadNextMedicacionUseCase;
import com.silveira.care360.domain.usecase.LoadSeguimientoUseCase;
import com.silveira.care360.domain.usecase.SaveIncidenciaUseCase;
import com.silveira.care360.domain.usecase.SignOutUseCase;
import com.silveira.care360.domain.usecase.UpdateCareProfilePhotoUseCase;
import com.silveira.care360.domain.usecase.UpdateCareProfileUseCase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.net.Uri;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetActiveGroupIdUseCase getActiveGroupIdUseCase;
    private final SignOutUseCase signOutUseCase;
    private final LoadHomeDataUseCase loadHomeDataUseCase;
    private final LoadNextMedicacionUseCase loadNextMedicacionUseCase;
    private final LoadNextCitaUseCase loadNextCitaUseCase;
    private final LoadLatestIncidenciaUseCase loadLatestIncidenciaUseCase;
    private final LoadIncidenciasUseCase loadIncidenciasUseCase;
    private final LoadCareProfileUseCase loadCareProfileUseCase;
    private final LoadActividadDataUseCase loadActividadDataUseCase;
    private final LoadPatologiasUseCase loadPatologiasUseCase;
    private final LoadSeguimientoUseCase loadSeguimientoUseCase;
    private final ExportIncidenciasPdfUseCase exportIncidenciasPdfUseCase;
    private final SaveIncidenciaUseCase saveIncidenciaUseCase;
    private final DeleteIncidenciaUseCase deleteIncidenciaUseCase;
    private final ChangeActiveGroupUseCase changeActiveGroupUseCase;
    private final UpdateCareProfilePhotoUseCase updateCareProfilePhotoUseCase;
    private final UpdateCareProfileUseCase updateCareProfileUseCase;

    private final MutableLiveData<HomeState> _state = new MutableLiveData<>(new HomeState());
    public LiveData<HomeState> state = _state;

    private final MutableLiveData<GroupSelectorState> _groupSelectorEvent = new MutableLiveData<>();
    public LiveData<GroupSelectorState> groupSelectorEvent = _groupSelectorEvent;

    private final MutableLiveData<HomeNavigation> _navigation = new MutableLiveData<>();
    public LiveData<HomeNavigation> navigation = _navigation;

    private final MutableLiveData<HomeAction> _action = new MutableLiveData<>();
    public LiveData<HomeAction> action = _action;

    private final MutableLiveData<RecentActivityState> _recentActivity = new MutableLiveData<>(new RecentActivityState());
    public LiveData<RecentActivityState> recentActivity = _recentActivity;
    private final MutableLiveData<HealthSummaryState> _healthSummary = new MutableLiveData<>(new HealthSummaryState());
    public LiveData<HealthSummaryState> healthSummary = _healthSummary;

    private List<GroupMember> cachedMemberships = new ArrayList<>();

    @Inject
    public HomeViewModel(GetCurrentUserUseCase getCurrentUserUseCase,
                         GetActiveGroupIdUseCase getActiveGroupIdUseCase,
                         SignOutUseCase signOutUseCase,
                         LoadHomeDataUseCase loadHomeDataUseCase,
                         LoadNextMedicacionUseCase loadNextMedicacionUseCase,
                         LoadNextCitaUseCase loadNextCitaUseCase,
                         LoadLatestIncidenciaUseCase loadLatestIncidenciaUseCase,
                         LoadIncidenciasUseCase loadIncidenciasUseCase,
                         LoadCareProfileUseCase loadCareProfileUseCase,
                         LoadActividadDataUseCase loadActividadDataUseCase,
                         LoadPatologiasUseCase loadPatologiasUseCase,
                         LoadSeguimientoUseCase loadSeguimientoUseCase,
                         ExportIncidenciasPdfUseCase exportIncidenciasPdfUseCase,
                         SaveIncidenciaUseCase saveIncidenciaUseCase,
                         DeleteIncidenciaUseCase deleteIncidenciaUseCase,
                         ChangeActiveGroupUseCase changeActiveGroupUseCase,
                         UpdateCareProfilePhotoUseCase updateCareProfilePhotoUseCase,
                         UpdateCareProfileUseCase updateCareProfileUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getActiveGroupIdUseCase = getActiveGroupIdUseCase;
        this.signOutUseCase = signOutUseCase;
        this.loadHomeDataUseCase = loadHomeDataUseCase;
        this.loadNextMedicacionUseCase = loadNextMedicacionUseCase;
        this.loadNextCitaUseCase = loadNextCitaUseCase;
        this.loadLatestIncidenciaUseCase = loadLatestIncidenciaUseCase;
        this.loadIncidenciasUseCase = loadIncidenciasUseCase;
        this.loadCareProfileUseCase = loadCareProfileUseCase;
        this.loadActividadDataUseCase = loadActividadDataUseCase;
        this.loadPatologiasUseCase = loadPatologiasUseCase;
        this.loadSeguimientoUseCase = loadSeguimientoUseCase;
        this.exportIncidenciasPdfUseCase = exportIncidenciasPdfUseCase;
        this.saveIncidenciaUseCase = saveIncidenciaUseCase;
        this.deleteIncidenciaUseCase = deleteIncidenciaUseCase;
        this.changeActiveGroupUseCase = changeActiveGroupUseCase;
        this.updateCareProfilePhotoUseCase = updateCareProfilePhotoUseCase;
        this.updateCareProfileUseCase = updateCareProfileUseCase;
    }

    public String getLoggedUserEmail() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().trim().isEmpty()) {
            return currentUser.getEmail();
        }
        return "Usuario";
    }

    public boolean hasAuthenticatedUser() {
        User currentUser = getCurrentUserUseCase.execute();
        return currentUser != null && currentUser.getId() != null && !currentUser.getId().trim().isEmpty();
    }

    public void loadHomeData(String navigationSource) {
        boolean fromCreateGroup = "create_group".equals(navigationSource);
        loadMembershipsAndOpenSelector(true, fromCreateGroup);
    }

    public void reloadHomeAfterLanguageChange() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _navigation.setValue(HomeNavigation.WELCOME);
            return;
        }

        _state.setValue(copyState(true, null));
        getActiveGroupIdUseCase.execute(currentUser.getId(), new ResultCallback<String>() {
            @Override
            public void onSuccess(String activeGroupId) {
                reloadCurrentGroupWithoutSelector(currentUser, activeGroupId);
            }

            @Override
            public void onError(String message) {
                reloadCurrentGroupWithoutSelector(currentUser, null);
            }
        });
    }

    private void reloadCurrentGroupWithoutSelector(User currentUser, String activeGroupId) {
        loadHomeDataUseCase.execute(currentUser.getId(), false, false, new ResultCallback<LoadHomeDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadHomeDataUseCase.Result result) {
                cachedMemberships = result != null ? result.getMemberships() : new ArrayList<>();

                if (result == null) {
                    emitGroupError("Error cargando grupos");
                    return;
                }

                if (result.getType() == LoadHomeDataUseCase.Result.Type.NAVIGATE_GROUP_ENTRY || cachedMemberships.isEmpty()) {
                    _state.postValue(copyState(false, null));
                    _navigation.postValue(HomeNavigation.GROUP_ENTRY);
                    return;
                }

                GroupMember activeMembership = findMembershipByGroupId(cachedMemberships, activeGroupId);
                if (activeMembership == null && cachedMemberships.size() == 1) {
                    activeMembership = cachedMemberships.get(0);
                }

                if (activeMembership != null) {
                    publishBaseStateAndLoadNext(
                            currentUser.getId(),
                            new HomeState(
                                    false,
                                    activeMembership.getGroupName(),
                                    activeMembership.getCareName(),
                                    null,
                                    null,
                                    activeMembership.getGroupId(),
                                    "",
                                    "",
                                    false,
                                    "",
                                    "",
                                    "",
                                    false,
                                    "",
                                    "",
                                    "",
                                    "",
                                    null
                            )
                    );
                    return;
                }

                _state.postValue(copyState(false, null));
                _groupSelectorEvent.postValue(new GroupSelectorState(cachedMemberships, false));
            }

            @Override
            public void onError(String message) {
                emitGroupError("Error cargando grupos");
            }
        });
    }

    public void onChangeGroupClicked() {
        if (cachedMemberships != null && !cachedMemberships.isEmpty()) {
            if (cachedMemberships.size() == 1) {
                _action.setValue(new ShowMessageAction("Solo tienes un grupo disponible"));
                return;
            }
            _groupSelectorEvent.setValue(new GroupSelectorState(cachedMemberships, false));
            return;
        }
        loadMembershipsForGroupChange();
    }

    private void loadMembershipsForGroupChange() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _navigation.setValue(HomeNavigation.WELCOME);
            return;
        }

        loadHomeDataUseCase.execute(currentUser.getId(), false, false, new ResultCallback<LoadHomeDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadHomeDataUseCase.Result result) {
                cachedMemberships = result != null ? result.getMemberships() : new ArrayList<>();

                if (result == null || cachedMemberships.isEmpty()
                        || result.getType() == LoadHomeDataUseCase.Result.Type.NAVIGATE_GROUP_ENTRY) {
                    _navigation.postValue(HomeNavigation.GROUP_ENTRY);
                    return;
                }

                if (cachedMemberships.size() == 1) {
                    _action.postValue(new ShowMessageAction("Solo tienes un grupo disponible"));
                    return;
                }

                _groupSelectorEvent.postValue(new GroupSelectorState(cachedMemberships, false));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction("No se pudieron cargar tus grupos"));
            }
        });
    }

    private void loadMembershipsAndOpenSelector(boolean mandatory, boolean fromCreateGroup) {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _navigation.setValue(HomeNavigation.WELCOME);
            return;
        }

        HomeState currentState = _state.getValue();
        _state.setValue(new HomeState(
                true,
                currentState != null ? currentState.groupName : null,
                currentState != null ? currentState.careName : null,
                currentState != null ? currentState.careAge : null,
                currentState != null ? currentState.carePhotoUri : null,
                currentState != null ? currentState.carePhone : "",
                currentState != null ? currentState.careAddress : "",
                currentState != null ? currentState.emergencyContactName : "",
                currentState != null ? currentState.emergencyContactPhone : "",
                currentState != null ? currentState.careAllergies : "",
                currentState != null ? currentState.careConditions : "",
                currentState != null ? currentState.activeGroupId : null,
                currentState != null ? currentState.nextMedicacionNombre : "",
                currentState != null ? currentState.nextMedicacionHora : "",
                currentState != null && currentState.nextMedicacionAlertasActivas,
                currentState != null ? currentState.nextCitaTitulo : "",
                currentState != null ? currentState.nextCitaHorario : "",
                currentState != null ? currentState.nextCitaEncargada : "",
                currentState != null && currentState.nextCitaRecordatorioActivo,
                currentState != null ? currentState.lastIncidenciaTipo : "",
                currentState != null ? currentState.lastIncidenciaHorario : "",
                currentState != null ? currentState.lastIncidenciaNivel : "",
                currentState != null ? currentState.lastIncidenciaDescripcion : "",
                null
        ));

        loadHomeDataUseCase.execute(currentUser.getId(), mandatory, fromCreateGroup, new ResultCallback<LoadHomeDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadHomeDataUseCase.Result result) {
                cachedMemberships = result != null ? result.getMemberships() : new ArrayList<>();

                if (result == null) {
                    emitGroupError("Error cargando grupos");
                    return;
                }

                switch (result.getType()) {
                    case NAVIGATE_GROUP_ENTRY:
                        _state.postValue(copyState(false, null));
                        _navigation.postValue(HomeNavigation.GROUP_ENTRY);
                        return;
                    case SELECTED_GROUP:
                        publishBaseStateAndLoadNext(
                                currentUser.getId(),
                                new HomeState(false, result.getGroupName(), result.getCareName(), null, null,
                                        result.getGroupId(), "", "", false, "", "", false, "", "", "", "", null)
                        );
                        return;
                    case OPEN_SELECTOR:
                        _state.postValue(copyState(false, null));
                        _groupSelectorEvent.postValue(new GroupSelectorState(result.getMemberships(), result.isMandatory()));
                        return;
                    default:
                        emitGroupError("Error cargando grupos");
                }
            }

            @Override
            public void onError(String message) {
                emitGroupError("Error cargando grupos");
            }
        });
    }

    public void onGroupSelected(String groupId) {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _navigation.setValue(HomeNavigation.WELCOME);
            return;
        }

        HomeState currentState = _state.getValue();
        _state.setValue(new HomeState(
                true,
                currentState != null ? currentState.groupName : null,
                currentState != null ? currentState.careName : null,
                null,
                null,
                "",
                "",
                "",
                "",
                "",
                "",
                groupId,
                currentState != null ? currentState.nextMedicacionNombre : "",
                currentState != null ? currentState.nextMedicacionHora : "",
                currentState != null && currentState.nextMedicacionAlertasActivas,
                currentState != null ? currentState.nextCitaTitulo : "",
                currentState != null ? currentState.nextCitaHorario : "",
                currentState != null ? currentState.nextCitaEncargada : "",
                currentState != null && currentState.nextCitaRecordatorioActivo,
                currentState != null ? currentState.lastIncidenciaTipo : "",
                currentState != null ? currentState.lastIncidenciaHorario : "",
                currentState != null ? currentState.lastIncidenciaNivel : "",
                currentState != null ? currentState.lastIncidenciaDescripcion : "",
                null
        ));

        changeActiveGroupUseCase.execute(currentUser.getId(), groupId, cachedMemberships, new ResultCallback<ChangeActiveGroupUseCase.Result>() {
            @Override
            public void onSuccess(ChangeActiveGroupUseCase.Result result) {
                publishBaseStateAndLoadNext(
                        currentUser.getId(),
                        new HomeState(false, result.getGroupName(), result.getCareName(), null, null,
                                groupId, "", "", false, "", "", false, "", "", "", "", null)
                );
            }

            @Override
            public void onError(String message) {
                HomeState current = _state.getValue();
                _state.postValue(copyState(false, message != null ? message : "Error cambiando de grupo"));
            }
        });
    }

    private void publishBaseStateAndLoadNext(String userId, HomeState baseState) {
        _state.postValue(baseState);
        loadCareProfile(baseState);
        loadHealthSummary(baseState.activeGroupId);

        loadNextMedicacionUseCase.execute(userId, new ResultCallback<LoadNextMedicacionUseCase.Result>() {
            @Override
            public void onSuccess(LoadNextMedicacionUseCase.Result result) {
                HomeState latest = _state.getValue();
                HomeState merged = new HomeState(
                        latest != null && latest.isLoading,
                        latest != null ? latest.groupName : baseState.groupName,
                        latest != null ? latest.careName : baseState.careName,
                        mergeCareAge(latest, baseState),
                        mergeCarePhoto(latest, baseState),
                        latest != null ? latest.carePhone : baseState.carePhone,
                        latest != null ? latest.careAddress : baseState.careAddress,
                        latest != null ? latest.emergencyContactName : baseState.emergencyContactName,
                        latest != null ? latest.emergencyContactPhone : baseState.emergencyContactPhone,
                        latest != null ? latest.careAllergies : baseState.careAllergies,
                        latest != null ? latest.careConditions : baseState.careConditions,
                        latest != null ? latest.activeGroupId : baseState.activeGroupId,
                        result.getNombre(),
                        result.getHorario(),
                        result.isAlertasActivas(),
                        latest != null ? latest.nextCitaTitulo : baseState.nextCitaTitulo,
                        latest != null ? latest.nextCitaHorario : baseState.nextCitaHorario,
                        latest != null ? latest.nextCitaEncargada : baseState.nextCitaEncargada,
                        latest != null && latest.nextCitaRecordatorioActivo,
                        latest != null ? latest.lastIncidenciaTipo : baseState.lastIncidenciaTipo,
                        latest != null ? latest.lastIncidenciaHorario : baseState.lastIncidenciaHorario,
                        latest != null ? latest.lastIncidenciaNivel : baseState.lastIncidenciaNivel,
                        latest != null ? latest.lastIncidenciaDescripcion : baseState.lastIncidenciaDescripcion,
                        latest != null ? latest.errorMessage : baseState.errorMessage
                );
                _state.postValue(merged);
                loadNextCita(userId, merged);
            }

            @Override
            public void onError(String message) {
                HomeState latest = _state.getValue();
                HomeState merged = new HomeState(
                        latest != null && latest.isLoading,
                        latest != null ? latest.groupName : baseState.groupName,
                        latest != null ? latest.careName : baseState.careName,
                        mergeCareAge(latest, baseState),
                        mergeCarePhoto(latest, baseState),
                        latest != null ? latest.carePhone : baseState.carePhone,
                        latest != null ? latest.careAddress : baseState.careAddress,
                        latest != null ? latest.emergencyContactName : baseState.emergencyContactName,
                        latest != null ? latest.emergencyContactPhone : baseState.emergencyContactPhone,
                        latest != null ? latest.careAllergies : baseState.careAllergies,
                        latest != null ? latest.careConditions : baseState.careConditions,
                        latest != null ? latest.activeGroupId : baseState.activeGroupId,
                        latest != null ? latest.nextMedicacionNombre : "",
                        latest != null ? latest.nextMedicacionHora : "",
                        false,
                        latest != null ? latest.nextCitaTitulo : baseState.nextCitaTitulo,
                        latest != null ? latest.nextCitaHorario : baseState.nextCitaHorario,
                        latest != null ? latest.nextCitaEncargada : baseState.nextCitaEncargada,
                        latest != null && latest.nextCitaRecordatorioActivo,
                        latest != null ? latest.lastIncidenciaTipo : baseState.lastIncidenciaTipo,
                        latest != null ? latest.lastIncidenciaHorario : baseState.lastIncidenciaHorario,
                        latest != null ? latest.lastIncidenciaNivel : baseState.lastIncidenciaNivel,
                        latest != null ? latest.lastIncidenciaDescripcion : baseState.lastIncidenciaDescripcion,
                        latest != null ? latest.errorMessage : baseState.errorMessage
                );
                _state.postValue(merged);
                loadNextCita(userId, merged);
            }
        });
    }

    private void loadCareProfile(HomeState baseState) {
        if (baseState == null || isBlank(baseState.activeGroupId)) {
            return;
        }

        loadCareProfileUseCase.execute(baseState.activeGroupId, new ResultCallback<Group>() {
            @Override
            public void onSuccess(Group result) {
                if (result == null) {
                    return;
                }
                HomeState latest = _state.getValue();
                _state.postValue(new HomeState(
                        latest != null && latest.isLoading,
                        latest != null ? latest.groupName : baseState.groupName,
                        !isBlank(result.getCareName()) ? result.getCareName() : (latest != null ? latest.careName : baseState.careName),
                        result.getCareAge(),
                        resolveCarePhotoSource(result),
                        result.getCarePhone(),
                        result.getCareAddress(),
                        result.getEmergencyContactName(),
                        result.getEmergencyContactPhone(),
                        result.getCareAllergies(),
                        result.getCareConditions(),
                        latest != null ? latest.activeGroupId : baseState.activeGroupId,
                        latest != null ? latest.nextMedicacionNombre : baseState.nextMedicacionNombre,
                        latest != null ? latest.nextMedicacionHora : baseState.nextMedicacionHora,
                        latest != null && latest.nextMedicacionAlertasActivas,
                        latest != null ? latest.nextCitaTitulo : baseState.nextCitaTitulo,
                        latest != null ? latest.nextCitaHorario : baseState.nextCitaHorario,
                        latest != null ? latest.nextCitaEncargada : baseState.nextCitaEncargada,
                        latest != null && latest.nextCitaRecordatorioActivo,
                        latest != null ? latest.lastIncidenciaTipo : baseState.lastIncidenciaTipo,
                        latest != null ? latest.lastIncidenciaHorario : baseState.lastIncidenciaHorario,
                        latest != null ? latest.lastIncidenciaNivel : baseState.lastIncidenciaNivel,
                        latest != null ? latest.lastIncidenciaDescripcion : baseState.lastIncidenciaDescripcion,
                        latest != null ? latest.errorMessage : baseState.errorMessage
                ));
            }

            @Override
            public void onError(String message) {
                // keep Home stable
            }
        });
    }

    private void loadNextCita(String userId, HomeState baseState) {
        loadNextCitaUseCase.execute(userId, new ResultCallback<LoadNextCitaUseCase.Result>() {
            @Override
            public void onSuccess(LoadNextCitaUseCase.Result result) {
                HomeState latest = _state.getValue();
                HomeState merged = new HomeState(
                        latest != null && latest.isLoading,
                        latest != null ? latest.groupName : baseState.groupName,
                        latest != null ? latest.careName : baseState.careName,
                        mergeCareAge(latest, baseState),
                        mergeCarePhoto(latest, baseState),
                        latest != null ? latest.carePhone : baseState.carePhone,
                        latest != null ? latest.careAddress : baseState.careAddress,
                        latest != null ? latest.emergencyContactName : baseState.emergencyContactName,
                        latest != null ? latest.emergencyContactPhone : baseState.emergencyContactPhone,
                        latest != null ? latest.careAllergies : baseState.careAllergies,
                        latest != null ? latest.careConditions : baseState.careConditions,
                        latest != null ? latest.activeGroupId : baseState.activeGroupId,
                        latest != null ? latest.nextMedicacionNombre : baseState.nextMedicacionNombre,
                        latest != null ? latest.nextMedicacionHora : baseState.nextMedicacionHora,
                        latest != null && latest.nextMedicacionAlertasActivas,
                        result != null ? result.getTitulo() : "",
                        result != null ? result.getHorario() : "",
                        result != null ? result.getPersonaEncargada() : "",
                        result != null && result.isRecordatorioActivo(),
                        latest != null ? latest.lastIncidenciaTipo : baseState.lastIncidenciaTipo,
                        latest != null ? latest.lastIncidenciaHorario : baseState.lastIncidenciaHorario,
                        latest != null ? latest.lastIncidenciaNivel : baseState.lastIncidenciaNivel,
                        latest != null ? latest.lastIncidenciaDescripcion : baseState.lastIncidenciaDescripcion,
                        latest != null ? latest.errorMessage : baseState.errorMessage
                );
                _state.postValue(merged);
                loadLatestIncidencia(userId, merged);
            }

            @Override
            public void onError(String message) {
                loadLatestIncidencia(userId, baseState);
            }
        });
    }

    private void loadLatestIncidencia(String userId, HomeState baseState) {
        loadLatestIncidenciaUseCase.execute(userId, new ResultCallback<LoadLatestIncidenciaUseCase.Result>() {
            @Override
            public void onSuccess(LoadLatestIncidenciaUseCase.Result result) {
                HomeState latest = _state.getValue();
                _state.postValue(new HomeState(
                        latest != null && latest.isLoading,
                        latest != null ? latest.groupName : baseState.groupName,
                        latest != null ? latest.careName : baseState.careName,
                        mergeCareAge(latest, baseState),
                        mergeCarePhoto(latest, baseState),
                        latest != null ? latest.carePhone : baseState.carePhone,
                        latest != null ? latest.careAddress : baseState.careAddress,
                        latest != null ? latest.emergencyContactName : baseState.emergencyContactName,
                        latest != null ? latest.emergencyContactPhone : baseState.emergencyContactPhone,
                        latest != null ? latest.careAllergies : baseState.careAllergies,
                        latest != null ? latest.careConditions : baseState.careConditions,
                        latest != null ? latest.activeGroupId : baseState.activeGroupId,
                        latest != null ? latest.nextMedicacionNombre : baseState.nextMedicacionNombre,
                        latest != null ? latest.nextMedicacionHora : baseState.nextMedicacionHora,
                        latest != null && latest.nextMedicacionAlertasActivas,
                        latest != null ? latest.nextCitaTitulo : baseState.nextCitaTitulo,
                        latest != null ? latest.nextCitaHorario : baseState.nextCitaHorario,
                        latest != null ? latest.nextCitaEncargada : baseState.nextCitaEncargada,
                        latest != null && latest.nextCitaRecordatorioActivo,
                        result != null ? result.getTipo() : "",
                        result != null ? result.getHorario() : "",
                        result != null ? result.getNivel() : "",
                        result != null ? result.getDescripcion() : "",
                        latest != null ? latest.errorMessage : baseState.errorMessage
                ));
                loadRecentActivity(userId);
            }

            @Override
            public void onError(String message) {
                loadRecentActivity(userId);
            }
        });
    }

    private void loadRecentActivity(String userId) {
        loadActividadDataUseCase.execute(userId, new ResultCallback<LoadActividadDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadActividadDataUseCase.Result result) {
                List<ActividadItem> items = result != null ? result.getActivities() : new ArrayList<>();
                ActividadItem latest = items.isEmpty() ? null : items.get(0);
                _recentActivity.postValue(new RecentActivityState(
                        items,
                        result != null ? result.getTodayCount() : 0,
                        latest,
                        null
                ));
            }

            @Override
            public void onError(String message) {
                RecentActivityState current = _recentActivity.getValue();
                _recentActivity.postValue(new RecentActivityState(
                        current != null ? current.items : new ArrayList<>(),
                        current != null ? current.todayCount : 0,
                        current != null ? current.latestItem : null,
                        message
                ));
            }
        });
    }

    private void loadHealthSummary(String groupId) {
        if (isBlank(groupId)) {
            _healthSummary.postValue(new HealthSummaryState(0, null, null));
            return;
        }
        loadPatologiasUseCase.execute(groupId, new ResultCallback<List<Patologia>>() {
            @Override
            public void onSuccess(List<Patologia> patologias) {
                final int patologiasCount = patologias != null ? patologias.size() : 0;
                loadSeguimientoUseCase.execute(groupId, new ResultCallback<List<SeguimientoRegistro>>() {
                    @Override
                    public void onSuccess(List<SeguimientoRegistro> registros) {
                        SeguimientoRegistro latest = (registros != null && !registros.isEmpty()) ? registros.get(0) : null;
                        _healthSummary.postValue(new HealthSummaryState(
                                patologiasCount,
                                buildSeguimientoSummary(latest),
                                null
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        _healthSummary.postValue(new HealthSummaryState(
                                patologiasCount,
                                null,
                                message != null ? message : "No se pudo cargar el seguimiento"
                        ));
                    }
                });
            }

            @Override
            public void onError(String message) {
                _healthSummary.postValue(new HealthSummaryState(
                        0,
                        null,
                        message != null ? message : "No se pudieron cargar las patologías"
                ));
            }
        });
    }

    private String buildSeguimientoSummary(SeguimientoRegistro registro) {
        if (registro == null) {
            return null;
        }
        String tipo = safeTrim(registro.getTipo());
        String valorPrincipal = safeTrim(registro.getValorPrincipal());
        String valorSecundario = safeTrim(registro.getValorSecundario());
        String notas = safeTrim(registro.getNotas());
        String valor;
        if (SeguimientoRegistro.TIPO_TENSION.equals(tipo) && !isBlank(valorSecundario)) {
            valor = valorPrincipal + "/" + valorSecundario;
        } else if (SeguimientoRegistro.TIPO_GLUCOSA.equals(tipo)) {
            valor = valorPrincipal + " mg/dL";
        } else if (SeguimientoRegistro.TIPO_TEMPERATURA.equals(tipo)) {
            valor = valorPrincipal + " °C";
        } else if (SeguimientoRegistro.TIPO_PESO.equals(tipo)) {
            valor = valorPrincipal + " kg";
        } else {
            valor = valorPrincipal;
        }
        if (isBlank(valor)) {
            valor = notas;
        } else if (!isBlank(notas)) {
            valor = valor + " · " + notas;
        }
        return valor;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void emitGroupError(String message) {
        HomeState current = _state.getValue();
        _state.postValue(copyState(false, message));
    }

    private Integer mergeCareAge(HomeState latest, HomeState fallback) {
        if (latest != null && latest.careAge != null && latest.careAge > 0) {
            return latest.careAge;
        }
        return fallback != null ? fallback.careAge : null;
    }

    private String mergeCarePhoto(HomeState latest, HomeState fallback) {
        if (fallback == null) {
            return null;
        }
        if (latest == null) {
            return fallback.carePhotoUri;
        }
        if (isBlank(latest.activeGroupId) || isBlank(fallback.activeGroupId)) {
            return fallback.carePhotoUri;
        }
        if (!latest.activeGroupId.equals(fallback.activeGroupId)) {
            return fallback.carePhotoUri;
        }
        if (!isBlank(latest.carePhotoUri)) {
            return latest.carePhotoUri;
        }
        return fallback.carePhotoUri;
    }

    private HomeState copyState(boolean isLoading, String errorMessage) {
        HomeState current = _state.getValue();
        return new HomeState(
                isLoading,
                current != null ? current.groupName : null,
                current != null ? current.careName : null,
                current != null ? current.careAge : null,
                current != null ? current.carePhotoUri : null,
                current != null ? current.carePhone : "",
                current != null ? current.careAddress : "",
                current != null ? current.emergencyContactName : "",
                current != null ? current.emergencyContactPhone : "",
                current != null ? current.careAllergies : "",
                current != null ? current.careConditions : "",
                current != null ? current.activeGroupId : null,
                current != null ? current.nextMedicacionNombre : "",
                current != null ? current.nextMedicacionHora : "",
                current != null && current.nextMedicacionAlertasActivas,
                current != null ? current.nextCitaTitulo : "",
                current != null ? current.nextCitaHorario : "",
                current != null ? current.nextCitaEncargada : "",
                current != null && current.nextCitaRecordatorioActivo,
                current != null ? current.lastIncidenciaTipo : "",
                current != null ? current.lastIncidenciaHorario : "",
                current != null ? current.lastIncidenciaNivel : "",
                current != null ? current.lastIncidenciaDescripcion : "",
                errorMessage
        );
    }

    private String resolveCarePhotoSource(Group group) {
        return null;
    }

    public void onCreateNewGroupClicked() {
        _navigation.setValue(HomeNavigation.GROUP_ENTRY);
    }

    public void onJoinWithCodeClicked() {
        _navigation.setValue(HomeNavigation.GROUP_ENTRY);
    }

    public void performLogout() {
        signOutUseCase.execute(null);
    }

    public void onGroupSelectorHandled() {
        _groupSelectorEvent.setValue(null);
    }

    public void onNavigationHandled() {
        _navigation.setValue(null);
    }

    public void onProximaMedicacionClicked() {
        _action.setValue(new NavigateToMedicacionAction());
    }

    public void onAddMedicacionClicked() {
        _action.setValue(new NavigateToMedicacionAction());
    }

    public void onProximaCitaClicked() {
        _action.setValue(new NavigateToCitasAction());
    }

    public void onCareProfileClicked() {
        HomeState current = _state.getValue();
        _action.setValue(new ShowCareProfileAction(
                current != null ? current.careName : "",
                current != null ? current.careAge : null,
                current != null ? current.carePhotoUri : null,
                current != null ? current.carePhone : "",
                current != null ? current.careAddress : "",
                current != null ? current.emergencyContactName : "",
                current != null ? current.emergencyContactPhone : "",
                current != null ? current.careAllergies : "",
                current != null ? current.careConditions : "",
                current != null ? current.activeGroupId : null
        ));
    }

    public void onActividadClicked() {
        _action.setValue(new NavigateToActividadAction());
    }

    public void onSeguimientoClicked() {
        String activeGroupId = resolveActiveGroupId(_state.getValue());
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        _action.setValue(new NavigateToSeguimientoAction(activeGroupId));
    }

    public void onPatologiasClicked() {
        String activeGroupId = resolveActiveGroupId(_state.getValue());
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        _action.setValue(new NavigateToPatologiasAction(activeGroupId));
    }

    public void onCareProfileSaved(String careName,
                                   Integer careAge,
                                   String carePhotoUri,
                                   String carePhone,
                                   String careAddress,
                                   String emergencyContactName,
                                   String emergencyContactPhone,
                                   String careAllergies,
                                   String careConditions) {
        User currentUser = getCurrentUserUseCase.execute();
        HomeState current = _state.getValue();
        String activeGroupId = resolveActiveGroupId(current);

        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        if (isBlank(careName)) {
            _action.setValue(new ShowMessageAction("El nombre es obligatorio"));
            return;
        }

        _state.setValue(copyState(true, null));
        String trimmedName = careName.trim();
        String trimmedPhone = sanitizeOptional(carePhone);
        String trimmedAddress = sanitizeOptional(careAddress);
        String trimmedEmergencyName = sanitizeOptional(emergencyContactName);
        String trimmedEmergencyPhone = sanitizeOptional(emergencyContactPhone);
        String trimmedAllergies = sanitizeOptional(careAllergies);
        String trimmedConditions = sanitizeOptional(careConditions);
        boolean hasEmergencyName = !isBlank(trimmedEmergencyName);
        boolean hasEmergencyPhone = !isBlank(trimmedEmergencyPhone);
        if (hasEmergencyName != hasEmergencyPhone) {
            _state.setValue(copyState(false, null));
            _action.setValue(new ShowMessageAction("Si rellenas el contacto de emergencia, debes indicar nombre y teléfono"));
            return;
        }
        String localDisplayPhotoUri = isLocalPhotoSource(carePhotoUri) ? carePhotoUri : null;
        persistCareProfile(current, activeGroupId, trimmedName, careAge, localDisplayPhotoUri,
                trimmedPhone, trimmedAddress, trimmedEmergencyName, trimmedEmergencyPhone,
                trimmedAllergies, trimmedConditions);
    }

    private void persistCareProfile(HomeState current,
                                    String activeGroupId,
                                    String careName,
                                    Integer careAge,
                                    String localDisplayPhotoUri,
                                    String carePhone,
                                    String careAddress,
                                    String emergencyContactName,
                                    String emergencyContactPhone,
                                    String careAllergies,
                                    String careConditions) {
        updateCareProfileUseCase.execute(
                activeGroupId,
                careName,
                careAge,
                null,
                null,
                carePhone,
                careAddress,
                emergencyContactName,
                emergencyContactPhone,
                careAllergies,
                careConditions,
                new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                HomeState latest = _state.getValue();
                _state.postValue(new HomeState(
                        false,
                        latest != null ? latest.groupName : current.groupName,
                        careName,
                        careAge,
                        localDisplayPhotoUri,
                        carePhone,
                        careAddress,
                        emergencyContactName,
                        emergencyContactPhone,
                        careAllergies,
                        careConditions,
                        latest != null ? latest.activeGroupId : activeGroupId,
                        latest != null ? latest.nextMedicacionNombre : current.nextMedicacionNombre,
                        latest != null ? latest.nextMedicacionHora : current.nextMedicacionHora,
                        latest != null && latest.nextMedicacionAlertasActivas,
                        latest != null ? latest.nextCitaTitulo : current.nextCitaTitulo,
                        latest != null ? latest.nextCitaHorario : current.nextCitaHorario,
                        latest != null ? latest.nextCitaEncargada : current.nextCitaEncargada,
                        latest != null && latest.nextCitaRecordatorioActivo,
                        latest != null ? latest.lastIncidenciaTipo : current.lastIncidenciaTipo,
                        latest != null ? latest.lastIncidenciaHorario : current.lastIncidenciaHorario,
                        latest != null ? latest.lastIncidenciaNivel : current.lastIncidenciaNivel,
                        latest != null ? latest.lastIncidenciaDescripcion : current.lastIncidenciaDescripcion,
                        null
                ));
                _action.postValue(new ShowMessageAction("Perfil actualizado"));
            }

            @Override
            public void onError(String message) {
                _state.postValue(copyState(false, message != null ? message : "No se pudo actualizar el perfil"));
            }
        });
    }

    public void onUltimaIncidenciaClicked() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        loadIncidenciasUseCase.execute(currentUser.getId(), new ResultCallback<List<Incidencia>>() {
            @Override
            public void onSuccess(List<Incidencia> incidencias) {
                _action.postValue(new ShowIncidenciasHistoryAction(
                        incidencias != null ? incidencias : new ArrayList<>()
                ));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction("No se pudieron cargar las incidencias"));
            }
        });
    }

    public void onDeleteIncidenciaClicked(Incidencia incidencia) {
        if (incidencia == null || isBlank(incidencia.getId())) {
            _action.setValue(new ShowMessageAction("No se pudo eliminar la incidencia"));
            return;
        }
        _action.setValue(new ConfirmDeleteIncidenciaAction(incidencia));
    }

    public void confirmDeleteIncidencia(Incidencia incidencia) {
        HomeState current = _state.getValue();
        String activeGroupId = resolveActiveGroupId(current);
        if (incidencia == null || isBlank(incidencia.getId()) || isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No se pudo eliminar la incidencia"));
            return;
        }
        deleteIncidenciaUseCase.execute(activeGroupId, incidencia.getId(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Incidencia eliminada"));
                refreshHomeCards();
                User currentUser = getCurrentUserUseCase.execute();
                if (currentUser != null && !isBlank(currentUser.getId())) {
                    onUltimaIncidenciaClicked();
                }
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo eliminar la incidencia"));
            }
        });
    }

    public void onExportIncidenciasPdfClicked(List<Incidencia> incidencias) {
        if (incidencias == null || incidencias.isEmpty()) {
            _action.setValue(new ShowMessageAction("No hay incidencias para exportar"));
            return;
        }
        exportIncidenciasPdfUseCase.execute(incidencias, new ResultCallback<com.silveira.care360.domain.report.IncidenciasPdfExporter.Result>() {
            @Override
            public void onSuccess(com.silveira.care360.domain.report.IncidenciasPdfExporter.Result result) {
                _action.postValue(new ShareIncidenciasPdfAction(result.getUri(), result.getFileName()));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo generar el PDF"));
            }
        });
    }

    public void onRegistrarIncidenciaClicked() {
        _action.setValue(new ShowIncidenciaEditorAction());
    }

    public void onIncidenciaEditorConfirmed(String tipo,
                                            String fecha,
                                            String hora,
                                            String nivel,
                                            String descripcion) {
        User currentUser = getCurrentUserUseCase.execute();
        HomeState current = _state.getValue();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        String activeGroupId = resolveActiveGroupId(current);
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        if (isBlank(tipo) || isBlank(fecha) || isBlank(hora) || isBlank(nivel)) {
            _action.setValue(new ShowMessageAction("Completa tipo, fecha, hora y nivel"));
            return;
        }
        if (isFutureIncidencia(fecha, hora)) {
            _action.setValue(new ShowMessageAction("La incidencia no puede registrarse en una fecha u hora futura"));
            return;
        }

        _state.setValue(copyState(true, null));
        saveIncidenciaUseCase.execute(
                activeGroupId,
                currentUser.getId(),
                tipo,
                fecha,
                hora,
                nivel,
                descripcion,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        HomeState latest = _state.getValue();
                        _state.postValue(new HomeState(
                                false,
                                latest != null ? latest.groupName : current.groupName,
                                latest != null ? latest.careName : current.careName,
                                mergeCareAge(latest, current),
                                mergeCarePhoto(latest, current),
                                latest != null ? latest.carePhone : current.carePhone,
                                latest != null ? latest.careAddress : current.careAddress,
                                latest != null ? latest.emergencyContactName : current.emergencyContactName,
                                latest != null ? latest.emergencyContactPhone : current.emergencyContactPhone,
                                latest != null ? latest.careAllergies : current.careAllergies,
                                latest != null ? latest.careConditions : current.careConditions,
                                latest != null ? latest.activeGroupId : activeGroupId,
                                latest != null ? latest.nextMedicacionNombre : current.nextMedicacionNombre,
                                latest != null ? latest.nextMedicacionHora : current.nextMedicacionHora,
                                latest != null && latest.nextMedicacionAlertasActivas,
                                latest != null ? latest.nextCitaTitulo : current.nextCitaTitulo,
                                latest != null ? latest.nextCitaHorario : current.nextCitaHorario,
                                latest != null ? latest.nextCitaEncargada : current.nextCitaEncargada,
                                latest != null && latest.nextCitaRecordatorioActivo,
                                tipo.trim(),
                                buildIncidenciaHorario(fecha, hora),
                                nivel.trim(),
                                descripcion != null ? descripcion.trim() : "",
                                null
                        ));
                        _action.postValue(new ShowMessageAction("Incidencia registrada"));
                        refreshHomeCards();
                    }

                    @Override
                    public void onError(String message) {
                        _state.postValue(copyState(false, message != null ? message : "No se pudo guardar la incidencia"));
                    }
                }
        );
    }

    private String resolveActiveGroupId(HomeState current) {
        if (current != null && !isBlank(current.activeGroupId)) {
            return current.activeGroupId;
        }
        if (cachedMemberships == null || cachedMemberships.isEmpty() || current == null) {
            return null;
        }
        for (GroupMember member : cachedMemberships) {
            if (member == null) continue;
            String groupName = member.getGroupName();
            if (!isBlank(groupName) && groupName.equalsIgnoreCase(current.groupName)) {
                return member.getGroupId();
            }
        }
        if (cachedMemberships.size() == 1) {
            return cachedMemberships.get(0).getGroupId();
        }
        return null;
    }

    private GroupMember findMembershipByGroupId(List<GroupMember> memberships, String groupId) {
        if (memberships == null || isBlank(groupId)) {
            return null;
        }
        for (GroupMember member : memberships) {
            if (member != null && groupId.equals(member.getGroupId())) {
                return member;
            }
        }
        return null;
    }

    public void refreshNextMedicacion() {
        refreshHomeCards();
    }

    public void refreshHomeCards() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || currentUser.getId() == null || currentUser.getId().trim().isEmpty()) {
            return;
        }
        HomeState current = _state.getValue();
        if (current == null || (isBlank(current.groupName) && isBlank(current.activeGroupId))) {
            return;
        }
        loadCareProfile(current);
        loadHealthSummary(current.activeGroupId);
        loadNextMedicacionUseCase.execute(currentUser.getId(), new ResultCallback<LoadNextMedicacionUseCase.Result>() {
            @Override
            public void onSuccess(LoadNextMedicacionUseCase.Result result) {
                HomeState latest = _state.getValue();
                HomeState merged = new HomeState(
                        latest != null && latest.isLoading,
                        latest != null ? latest.groupName : current.groupName,
                        latest != null ? latest.careName : current.careName,
                        mergeCareAge(latest, current),
                        mergeCarePhoto(latest, current),
                        latest != null ? latest.carePhone : current.carePhone,
                        latest != null ? latest.careAddress : current.careAddress,
                        latest != null ? latest.emergencyContactName : current.emergencyContactName,
                        latest != null ? latest.emergencyContactPhone : current.emergencyContactPhone,
                        latest != null ? latest.careAllergies : current.careAllergies,
                        latest != null ? latest.careConditions : current.careConditions,
                        latest != null ? latest.activeGroupId : current.activeGroupId,
                        result != null ? result.getNombre() : "",
                        result != null ? result.getHorario() : "",
                        result != null && result.isAlertasActivas(),
                        latest != null ? latest.nextCitaTitulo : current.nextCitaTitulo,
                        latest != null ? latest.nextCitaHorario : current.nextCitaHorario,
                        latest != null ? latest.nextCitaEncargada : current.nextCitaEncargada,
                        latest != null && latest.nextCitaRecordatorioActivo,
                        latest != null ? latest.lastIncidenciaTipo : current.lastIncidenciaTipo,
                        latest != null ? latest.lastIncidenciaHorario : current.lastIncidenciaHorario,
                        latest != null ? latest.lastIncidenciaNivel : current.lastIncidenciaNivel,
                        latest != null ? latest.lastIncidenciaDescripcion : current.lastIncidenciaDescripcion,
                        latest != null ? latest.errorMessage : current.errorMessage
                );
                _state.postValue(merged);
                loadNextCita(currentUser.getId(), merged);
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private boolean isFutureIncidencia(String fecha, String hora) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formatter.setLenient(false);
            Date parsed = formatter.parse(fecha.trim() + " " + hora.trim());
            return parsed != null && parsed.getTime() > System.currentTimeMillis();
        } catch (Exception ignored) {
            return false;
        }
    }

    private String buildIncidenciaHorario(String fecha, String hora) {
        boolean hasFecha = !isBlank(fecha);
        boolean hasHora = !isBlank(hora);
        if (hasFecha && hasHora) {
            return fecha.trim() + " · " + hora.trim();
        }
        if (hasFecha) {
            return fecha.trim();
        }
        if (hasHora) {
            return hora.trim();
        }
        return "";
    }

    private boolean isRemotePhotoSource(String value) {
        if (isBlank(value)) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    private boolean isLocalPhotoSource(String value) {
        if (isBlank(value)) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("content://") || normalized.startsWith("file://");
    }

    private String sanitizeOptional(String value) {
        return isBlank(value) ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class HomeState {
        public final boolean isLoading;
        public final String groupName;
        public final String careName;
        public final Integer careAge;
        public final String carePhotoUri;
        public final String carePhone;
        public final String careAddress;
        public final String emergencyContactName;
        public final String emergencyContactPhone;
        public final String careAllergies;
        public final String careConditions;
        public final String activeGroupId;
        public final String nextMedicacionNombre;
        public final String nextMedicacionHora;
        public final boolean nextMedicacionAlertasActivas;
        public final String nextCitaTitulo;
        public final String nextCitaHorario;
        public final String nextCitaEncargada;
        public final boolean nextCitaRecordatorioActivo;
        public final String lastIncidenciaTipo;
        public final String lastIncidenciaHorario;
        public final String lastIncidenciaNivel;
        public final String lastIncidenciaDescripcion;
        public final String errorMessage;

        public HomeState() {
            this(false, null, null, null, null, "", "", "", "", "", "", null, "", "", false, "", "", "", false, "", "", "", "", null);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, "", "", "", "", null,
                    "", "",
                    nextMedicacionNombre, nextMedicacionHora, nextMedicacionAlertasActivas, nextCitaTitulo,
                    nextCitaHorario, "", nextCitaRecordatorioActivo, lastIncidenciaTipo, lastIncidenciaHorario,
                    lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         String nextCitaEncargada,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, "", "", "", "", null,
                    "", "",
                    nextMedicacionNombre, nextMedicacionHora, nextMedicacionAlertasActivas, nextCitaTitulo,
                    nextCitaHorario, nextCitaEncargada, nextCitaRecordatorioActivo, lastIncidenciaTipo, lastIncidenciaHorario,
                    lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String carePhone,
                         String careAddress,
                         String emergencyContactName,
                         String emergencyContactPhone,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, carePhone, careAddress, emergencyContactName, emergencyContactPhone, null, nextMedicacionNombre, nextMedicacionHora,
                    "", "",
                    nextMedicacionAlertasActivas, nextCitaTitulo, nextCitaHorario, "", nextCitaRecordatorioActivo,
                    lastIncidenciaTipo, lastIncidenciaHorario, lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String carePhone,
                         String careAddress,
                         String emergencyContactName,
                         String emergencyContactPhone,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         String nextCitaEncargada,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, carePhone, careAddress, emergencyContactName, emergencyContactPhone, null, nextMedicacionNombre, nextMedicacionHora,
                    "", "",
                    nextMedicacionAlertasActivas, nextCitaTitulo, nextCitaHorario, nextCitaEncargada, nextCitaRecordatorioActivo,
                    lastIncidenciaTipo, lastIncidenciaHorario, lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String activeGroupId,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, "", "", "", "", activeGroupId,
                    "", "",
                    nextMedicacionNombre, nextMedicacionHora, nextMedicacionAlertasActivas, nextCitaTitulo,
                    nextCitaHorario, "", nextCitaRecordatorioActivo, lastIncidenciaTipo, lastIncidenciaHorario,
                    lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String activeGroupId,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         String nextCitaEncargada,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, "", "", "", "", activeGroupId,
                    "", "",
                    nextMedicacionNombre, nextMedicacionHora, nextMedicacionAlertasActivas, nextCitaTitulo,
                    nextCitaHorario, nextCitaEncargada, nextCitaRecordatorioActivo, lastIncidenciaTipo, lastIncidenciaHorario,
                    lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String carePhone,
                         String careAddress,
                         String emergencyContactName,
                         String emergencyContactPhone,
                         String activeGroupId,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, carePhone, careAddress, emergencyContactName, emergencyContactPhone, "", "", activeGroupId,
                    nextMedicacionNombre, nextMedicacionHora, nextMedicacionAlertasActivas, nextCitaTitulo, nextCitaHorario, "",
                    nextCitaRecordatorioActivo, lastIncidenciaTipo, lastIncidenciaHorario, lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String carePhone,
                         String careAddress,
                         String emergencyContactName,
                         String emergencyContactPhone,
                         String activeGroupId,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         String nextCitaEncargada,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, carePhone, careAddress, emergencyContactName, emergencyContactPhone, "", "", activeGroupId,
                    nextMedicacionNombre, nextMedicacionHora, nextMedicacionAlertasActivas, nextCitaTitulo, nextCitaHorario, nextCitaEncargada,
                    nextCitaRecordatorioActivo, lastIncidenciaTipo, lastIncidenciaHorario, lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String carePhone,
                         String careAddress,
                         String emergencyContactName,
                         String emergencyContactPhone,
                         String careAllergies,
                         String careConditions,
                         String activeGroupId,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this(isLoading, groupName, careName, careAge, carePhotoUri, carePhone, careAddress, emergencyContactName, emergencyContactPhone, careAllergies, careConditions, activeGroupId,
                    nextMedicacionNombre, nextMedicacionHora, nextMedicacionAlertasActivas, nextCitaTitulo, nextCitaHorario, "",
                    nextCitaRecordatorioActivo, lastIncidenciaTipo, lastIncidenciaHorario, lastIncidenciaNivel, lastIncidenciaDescripcion, errorMessage);
        }

        public HomeState(boolean isLoading,
                         String groupName,
                         String careName,
                         Integer careAge,
                         String carePhotoUri,
                         String carePhone,
                         String careAddress,
                         String emergencyContactName,
                         String emergencyContactPhone,
                         String careAllergies,
                         String careConditions,
                         String activeGroupId,
                         String nextMedicacionNombre,
                         String nextMedicacionHora,
                         boolean nextMedicacionAlertasActivas,
                         String nextCitaTitulo,
                         String nextCitaHorario,
                         String nextCitaEncargada,
                         boolean nextCitaRecordatorioActivo,
                         String lastIncidenciaTipo,
                         String lastIncidenciaHorario,
                         String lastIncidenciaNivel,
                         String lastIncidenciaDescripcion,
                         String errorMessage) {
            this.isLoading = isLoading;
            this.groupName = groupName;
            this.careName = careName;
            this.careAge = careAge;
            this.carePhotoUri = carePhotoUri;
            this.carePhone = carePhone;
            this.careAddress = careAddress;
            this.emergencyContactName = emergencyContactName;
            this.emergencyContactPhone = emergencyContactPhone;
            this.careAllergies = careAllergies;
            this.careConditions = careConditions;
            this.activeGroupId = activeGroupId;
            this.nextMedicacionNombre = nextMedicacionNombre;
            this.nextMedicacionHora = nextMedicacionHora;
            this.nextMedicacionAlertasActivas = nextMedicacionAlertasActivas;
            this.nextCitaTitulo = nextCitaTitulo;
            this.nextCitaHorario = nextCitaHorario;
            this.nextCitaEncargada = nextCitaEncargada;
            this.nextCitaRecordatorioActivo = nextCitaRecordatorioActivo;
            this.lastIncidenciaTipo = lastIncidenciaTipo;
            this.lastIncidenciaHorario = lastIncidenciaHorario;
            this.lastIncidenciaNivel = lastIncidenciaNivel;
            this.lastIncidenciaDescripcion = lastIncidenciaDescripcion;
            this.errorMessage = errorMessage;
        }
    }

    public static class GroupSelectorState {
        public final List<GroupMember> memberships;
        public final boolean mandatory;

        public GroupSelectorState(List<GroupMember> memberships, boolean mandatory) {
            this.memberships = memberships;
            this.mandatory = mandatory;
        }
    }

    public enum HomeNavigation {
        GROUP_ENTRY,
        WELCOME
    }

    public static abstract class HomeAction { }

    public static class NavigateToMedicacionAction extends HomeAction { }
    public static class NavigateToCitasAction extends HomeAction { }
    public static class NavigateToActividadAction extends HomeAction { }
    public static class NavigateToSeguimientoAction extends HomeAction {
        public final String groupId;

        public NavigateToSeguimientoAction(String groupId) {
            this.groupId = groupId;
        }
    }
    public static class NavigateToPatologiasAction extends HomeAction {
        public final String groupId;

        public NavigateToPatologiasAction(String groupId) {
            this.groupId = groupId;
        }
    }
    public static class ShowCareProfileAction extends HomeAction {
        public final String careName;
        public final Integer careAge;
        public final String carePhotoUri;
        public final String carePhone;
        public final String careAddress;
        public final String emergencyContactName;
        public final String emergencyContactPhone;
        public final String careAllergies;
        public final String careConditions;
        public final String groupId;

        public ShowCareProfileAction(String careName, Integer careAge, String carePhotoUri,
                                     String carePhone, String careAddress,
                                     String emergencyContactName, String emergencyContactPhone,
                                     String careAllergies, String careConditions,
                                     String groupId) {
            this.careName = careName;
            this.careAge = careAge;
            this.carePhotoUri = carePhotoUri;
            this.carePhone = carePhone;
            this.careAddress = careAddress;
            this.emergencyContactName = emergencyContactName;
            this.emergencyContactPhone = emergencyContactPhone;
            this.careAllergies = careAllergies;
            this.careConditions = careConditions;
            this.groupId = groupId;
        }
    }
    public static class ShowIncidenciaEditorAction extends HomeAction { }
    public static class ShowIncidenciasHistoryAction extends HomeAction {
        public final List<Incidencia> incidencias;

        public ShowIncidenciasHistoryAction(List<Incidencia> incidencias) {
            this.incidencias = incidencias;
        }
    }

    public static class ShareIncidenciasPdfAction extends HomeAction {
        public final Uri uri;
        public final String fileName;

        public ShareIncidenciasPdfAction(Uri uri, String fileName) {
            this.uri = uri;
            this.fileName = fileName;
        }
    }

    public static class ConfirmDeleteIncidenciaAction extends HomeAction {
        public final Incidencia incidencia;

        public ConfirmDeleteIncidenciaAction(Incidencia incidencia) {
            this.incidencia = incidencia;
        }
    }

    public static class ShowMessageAction extends HomeAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }

    public static class RecentActivityState {
        public final List<ActividadItem> items;
        public final int todayCount;
        public final ActividadItem latestItem;
        public final String errorMessage;

        public RecentActivityState() {
            this(new ArrayList<>(), 0, null, null);
        }

        public RecentActivityState(List<ActividadItem> items, int todayCount, ActividadItem latestItem, String errorMessage) {
            this.items = items != null ? items : new ArrayList<>();
            this.todayCount = todayCount;
            this.latestItem = latestItem;
            this.errorMessage = errorMessage;
        }
    }

    public static class HealthSummaryState {
        public final int patologiasCount;
        public final String seguimientoResumen;
        public final String errorMessage;

        public HealthSummaryState() {
            this(0, null, null);
        }

        public HealthSummaryState(int patologiasCount, String seguimientoResumen, String errorMessage) {
            this.patologiasCount = patologiasCount;
            this.seguimientoResumen = seguimientoResumen;
            this.errorMessage = errorMessage;
        }
    }
}
