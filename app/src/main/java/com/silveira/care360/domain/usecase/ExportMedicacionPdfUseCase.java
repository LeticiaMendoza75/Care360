package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.report.MedicacionPdfExporter;

import java.util.List;

import javax.inject.Inject;

public class ExportMedicacionPdfUseCase {

    private final MedicacionPdfExporter medicacionPdfExporter;

    @Inject
    public ExportMedicacionPdfUseCase(MedicacionPdfExporter medicacionPdfExporter) {
        this.medicacionPdfExporter = medicacionPdfExporter;
    }

    public void execute(List<Medicamento> medicamentos, ResultCallback<MedicacionPdfExporter.Result> callback) {
        if (medicamentos == null || medicamentos.isEmpty()) {
            callback.onError("No hay medicacion para exportar");
            return;
        }
        medicacionPdfExporter.export(medicamentos, callback);
    }
}
