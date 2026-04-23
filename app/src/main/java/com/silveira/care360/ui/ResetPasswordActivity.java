package com.silveira.care360.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.silveira.care360.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordActivity extends AppCompatActivity {
    private ResetPasswordViewModel viewModel;
    private TextInputEditText edtEmail;
    private MaterialButton btnEnviar;
    private TextView txtVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        viewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);

        edtEmail = findViewById(R.id.edtEmail);
        btnEnviar = findViewById(R.id.btnEnviar);
        txtVolver = findViewById(R.id.txtVolver);

        txtVolver.setOnClickListener(v -> finish());
        btnEnviar.setOnClickListener(v -> startResetFlow());

        observeViewModel();
    }

    private void startResetFlow() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UiMessageUtils.show(this, "Introduce un email valido");
            edtEmail.requestFocus();
            return;
        }

        viewModel.startResetFlow(email);
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> btnEnviar.setEnabled(!state.isLoading));

        viewModel.action.observe(this, action -> {
            if (action == null) {
                return;
            }

            if (action instanceof ResetPasswordViewModel.ShowMessageAction) {
                String message = ((ResetPasswordViewModel.ShowMessageAction) action).message;
                UiMessageUtils.show(this, message);
            } else if (action instanceof ResetPasswordViewModel.ShowNoAccountDialogAction) {
                showNoAccountDialog();
            } else if (action instanceof ResetPasswordViewModel.ShowGoogleOnlyDialogAction) {
                showGoogleOnlyDialog();
            } else if (action instanceof ResetPasswordViewModel.ShowEmailSentDialogAction) {
                showEmailSentDialog();
            }

            viewModel.onActionHandled();
        });
    }

    private void showEmailSentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Email enviado")
                .setMessage("Te hemos enviado un enlace para restablecer tu contrasena.\n\nRevisa tu bandeja de entrada (y spam).")
                .setPositiveButton("Aceptar", (d, w) -> finish())
                .show();
    }

    private void showNoAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No tienes cuenta")
                .setMessage("No existe una cuenta con este email.")
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void showGoogleOnlyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cuenta registrada con Google")
                .setMessage("Este correo esta registrado con Google.\n\nPara entrar, vuelve atras y pulsa \"Acceder con Google\".")
                .setPositiveButton("Entendido", null)
                .show();
    }
}
