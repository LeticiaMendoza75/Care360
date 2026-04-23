package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.report.CitasPdfExporter;

import java.util.List;

import javax.inject.Inject;

public class ExportCitasPdfUseCase {

    private final CitasPdfExporter citasPdfExporter;

    @Inject
    public ExportCitasPdfUseCase(CitasPdfExporter citasPdfExporter) {
        this.citasPdfExporter = citasPdfExporter;
    }

    public void execute(List<Cita> citas, ResultCallback<CitasPdfExporter.Result> callback) {
        if (citas == null || citas.isEmpty()) {
            callback.onError("No hay citas para exportar");
            return;
        }
        citasPdfExporter.export(citas, callback);
    }
}
