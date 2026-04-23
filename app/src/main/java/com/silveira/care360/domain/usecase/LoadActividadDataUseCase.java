package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.ActividadItem;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.model.Patologia;
import com.silveira.care360.domain.model.SeguimientoRegistro;
import com.silveira.care360.domain.repository.CitaRepository;
import com.silveira.care360.domain.repository.GroupRepository;
import com.silveira.care360.domain.repository.IncidenciaRepository;
import com.silveira.care360.domain.repository.MedicamentoRepository;
import com.silveira.care360.domain.repository.PatologiaRepository;
import com.silveira.care360.domain.repository.SeguimientoRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

public class LoadActividadDataUseCase {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final CitaRepository citaRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final PatologiaRepository patologiaRepository;
    private final SeguimientoRepository seguimientoRepository;

    @Inject
    public LoadActividadDataUseCase(UserRepository userRepository,
                                    GroupRepository groupRepository,
                                    MedicamentoRepository medicamentoRepository,
                                    CitaRepository citaRepository,
                                    IncidenciaRepository incidenciaRepository,
                                    PatologiaRepository patologiaRepository,
                                    SeguimientoRepository seguimientoRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.citaRepository = citaRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.patologiaRepository = patologiaRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (isBlank(groupId)) {
                    callback.onSuccess(new Result(null, new ArrayList<>(), 0));
                    return;
                }
                loadMembersThenActivity(groupId.trim(), callback);
            }

            @Override
            public void onError(String message) {
                callback.onError("No se pudo cargar la actividad");
            }
        });
    }

    private void loadMembersThenActivity(String groupId, ResultCallback<Result> callback) {
        groupRepository.getGroupMembers(groupId, new ResultCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> members) {
                loadMedicacion(groupId, buildActorMap(members), callback);
            }

            @Override
            public void onError(String message) {
                loadMedicacion(groupId, new HashMap<>(), callback);
            }
        });
    }

    private void loadMedicacion(String groupId,
                                Map<String, String> actorMap,
                                ResultCallback<Result> callback) {
        medicamentoRepository.getMedicamentosByGroupId(groupId, new ResultCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> medicamentos) {
                loadCitas(groupId, actorMap, medicamentos != null ? medicamentos : new ArrayList<>(), callback);
            }

            @Override
            public void onError(String message) {
                loadCitas(groupId, actorMap, new ArrayList<>(), callback);
            }
        });
    }

    private void loadCitas(String groupId,
                           Map<String, String> actorMap,
                           List<Medicamento> medicamentos,
                           ResultCallback<Result> callback) {
        citaRepository.getCitasByGroupId(groupId, new ResultCallback<List<Cita>>() {
            @Override
            public void onSuccess(List<Cita> citas) {
                loadIncidencias(groupId, actorMap, medicamentos, citas != null ? citas : new ArrayList<>(), callback);
            }

            @Override
            public void onError(String message) {
                loadIncidencias(groupId, actorMap, medicamentos, new ArrayList<>(), callback);
            }
        });
    }

    private void loadIncidencias(String groupId,
                                 Map<String, String> actorMap,
                                 List<Medicamento> medicamentos,
                                 List<Cita> citas,
                                 ResultCallback<Result> callback) {
        incidenciaRepository.getIncidenciasByGroupId(groupId, new ResultCallback<List<Incidencia>>() {
            @Override
            public void onSuccess(List<Incidencia> incidencias) {
                loadPatologias(groupId, actorMap, medicamentos, citas,
                        incidencias != null ? incidencias : new ArrayList<>(), callback);
            }

            @Override
            public void onError(String message) {
                loadPatologias(groupId, actorMap, medicamentos, citas, new ArrayList<>(), callback);
            }
        });
    }

    private void loadPatologias(String groupId,
                                Map<String, String> actorMap,
                                List<Medicamento> medicamentos,
                                List<Cita> citas,
                                List<Incidencia> incidencias,
                                ResultCallback<Result> callback) {
        patologiaRepository.loadPatologiasIncludingDeleted(groupId, new ResultCallback<List<Patologia>>() {
            @Override
            public void onSuccess(List<Patologia> patologias) {
                loadSeguimiento(groupId, actorMap, medicamentos, citas, incidencias,
                        patologias != null ? patologias : new ArrayList<>(), callback);
            }

            @Override
            public void onError(String message) {
                loadSeguimiento(groupId, actorMap, medicamentos, citas, incidencias, new ArrayList<>(), callback);
            }
        });
    }

    private void loadSeguimiento(String groupId,
                                 Map<String, String> actorMap,
                                 List<Medicamento> medicamentos,
                                 List<Cita> citas,
                                 List<Incidencia> incidencias,
                                 List<Patologia> patologias,
                                 ResultCallback<Result> callback) {
        seguimientoRepository.loadRegistrosIncludingDeleted(groupId, new ResultCallback<List<SeguimientoRegistro>>() {
            @Override
            public void onSuccess(List<SeguimientoRegistro> registros) {
                List<ActividadItem> items = new ArrayList<>();
                appendMedicacion(items, medicamentos, actorMap);
                appendCitas(items, citas, actorMap);
                appendIncidencias(items, incidencias, actorMap);
                appendPatologias(items, patologias, actorMap);
                appendSeguimiento(items, registros != null ? registros : new ArrayList<>(), actorMap);
                Collections.sort(items, (left, right) -> Long.compare(right.getTimestamp(), left.getTimestamp()));
                callback.onSuccess(new Result(groupId, items, countToday(items)));
            }

            @Override
            public void onError(String message) {
                List<ActividadItem> items = new ArrayList<>();
                appendMedicacion(items, medicamentos, actorMap);
                appendCitas(items, citas, actorMap);
                appendIncidencias(items, incidencias, actorMap);
                appendPatologias(items, patologias, actorMap);
                Collections.sort(items, (left, right) -> Long.compare(right.getTimestamp(), left.getTimestamp()));
                callback.onSuccess(new Result(groupId, items, countToday(items)));
            }
        });
    }

    private void appendMedicacion(List<ActividadItem> target,
                                  List<Medicamento> medicamentos,
                                  Map<String, String> actorMap) {
        if (medicamentos == null) return;
        for (Medicamento medicamento : medicamentos) {
            if (medicamento == null) continue;
            String actorId = resolveActorId(medicamento.getCreatedBy(), medicamento.getUpdatedBy(), medicamento.getCreatedAt(), medicamento.getUpdatedAt());
            long timestamp = resolveTimestamp(medicamento.getCreatedAt(), medicamento.getUpdatedAt());
            if (timestamp <= 0L) continue;
            target.add(new ActividadItem(
                    safeId(medicamento.getId(), "med"),
                    ActividadItem.TYPE_MEDICACION,
                    medicamento.isDeleted()
                            ? ActividadItem.ACTION_DELETED
                            : resolveActionType(medicamento.getCreatedAt(), medicamento.getUpdatedAt()),
                    actorId,
                    resolveActorName(actorId, actorMap),
                    textOrFallback(medicamento.getNombre(), "Medicacion"),
                    timestamp
            ));
        }
    }

    private void appendCitas(List<ActividadItem> target,
                             List<Cita> citas,
                             Map<String, String> actorMap) {
        if (citas == null) return;
        for (Cita cita : citas) {
            if (cita == null) continue;
            String actorId = resolveActorId(cita.getCreatedBy(), cita.getUpdatedBy(), cita.getCreatedAt(), cita.getUpdatedAt());
            long timestamp = resolveTimestamp(cita.getCreatedAt(), cita.getUpdatedAt());
            if (timestamp <= 0L) continue;
            target.add(new ActividadItem(
                    safeId(cita.getId(), "cita"),
                    ActividadItem.TYPE_CITA,
                    resolveActionType(cita.getCreatedAt(), cita.getUpdatedAt()),
                    actorId,
                    resolveActorName(actorId, actorMap),
                    textOrFallback(cita.getTitulo(), "Cita"),
                    timestamp
            ));
        }
    }

    private void appendIncidencias(List<ActividadItem> target,
                                   List<Incidencia> incidencias,
                                   Map<String, String> actorMap) {
        if (incidencias == null) return;
        for (Incidencia incidencia : incidencias) {
            if (incidencia == null) continue;
            String actorId = resolveActorId(incidencia.getCreatedBy(), incidencia.getUpdatedBy(), incidencia.getCreatedAt(), incidencia.getUpdatedAt());
            long timestamp = resolveTimestamp(incidencia.getCreatedAt(), incidencia.getUpdatedAt());
            if (timestamp <= 0L) continue;
            target.add(new ActividadItem(
                    safeId(incidencia.getId(), "inc"),
                    ActividadItem.TYPE_INCIDENCIA,
                    resolveActionType(incidencia.getCreatedAt(), incidencia.getUpdatedAt()),
                    actorId,
                    resolveActorName(actorId, actorMap),
                    textOrFallback(incidencia.getTipo(), "Incidencia"),
                    timestamp
            ));
        }
    }

    private void appendPatologias(List<ActividadItem> target,
                                  List<Patologia> patologias,
                                  Map<String, String> actorMap) {
        if (patologias == null) return;
        for (Patologia patologia : patologias) {
            if (patologia == null) continue;
            String actorId = resolveActorId(patologia.getCreatedBy(), patologia.getUpdatedBy(),
                    patologia.getCreatedAt(), patologia.getUpdatedAt());
            long timestamp = resolveTimestamp(patologia.getCreatedAt(), patologia.getUpdatedAt());
            if (timestamp <= 0L) continue;
            target.add(new ActividadItem(
                    safeId(patologia.getId(), "pat"),
                    ActividadItem.TYPE_PATOLOGIA,
                    patologia.isDeleted()
                            ? ActividadItem.ACTION_DELETED
                            : resolveActionType(patologia.getCreatedAt(), patologia.getUpdatedAt()),
                    actorId,
                    resolveActorName(actorId, actorMap),
                    textOrFallback(patologia.getNombre(), "Patologia"),
                    timestamp
            ));
        }
    }

    private void appendSeguimiento(List<ActividadItem> target,
                                   List<SeguimientoRegistro> registros,
                                   Map<String, String> actorMap) {
        if (registros == null) return;
        for (SeguimientoRegistro registro : registros) {
            if (registro == null) continue;
            String actorId = resolveActorId(registro.getCreatedBy(), registro.getUpdatedBy(),
                    registro.getCreatedAt(), registro.getUpdatedAt());
            long timestamp = resolveTimestamp(registro.getCreatedAt(), registro.getUpdatedAt());
            if (timestamp <= 0L) continue;
            target.add(new ActividadItem(
                    safeId(registro.getId(), "seg"),
                    ActividadItem.TYPE_SEGUIMIENTO,
                    registro.isDeleted()
                            ? ActividadItem.ACTION_DELETED
                            : resolveActionType(registro.getCreatedAt(), registro.getUpdatedAt()),
                    actorId,
                    resolveActorName(actorId, actorMap),
                    buildSeguimientoTitle(registro),
                    timestamp
            ));
        }
    }

    private Map<String, String> buildActorMap(List<GroupMember> members) {
        Map<String, String> actorMap = new HashMap<>();
        if (members == null) return actorMap;
        for (GroupMember member : members) {
            if (member == null) continue;
            String displayName = textOrFallback(member.getName(), textOrFallback(member.getEmail(), "Alguien"));
            if (!isBlank(member.getUserId())) {
                actorMap.put(member.getUserId().trim(), displayName);
            }
            if (!isBlank(member.getId())) {
                actorMap.put(member.getId().trim(), displayName);
            }
        }
        return actorMap;
    }

    private int countToday(List<ActividadItem> items) {
        if (items == null || items.isEmpty()) return 0;
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        long startMillis = start.getTimeInMillis();
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);
        long endMillis = end.getTimeInMillis();
        int count = 0;
        for (ActividadItem item : items) {
            long ts = item != null ? item.getTimestamp() : 0L;
            if (ts >= startMillis && ts < endMillis) {
                count++;
            }
        }
        return count;
    }

    private String resolveActionType(long createdAt, long updatedAt) {
        return updatedAt > createdAt && updatedAt > 0L
                ? ActividadItem.ACTION_UPDATED
                : ActividadItem.ACTION_CREATED;
    }

    private String resolveActorId(String createdBy, String updatedBy, long createdAt, long updatedAt) {
        if (updatedAt > createdAt && !isBlank(updatedBy)) {
            return updatedBy.trim();
        }
        if (!isBlank(createdBy)) {
            return createdBy.trim();
        }
        return !isBlank(updatedBy) ? updatedBy.trim() : "";
    }

    private long resolveTimestamp(long createdAt, long updatedAt) {
        return updatedAt > createdAt ? updatedAt : createdAt;
    }

    private String resolveActorName(String actorId, Map<String, String> actorMap) {
        if (!isBlank(actorId) && actorMap.containsKey(actorId.trim())) {
            return actorMap.get(actorId.trim());
        }
        return "Alguien";
    }

    private String textOrFallback(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String safeId(String id, String prefix) {
        return isBlank(id) ? prefix + "_" + System.nanoTime() : id.trim();
    }

    private String buildSeguimientoTitle(SeguimientoRegistro registro) {
        if (registro == null) {
            return "Seguimiento";
        }
        String tipo = textOrFallback(registro.getTipo(), "Seguimiento");
        String valorPrincipal = textOrFallback(registro.getValorPrincipal(), "");
        String valorSecundario = textOrFallback(registro.getValorSecundario(), "");
        StringBuilder builder = new StringBuilder(tipo);
        if (!isBlank(valorPrincipal)) {
            builder.append(": ").append(valorPrincipal);
            if (!isBlank(valorSecundario)) {
                builder.append("/").append(valorSecundario);
            }
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class Result {
        private final String activeGroupId;
        private final List<ActividadItem> activities;
        private final int todayCount;

        public Result(String activeGroupId, List<ActividadItem> activities, int todayCount) {
            this.activeGroupId = activeGroupId;
            this.activities = activities != null ? activities : new ArrayList<>();
            this.todayCount = todayCount;
        }

        public String getActiveGroupId() {
            return activeGroupId;
        }

        public List<ActividadItem> getActivities() {
            return activities;
        }

        public int getTodayCount() {
            return todayCount;
        }
    }
}
