package com.silveira.care360.ui;

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
import com.silveira.care360.domain.model.Patologia;
import com.silveira.care360.ui.adapter.PatologiasAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PatologiasActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";

    private PatologiasViewModel viewModel;
    private String groupId;
    private RecyclerView rvPatologias;
    private TextView txtEmpty;
    private PatologiasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patologias);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        viewModel = new ViewModelProvider(this).get(PatologiasViewModel.class);

        ImageButton btnBack = findViewById(R.id.btnBackPatologias);
        MaterialButton btnAdd = findViewById(R.id.btnAddPatologia);
        rvPatologias = findViewById(R.id.rvPatologias);
        txtEmpty = findViewById(R.id.txtEmptyPatologias);

        adapter = new PatologiasAdapter(new PatologiasAdapter.Listener() {
            @Override
            public void onEdit(Patologia patologia) {
                showEditDialog(patologia);
            }

            @Override
            public void onDelete(Patologia patologia) {
                showDeleteDialog(patologia);
            }
        });

        rvPatologias.setLayoutManager(new LinearLayoutManager(this));
        rvPatologias.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showEditDialog(null));

        observeViewModel();
        viewModel.load(groupId);
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) return;
            adapter.submitList(state.items);
            boolean hasItems = state.items != null && !state.items.isEmpty();
            rvPatologias.setVisibility(hasItems ? android.view.View.VISIBLE : android.view.View.GONE);
            txtEmpty.setVisibility(hasItems ? android.view.View.GONE : android.view.View.VISIBLE);
            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                UiMessageUtils.show(this, state.errorMessage);
            }
        });

        viewModel.action.observe(this, action -> {
            if (action == null) return;
            if (action instanceof PatologiasViewModel.ShowMessageAction) {
                UiMessageUtils.show(this, ((PatologiasViewModel.ShowMessageAction) action).message);
            }
            viewModel.onActionHandled();
        });
    }

    private void showEditDialog(Patologia patologia) {
        EditText etNombre = new EditText(this);
        etNombre.setHint(R.string.patologias_name_req);
        etNombre.setText(patologia != null ? patologia.getNombre() : "");

        EditText etDescripcion = new EditText(this);
        etDescripcion.setHint(R.string.patologias_description);
        etDescripcion.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etDescripcion.setMinLines(3);
        etDescripcion.setText(patologia != null ? patologia.getDescripcion() : "");

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = dp(20);
        container.setPadding(padding, padding, padding, 0);
        container.addView(etNombre);
        container.addView(etDescripcion);

        new AlertDialog.Builder(this)
                .setTitle(patologia == null ? R.string.patologias_add : R.string.patologias_edit)
                .setView(container)
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.citas_guardar, (dialog, which) ->
                        viewModel.save(
                                groupId,
                                patologia != null ? patologia.getId() : "",
                                etNombre.getText() != null ? etNombre.getText().toString() : "",
                                etDescripcion.getText() != null ? etDescripcion.getText().toString() : ""
                        ))
                .show();
    }

    private void showDeleteDialog(Patologia patologia) {
        if (patologia == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.patologias_delete_title)
                .setMessage(getString(R.string.patologias_delete_message, patologia.getNombre()))
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.patologias_delete_confirm, (dialog, which) ->
                        viewModel.delete(groupId, patologia.getId()))
                .show();
    }

    private int dp(int value) {
        return (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}
