package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.domain.report.IncidenciasPdfExporter;

import java.util.List;

import javax.inject.Inject;

public class ExportIncidenciasPdfUseCase {

    private final IncidenciasPdfExporter incidenciasPdfExporter;

    @Inject
    public ExportIncidenciasPdfUseCase(IncidenciasPdfExporter incidenciasPdfExporter) {
        this.incidenciasPdfExporter = incidenciasPdfExporter;
    }

    public void execute(List<Incidencia> incidencias,
                        ResultCallback<IncidenciasPdfExporter.Result> callback) {
        incidenciasPdfExporter.export(incidencias, callback);
    }
}
