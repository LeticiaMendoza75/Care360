package com.silveira.care360.domain.usecase;

import com.silveira.care360.core.citas.CitaTimeCalculator;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.repository.CitaRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

public class LoadNextCitaUseCase {

    private final UserRepository userRepository;
    private final CitaRepository citaRepository;

    @Inject
    public LoadNextCitaUseCase(UserRepository userRepository, CitaRepository citaRepository) {
        this.userRepository = userRepository;
        this.citaRepository = citaRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onSuccess(Result.empty());
                    return;
                }

                citaRepository.getCitasByGroupId(groupId, new ResultCallback<List<Cita>>() {
                    @Override
                    public void onSuccess(List<Cita> citas) {
                        long now = System.currentTimeMillis();
                        Cita next = null;
                        long nextMillis = Long.MAX_VALUE;

                        if (citas != null) {
                            for (Cita cita : citas) {
                                long millis = CitaTimeCalculator.toMillis(cita);
                                if (millis <= now) continue;
                                if (millis < nextMillis) {
                                    nextMillis = millis;
                                    next = cita;
                                }
                            }
                        }

                        if (next == null) {
                            callback.onSuccess(Result.empty());
                            return;
                        }

                        callback.onSuccess(new Result(
                                next.getTitulo(),
                                CitaTimeCalculator.buildDisplayDateTime(next),
                                next.getPersonaEncargada(),
                                next.isRecordatorioActivo()
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("No se pudo cargar la proxima cita");
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError("No se pudo cargar el grupo activo");
            }
        });
    }

    public static class Result {
        private final String titulo;
        private final String horario;
        private final String personaEncargada;
        private final boolean recordatorioActivo;

        public Result(String titulo, String horario, String personaEncargada, boolean recordatorioActivo) {
            this.titulo = titulo;
            this.horario = horario;
            this.personaEncargada = personaEncargada;
            this.recordatorioActivo = recordatorioActivo;
        }

        public static Result empty() { return new Result("", "", "", false); }
        public String getTitulo() { return titulo; }
        public String getHorario() { return horario; }
        public String getPersonaEncargada() { return personaEncargada; }
        public boolean isRecordatorioActivo() { return recordatorioActivo; }
    }
}
