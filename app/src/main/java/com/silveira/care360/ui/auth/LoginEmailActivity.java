package com.silveira.care360.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.silveira.care360.R;
import com.silveira.care360.ui.GroupEntryActivity;
import com.silveira.care360.ui.HomeActivity;
import com.silveira.care360.ui.RegisterActivity;
import com.silveira.care360.ui.UiMessageUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginEmailActivity extends AppCompatActivity {

    private LoginEmailViewModel viewModel;
    private ImageButton btnBackLoginEmail;
    private TextInputEditText edtEmail, edtPassword;
    private Button btnEntrar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        viewModel = new ViewModelProvider(this).get(LoginEmailViewModel.class);

        btnBackLoginEmail = findViewById(R.id.btnBackLoginEmail);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnEntrar = findViewById(R.id.btnEntrar);
        progressBar = findViewById(R.id.progressBar);

        btnEntrar.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            if (validate(email, pass)) {
                viewModel.startLoginFlow(email, pass);
            }
        });

        btnBackLoginEmail.setOnClickListener(v -> finish());

        observeViewModel();
    }

    private boolean validate(String email, String pass) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UiMessageUtils.show(this, "Introduce un email válido");
            return false;
        }
        if (pass.isEmpty() || pass.length() < 6) {
            UiMessageUtils.show(this, "La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        return true;
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            btnEntrar.setEnabled(!state.isLoading);
            progressBar.setVisibility(state.isLoading ? View.VISIBLE : View.GONE);

            if (state.errorMessage != null) {
                UiMessageUtils.show(this, state.errorMessage);
            }

            if (state.errorType != null) {
                handleErrorType(state.errorType, state.extraData);
            }
        });

        viewModel.navigation.observe(this, nav -> {
            if (nav == null) return;
            switch (nav) {
                case HOME:
                    goToActivity(HomeActivity.class);
                    break;
                case GROUP_ENTRY:
                    goToActivity(GroupEntryActivity.class);
                    break;
            }
        });
    }

    private void handleErrorType(LoginEmailViewModel.LoginErrorType type, String data) {
        if (type == LoginEmailViewModel.LoginErrorType.GOOGLE_ACCOUNT) {
            showGoogleAccountDialog();
        } else if (type == LoginEmailViewModel.LoginErrorType.NO_ACCOUNT) {
            showNoAccountDialog(data);
        }
    }

    private void showNoAccountDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("No tienes cuenta")
                .setMessage("No existe una cuenta con este email.\n¿Quieres crear una nueva?")
                .setPositiveButton("Crear cuenta", (d, which) -> {
                    Intent i = new Intent(this, RegisterActivity.class);
                    i.putExtra("prefill_email", email);
                    startActivity(i);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showGoogleAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cuenta registrada con Google")
                .setMessage("Este correo ya está registrado con Google.\n\nPulsa “Acceder con Google” en la pantalla anterior.")
                .setPositiveButton("Ir a Acceder con Google", (d, which) -> finish())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void goToActivity(Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
