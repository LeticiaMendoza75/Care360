package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.domain.repository.IncidenciaRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

public class LoadLatestIncidenciaUseCase {

    private final UserRepository userRepository;
    private final IncidenciaRepository incidenciaRepository;

    @Inject
    public LoadLatestIncidenciaUseCase(UserRepository userRepository,
                                       IncidenciaRepository incidenciaRepository) {
        this.userRepository = userRepository;
        this.incidenciaRepository = incidenciaRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onSuccess(Result.empty());
                    return;
                }
                incidenciaRepository.getIncidenciasByGroupId(groupId, new ResultCallback<List<Incidencia>>() {
                    @Override
                    public void onSuccess(List<Incidencia> incidencias) {
                        if (incidencias == null || incidencias.isEmpty()) {
                            callback.onSuccess(Result.empty());
                            return;
                        }
                        Incidencia incidencia = incidencias.get(0);
                        String horario = buildHorario(incidencia.getFecha(), incidencia.getHora());
                        callback.onSuccess(new Result(incidencia.getTipo(), horario, incidencia.getNivel(), incidencia.getDescripcion()));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private String buildHorario(String fecha, String hora) {
        boolean hasFecha = fecha != null && !fecha.trim().isEmpty();
        boolean hasHora = hora != null && !hora.trim().isEmpty();
        if (hasFecha && hasHora) return fecha.trim() + " · " + hora.trim();
        if (hasFecha) return fecha.trim();
        if (hasHora) return hora.trim();
        return "";
    }

    public static class Result {
        private final String tipo;
        private final String horario;
        private final String nivel;
        private final String descripcion;

        public Result(String tipo, String horario, String nivel, String descripcion) {
            this.tipo = tipo != null ? tipo : "";
            this.horario = horario != null ? horario : "";
            this.nivel = nivel != null ? nivel : "";
            this.descripcion = descripcion != null ? descripcion : "";
        }

        public static Result empty() {
            return new Result("", "", "", "");
        }

        public String getTipo() { return tipo; }
        public String getHorario() { return horario; }
        public String getNivel() { return nivel; }
        public String getDescripcion() { return descripcion; }
    }
}
