package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.domain.repository.IncidenciaRepository;

import java.util.UUID;

import javax.inject.Inject;

public class SaveIncidenciaUseCase {

    private final IncidenciaRepository incidenciaRepository;

    @Inject
    public SaveIncidenciaUseCase(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    public void execute(String groupId,
                        String userId,
                        String tipo,
                        String fecha,
                        String hora,
                        String nivel,
                        String descripcion,
                        ResultCallback<Void> callback) {
        long now = System.currentTimeMillis();
        Incidencia incidencia = new Incidencia(
                UUID.randomUUID().toString(),
                tipo != null ? tipo.trim() : "",
                fecha != null ? fecha.trim() : "",
                hora != null ? hora.trim() : "",
                nivel != null ? nivel.trim() : "",
                descripcion != null ? descripcion.trim() : "",
                userId,
                now,
                userId,
                now
        );
        incidenciaRepository.addIncidencia(groupId, incidencia, callback);
    }
}
