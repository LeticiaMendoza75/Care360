package com.silveira.care360.ui.medicacion;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.ui.BottomNavManager;
import com.silveira.care360.ui.UiMessageUtils;
import com.silveira.care360.ui.adapter.MedicamentosAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MedicacionActivity extends AppCompatActivity implements MedicamentosAdapter.Listener {

    private MedicacionViewModel viewModel;

    private ImageButton btnBackMedicacion;
    private ImageButton btnExportMedicacion;
    private MaterialButton btnAnadirMedicamento;
    private RecyclerView rvMedicamentos;

    private MedicamentosAdapter medicamentosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicacion);

        viewModel = new ViewModelProvider(this).get(MedicacionViewModel.class);

        initViews();
        setupRecycler();
        setupHeaderActions();
        setupListeners();
        BottomNavManager.bind(this, BottomNavManager.Tab.MEDICACION);
        observeViewModel();

        viewModel.loadMedicacionData();
    }

    private void initViews() {
        btnBackMedicacion = findViewById(R.id.btnBackMedicacion);
        btnAnadirMedicamento = findViewById(R.id.btnAnadirMedicamento);
        rvMedicamentos = findViewById(R.id.rvMedicamentos);
    }

    private void setupRecycler() {
        rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        medicamentosAdapter = new MedicamentosAdapter(this);
        rvMedicamentos.setAdapter(medicamentosAdapter);
    }

    private void setupHeaderActions() {
        LinearLayout header = findViewById(R.id.layoutHeaderMedicacion);
        if (header == null) {
            return;
        }

        if (header.getChildCount() > 0) {
            View trailing = header.getChildAt(header.getChildCount() - 1);
            header.removeView(trailing);
        }

        btnExportMedicacion = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        btnExportMedicacion.setLayoutParams(params);
        btnExportMedicacion.setBackgroundResource(android.R.drawable.list_selector_background);
        btnExportMedicacion.setImageResource(android.R.drawable.stat_sys_download_done);
        btnExportMedicacion.setColorFilter(Color.WHITE);
        btnExportMedicacion.setContentDescription("Descargar PDF");
        btnExportMedicacion.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        btnExportMedicacion.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        header.addView(btnExportMedicacion);
    }

    private void setupListeners() {
        btnBackMedicacion.setOnClickListener(v -> finish());
        btnAnadirMedicamento.setOnClickListener(v -> viewModel.onAnadirMedicamentoClicked());
        if (btnExportMedicacion != null) {
            btnExportMedicacion.setOnClickListener(v -> viewModel.onExportPdfClicked());
        }
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) return;

            medicamentosAdapter.submitList(state.medicamentos);
            btnAnadirMedicamento.setEnabled(!state.isLoading);
            if (btnExportMedicacion != null) {
                btnExportMedicacion.setEnabled(!state.isLoading);
                btnExportMedicacion.setAlpha(state.isLoading ? 0.5f : 1f);
            }

            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                UiMessageUtils.show(this, state.errorMessage);
            }
        });

        viewModel.action.observe(this, action -> {
            if (action == null) return;

            if (action instanceof MedicacionViewModel.ShowMedicamentoEditorAction) {
                Medicamento medicamento = ((MedicacionViewModel.ShowMedicamentoEditorAction) action).medicamento;
                new DialogoAnadirMedicamento(this).show(
                        medicamento,
                        viewModel::onMedicamentoEditorConfirmed,
                        medicamento != null ? () -> viewModel.confirmDeleteMedicamento(medicamento) : null
                );

            } else if (action instanceof MedicacionViewModel.NavigateToDetalleMedicamentoAction) {
                String medicamentoId =
                        ((MedicacionViewModel.NavigateToDetalleMedicamentoAction) action).medicamentoId;
                abrirDetalleMedicamento(medicamentoId);

            } else if (action instanceof MedicacionViewModel.ConfirmDeleteMedicamentoAction) {
                Medicamento medicamento = ((MedicacionViewModel.ConfirmDeleteMedicamentoAction) action).medicamento;
                showDeleteConfirmation(medicamento);

            } else if (action instanceof MedicacionViewModel.ShowMessageAction) {
                String message = ((MedicacionViewModel.ShowMessageAction) action).message;
                UiMessageUtils.show(this, message);

            } else if (action instanceof MedicacionViewModel.ShowExactAlarmPermissionAction) {
                showExactAlarmPermissionDialog();

            } else if (action instanceof MedicacionViewModel.ShareMedicacionPdfAction) {
                MedicacionViewModel.ShareMedicacionPdfAction shareAction =
                        (MedicacionViewModel.ShareMedicacionPdfAction) action;
                sharePdf(shareAction.uri, shareAction.fileName);
            }

            viewModel.onActionHandled();
        });
    }

    private void abrirDetalleMedicamento(String medicamentoId) {
        Intent intent = new Intent(this, DetalleMedicamentoActivity.class);
        intent.putExtra(DetalleMedicamentoActivity.EXTRA_MEDICAMENTO_ID, medicamentoId);
        startActivity(intent);
    }

    private void sharePdf(Uri uri, String fileName) {
        if (uri == null) {
            UiMessageUtils.show(this, "No se pudo compartir el PDF");
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Informe de medicacion");
        shareIntent.putExtra(Intent.EXTRA_TEXT, fileName != null ? fileName : "Informe de medicacion");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Compartir PDF"));
    }

    private void showDeleteConfirmation(Medicamento medicamento) {
        if (medicamento == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar medicamento")
                .setMessage("Se eliminara \"" + medicamento.getNombre() + "\". Esta accion no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.confirmDeleteMedicamento(medicamento))
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
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        ));
    }

    @Override
    public void onVerMasClicked(Medicamento medicamento) {
        viewModel.onVerMasClicked(medicamento);
    }

    @Override
    public void onGestionarClicked(Medicamento medicamento) {
        viewModel.onEditarMedicamentoClicked(medicamento);
    }
}
