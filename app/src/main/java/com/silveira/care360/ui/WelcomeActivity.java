package com.silveira.care360.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.ui.auth.WelcomeViewModel;
import com.silveira.care360.ui.auth.LoginEmailActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WelcomeActivity extends AppCompatActivity {

    private WelcomeViewModel viewModel;
    private MaterialButton btnGoogle, btnEmail;
    private TextView txtCrearCuenta, txtOlvidaste, txtLangEu, txtLangEs;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                viewModel.onGoogleSignInResult(result.getData());
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        viewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        btnGoogle = findViewById(R.id.btnGoogle);
        btnEmail = findViewById(R.id.btnEmail);
        txtCrearCuenta = findViewById(R.id.txtCrearCuenta);
        txtOlvidaste = findViewById(R.id.txtOlvidaste);
        txtLangEu = findViewById(R.id.txtLangEu);
        txtLangEs = findViewById(R.id.txtLangEs);
        // El progressBar es opcional según el XML
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnGoogle.setOnClickListener(v -> {
            Intent intent = viewModel.getGoogleSignInIntent();
            googleLauncher.launch(intent);
        });

        btnEmail.setOnClickListener(v ->
                startActivity(new Intent(this, LoginEmailActivity.class))
        );

        txtCrearCuenta.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        txtOlvidaste.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class))
        );

        // Listeners para cambio de idioma
        txtLangEu.setOnClickListener(v -> LanguageUtils.setLocale(this, "eu"));
        txtLangEs.setOnClickListener(v -> LanguageUtils.setLocale(this, "es"));
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (progressBar != null) {
                progressBar.setVisibility(state.isLoading ? View.VISIBLE : View.GONE);
            }
            btnGoogle.setEnabled(!state.isLoading);
            btnEmail.setEnabled(!state.isLoading);

            if (state.errorResId != null) {
                UiMessageUtils.show(this, getString(state.errorResId));
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
            viewModel.onNavigationDone();
        });
    }

    private void goToActivity(Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
