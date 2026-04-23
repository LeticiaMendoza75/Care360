package com.silveira.care360.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.SeguimientoRegistro;
import com.silveira.care360.ui.adapter.SeguimientoAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SeguimientoActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";

    private SeguimientoViewModel viewModel;
    private String groupId;
    private RecyclerView rvSeguimiento;
    private TextView txtEmpty;
    private SeguimientoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        viewModel = new ViewModelProvider(this).get(SeguimientoViewModel.class);

        ImageButton btnBack = findViewById(R.id.btnBackSeguimiento);
        MaterialButton btnPatologias = findViewById(R.id.btnPatologiasDesdeSeguimiento);
        MaterialButton btnAdd = findViewById(R.id.btnAddSeguimiento);
        rvSeguimiento = findViewById(R.id.rvSeguimiento);
        txtEmpty = findViewById(R.id.txtEmptySeguimiento);

        adapter = new SeguimientoAdapter(new SeguimientoAdapter.Listener() {
            @Override
            public void onEdit(SeguimientoRegistro registro) {
                showEditDialog(registro);
            }

            @Override
            public void onDelete(SeguimientoRegistro registro) {
                showDeleteDialog(registro);
            }
        });

        rvSeguimiento.setLayoutManager(new LinearLayoutManager(this));
        rvSeguimiento.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnPatologias.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatologiasActivity.class);
            intent.putExtra(PatologiasActivity.EXTRA_GROUP_ID, groupId);
            startActivity(intent);
        });
        btnAdd.setOnClickListener(v -> showTypeSelector(null));

        observeViewModel();
        viewModel.load(groupId);
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) return;
            adapter.submitList(state.items);
            boolean hasItems = state.items != null && !state.items.isEmpty();
            rvSeguimiento.setVisibility(hasItems ? android.view.View.VISIBLE : android.view.View.GONE);
            txtEmpty.setVisibility(hasItems ? android.view.View.GONE : android.view.View.VISIBLE);
            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                UiMessageUtils.show(this, state.errorMessage);
            }
        });

        viewModel.action.observe(this, action -> {
            if (action == null) return;
            if (action instanceof SeguimientoViewModel.ShowMessageAction) {
                UiMessageUtils.show(this, ((SeguimientoViewModel.ShowMessageAction) action).message);
            }
            viewModel.onActionHandled();
        });
    }

    private void showTypeSelector(SeguimientoRegistro registro) {
        String[] labels = new String[]{
                getString(R.string.seguimiento_type_tension),
                getString(R.string.seguimiento_type_glucosa),
                getString(R.string.seguimiento_type_temperatura),
                getString(R.string.seguimiento_type_peso)
        };
        String[] values = new String[]{
                SeguimientoRegistro.TIPO_TENSION,
                SeguimientoRegistro.TIPO_GLUCOSA,
                SeguimientoRegistro.TIPO_TEMPERATURA,
                SeguimientoRegistro.TIPO_PESO
        };
        int checked = 0;
        if (registro != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(registro.getTipo())) {
                    checked = i;
                    break;
                }
            }
        }
        final int[] selected = {checked};
        new AlertDialog.Builder(this)
                .setTitle(R.string.seguimiento_select_type)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.citas_continuar, (dialog, which) ->
                        showEditDialog(registro, values[selected[0]], labels[selected[0]]))
                .setNegativeButton(R.string.citas_cancelar, null)
                .show();
    }

    private void showEditDialog(SeguimientoRegistro registro) {
        String tipo = registro != null ? registro.getTipo() : null;
        if (tipo == null || tipo.trim().isEmpty()) {
            showTypeSelector(registro);
            return;
        }
        String label = getLabelForType(tipo);
        showEditDialog(registro, tipo, label);
    }

    private void showEditDialog(SeguimientoRegistro registro, String tipo, String label) {
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = dp(20);
        container.setPadding(padding, padding, padding, 0);

        EditText etPrincipal = new EditText(this);
        etPrincipal.setHint(getPrimaryHintForType(tipo));
        etPrincipal.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrincipal.setText(registro != null ? registro.getValorPrincipal() : "");
        container.addView(etPrincipal);

        EditText etSecundario = null;
        if (SeguimientoRegistro.TIPO_TENSION.equals(tipo)) {
            etSecundario = new EditText(this);
            etSecundario.setHint(R.string.seguimiento_value_secondary_req);
            etSecundario.setInputType(InputType.TYPE_CLASS_NUMBER);
            etSecundario.setText(registro != null ? registro.getValorSecundario() : "");
            container.addView(etSecundario);
        }

        EditText etNotas = new EditText(this);
        etNotas.setHint(R.string.seguimiento_notes);
        etNotas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etNotas.setMinLines(2);
        etNotas.setText(registro != null ? registro.getNotas() : "");
        container.addView(etNotas);

        EditText finalEtSecundario = etSecundario;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.seguimiento_edit_title, label))
                .setView(container)
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.citas_guardar, (dialog, which) ->
                        viewModel.save(
                                groupId,
                                registro != null ? registro.getId() : "",
                                tipo,
                                etPrincipal.getText() != null ? etPrincipal.getText().toString() : "",
                                finalEtSecundario != null && finalEtSecundario.getText() != null ? finalEtSecundario.getText().toString() : "",
                                etNotas.getText() != null ? etNotas.getText().toString() : ""
                        ))
                .show();
    }

    private void showDeleteDialog(SeguimientoRegistro registro) {
        if (registro == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.seguimiento_delete_title)
                .setMessage(R.string.seguimiento_delete_message)
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.seguimiento_delete_confirm, (dialog, which) ->
                        viewModel.delete(groupId, registro.getId()))
                .show();
    }

    private String getLabelForType(String tipo) {
        if (SeguimientoRegistro.TIPO_TENSION.equals(tipo)) return getString(R.string.seguimiento_type_tension);
        if (SeguimientoRegistro.TIPO_GLUCOSA.equals(tipo)) return getString(R.string.seguimiento_type_glucosa);
        if (SeguimientoRegistro.TIPO_TEMPERATURA.equals(tipo)) return getString(R.string.seguimiento_type_temperatura);
        if (SeguimientoRegistro.TIPO_PESO.equals(tipo)) return getString(R.string.seguimiento_type_peso);
        return getString(R.string.seguimiento_title);
    }

    private int getPrimaryHintForType(String tipo) {
        if (SeguimientoRegistro.TIPO_TENSION.equals(tipo)) return R.string.seguimiento_value_primary_req;
        if (SeguimientoRegistro.TIPO_GLUCOSA.equals(tipo)) return R.string.seguimiento_value_glucosa_req;
        if (SeguimientoRegistro.TIPO_TEMPERATURA.equals(tipo)) return R.string.seguimiento_value_temperatura_req;
        if (SeguimientoRegistro.TIPO_PESO.equals(tipo)) return R.string.seguimiento_value_peso_req;
        return R.string.seguimiento_value_primary_req;
    }

    private int dp(int value) {
        return (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}
