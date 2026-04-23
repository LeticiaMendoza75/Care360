package com.silveira.care360.ui.medicacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.ui.UiMessageUtils;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetalleMedicamentoActivity extends AppCompatActivity {

    public static final String EXTRA_MEDICAMENTO_ID = "medicamento_id";

    private DetalleMedicamentoViewModel viewModel;

    private ImageButton btnBackDetalleMedicamento;

    private TextView txtDetalleNombreMedicamento;
    private TextView txtDetalleFechaInicio;
    private TextView txtDetalleFechaFin;
    private TextView txtDetalleRecordatoriosEstado;
    private TextView txtObservacionesMedicamento;

    private MaterialButton btnDetalleAnadirDia;
    private ImageButton btnDetalleEditarMedicamento;
    private ImageButton btnDetalleEliminarMedicamento;

    private LinearLayout layoutDetalleDiasContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_medicamento);

        viewModel = new ViewModelProvider(this).get(DetalleMedicamentoViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
        cargarMedicamentoDesdeIntent();
    }

    private void initViews() {
        btnBackDetalleMedicamento = findViewById(R.id.btnBackDetalleMedicamento);

        txtDetalleNombreMedicamento = findViewById(R.id.txtDetalleNombreMedicamento);
        txtDetalleFechaInicio = findViewById(R.id.txtDetalleFechaInicio);
        txtDetalleFechaFin = findViewById(R.id.txtDetalleFechaFin);
        txtDetalleRecordatoriosEstado = findViewById(R.id.txtDetalleRecordatoriosEstado);
        txtObservacionesMedicamento = findViewById(R.id.txtObservacionesMedicamento);

        btnDetalleAnadirDia = findViewById(R.id.btnDetalleAnadirDia);
        btnDetalleEditarMedicamento = findViewById(R.id.btnDetalleEditarMedicamento);
        btnDetalleEliminarMedicamento = findViewById(R.id.btnDetalleEliminarMedicamento);

        layoutDetalleDiasContainer = findViewById(R.id.layoutDetalleDiasContainer);
    }

    private void setupListeners() {
        btnBackDetalleMedicamento.setOnClickListener(v -> finish());
        btnDetalleAnadirDia.setOnClickListener(v -> viewModel.onAnadirDiaClicked());
        btnDetalleEditarMedicamento.setOnClickListener(v -> viewModel.onEditarMedicamentoClicked());
        btnDetalleEliminarMedicamento.setOnClickListener(v -> viewModel.onEliminarMedicamentoClicked());
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) return;

            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                UiMessageUtils.show(this, state.errorMessage);
            }

            if (state.medicamento != null) {
                bindMedicamento(state.medicamento);
            }
        });

        viewModel.action.observe(this, action -> {
            if (action == null) {
                return;
            }

            if (action instanceof DetalleMedicamentoViewModel.ShowMedicamentoEditorAction) {
                Medicamento medicamento = ((DetalleMedicamentoViewModel.ShowMedicamentoEditorAction) action).medicamento;
                new DialogoAnadirMedicamento(this).show(
                        medicamento,
                        viewModel::onMedicamentoEditorConfirmed,
                        medicamento != null ? viewModel::confirmDeleteMedicamento : null
                );

            } else if (action instanceof DetalleMedicamentoViewModel.ConfirmDeleteMedicamentoAction) {
                showDeleteConfirmation(((DetalleMedicamentoViewModel.ConfirmDeleteMedicamentoAction) action).medicamento);

            } else if (action instanceof DetalleMedicamentoViewModel.ShowMessageAction) {
                UiMessageUtils.show(this, ((DetalleMedicamentoViewModel.ShowMessageAction) action).message);

            } else if (action instanceof DetalleMedicamentoViewModel.ShowExactAlarmPermissionAction) {
                showExactAlarmPermissionDialog();

            } else if (action instanceof DetalleMedicamentoViewModel.FinishAction) {
                finish();
            }

            viewModel.onActionHandled();
        });
    }

    private void cargarMedicamentoDesdeIntent() {
        String medicamentoId = getIntent().getStringExtra(EXTRA_MEDICAMENTO_ID);

        if (medicamentoId == null || medicamentoId.trim().isEmpty()) {
            UiMessageUtils.show(this, "No se recibió el id del medicamento");
            finish();
            return;
        }

        viewModel.loadMedicamentoDetalle(medicamentoId);
    }

    private void bindMedicamento(Medicamento medicamento) {
        txtDetalleNombreMedicamento.setText(safeText(medicamento.getNombre(), "Medicamento"));
        txtDetalleFechaInicio.setText(safeText(medicamento.getFechaInicio(), "-"));
        txtDetalleFechaFin.setText(safeText(medicamento.getFechaFin(), "-"));
        txtDetalleRecordatoriosEstado.setText(
                medicamento.isAlertasActivas()
                        ? getString(R.string.medicacion_avisos_activos)
                        : getString(R.string.medicacion_avisos_desactivados)
        );
        txtObservacionesMedicamento.setText(
                safeText(medicamento.getObservaciones(), "Sin observaciones")
        );

        renderDias(medicamento.getDias());
    }

    private void renderDias(List<DiaMedicacion> dias) {
        layoutDetalleDiasContainer.removeAllViews();

        if (dias == null || dias.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DiaMedicacion dia : dias) {
            View diaView = inflater.inflate(
                    R.layout.item_dia_medicacion,
                    layoutDetalleDiasContainer,
                    false
            );

            TextView txtFechaDiaMedicacion = diaView.findViewById(R.id.txtFechaDiaMedicacion);
            LinearLayout layoutHorasContainer = diaView.findViewById(R.id.layoutHorasContainer);
            ImageButton btnEditarDiaMedicacion = diaView.findViewById(R.id.btnEditarDiaMedicacion);
            ImageButton btnEliminarDiaMedicacion = diaView.findViewById(R.id.btnEliminarDiaMedicacion);
            MaterialButton btnAddHoraDia = diaView.findViewById(R.id.btnAddHoraDia);

            txtFechaDiaMedicacion.setText(safeText(dia.getFecha(), "-"));

            btnEditarDiaMedicacion.setVisibility(View.VISIBLE);
            btnEliminarDiaMedicacion.setVisibility(View.VISIBLE);
            btnAddHoraDia.setVisibility(View.VISIBLE);

            btnEditarDiaMedicacion.setOnClickListener(v -> viewModel.onEditarDiaClicked(dia));
            btnEliminarDiaMedicacion.setOnClickListener(v -> showDeleteDayConfirmation(dia));
            btnAddHoraDia.setOnClickListener(v -> viewModel.onAnadirHoraClicked(dia));

            renderHoras(layoutHorasContainer, btnAddHoraDia, dia.getHoras());

            layoutDetalleDiasContainer.addView(diaView);
        }
    }

    private void renderHoras(LinearLayout container, MaterialButton btnAddHoraDia, List<String> horas) {
        container.removeAllViews();

        if (horas != null && !horas.isEmpty()) {
            for (String hora : horas) {
                TextView chip = createHoraChip(container, safeText(hora, "--:--"));
                container.addView(chip);
            }
        }

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(40)
        );
        if (container.getChildCount() > 0) {
            buttonParams.leftMargin = dpToPx(8);
        }
        btnAddHoraDia.setLayoutParams(buttonParams);
        container.addView(btnAddHoraDia);
    }

    private TextView createHoraChip(LinearLayout parent, String hora) {
        TextView chip = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        if (parent.getChildCount() > 0) {
            params.leftMargin = dpToPx(8);
        }

        chip.setLayoutParams(params);
        chip.setBackgroundResource(R.drawable.bg_chip_hora_medicacion);
        chip.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dot_small, 0, 0, 0);
        chip.setCompoundDrawablePadding(dpToPx(6));
        chip.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        chip.setText(hora);
        chip.setTextColor(0xFF1F2937);
        chip.setTextSize(13);

        return chip;
    }

    private void showDeleteConfirmation(Medicamento medicamento) {
        if (medicamento == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar medicamento")
                .setMessage("Se eliminara \"" + medicamento.getNombre() + "\". Esta accion no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.confirmDeleteMedicamento())
                .show();
    }

    private void showDeleteDayConfirmation(DiaMedicacion dia) {
        if (dia == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar dia")
                .setMessage("Se eliminara el dia " + safeText(dia.getFecha(), "-") + " y sus horas.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.onEliminarDiaClicked(dia))
                .show();
    }

    private void showExactAlarmPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Alarmas exactas necesarias")
                .setMessage("Para avisar a la hora exacta de la medicacion, activa las alarmas exactas en el sistema.")
                .setNegativeButton("Mas tarde", null)
                .setPositiveButton("Configurar ahora", (dialog, which) -> openExactAlarmSettings())
                .show();
    }

    private void openExactAlarmSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
