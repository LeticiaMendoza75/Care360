package com.silveira.care360.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.ui.adapter.CitasAdapter;
import com.silveira.care360.ui.citas.CitasViewModel;
import com.silveira.care360.ui.UiMessageUtils;

import java.util.Calendar;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CitasActivity extends AppCompatActivity {

    private CitasViewModel viewModel;
    private RecyclerView rvCitas;
    private MaterialButton btnAnadirCita;
    private TextView txtEmpty;
    private ImageButton btnExportCitas;
    private CitasAdapter citasAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas);

        viewModel = new ViewModelProvider(this).get(CitasViewModel.class);

        initDynamicViews();
        BottomNavManager.bind(this, BottomNavManager.Tab.CITAS);
        observeViewModel();
        viewModel.loadCitasData();
    }

    private void initDynamicViews() {
        View contentRoot = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        if (!(contentRoot instanceof ConstraintLayout)) {
            throw new IllegalStateException("La pantalla de citas necesita un ConstraintLayout como raiz");
        }
        ConstraintLayout root = (ConstraintLayout) contentRoot;

        setupHeaderActions();

        txtEmpty = findViewById(R.id.txtCitasPlaceholder);
        txtEmpty.setText(R.string.citas_vacio);
        txtEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        rvCitas = new RecyclerView(this);
        rvCitas.setId(View.generateViewId());
        rvCitas.setLayoutManager(new LinearLayoutManager(this));
        rvCitas.setClipToPadding(false);
        rvCitas.setPadding(dp(16), dp(16), dp(16), dp(16));
        citasAdapter = new CitasAdapter(new CitasAdapter.Listener() {
            @Override
            public void onVerMasClicked(Cita cita) {
                viewModel.onVerMasClicked(cita);
            }

            @Override
            public void onGestionarClicked(Cita cita) {
                viewModel.onGestionarClicked(cita);
            }
        });
        rvCitas.setAdapter(citasAdapter);

        btnAnadirCita = new MaterialButton(this);
        btnAnadirCita.setId(View.generateViewId());
        btnAnadirCita.setText(R.string.citas_anadir);
        btnAnadirCita.setAllCaps(false);
        btnAnadirCita.setTextColor(Color.parseColor("#0F4BB3"));
        btnAnadirCita.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        btnAnadirCita.setTypeface(Typeface.DEFAULT_BOLD);
        btnAnadirCita.setCornerRadius(dp(26));
        btnAnadirCita.setBackgroundColor(Color.WHITE);
        btnAnadirCita.setOnClickListener(v -> viewModel.onAnadirCitaClicked());

        root.addView(rvCitas);
        root.addView(btnAnadirCita);

        ConstraintSet set = new ConstraintSet();
        set.clone(root);
        set.connect(rvCitas.getId(), ConstraintSet.TOP, R.id.layoutHeaderCitas, ConstraintSet.BOTTOM, dp(4));
        set.connect(rvCitas.getId(), ConstraintSet.BOTTOM, btnAnadirCita.getId(), ConstraintSet.TOP, dp(8));
        set.connect(rvCitas.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
        set.connect(rvCitas.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        set.constrainHeight(rvCitas.getId(), 0);
        set.constrainWidth(rvCitas.getId(), 0);

        set.connect(btnAnadirCita.getId(), ConstraintSet.BOTTOM, R.id.bottomNavCitas, ConstraintSet.TOP, dp(12));
        set.connect(btnAnadirCita.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dp(16));
        set.connect(btnAnadirCita.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dp(16));
        set.constrainHeight(btnAnadirCita.getId(), dp(52));
        set.constrainWidth(btnAnadirCita.getId(), 0);
        set.applyTo(root);
    }

    private void setupHeaderActions() {
        LinearLayout header = findViewById(R.id.layoutHeaderCitas);
        if (header == null) {
            return;
        }

        header.setGravity(Gravity.CENTER_VERTICAL);

        if (header.getChildCount() > 0) {
            View titleView = header.getChildAt(0);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            );
            titleView.setLayoutParams(titleParams);
            if (titleView instanceof TextView) {
                ((TextView) titleView).setGravity(Gravity.CENTER);
            }
        }

        btnExportCitas = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(40), dp(40));
        btnExportCitas.setLayoutParams(params);
        btnExportCitas.setBackgroundResource(android.R.drawable.list_selector_background);
        btnExportCitas.setImageResource(android.R.drawable.stat_sys_download_done);
        btnExportCitas.setColorFilter(Color.WHITE);
        btnExportCitas.setContentDescription("Descargar PDF");
        btnExportCitas.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        btnExportCitas.setPadding(dp(8), dp(8), dp(8), dp(8));
        btnExportCitas.setOnClickListener(v -> viewModel.onExportPdfClicked());

        header.addView(btnExportCitas);
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) return;

            citasAdapter.submitList(state.citas);
            boolean hasItems = state.citas != null && !state.citas.isEmpty();
            txtEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            rvCitas.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            btnAnadirCita.setEnabled(!state.isLoading);
            if (btnExportCitas != null) {
                btnExportCitas.setEnabled(!state.isLoading);
                btnExportCitas.setAlpha(state.isLoading ? 0.5f : 1f);
            }

            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                UiMessageUtils.show(this, state.errorMessage);
            }
        });

        viewModel.action.observe(this, action -> {
            if (action == null) return;

            if (action instanceof CitasViewModel.ShowCitaEditorAction) {
                showCitaEditorDialog(((CitasViewModel.ShowCitaEditorAction) action).cita);
            } else if (action instanceof CitasViewModel.ShowCitaDetailAction) {
                showCitaDetailDialog(((CitasViewModel.ShowCitaDetailAction) action).cita);
            } else if (action instanceof CitasViewModel.ConfirmDeleteCitaAction) {
                showDeleteConfirmation(((CitasViewModel.ConfirmDeleteCitaAction) action).cita);
            } else if (action instanceof CitasViewModel.ShowMessageAction) {
                UiMessageUtils.show(this, ((CitasViewModel.ShowMessageAction) action).message);
            } else if (action instanceof CitasViewModel.ShareCitasPdfAction) {
                CitasViewModel.ShareCitasPdfAction shareAction = (CitasViewModel.ShareCitasPdfAction) action;
                sharePdf(shareAction.uri, shareAction.fileName);
            }

            viewModel.onActionHandled();
        });
    }

    private void sharePdf(Uri uri, String fileName) {
        if (uri == null) {
            UiMessageUtils.show(this, "No se pudo compartir el PDF");
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.citas_pdf_title));
        shareIntent.putExtra(Intent.EXTRA_TEXT, fileName != null ? fileName : getString(R.string.citas_pdf_title));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Compartir PDF"));
    }

    private void showCitaDetailDialog(Cita cita) {
        if (cita == null) return;

        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.citas_detalle_fecha, textOrFallback(cita.getFecha(), "—")));
        message.append("\n").append(getString(R.string.citas_detalle_hora, textOrFallback(cita.getHora(), "—")));
        message.append("\n").append(getString(R.string.citas_detalle_lugar, textOrFallback(cita.getLugar(), "—")));
        message.append("\n").append(getString(R.string.citas_detalle_profesional, textOrFallback(cita.getProfesional(), "—")));
        if (hasText(cita.getPersonaEncargada())) {
            message.append("\n").append(getString(R.string.citas_detalle_persona_encargada, cita.getPersonaEncargada().trim()));
        }
        if (hasText(cita.getObservaciones())) {
            message.append("\n\n").append(getString(R.string.citas_detalle_notas)).append("\n")
                    .append(cita.getObservaciones().trim());
        }

        new AlertDialog.Builder(this)
                .setTitle(textOrFallback(cita.getTitulo(), getString(R.string.citas_generica)))
                .setMessage(message.toString())
                .setPositiveButton(R.string.citas_cerrar, null)
                .show();
    }

    private void showDeleteConfirmation(Cita cita) {
        if (cita == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.citas_eliminar_titulo)
                .setMessage(getString(R.string.citas_eliminar_mensaje,
                        textOrFallback(cita.getTitulo(), getString(R.string.citas_generica))))
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.citas_eliminar, (dialog, which) -> viewModel.confirmDeleteCita(cita))
                .show();
    }

    private void showCitaEditorDialog(Cita original) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(12), dp(20), dp(4));
        scrollView.addView(container);

        TextView txtRequiredInfo = new TextView(this);
        txtRequiredInfo.setText(R.string.citas_campos_obligatorios);
        txtRequiredInfo.setTextColor(Color.parseColor("#7A8695"));
        txtRequiredInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams requiredParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        requiredParams.bottomMargin = dp(10);
        txtRequiredInfo.setLayoutParams(requiredParams);

        EditText etTitulo = createField(getString(R.string.citas_campo_titulo_req));
        EditText etFecha = createPickerField(getString(R.string.citas_campo_fecha_req));
        EditText etHora = createPickerField(getString(R.string.citas_campo_hora_req));
        EditText etLugar = createField(getString(R.string.citas_campo_lugar));
        EditText etProfesional = createField(getString(R.string.citas_campo_profesional));
        EditText etPersonaEncargada = createField(getString(R.string.citas_campo_persona_encargada));
        EditText etObservaciones = createField(getString(R.string.citas_campo_observaciones));
        etObservaciones.setMinLines(3);
        etObservaciones.setGravity(Gravity.TOP | Gravity.START);

        if (original != null) {
            etTitulo.setText(textOrFallback(original.getTitulo(), ""));
            etFecha.setText(textOrFallback(original.getFecha(), ""));
            etHora.setText(textOrFallback(original.getHora(), ""));
            etLugar.setText(textOrFallback(original.getLugar(), ""));
            etProfesional.setText(textOrFallback(original.getProfesional(), ""));
            etPersonaEncargada.setText(textOrFallback(original.getPersonaEncargada(), ""));
            etObservaciones.setText(textOrFallback(original.getObservaciones(), ""));
        }

        etFecha.setOnClickListener(v -> showDatePicker(etFecha));
        etHora.setOnClickListener(v -> showTimePicker(etHora));

        container.addView(txtRequiredInfo);
        container.addView(etTitulo);
        container.addView(etFecha);
        container.addView(etHora);
        container.addView(etLugar);
        container.addView(etProfesional);
        container.addView(etPersonaEncargada);
        container.addView(etObservaciones);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(original == null ? getString(R.string.citas_nueva) : getString(R.string.citas_gestionar))
                .setView(scrollView)
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.citas_guardar, null);

        if (original != null) {
            builder.setNeutralButton(R.string.citas_eliminar, null);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Cita cita = original != null ? original : new Cita();
                cita.setTitulo(textOrFallback(etTitulo.getText().toString(), ""));
                cita.setFecha(textOrFallback(etFecha.getText().toString(), ""));
                cita.setHora(textOrFallback(etHora.getText().toString(), ""));
                cita.setLugar(textOrFallback(etLugar.getText().toString(), ""));
                cita.setProfesional(textOrFallback(etProfesional.getText().toString(), ""));
                cita.setPersonaEncargada(textOrFallback(etPersonaEncargada.getText().toString(), ""));
                cita.setObservaciones(textOrFallback(etObservaciones.getText().toString(), ""));
                viewModel.onCitaEditorConfirmed(cita);
                dialog.dismiss();
            });

            if (original != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    dialog.dismiss();
                    viewModel.onDeleteCitaClicked(original);
                });
            }
        });

        dialog.show();
    }

    private EditText createField(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        field.setTextColor(Color.parseColor("#1B1B1B"));
        field.setHintTextColor(Color.parseColor("#7A8695"));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F8FBFF"));
        bg.setStroke(dp(1), Color.parseColor("#D7E2EF"));
        bg.setCornerRadius(dp(14));
        field.setBackground(bg);
        field.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(10);
        field.setLayoutParams(params);
        return field;
    }

    private EditText createPickerField(String hint) {
        EditText field = createField(hint);
        field.setFocusable(false);
        field.setClickable(true);
        field.setInputType(InputType.TYPE_NULL);
        return field;
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) ->
                target.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) ->
                target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true).show();
    }

    private String textOrFallback(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}
