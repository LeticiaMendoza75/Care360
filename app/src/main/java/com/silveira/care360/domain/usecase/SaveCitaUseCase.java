package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.repository.CitaRepository;

import javax.inject.Inject;

public class SaveCitaUseCase {

    private final CitaRepository citaRepository;

    @Inject
    public SaveCitaUseCase(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public void execute(String activeGroupId, String currentUserId, String titulo, String fecha, String hora,
                        String lugar, String profesional, String personaEncargada, String observaciones, boolean recordatorioActivo,
                        ResultCallback<Void> callback) {
        long now = System.currentTimeMillis();
        Cita cita = new Cita(null, titulo, fecha, hora, lugar, profesional, personaEncargada, observaciones,
                recordatorioActivo, currentUserId, now, currentUserId, now);
        citaRepository.addCita(activeGroupId, cita, callback);
    }
}
