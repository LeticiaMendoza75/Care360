package com.silveira.care360.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.silveira.care360.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GroupEntryActivity extends AppCompatActivity {
    private GroupEntryViewModel viewModel;
    private ImageButton btnBackGroupEntry;
    private TextInputEditText edtCodigo;
    private Button btnUnirme;
    private Button btnCrearGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_entry);

        viewModel = new ViewModelProvider(this).get(GroupEntryViewModel.class);

        btnBackGroupEntry = findViewById(R.id.btnBackGroupEntry);
        btnCrearGrupo = findViewById(R.id.btnCrearGrupo);
        edtCodigo = findViewById(R.id.edtCodigo);
        btnUnirme = findViewById(R.id.btnUnirme);

        btnBackGroupEntry.setOnClickListener(v -> finish());
        btnCrearGrupo.setOnClickListener(v -> viewModel.onCreateGroupClicked());
        btnUnirme.setOnClickListener(v -> {
            String code = edtCodigo.getText() != null ? edtCodigo.getText().toString() : "";
            viewModel.joinWithCode(code);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> btnUnirme.setEnabled(!state.isLoading));

        viewModel.action.observe(this, action -> {
            if (action == null) {
                return;
            }

            if (action instanceof GroupEntryViewModel.ShowMessageAction) {
                String message = ((GroupEntryViewModel.ShowMessageAction) action).message;
                UiMessageUtils.show(this, message);
            } else if (action instanceof GroupEntryViewModel.NavigateToCreateGroupAction) {
                Intent intent = new Intent(this, CreateGroupActivity.class);
                startActivity(intent);
            } else if (action instanceof GroupEntryViewModel.NavigateToHomeAction) {
                String message = ((GroupEntryViewModel.NavigateToHomeAction) action).message;
                if (message != null && !message.trim().isEmpty()) {
                    UiMessageUtils.show(this, message);
                }
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }

            viewModel.onActionHandled();
        });
    }
}
