package com.silveira.care360.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.silveira.care360.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateGroupActivity extends AppCompatActivity {
    private CreateGroupViewModel viewModel;
    private TextInputLayout inputNombreGrupo;
    private TextInputLayout inputPersonaCuidada;
    private TextInputEditText edtNombreGrupo;
    private TextInputEditText edtPersonaCuidada;
    private Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        viewModel = new ViewModelProvider(this).get(CreateGroupViewModel.class);

        configurarHeader();

        inputNombreGrupo = findViewById(R.id.inputNombreGrupo);
        inputPersonaCuidada = findViewById(R.id.inputPersonaCuidada);
        edtNombreGrupo = findViewById(R.id.edtNombreGrupo);
        edtPersonaCuidada = findViewById(R.id.edtPersonaCuidada);
        btnContinuar = findViewById(R.id.btnContinuar);

        btnContinuar.setOnClickListener(v -> {
            if (!validarCampos()) {
                return;
            }

            String nombreGrupo = edtNombreGrupo.getText() != null
                    ? edtNombreGrupo.getText().toString().trim()
                    : "";
            String personaCuidada = edtPersonaCuidada.getText() != null
                    ? edtPersonaCuidada.getText().toString().trim()
                    : "";

            viewModel.createGroup(nombreGrupo, personaCuidada);
        });

        observeViewModel();
    }

    private void configurarHeader() {
        String email = viewModel.getLoggedUserEmail();

        HeaderManager.configurar(this, email, () ->
                viewModel.performLogout(() -> {
                    Intent intent = new Intent(CreateGroupActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }));
    }

    private boolean validarCampos() {
        inputNombreGrupo.setError(null);
        inputPersonaCuidada.setError(null);

        String nombreGrupo = edtNombreGrupo.getText() != null ? edtNombreGrupo.getText().toString().trim() : "";
        String personaCuidada = edtPersonaCuidada.getText() != null ? edtPersonaCuidada.getText().toString().trim() : "";

        boolean ok = true;
        if (nombreGrupo.isEmpty()) {
            inputNombreGrupo.setError(getString(R.string.create_group_name_hint));
            ok = false;
        }
        if (personaCuidada.isEmpty()) {
            inputPersonaCuidada.setError(getString(R.string.create_group_care_name_hint));
            ok = false;
        }
        return ok;
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> btnContinuar.setEnabled(!state.isLoading));

        viewModel.action.observe(this, action -> {
            if (action == null) {
                return;
            }

            if (action instanceof CreateGroupViewModel.ShowMessageAction) {
                String message = ((CreateGroupViewModel.ShowMessageAction) action).message;
                UiMessageUtils.show(this, message);
            } else if (action instanceof CreateGroupViewModel.NavigateToHomeAction) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("navigation_source", "create_group");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            viewModel.onActionHandled();
        });
    }
}
