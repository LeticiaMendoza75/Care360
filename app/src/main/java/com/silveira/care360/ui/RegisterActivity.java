package com.silveira.care360.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.silveira.care360.R;
import com.silveira.care360.ui.auth.RegisterViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;
    private TextInputEditText edtNombre, edtEmail, edtPassword, edtPassword2;
    private MaterialButton btnCrearCuenta;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        edtNombre = findViewById(R.id.edtNombre);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword2 = findViewById(R.id.edtPassword2);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        progressBar = findViewById(R.id.progressBar);

        String prefill = getIntent().getStringExtra("prefill_email");
        if (prefill != null) edtEmail.setText(prefill);

        findViewById(R.id.txtVolver).setOnClickListener(v -> finish());

        btnCrearCuenta.setOnClickListener(v -> {
            if (validate()) {
                viewModel.register(
                    edtNombre.getText().toString().trim(),
                    edtEmail.getText().toString().trim(),
                    edtPassword.getText().toString()
                );
            }
        });

        observeViewModel();
    }

    private boolean validate() {
        // Validation logic...
        return true; 
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            btnCrearCuenta.setEnabled(!state.isLoading);
            if (progressBar != null) progressBar.setVisibility(state.isLoading ? View.VISIBLE : View.GONE);
            if (state.errorMessage != null) UiMessageUtils.show(this, state.errorMessage);
        });

        viewModel.navigation.observe(this, nav -> {
            if (nav == null) return;
            if (nav == RegisterViewModel.RegisterNavigation.HOME_OR_GROUP) {
                showHomeOrGroupDialog();
            } else {
                goToActivity(GroupEntryActivity.class);
            }
        });
    }

    private void showHomeOrGroupDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Ya perteneces a un grupo")
                .setMessage("Hemos detectado que tienes un grupo activo. ¿Quieres entrar a ese grupo?")
                .setPositiveButton("Sí, entrar", (d, w) -> goToActivity(HomeActivity.class))
                .setNegativeButton("No, crear/unirme a otro", (d, w) -> goToActivity(GroupEntryActivity.class))
                .show();
    }

    private void goToActivity(Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
