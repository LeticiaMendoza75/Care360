package com.silveira.care360.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.ActividadItem;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.ui.medicacion.MedicacionActivity;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final String CARE_PROFILE_PHOTO_PREFS = "care_profile_photo_prefs";

    public static final String EXTRA_SHOW_MEDICATION_ALERT = "show_medication_alert";
    public static final String EXTRA_ALERT_MEDICATION_ID = "alert_medication_id";
    public static final String EXTRA_ALERT_MEDICATION_NAME = "alert_medication_name";
    public static final String EXTRA_ALERT_MEDICATION_TIME = "alert_medication_time";
    public static final String EXTRA_ALERT_MEDICATION_DATE = "alert_medication_date";

    private HomeViewModel viewModel;

    private TextView txtNombreCuidada;
    private TextView txtGrupo;
    private TextView txtHeaderGroupName;
    private TextView txtCenteredHeaderGroupName;
    private TextView txtHeaderLanguage;
    private TextView txtHeaderMenu;
    private TextView txtStandaloneCareName;
    private TextView txtStandaloneCareAge;
    private TextView txtStandaloneEditProfile;
    private TextView txtMedicacionNombre;
    private TextView txtMedicacionHora;
    private TextView txtCitaTitulo;
    private TextView txtCitaHorario;
    private TextView txtCitaEncargada;
    private TextView txtIncidenciaTitulo;
    private TextView txtIncidenciaHorario;
    private TextView txtIncidenciaNivel;
    private TextView txtActividadRecienteCount;
    private TextView txtActividadRecienteHeadline;
    private TextView txtActividadRecienteDetail;
    private TextView txtActividadRecienteFooter;
    private TextView txtSeguimientoSaludSubtitulo;
    private TextView txtSeguimientoSaludDetalle;

    private MaterialCardView cardProximaMedicacion;
    private MaterialCardView cardProximaCita;
    private MaterialCardView cardUltimaIncidencia;
    private MaterialCardView cardActividad;
    private MaterialCardView cardSeguimientoSalud;
    private ImageButton btnAddMedicacion;
    private MaterialButton btnIncidencia;
    private MaterialButton btnSOS;
    private ImageButton btnVerMasIncidencias;
    private LinearLayout homeTopControlsLayout;
    private LinearLayout homeStandaloneProfileRow;
    private ImageView imgStandaloneProfileAvatar;
    private TextView txtStandaloneAddPhoto;
    private AlertDialog currentIncidenciasDialog;

    private Ringtone activeAlertRingtone;
    private boolean launchedFromMedicationAlert;
    private String lastResolvedGroupName;
    private String pendingProfilePhotoUri;
    private String pendingProfileCareName;
    private Integer pendingProfileCareAge;
    private String pendingProfileCarePhone;
    private String pendingProfileCareAddress;
    private String pendingProfileEmergencyContactName;
    private String pendingProfileEmergencyContactPhone;
    private String pendingProfileCareAllergies;
    private String pendingProfileCareConditions;
    private String pendingProfileGroupId;
    private boolean careProfileSaveConfirmed;
    private boolean reopenCareProfileOnResume;
    private String currentEmergencyContactName;
    private String currentEmergencyContactPhone;
    private ImageView pendingProfilePhotoTarget;
    private TextView pendingEmergencyContactSummaryTarget;
    private final ActivityResultLauncher<String[]> pickCarePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {
                }
                String targetGroupId = hasText(pendingProfileGroupId)
                        ? pendingProfileGroupId
                        : resolveActiveGroupIdForProfileStorage();
                String copiedPhotoUri = copyProfilePhotoToInternalStorage(uri, targetGroupId);
                pendingProfilePhotoUri = hasText(copiedPhotoUri) ? copiedPhotoUri : uri.toString();
                if (!hasText(copiedPhotoUri)) {
                    showUiMessage(getString(R.string.profile_photo_save_error));
                }
                if (pendingProfilePhotoTarget != null) {
                    loadCarePhotoInto(pendingProfilePhotoTarget, pendingProfilePhotoUri);
                }
            });
    private final ActivityResultLauncher<Intent> pickEmergencyContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result == null || result.getResultCode() != RESULT_OK) {
                    return;
                }
                Intent data = result.getData();
                Uri uri = data != null ? data.getData() : null;
                if (uri != null) {
                    bindEmergencyContactFromUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String navigationSource = getIntent().getStringExtra("navigation_source");
        boolean isLanguageRefresh = getIntent().getBooleanExtra(LanguageUtils.EXTRA_LANGUAGE_REFRESH, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View headerView = findViewById(R.id.headerUsuario);
        if (headerView != null) {
            txtHeaderGroupName = headerView.findViewById(R.id.txtHeaderGroupTitle);
            txtHeaderLanguage = headerView.findViewById(R.id.txtHeaderLanguage);
            txtHeaderMenu = headerView.findViewById(R.id.txtHeaderMenu);
            View headerLanguageSelector = headerView.findViewById(R.id.headerLanguageSelector);
            if (headerLanguageSelector != null) {
                headerLanguageSelector.setOnClickListener(v -> showHomeLanguageMenu());
            }
            if (txtHeaderMenu != null) {
                txtHeaderMenu.setOnClickListener(v -> showHomeUserMenuDialog());
            }
        }

        txtNombreCuidada = findViewById(R.id.txtNombreCuidada);
        txtGrupo = findViewById(R.id.txtGrupo);
        homeStandaloneProfileRow = findViewById(R.id.rowTopProfile);
        imgStandaloneProfileAvatar = findViewById(R.id.imgTopProfileAvatarStandalone);
        txtStandaloneAddPhoto = findViewById(R.id.txtTopProfileAddPhoto);
        txtStandaloneCareName = findViewById(R.id.txtNombreCuidadaStandalone);
        txtStandaloneCareAge = findViewById(R.id.txtEdadStandalone);
        txtMedicacionNombre = findViewById(R.id.txtMedicacionNombre);
        txtMedicacionHora = findViewById(R.id.txtMedicacionHora);
        cardProximaMedicacion = findViewById(R.id.cardProximaMedicacion);
        cardProximaCita = findViewById(R.id.cardB);
        cardUltimaIncidencia = findViewById(R.id.cardC);
        cardActividad = findViewById(R.id.cardD);
        cardSeguimientoSalud = findViewById(R.id.cardSeguimientoSalud);
        btnAddMedicacion = findViewById(R.id.btnAddMedicacion);
        btnIncidencia = findViewById(R.id.btnIncidencia);
        btnSOS = findViewById(R.id.btnSOS);
        bindCitaCardViews();
        bindIncidenciaCardViews();
        bindActividadCardViews();
        bindSeguimientoSaludCardViews();
        ensureEditProfileHint();
        ensureSeguimientoSaludCard();
        applyInitialHomeEmptyState();
        if (btnIncidencia != null) {
            btnIncidencia.setText(R.string.home_nueva_incidencia);
        }
        if (btnSOS != null) {
            btnSOS.setText(R.string.home_sos_mode);
            btnSOS.setOnClickListener(v -> showSosDialog());
        }
        setupHomeLanguageSwitcher();
        setupHomeCareProfileRow();
        applyLocalizedStaticTexts();

        observeViewModel();
        configurarHeader();

        cardProximaMedicacion.setOnClickListener(v -> viewModel.onProximaMedicacionClicked());
        if (cardProximaCita != null) {
            cardProximaCita.setOnClickListener(v -> viewModel.onProximaCitaClicked());
        }
        if (cardUltimaIncidencia != null) {
            cardUltimaIncidencia.setOnClickListener(v -> viewModel.onUltimaIncidenciaClicked());
        }
        if (cardActividad != null) {
            cardActividad.setOnClickListener(v -> viewModel.onActividadClicked());
        }
        View citaMore = findViewById(R.id.imgCitasMore);
        if (citaMore != null) {
            citaMore.setOnClickListener(v -> viewModel.onProximaCitaClicked());
        }
        View actividadMore = findViewById(R.id.imgActividadMore);
        if (actividadMore != null) {
            actividadMore.setOnClickListener(v -> viewModel.onActividadClicked());
        }
        if (cardSeguimientoSalud != null) {
            cardSeguimientoSalud.setOnClickListener(v -> viewModel.onSeguimientoClicked());
        }
        View seguimientoMore = findViewById(R.id.imgSeguimientoMore);
        if (seguimientoMore != null) {
            seguimientoMore.setOnClickListener(v -> viewModel.onSeguimientoClicked());
        }
        if (homeStandaloneProfileRow != null) {
            homeStandaloneProfileRow.setOnClickListener(v -> viewModel.onCareProfileClicked());
        }
        if (btnIncidencia != null) {
            btnIncidencia.setOnClickListener(v -> viewModel.onRegistrarIncidenciaClicked());
        }
        btnAddMedicacion.setOnClickListener(v -> viewModel.onAddMedicacionClicked());
        BottomNavManager.bind(this, BottomNavManager.Tab.HOME);

        requestNotificationPermissionIfNeeded();
        launchedFromMedicationAlert = isMedicationAlertIntent(getIntent());
        if (!launchedFromMedicationAlert) {
            if (isLanguageRefresh) {
                getIntent().removeExtra(LanguageUtils.EXTRA_LANGUAGE_REFRESH);
                viewModel.reloadHomeAfterLanguageChange();
            } else {
                viewModel.loadHomeData(navigationSource);
            }
        }
        handleMedicationAlertIntent(getIntent());
    }

    private void configurarHeader() {
        String email = viewModel.getLoggedUserEmail();

        HeaderManager.configurar(this, email, new HeaderManager.HeaderActions() {
            @Override
            public boolean shouldShowChangeGroup() {
                return true;
            }

            @Override
            public void onChangeGroup() {
                viewModel.onChangeGroupClicked();
            }

            @Override
            public void onLogout() {
                Log.d(TAG, "Cerrando sesion...");
                viewModel.performLogout();

                Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        View headerView = findViewById(R.id.headerUsuario);
        if (headerView != null) {
            View txtConectadoComo = headerView.findViewById(R.id.txtConectadoComo);
            View txtCambiarGrupo = headerView.findViewById(R.id.txtCambiarGrupo);
            View txtCerrarSesion = headerView.findViewById(R.id.txtCerrarSesion);
            if (txtConectadoComo != null) {
                txtConectadoComo.setVisibility(View.GONE);
            }
            if (txtCambiarGrupo != null) {
                txtCambiarGrupo.setVisibility(View.GONE);
            }
            if (txtCerrarSesion != null) {
                txtCerrarSesion.setVisibility(View.GONE);
            }
        }

        applyCurrentHeaderGroupName();
    }

    private void setupHomeLanguageSwitcher() {
        if (txtHeaderLanguage != null) {
            txtHeaderLanguage.setText(getCurrentLanguageLabel());
            return;
        }

        View headerView = findViewById(R.id.headerUsuario);
        if (!(headerView instanceof ConstraintLayout)) {
            return;
        }

        ConstraintLayout header = (ConstraintLayout) headerView;
        View rightColumnView = null;
        for (int i = 0; i < header.getChildCount(); i++) {
            View child = header.getChildAt(i);
            if (child instanceof LinearLayout) {
                rightColumnView = child;
            }
        }
        if (!(rightColumnView instanceof LinearLayout)) {
            return;
        }
        LinearLayout rightColumn = (LinearLayout) rightColumnView;
        ensureViewHasId(rightColumn);

        if (homeTopControlsLayout != null) {
            ViewParent currentParent = homeTopControlsLayout.getParent();
            if (currentParent instanceof ViewGroup) {
                ((ViewGroup) currentParent).removeView(homeTopControlsLayout);
            }
        }

        homeTopControlsLayout = new LinearLayout(this);
        homeTopControlsLayout.setId(View.generateViewId());
        homeTopControlsLayout.setOrientation(LinearLayout.HORIZONTAL);
        homeTopControlsLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        controlsParams.topMargin = dp(2);
        homeTopControlsLayout.setLayoutParams(controlsParams);

        LinearLayout languageLayout = new LinearLayout(this);
        languageLayout.setOrientation(LinearLayout.HORIZONTAL);
        languageLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        languageLayout.setClickable(true);
        languageLayout.setFocusable(true);
        TextView txtCurrentLang = new TextView(this);
        txtCurrentLang.setText(getCurrentLanguageLabel());
        txtCurrentLang.setTextColor(Color.WHITE);
        txtCurrentLang.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        txtCurrentLang.setTypeface(txtCurrentLang.getTypeface(), Typeface.BOLD);
        txtCurrentLang.setPadding(dp(6), dp(2), dp(6), dp(2));

        TextView txtArrow = new TextView(this);
        txtArrow.setText("▼");
        txtArrow.setTextColor(Color.WHITE);
        txtArrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        txtArrow.setPadding(0, dp(1), 0, 0);

        languageLayout.addView(txtCurrentLang);
        languageLayout.addView(txtArrow);
        languageLayout.setOnClickListener(v -> showHomeLanguageMenu());

        TextView txtMenu = new TextView(this);
        txtMenu.setText("...");
        txtMenu.setTextColor(Color.WHITE);
        txtMenu.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        txtMenu.setTypeface(txtMenu.getTypeface(), Typeface.BOLD);
        txtMenu.setPadding(dp(20), dp(0), dp(4), dp(0));
        txtMenu.setClickable(true);
        txtMenu.setFocusable(true);
        txtMenu.setOnClickListener(v -> showHomeUserMenuDialog());

        homeTopControlsLayout.addView(languageLayout);
        homeTopControlsLayout.addView(txtMenu);
        rightColumn.addView(homeTopControlsLayout);
    }

    private void setupCenteredHeaderGroupName() {
        if (txtHeaderGroupName != null && txtHeaderGroupName.getId() == R.id.txtHeaderGroupTitle) {
            applyCurrentHeaderGroupName();
            return;
        }

        View headerView = findViewById(R.id.headerUsuario);
        if (!(headerView instanceof ConstraintLayout)) {
            return;
        }
        ConstraintLayout header = (ConstraintLayout) headerView;
        View logo = header.findViewById(R.id.logo);
        if (logo == null) {
            return;
        }

        if (txtCenteredHeaderGroupName == null) {
            txtCenteredHeaderGroupName = new TextView(this);
            txtCenteredHeaderGroupName.setId(View.generateViewId());
            txtCenteredHeaderGroupName.setTextColor(Color.WHITE);
            txtCenteredHeaderGroupName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            txtCenteredHeaderGroupName.setTypeface(txtCenteredHeaderGroupName.getTypeface(), Typeface.BOLD);
            txtCenteredHeaderGroupName.setMaxLines(1);
            txtCenteredHeaderGroupName.setEllipsize(android.text.TextUtils.TruncateAt.END);
            txtCenteredHeaderGroupName.setGravity(android.view.Gravity.CENTER);
            header.addView(txtCenteredHeaderGroupName);

            ConstraintSet set = new ConstraintSet();
            set.clone(header);
            set.constrainWidth(txtCenteredHeaderGroupName.getId(), 0);
            set.constrainHeight(txtCenteredHeaderGroupName.getId(), ConstraintLayout.LayoutParams.WRAP_CONTENT);
            set.connect(txtCenteredHeaderGroupName.getId(), ConstraintSet.START, logo.getId(), ConstraintSet.END, dp(12));
            set.connect(txtCenteredHeaderGroupName.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dp(86));
            set.connect(txtCenteredHeaderGroupName.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
            set.connect(txtCenteredHeaderGroupName.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
            set.setHorizontalBias(txtCenteredHeaderGroupName.getId(), 0f);
            set.applyTo(header);
        }

        applyCurrentHeaderGroupName();
    }

    private void applyCurrentHeaderGroupName() {
        if (!hasText(lastResolvedGroupName)) {
            return;
        }
        if (txtHeaderGroupName != null && txtHeaderGroupName.getId() == R.id.txtHeaderGroupTitle) {
            txtHeaderGroupName.setVisibility(View.VISIBLE);
            txtHeaderGroupName.setText(lastResolvedGroupName);
        } else if (txtCenteredHeaderGroupName != null) {
            txtCenteredHeaderGroupName.setVisibility(View.VISIBLE);
            txtCenteredHeaderGroupName.setText(lastResolvedGroupName);
        } else if (txtHeaderGroupName != null) {
            txtHeaderGroupName.setVisibility(View.VISIBLE);
            txtHeaderGroupName.setText(lastResolvedGroupName);
        } else if (txtGrupo != null) {
            txtGrupo.setText(getString(R.string.home_grupo, lastResolvedGroupName));
        }
    }

    private void showHomeLanguageMenu() {
        String[] labels = new String[]{getString(R.string.lang_es), getString(R.string.lang_eu)};
        String[] codes = new String[]{"es", "eu"};
        new AlertDialog.Builder(this)
                .setTitle(R.string.language_selector_title)
                .setItems(labels, (dialog, which) -> LanguageUtils.setLocale(this, codes[which]))
                .show();
    }

    private String getCurrentLanguageLabel() {
        String code = getResources().getConfiguration().getLocales().get(0).getLanguage();
        if ("eu".equalsIgnoreCase(code)) {
            return getString(R.string.lang_eu);
        }
        return getString(R.string.lang_es);
    }

    private void setupHomeCareProfileRow() {
        if (txtGrupo != null) {
            txtGrupo.setVisibility(View.GONE);
        }
        if (txtNombreCuidada != null) txtNombreCuidada.setVisibility(View.GONE);
        View rowEdad = findViewById(R.id.rowEdad);
        if (rowEdad != null) rowEdad.setVisibility(View.GONE);
        if (homeStandaloneProfileRow != null) {
            homeStandaloneProfileRow.setVisibility(View.VISIBLE);
            homeStandaloneProfileRow.bringToFront();
        }

        if (imgStandaloneProfileAvatar != null) {
            imgStandaloneProfileAvatar.setVisibility(View.VISIBLE);
            imgStandaloneProfileAvatar.setImageResource(R.drawable.ic_avatar_neutro);
            imgStandaloneProfileAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgStandaloneProfileAvatar.setContentDescription(getString(R.string.home_care_avatar_cd));
        }
        updateStandalonePhotoPlaceholder(false);
        if (txtStandaloneCareName != null) {
            txtStandaloneCareName.setVisibility(View.VISIBLE);
        }
        ensureEditProfileHint();
        updateCareProfileSummary(null, null);
    }

    private void ensureEditProfileHint() {
        if (homeStandaloneProfileRow == null) {
            return;
        }
        View details = homeStandaloneProfileRow.getChildCount() > 1 ? homeStandaloneProfileRow.getChildAt(1) : null;
        if (!(details instanceof LinearLayout)) {
            return;
        }
        LinearLayout detailsColumn = (LinearLayout) details;
        if (txtStandaloneEditProfile == null) {
            View existing = detailsColumn.findViewWithTag("edit_profile_hint");
            if (existing instanceof TextView) {
                txtStandaloneEditProfile = (TextView) existing;
            }
        }
        if (txtStandaloneEditProfile == null) {
            txtStandaloneEditProfile = new TextView(this);
            txtStandaloneEditProfile.setTag("edit_profile_hint");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(6);
            txtStandaloneEditProfile.setLayoutParams(params);
            txtStandaloneEditProfile.setTextColor(Color.parseColor("#EAF3FF"));
            txtStandaloneEditProfile.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            txtStandaloneEditProfile.setTypeface(txtStandaloneEditProfile.getTypeface(), Typeface.BOLD);
            detailsColumn.addView(txtStandaloneEditProfile);
        }
        txtStandaloneEditProfile.setVisibility(View.VISIBLE);
        txtStandaloneEditProfile.setText(R.string.profile_edit_profile);
    }

    private void ensureSeguimientoSaludCard() {
        cardSeguimientoSalud = findViewById(R.id.cardSeguimientoSalud);
        if (cardSeguimientoSalud == null) {
            return;
        }
    }

    private void showHomeUserMenuDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(18), dp(20), dp(8));

        LinearLayout profileRow = new LinearLayout(this);
        profileRow.setOrientation(LinearLayout.HORIZONTAL);
        profileRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        avatar.setLayoutParams(avatarParams);
        avatar.setGravity(android.view.Gravity.CENTER);
        avatar.setTextColor(Color.WHITE);
        avatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        avatar.setTypeface(avatar.getTypeface(), Typeface.BOLD);
        avatar.setText(getAvatarInitial());
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setColor(Color.parseColor("#2F6EF7"));
        avatarBg.setShape(GradientDrawable.OVAL);
        avatar.setBackground(avatarBg);

        TextView txtEmail = new TextView(this);
        LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        emailParams.setMarginStart(dp(12));
        txtEmail.setLayoutParams(emailParams);
        txtEmail.setText(viewModel.getLoggedUserEmail());
        txtEmail.setTextColor(Color.WHITE);
        txtEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        txtEmail.setTypeface(txtEmail.getTypeface(), Typeface.BOLD);

        profileRow.addView(avatar);
        profileRow.addView(txtEmail);

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        dividerParams.topMargin = dp(14);
        dividerParams.bottomMargin = dp(10);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#55FFFFFF"));

        TextView txtCambiarGrupo = createMenuItemText(R.string.header_change_group);
        TextView txtCerrarSesion = createMenuItemText(R.string.header_logout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(container)
                .create();

        txtCambiarGrupo.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.onChangeGroupClicked();
        });

        txtCerrarSesion.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.performLogout();
            Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        container.addView(profileRow);
        container.addView(divider);
        container.addView(txtCambiarGrupo);
        container.addView(txtCerrarSesion);

        dialog.show();
    }

    private TextView createMenuItemText(int textRes) {
        TextView tv = new TextView(this);
        tv.setText(textRes);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tv.setPadding(dp(4), dp(10), dp(4), dp(10));
        tv.setClickable(true);
        tv.setFocusable(true);
        return tv;
    }

    private String getAvatarInitial() {
        String email = viewModel.getLoggedUserEmail();
        if (hasText(email)) {
            return email.substring(0, 1).toUpperCase(Locale.getDefault());
        }
        return "U";
    }

    private void applyLocalizedStaticTexts() {
        TextView txtTituloProximaMedicacion = findViewById(R.id.txtTituloProximaMedicacion);
        if (txtTituloProximaMedicacion != null) {
            txtTituloProximaMedicacion.setText(R.string.home_proxima_medicacion);
        }

        TextView txtCitasLabel = findViewById(R.id.txtCitasLabel);
        if (txtCitasLabel != null) {
            txtCitasLabel.setText(R.string.home_proxima_cita);
        }

        if (cardProximaCita != null) {
            View content = cardProximaCita.getChildAt(0);
            if (content instanceof LinearLayout) {
                LinearLayout vertical = (LinearLayout) content;
                if (vertical.getChildCount() > 0 && vertical.getChildAt(0) instanceof LinearLayout) {
                    LinearLayout header = (LinearLayout) vertical.getChildAt(0);
                    if (header.getChildCount() > 1 && header.getChildAt(1) instanceof TextView) {
                        ((TextView) header.getChildAt(1)).setText(R.string.home_proxima_cita);
                    }
                    if (header.getChildCount() > 0 && header.getChildAt(0) instanceof android.widget.ImageView) {
                        header.getChildAt(0).setContentDescription(getString(R.string.nav_citas));
                    }
                }
            }
        }

        if (cardUltimaIncidencia != null) {
            View content = cardUltimaIncidencia.getChildAt(0);
            if (content instanceof LinearLayout) {
                LinearLayout vertical = (LinearLayout) content;
                if (vertical.getChildCount() > 0 && vertical.getChildAt(0) instanceof LinearLayout) {
                    LinearLayout header = (LinearLayout) vertical.getChildAt(0);
                    if (header.getChildCount() > 1 && header.getChildAt(1) instanceof TextView) {
                        ((TextView) header.getChildAt(1)).setText(R.string.home_ultima_incidencia);
                    }
                    if (header.getChildCount() > 0 && header.getChildAt(0) instanceof android.widget.ImageView) {
                        header.getChildAt(0).setContentDescription(getString(R.string.home_ultima_incidencia));
                    }
                }
            }
        }

        if (cardActividad != null) {
            View content = cardActividad.getChildAt(0);
            if (content instanceof LinearLayout) {
                LinearLayout vertical = (LinearLayout) content;
                if (vertical.getChildCount() > 0 && vertical.getChildAt(0) instanceof LinearLayout) {
                    LinearLayout header = (LinearLayout) vertical.getChildAt(0);
                    if (header.getChildCount() > 1 && header.getChildAt(1) instanceof TextView) {
                        ((TextView) header.getChildAt(1)).setText(R.string.activity_recent_title);
                    }
                    if (header.getChildCount() > 0 && header.getChildAt(0) instanceof android.widget.ImageView) {
                        header.getChildAt(0).setContentDescription(getString(R.string.activity_recent_title));
                    }
                }
                if (vertical.getChildCount() > 1 && vertical.getChildAt(1) instanceof TextView) {
                    ((TextView) vertical.getChildAt(1)).setText(R.string.activity_recent_subtitle_zero);
                }
            }
        }

        if (cardSeguimientoSalud != null && cardSeguimientoSalud.getChildCount() > 0 && cardSeguimientoSalud.getChildAt(0) instanceof LinearLayout) {
            LinearLayout vertical = (LinearLayout) cardSeguimientoSalud.getChildAt(0);
            if (vertical.getChildCount() > 0 && vertical.getChildAt(0) instanceof LinearLayout) {
                LinearLayout header = (LinearLayout) vertical.getChildAt(0);
                if (header.getChildCount() > 1 && header.getChildAt(1) instanceof TextView) {
                    ((TextView) header.getChildAt(1)).setText(R.string.home_health_title);
                }
                if (header.getChildCount() > 0 && header.getChildAt(0) instanceof ImageView) {
                    header.getChildAt(0).setContentDescription(getString(R.string.home_health_title));
                }
            }
            if (vertical.getChildCount() > 1 && vertical.getChildAt(1) instanceof TextView) {
                ((TextView) vertical.getChildAt(1)).setText(R.string.home_health_subtitle);
            }
            if (vertical.getChildCount() > 2 && vertical.getChildAt(2) instanceof TextView) {
                ((TextView) vertical.getChildAt(2)).setText(R.string.home_health_empty);
            }
        }

        if (txtStandaloneEditProfile != null) {
            txtStandaloneEditProfile.setText(R.string.profile_edit_profile);
        }

        applyLocalizedBottomNavTexts();
    }

    private void applyLocalizedBottomNavTexts() {
        setBottomNavLabel(R.id.navInicio, R.string.nav_inicio);
        setBottomNavLabel(R.id.navMedicacion, R.string.nav_medicacion);
        setBottomNavLabel(R.id.navCitas, R.string.nav_citas);
        setBottomNavLabel(R.id.navDocumentos, R.string.nav_docs);
        setBottomNavLabel(R.id.navFamilia, R.string.nav_familia);
    }

    private void setBottomNavLabel(int navId, int textRes) {
        View nav = findViewById(navId);
        if (!(nav instanceof LinearLayout)) {
            return;
        }
        LinearLayout item = (LinearLayout) nav;
        for (int i = 0; i < item.getChildCount(); i++) {
            View child = item.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setText(textRes);
            } else if (child instanceof android.widget.ImageView) {
                child.setContentDescription(getString(textRes));
            }
        }
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) return;

            if (state.groupName != null && !state.groupName.trim().isEmpty()) {
                lastResolvedGroupName = state.groupName.trim();
                applyCurrentHeaderGroupName();
            }

            if (txtNombreCuidada != null && state.careName != null) {
                txtNombreCuidada.setText(state.careName);
            }
            updateCareProfileSummary(state.careName, state.careAge);
            if (imgStandaloneProfileAvatar != null) {
                imgStandaloneProfileAvatar.setImageResource(R.drawable.ic_avatar_neutro);
                updateStandalonePhotoPlaceholder(false);
            }
            currentEmergencyContactName = state.emergencyContactName;
            currentEmergencyContactPhone = state.emergencyContactPhone;

            boolean hasNextReminder = state.nextMedicacionHora != null && !state.nextMedicacionHora.trim().isEmpty();
            if (hasNextReminder) {
                txtMedicacionNombre.setText(state.nextMedicacionHora.trim());
                txtMedicacionHora.setText(state.nextMedicacionAlertasActivas
                        ? getString(R.string.home_aviso_activo)
                        : "");
            } else {
                txtMedicacionNombre.setText(R.string.home_proxima_medicacion_vacia);
                txtMedicacionHora.setText(R.string.home_proxima_medicacion_sin_datos);
            }

            if (txtCitaTitulo != null && txtCitaHorario != null) {
                boolean hasNextCita = state.nextCitaHorario != null && !state.nextCitaHorario.trim().isEmpty();
                if (hasNextCita) {
                    txtCitaTitulo.setText(hasText(state.nextCitaTitulo) ? state.nextCitaTitulo.trim() : getString(R.string.citas_generica));
                    txtCitaHorario.setText(state.nextCitaHorario.trim());
                    if (txtCitaEncargada != null) {
                        if (hasText(state.nextCitaEncargada)) {
                            txtCitaEncargada.setVisibility(View.VISIBLE);
                            txtCitaEncargada.setText(getString(R.string.citas_persona_encargada_resumen, state.nextCitaEncargada.trim()));
                        } else {
                            txtCitaEncargada.setVisibility(View.GONE);
                            txtCitaEncargada.setText("");
                        }
                    }
                } else {
                    txtCitaTitulo.setText(R.string.home_proxima_cita_vacia);
                    txtCitaHorario.setText(R.string.home_proxima_cita_sin_datos);
                    if (txtCitaEncargada != null) {
                        txtCitaEncargada.setVisibility(View.GONE);
                        txtCitaEncargada.setText("");
                    }
                }
            }

            if (txtIncidenciaTitulo != null && txtIncidenciaHorario != null) {
                boolean hasLastIncidencia = state.lastIncidenciaHorario != null && !state.lastIncidenciaHorario.trim().isEmpty();
                if (hasLastIncidencia) {
                    txtIncidenciaTitulo.setText(buildIncidenciaCardTitle(state.lastIncidenciaTipo, state.lastIncidenciaHorario));
                    txtIncidenciaHorario.setText(buildIncidenciaCardSubtitle(state.lastIncidenciaDescripcion));
                    applyIncidenciaNivelStyle(state.lastIncidenciaNivel);
                } else {
                    txtIncidenciaTitulo.setText(R.string.home_ultima_incidencia_vacia);
                    txtIncidenciaHorario.setText(R.string.home_ultima_incidencia_sin_datos);
                    applyIncidenciaNivelStyle(null);
                }
            }

            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                showUiMessage(state.errorMessage);
            }

            txtGrupo.setEnabled(!state.isLoading);
            if (btnIncidencia != null) {
                btnIncidencia.setEnabled(!state.isLoading);
            }
        });

        viewModel.groupSelectorEvent.observe(this, selectorState -> {
            if (selectorState == null) return;
            showGroupSelector(selectorState.memberships, selectorState.mandatory);
            viewModel.onGroupSelectorHandled();
        });

        viewModel.navigation.observe(this, nav -> {
            if (nav == null) return;

            switch (nav) {
                case GROUP_ENTRY:
                    goToGroupEntry();
                    break;
                case WELCOME:
                    goToWelcome();
                    break;
            }

            viewModel.onNavigationHandled();
        });

        viewModel.action.observe(this, action -> {
            if (action == null) return;

            if (action instanceof HomeViewModel.NavigateToMedicacionAction) {
                abrirPantallaMedicacion();
            } else if (action instanceof HomeViewModel.NavigateToCitasAction) {
                abrirPantallaCitas();
            } else if (action instanceof HomeViewModel.NavigateToActividadAction) {
                startActivity(new Intent(this, DocsActivity.class));
            } else if (action instanceof HomeViewModel.NavigateToSeguimientoAction) {
                openSeguimiento(((HomeViewModel.NavigateToSeguimientoAction) action).groupId);
            } else if (action instanceof HomeViewModel.NavigateToPatologiasAction) {
                openPatologias(((HomeViewModel.NavigateToPatologiasAction) action).groupId);
            } else if (action instanceof HomeViewModel.ShowCareProfileAction) {
                HomeViewModel.ShowCareProfileAction profileAction = (HomeViewModel.ShowCareProfileAction) action;
                showCareProfileDialog(profileAction.careName, profileAction.careAge, profileAction.carePhotoUri,
                        profileAction.carePhone, profileAction.careAddress,
                        profileAction.emergencyContactName, profileAction.emergencyContactPhone,
                        profileAction.careAllergies, profileAction.careConditions, profileAction.groupId);
            } else if (action instanceof HomeViewModel.ShowIncidenciaEditorAction) {
                showIncidenciaEditorDialog();
            } else if (action instanceof HomeViewModel.ShowIncidenciasHistoryAction) {
                showIncidenciasHistoryDialog(((HomeViewModel.ShowIncidenciasHistoryAction) action).incidencias);
            } else if (action instanceof HomeViewModel.ConfirmDeleteIncidenciaAction) {
                showDeleteIncidenciaDialog(((HomeViewModel.ConfirmDeleteIncidenciaAction) action).incidencia);
            } else if (action instanceof HomeViewModel.ShareIncidenciasPdfAction) {
                HomeViewModel.ShareIncidenciasPdfAction shareAction = (HomeViewModel.ShareIncidenciasPdfAction) action;
                sharePdf(shareAction.uri, shareAction.fileName, getString(R.string.incidencias_pdf_title));
            } else if (action instanceof HomeViewModel.ShowMessageAction) {
                showUiMessage(((HomeViewModel.ShowMessageAction) action).message);
            }

            viewModel.onActionHandled();
        });

        viewModel.recentActivity.observe(this, recentActivityState -> {
            if (recentActivityState == null) {
                return;
            }
            renderRecentActivity(recentActivityState.latestItem, recentActivityState.todayCount);
            if (hasText(recentActivityState.errorMessage)) {
                showUiMessage(recentActivityState.errorMessage);
            }
        });

        viewModel.healthSummary.observe(this, healthSummaryState -> {
            if (healthSummaryState == null) {
                return;
            }
            renderHealthSummary(healthSummaryState);
        });
    }

    private void showIncidenciaEditorDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        container.setPadding(padding, dp(12), padding, dp(4));

        TextView txtRequired = new TextView(this);
        txtRequired.setText(R.string.incidencias_campos_obligatorios);
        txtRequired.setTextColor(0xFF7A8695);
        txtRequired.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams requiredParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        requiredParams.bottomMargin = dp(10);
        txtRequired.setLayoutParams(requiredParams);

        EditText etTipo = createField(getString(R.string.incidencias_tipo_req));
        EditText etFecha = createPickerField(getString(R.string.incidencias_fecha_req));
        EditText etHora = createPickerField(getString(R.string.incidencias_hora_req));
        EditText etNivel = createPickerField(getString(R.string.incidencias_nivel_req));
        EditText etDescripcion = createField(getString(R.string.incidencias_descripcion));
        etDescripcion.setMinLines(3);
        etDescripcion.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

        etFecha.setOnClickListener(v -> showDatePicker(etFecha));
        etHora.setOnClickListener(v -> showTimePicker(etHora));
        etNivel.setOnClickListener(v -> showNivelSelector(etNivel));

        container.addView(txtRequired);
        container.addView(etTipo);
        container.addView(etFecha);
        container.addView(etHora);
        container.addView(etNivel);
        container.addView(etDescripcion);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.home_registrar_incidencia)
                .setView(container)
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.citas_guardar, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String tipo = textOrFallback(etTipo.getText().toString(), "");
            String fecha = textOrFallback(etFecha.getText().toString(), "");
            String hora = textOrFallback(etHora.getText().toString(), "");
            String nivel = textOrFallback(etNivel.getText().toString(), "");
            String descripcion = textOrFallback(etDescripcion.getText().toString(), "");

            if (!hasText(tipo) || !hasText(fecha) || !hasText(hora) || !hasText(nivel)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.incidencias_incomplete_title)
                        .setMessage(R.string.incidencias_incomplete_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }

            viewModel.onIncidenciaEditorConfirmed(tipo, fecha, hora, nivel, descripcion);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showNivelSelector(EditText target) {
        String[] niveles = new String[]{
                getString(R.string.incidencias_nivel_leve),
                getString(R.string.incidencias_nivel_moderada),
                getString(R.string.incidencias_nivel_urgente)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.incidencias_nivel_req)
                .setItems(niveles, (dialog, which) -> target.setText(niveles[which]))
                .show();
    }

    private void showIncidenciasHistoryDialog(List<Incidencia> incidencias) {
        if (currentIncidenciasDialog != null && currentIncidenciasDialog.isShowing()) {
            currentIncidenciasDialog.dismiss();
        }
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(8));
        scrollView.addView(container, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        if (incidencias == null || incidencias.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.home_ultima_incidencia_sin_datos);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            empty.setTextColor(0xFF4B5563);
            container.addView(empty);
        } else {
            for (int i = 0; i < incidencias.size(); i++) {
                Incidencia incidencia = incidencias.get(i);
                container.addView(createIncidenciaHistoryItem(incidencia));
            }
        }

        currentIncidenciasDialog = new AlertDialog.Builder(this)
                .setCustomTitle(createIncidenciasDialogHeader(incidencias))
                .setView(scrollView)
                .setNegativeButton(R.string.citas_cerrar, null)
                .setPositiveButton(R.string.incidencias_nueva, (dialog, which) -> viewModel.onRegistrarIncidenciaClicked())
                .show();
    }

    private View createIncidenciasDialogHeader(List<Incidencia> incidencias) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.setPadding(dp(24), dp(18), dp(12), dp(0));
        header.setBackgroundColor(Color.parseColor("#16324F"));

        TextView title = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        title.setLayoutParams(titleParams);
        title.setText(R.string.incidencias_titulo);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTextColor(Color.WHITE);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);

        ImageButton download = new ImageButton(this);
        LinearLayout.LayoutParams downloadParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        download.setLayoutParams(downloadParams);
        download.setBackgroundResource(android.R.drawable.list_selector_background);
        download.setImageResource(android.R.drawable.stat_sys_download_done);
        download.setColorFilter(Color.WHITE);
        download.setContentDescription(getString(R.string.incidencias_descargar_pdf));
        download.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        download.setPadding(dp(8), dp(8), dp(8), dp(8));
        download.setOnClickListener(v -> viewModel.onExportIncidenciasPdfClicked(incidencias));

        header.addView(title);
        header.addView(download);
        return header;
    }

    private View createIncidenciaHistoryItem(Incidencia incidencia) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(14), dp(12), dp(14), dp(12));

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xFFF8FBFF);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), 0xFFD9E2EC);
        item.setBackground(bg);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.bottomMargin = dp(10);
        item.setLayoutParams(itemParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        title.setLayoutParams(titleParams);
        title.setText(hasText(incidencia != null ? incidencia.getTipo() : null)
                ? incidencia.getTipo().trim()
                : getString(R.string.home_ultima_incidencia));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTextColor(0xFF1F2937);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        MaterialButton btnDelete = new MaterialButton(this);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnDelete.setLayoutParams(deleteParams);
        btnDelete.setAllCaps(false);
        btnDelete.setInsetTop(0);
        btnDelete.setInsetBottom(0);
        btnDelete.setMinHeight(0);
        btnDelete.setMinimumHeight(0);
        btnDelete.setText(R.string.incidencias_delete_short);
        btnDelete.setOnClickListener(v -> viewModel.onDeleteIncidenciaClicked(incidencia));

        header.addView(title);
        header.addView(btnDelete);

        TextView meta = new TextView(this);
        meta.setText(buildIncidenciaMeta(incidencia));
        meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        meta.setTextColor(0xFF4B5563);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        metaParams.topMargin = dp(4);
        meta.setLayoutParams(metaParams);

        item.addView(header);
        item.addView(meta);

        if (hasText(incidencia != null ? incidencia.getDescripcion() : null)) {
            TextView desc = new TextView(this);
            desc.setText(incidencia.getDescripcion().trim());
            desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            desc.setTextColor(0xFF374151);
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            descParams.topMargin = dp(6);
            desc.setLayoutParams(descParams);
            item.addView(desc);
        }

        return item;
    }

    private void showDeleteIncidenciaDialog(Incidencia incidencia) {
        if (incidencia == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.incidencias_delete_title)
                .setMessage(R.string.incidencias_delete_message)
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.incidencias_delete_confirm, (dialog, which) -> viewModel.confirmDeleteIncidencia(incidencia))
                .show();
    }

    private void showCareProfileDialog(String careName, Integer careAge, String carePhotoUri,
                                       String carePhone, String careAddress,
                                       String emergencyContactName, String emergencyContactPhone,
                                       String careAllergies, String careConditions,
                                       String requestedGroupId) {
        String activeGroupId = hasText(requestedGroupId)
                ? requestedGroupId
                : resolveActiveGroupIdForProfileStorage();

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(8));

        TextView txtPersonalSection = createSectionLabel(R.string.profile_section_personal);

        EditText etName = createField(getString(R.string.profile_care_name_req));
        etName.setText(textOrFallback(careName, ""));

        EditText etAge = createField(getString(R.string.profile_care_age));
        etAge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (careAge != null && careAge > 0) {
            etAge.setText(String.valueOf(careAge));
        }

        EditText etPhone = createField(getString(R.string.profile_care_phone));
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        etPhone.setText(textOrFallback(carePhone, ""));

        EditText etAddress = createField(getString(R.string.profile_care_address));
        etAddress.setText(textOrFallback(careAddress, ""));

        MaterialButton btnEmergencyContact = new MaterialButton(this);
        btnEmergencyContact.setText(R.string.profile_select_emergency_contact);
        btnEmergencyContact.setAllCaps(false);
        btnEmergencyContact.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView txtEmergencySummary = new TextView(this);
        txtEmergencySummary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        txtEmergencySummary.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        summaryParams.bottomMargin = dp(10);
        txtEmergencySummary.setLayoutParams(summaryParams);
        txtEmergencySummary.setText(buildEmergencyContactSummary(emergencyContactName, emergencyContactPhone));

        TextView txtHealthSection = createSectionLabel(R.string.profile_section_health);

        MaterialButton btnPatologias = new MaterialButton(this);
        btnPatologias.setText(R.string.profile_manage_pathologies);
        btnPatologias.setAllCaps(false);
        btnPatologias.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        MaterialButton btnSeguimiento = new MaterialButton(this);
        btnSeguimiento.setText(R.string.profile_view_follow_up);
        btnSeguimiento.setAllCaps(false);
        LinearLayout.LayoutParams seguimientoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        seguimientoParams.topMargin = dp(6);
        btnSeguimiento.setLayoutParams(seguimientoParams);

        MaterialButton btnMedicacion = new MaterialButton(this);
        btnMedicacion.setText(R.string.profile_add_medicacion);
        btnMedicacion.setAllCaps(false);
        btnMedicacion.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        MaterialButton btnCita = new MaterialButton(this);
        btnCita.setText(R.string.profile_add_cita);
        btnCita.setAllCaps(false);
        LinearLayout.LayoutParams citaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        citaParams.topMargin = dp(8);
        btnCita.setLayoutParams(citaParams);

        pendingProfileCareName = textOrFallback(careName, "");
        pendingProfileCareAge = careAge;
        pendingProfileCarePhone = textOrFallback(carePhone, "");
        pendingProfileCareAddress = textOrFallback(careAddress, "");
        pendingProfileEmergencyContactName = textOrFallback(emergencyContactName, "");
        pendingProfileEmergencyContactPhone = textOrFallback(emergencyContactPhone, "");
        pendingProfileCareAllergies = textOrFallback(careAllergies, "");
        pendingProfileCareConditions = textOrFallback(careConditions, "");
        pendingProfileGroupId = activeGroupId;
        careProfileSaveConfirmed = false;
        pendingProfilePhotoUri = null;
        pendingProfilePhotoTarget = null;
        pendingEmergencyContactSummaryTarget = txtEmergencySummary;
        btnEmergencyContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            pickEmergencyContactLauncher.launch(intent);
        });

        container.addView(txtPersonalSection);
        container.addView(etName);
        container.addView(etAge);
        container.addView(etPhone);
        container.addView(etAddress);
        container.addView(btnEmergencyContact);
        container.addView(txtEmergencySummary);
        container.addView(txtHealthSection);
        container.addView(btnPatologias);
        container.addView(btnSeguimiento);
        container.addView(btnMedicacion);
        container.addView(btnCita);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(container, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.profile_title)
                .setView(scrollView)
                .setNegativeButton(R.string.citas_cancelar, null)
                .setPositiveButton(R.string.citas_guardar, (d, w) -> {
                    careProfileSaveConfirmed = true;
                    Integer parsedAge = null;
                    if (hasText(etAge.getText() != null ? etAge.getText().toString() : null)) {
                        try {
                            parsedAge = Integer.parseInt(etAge.getText().toString().trim());
                        } catch (NumberFormatException ignored) {
                            parsedAge = null;
                        }
                    }
                    String currentName = textOrFallback(etName.getText() != null ? etName.getText().toString() : null, "");
                    pendingProfileCareName = currentName;
                    pendingProfileCareAge = parsedAge;
                    pendingProfileCarePhone = textOrFallback(etPhone.getText() != null ? etPhone.getText().toString() : null, "");
                    pendingProfileCareAddress = textOrFallback(etAddress.getText() != null ? etAddress.getText().toString() : null, "");
                    viewModel.onCareProfileSaved(
                            currentName,
                            parsedAge,
                            null,
                            pendingProfileCarePhone,
                            pendingProfileCareAddress,
                            pendingProfileEmergencyContactName,
                            pendingProfileEmergencyContactPhone,
                            pendingProfileCareAllergies,
                            pendingProfileCareConditions
                    );
                })
                .create();

        btnMedicacion.setOnClickListener(v -> {
            cachePendingCareProfileDraft(etName, etAge, etPhone, etAddress, null, null);
            reopenCareProfileOnResume = true;
            dialog.dismiss();
            viewModel.onAddMedicacionClicked();
        });
        btnCita.setOnClickListener(v -> {
            cachePendingCareProfileDraft(etName, etAge, etPhone, etAddress, null, null);
            reopenCareProfileOnResume = true;
            dialog.dismiss();
            viewModel.onProximaCitaClicked();
        });
        btnPatologias.setOnClickListener(v -> {
            cachePendingCareProfileDraft(etName, etAge, etPhone, etAddress, null, null);
            reopenCareProfileOnResume = true;
            dialog.dismiss();
            viewModel.onPatologiasClicked();
        });
        btnSeguimiento.setOnClickListener(v -> {
            cachePendingCareProfileDraft(etName, etAge, etPhone, etAddress, null, null);
            reopenCareProfileOnResume = true;
            dialog.dismiss();
            viewModel.onSeguimientoClicked();
        });

        dialog.setOnDismissListener(d -> {
            pendingProfilePhotoTarget = null;
            pendingEmergencyContactSummaryTarget = null;
            if (!reopenCareProfileOnResume) {
                clearPendingCareProfileDraft();
            }
        });

        dialog.show();
    }

    private void cachePendingCareProfileDraft(EditText etName, EditText etAge,
                                              EditText etPhone, EditText etAddress,
                                              EditText etAllergies, EditText etConditions) {
        pendingProfileCareName = textOrFallback(etName != null && etName.getText() != null ? etName.getText().toString() : null, "");
        pendingProfileCareAge = parseOptionalInteger(etAge != null && etAge.getText() != null ? etAge.getText().toString() : null);
        pendingProfileCarePhone = textOrFallback(etPhone != null && etPhone.getText() != null ? etPhone.getText().toString() : null, "");
        pendingProfileCareAddress = textOrFallback(etAddress != null && etAddress.getText() != null ? etAddress.getText().toString() : null, "");
        pendingProfileCareAllergies = textOrFallback(etAllergies != null && etAllergies.getText() != null ? etAllergies.getText().toString() : null, "");
        pendingProfileCareConditions = textOrFallback(etConditions != null && etConditions.getText() != null ? etConditions.getText().toString() : null, "");
    }

    private Integer parseOptionalInteger(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void clearPendingCareProfileDraft() {
        pendingProfileCareName = null;
        pendingProfileCareAge = null;
        pendingProfileCarePhone = null;
        pendingProfileCareAddress = null;
        pendingProfileEmergencyContactName = null;
        pendingProfileEmergencyContactPhone = null;
        pendingProfileCareAllergies = null;
        pendingProfileCareConditions = null;
        pendingProfileGroupId = null;
        pendingProfilePhotoUri = null;
        pendingProfilePhotoTarget = null;
        pendingEmergencyContactSummaryTarget = null;
        careProfileSaveConfirmed = false;
    }

    private void reopenCareProfileIfNeeded() {
        if (!reopenCareProfileOnResume) {
            return;
        }
        reopenCareProfileOnResume = false;
        showCareProfileDialog(
                pendingProfileCareName,
                pendingProfileCareAge,
                pendingProfilePhotoUri,
                pendingProfileCarePhone,
                pendingProfileCareAddress,
                pendingProfileEmergencyContactName,
                pendingProfileEmergencyContactPhone,
                pendingProfileCareAllergies,
                pendingProfileCareConditions,
                pendingProfileGroupId
        );
    }

    private void bindEmergencyContactFromUri(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    uri,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    null,
                    null,
                    null
            );
            if (cursor == null || !cursor.moveToFirst()) {
                showUiMessage(getString(R.string.profile_emergency_contact_pick_error));
                return;
            }
            String name = cursor.getString(0);
            String phone = cursor.getString(1);
            pendingProfileEmergencyContactName = textOrFallback(name, "");
            pendingProfileEmergencyContactPhone = textOrFallback(phone, "");
            if (pendingEmergencyContactSummaryTarget != null) {
                pendingEmergencyContactSummaryTarget.setText(
                        buildEmergencyContactSummary(pendingProfileEmergencyContactName, pendingProfileEmergencyContactPhone)
                );
            }
        } catch (Exception ignored) {
            showUiMessage(getString(R.string.profile_emergency_contact_pick_error));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String buildEmergencyContactSummary(String name, String phone) {
        if (!hasText(name) && !hasText(phone)) {
            return getString(R.string.profile_emergency_contact_empty);
        }
        if (hasText(name) && hasText(phone)) {
            return name.trim() + "\n" + phone.trim();
        }
        return hasText(name) ? name.trim() : phone.trim();
    }

    private void showUiMessage(String message) {
        if (!hasText(message)) {
            return;
        }
        View root = findViewById(android.R.id.content);
        if (root != null) {
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
            return;
        }
        UiMessageUtils.show(this, message);
    }

    private void updateStandalonePhotoPlaceholder(boolean showAddPhoto) {
        if (txtStandaloneAddPhoto != null) {
            txtStandaloneAddPhoto.setVisibility(showAddPhoto ? View.VISIBLE : View.GONE);
        }
        if (imgStandaloneProfileAvatar == null) {
            return;
        }
        if (showAddPhoto) {
            imgStandaloneProfileAvatar.setImageDrawable(null);
            imgStandaloneProfileAvatar.setBackgroundColor(Color.TRANSPARENT);
        } else {
            imgStandaloneProfileAvatar.setBackground(null);
        }
    }

    private void syncStandalonePhotoWithGroup(String groupId, String photoUri) {
        if (imgStandaloneProfileAvatar == null || !hasText(groupId)) {
            return;
        }
        String activeGroupId = resolveActiveGroupIdForProfileStorage();
        if (!hasText(activeGroupId) || !groupId.trim().equals(activeGroupId.trim())) {
            return;
        }
        String displayPhotoUri = resolveDisplayCarePhotoUri(photoUri, groupId);
        loadCarePhotoInto(imgStandaloneProfileAvatar, displayPhotoUri);
        updateStandalonePhotoPlaceholder(!hasText(displayPhotoUri));
    }

    private void showSosDialog() {
        String emergencyName = textOrFallback(currentEmergencyContactName, getString(R.string.sos_emergency_contact_default));
        String emergencyPhone = textOrFallback(currentEmergencyContactPhone, "");

        CharSequence[] options;
        if (hasText(emergencyPhone)) {
            options = new CharSequence[]{
                    getString(R.string.sos_call_112),
                    getString(R.string.sos_call_contact_value, emergencyName)
            };
        } else {
            options = new CharSequence[]{getString(R.string.sos_call_112)};
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.sos_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        dialPhone("112");
                        return;
                    }
                    if (hasText(emergencyPhone)) {
                        dialPhone(emergencyPhone);
                    } else {
                        showUiMessage(getString(R.string.sos_no_emergency_contact));
                    }
                })
                .setNegativeButton(R.string.citas_cancelar, null)
                .show();
    }

    private void dialPhone(String phone) {
        if (!hasText(phone)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.trim()));
        startActivity(intent);
    }

    private void loadCarePhotoInto(ImageView target, String photoUri) {
        if (target == null) {
            return;
        }
        target.setTag(photoUri);
        if (hasText(photoUri)) {
            String normalized = photoUri.trim();
            if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
                target.setImageResource(R.drawable.ic_avatar_neutro);
                new Thread(() -> loadRemoteImageInto(target, normalized)).start();
                return;
            }
            try {
                Uri parsedUri = Uri.parse(normalized);
                String scheme = parsedUri.getScheme();
                Bitmap bitmap = null;

                if ("file".equalsIgnoreCase(scheme)) {
                    bitmap = decodeFileBitmap(parsedUri);
                } else if ("content".equalsIgnoreCase(scheme)) {
                    bitmap = decodeContentBitmap(parsedUri);
                }

                if (bitmap != null) {
                    target.setImageBitmap(bitmap);
                    return;
                }

                target.setImageURI(null);
                target.setImageURI(parsedUri);
                if (target.getDrawable() != null) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        target.setImageResource(R.drawable.ic_avatar_neutro);
    }

    private Bitmap decodeFileBitmap(Uri fileUri) {
        if (fileUri == null || fileUri.getPath() == null) {
            return null;
        }
        FileInputStream inputStream = null;
        try {
            File file = new File(fileUri.getPath());
            if (!file.exists() || !file.isFile()) {
                return null;
            }
            inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return applyFileOrientation(bitmap, file.getAbsolutePath());
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Bitmap decodeContentBitmap(Uri contentUri) {
        if (contentUri == null) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(contentUri);
            if (inputStream == null) {
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return applyContentOrientation(bitmap, contentUri);
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Bitmap applyFileOrientation(Bitmap bitmap, String filePath) {
        if (bitmap == null || !hasText(filePath)) {
            return bitmap;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.media.ExifInterface exifInterface = new android.media.ExifInterface(filePath);
                int orientation = exifInterface.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                );
                return rotateBitmapIfNeeded(bitmap, orientation);
            }
        } catch (Exception ignored) {
        }
        return bitmap;
    }

    private Bitmap applyContentOrientation(Bitmap bitmap, Uri contentUri) {
        if (bitmap == null || contentUri == null) {
            return bitmap;
        }
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    contentUri,
                    new String[]{MediaStore.Images.ImageColumns.ORIENTATION},
                    null,
                    null,
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int orientation = cursor.getInt(0);
                if (orientation != 0) {
                    return rotateBitmap(bitmap, orientation);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bitmap;
    }

    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, int exifOrientation) {
        if (bitmap == null) {
            return null;
        }
        switch (exifOrientation) {
            case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);
            case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);
            case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (bitmap == null || degrees == 0) {
            return bitmap;
        }
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception ignored) {
            return bitmap;
        }
    }

    private String copyProfilePhotoToInternalStorage(Uri sourceUri, String groupId) {
        if (sourceUri == null) {
            return null;
        }
        File targetFile = buildPendingCareProfilePhotoFile(groupId);
        if (targetFile == null) {
            return null;
        }
        return copyUriToPhotoFile(sourceUri, targetFile);
    }

    private String resolveActiveGroupIdForProfileStorage() {
        if (viewModel == null || viewModel.state == null) {
            return null;
        }
        HomeViewModel.HomeState current = viewModel.state.getValue();
        return current != null ? current.activeGroupId : null;
    }

    private String resolveDisplayCarePhotoUri(String configuredUri, String groupId) {
        String cachedLocalUri = getCachedLocalCareProfilePhoto(groupId);
        if (hasText(cachedLocalUri)) {
            File cachedFile = parseLocalPhotoFile(cachedLocalUri);
            if (cachedFile != null && cachedFile.exists() && cachedFile.isFile()) {
                return cachedLocalUri;
            }
        }
        File localFile = buildCareProfilePhotoFile(groupId);
        if (localFile != null && localFile.exists() && localFile.isFile()) {
            return Uri.fromFile(localFile).toString();
        }
        if (hasText(configuredUri) && configuredUri.trim().toLowerCase(Locale.ROOT).startsWith("file://")) {
            return null;
        }
        return configuredUri;
    }

    private File buildCareProfilePhotoFile(String groupId) {
        String storageKey = resolvePhotoStorageKey(groupId);
        if (!hasText(storageKey)) {
            return null;
        }
        File dir = new File(getFilesDir(), "care-profile");
        return new File(dir, "profile_" + storageKey + ".jpg");
    }

    private File buildPendingCareProfilePhotoFile(String groupId) {
        String storageKey = resolvePhotoStorageKey(groupId);
        if (!hasText(storageKey)) {
            return null;
        }
        File dir = new File(getFilesDir(), "care-profile");
        return new File(dir, "profile_" + storageKey + "_draft.jpg");
    }

    private void deleteLocalCareProfilePhoto(String groupId) {
        File localFile = buildCareProfilePhotoFile(groupId);
        if (localFile != null && localFile.exists() && localFile.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            localFile.delete();
        }
    }

    private void deletePendingCareProfilePhoto(String groupId) {
        File pendingFile = buildPendingCareProfilePhotoFile(groupId);
        if (pendingFile != null && pendingFile.exists() && pendingFile.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            pendingFile.delete();
        }
    }

    private boolean isPendingCareProfilePhotoUri(String photoUri, String groupId) {
        File photoFile = parseLocalPhotoFile(photoUri);
        File pendingFile = buildPendingCareProfilePhotoFile(groupId);
        return photoFile != null && pendingFile != null && pendingFile.equals(photoFile);
    }

    private String persistPendingCareProfilePhoto(String groupId, String pendingPhotoUri) {
        if (!hasText(groupId)) {
            return null;
        }
        if (!hasText(pendingPhotoUri)) {
            deletePendingCareProfilePhoto(groupId);
            deleteLocalCareProfilePhoto(groupId);
            return null;
        }

        File pendingFile = parseLocalPhotoFile(pendingPhotoUri);
        File canonicalFile = buildCareProfilePhotoFile(groupId);
        if (canonicalFile == null) {
            return null;
        }
        if (pendingFile == null) {
            try {
                Uri pendingUri = Uri.parse(pendingPhotoUri);
                if (pendingUri != null && "content".equalsIgnoreCase(pendingUri.getScheme())) {
                    return copyUriToPhotoFile(pendingUri, canonicalFile);
                }
            } catch (Exception ignored) {
                return null;
            }
            return null;
        }
        if (canonicalFile.equals(pendingFile)) {
            deletePendingCareProfilePhoto(groupId);
            return Uri.fromFile(canonicalFile).toString();
        }
        if (!pendingFile.exists() || !pendingFile.isFile()) {
            return null;
        }

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(pendingFile);
            outputStream = new FileOutputStream(canonicalFile, false);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            if (!canonicalFile.exists() || !canonicalFile.isFile() || canonicalFile.length() <= 0L) {
                return null;
            }
            return Uri.fromFile(canonicalFile).toString();
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ignored) {
            }
            deletePendingCareProfilePhoto(groupId);
        }
    }

    private String copyUriToPhotoFile(Uri sourceUri, File targetFile) {
        if (sourceUri == null || targetFile == null) {
            return null;
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            File dir = targetFile.getParentFile();
            if (dir != null && !dir.exists() && !dir.mkdirs()) {
                return null;
            }
            inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) {
                return null;
            }
            outputStream = new FileOutputStream(targetFile, false);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            if (!targetFile.exists() || !targetFile.isFile() || targetFile.length() <= 0L) {
                return null;
            }
            return Uri.fromFile(targetFile).toString();
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String resolvePhotoStorageKey(String groupId) {
        if (hasText(groupId)) {
            return groupId.trim();
        }
        return null;
    }

    private void cacheLocalCareProfilePhoto(String groupId, String photoUri) {
        if (!hasText(groupId)) {
            return;
        }
        SharedPreferences prefs = getSharedPreferences(CARE_PROFILE_PHOTO_PREFS, MODE_PRIVATE);
        if (hasText(photoUri) && photoUri.trim().toLowerCase(Locale.ROOT).startsWith("file://")) {
            prefs.edit().putString(groupId.trim(), photoUri).apply();
        } else {
            prefs.edit().remove(groupId.trim()).apply();
        }
    }

    private String getCachedLocalCareProfilePhoto(String groupId) {
        if (!hasText(groupId)) {
            return null;
        }
        SharedPreferences prefs = getSharedPreferences(CARE_PROFILE_PHOTO_PREFS, MODE_PRIVATE);
        return prefs.getString(groupId.trim(), null);
    }

    private File parseLocalPhotoFile(String photoUri) {
        if (!hasText(photoUri)) {
            return null;
        }
        try {
            Uri parsedUri = Uri.parse(photoUri);
            if (parsedUri == null || !"file".equalsIgnoreCase(parsedUri.getScheme())) {
                return null;
            }
            String path = parsedUri.getPath();
            if (!hasText(path)) {
                return null;
            }
            return new File(path);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void loadRemoteImageInto(ImageView target, String photoUrl) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(photoUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setDoInput(true);
            connection.connect();
            inputStream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                return;
            }
            target.post(() -> {
                Object tag = target.getTag();
                if (tag instanceof String && photoUrl.equals(tag)) {
                    target.setImageBitmap(bitmap);
                }
            });
        } catch (Exception ignored) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String formatCareAge(Integer age) {
        if (age != null && age > 0) {
            return getString(R.string.profile_age_value, age);
        }
        return "";
    }

    private void updateCareProfileSummary(String careName, Integer careAge) {
        if (txtStandaloneCareName != null) {
            if (hasText(careName)) {
                txtStandaloneCareName.setText(careName.trim());
            } else {
                txtStandaloneCareName.setText(R.string.home_care_name_placeholder);
            }
        }
        if (txtStandaloneCareAge != null) {
            if (careAge != null && careAge > 0) {
                txtStandaloneCareAge.setVisibility(View.VISIBLE);
                txtStandaloneCareAge.setText(formatCareAge(careAge));
            } else {
                txtStandaloneCareAge.setText("");
                txtStandaloneCareAge.setVisibility(View.GONE);
            }
        }
    }

    private String buildIncidenciaMeta(Incidencia incidencia) {
        String fecha = incidencia != null ? textOrFallback(incidencia.getFecha(), "") : "";
        String hora = incidencia != null ? textOrFallback(incidencia.getHora(), "") : "";
        String nivel = incidencia != null ? textOrFallback(incidencia.getNivel(), "") : "";
        StringBuilder builder = new StringBuilder();
        if (hasText(fecha)) {
            builder.append(fecha);
        }
        if (hasText(hora)) {
            if (builder.length() > 0) builder.append(" · ");
            builder.append(hora);
        }
        if (hasText(nivel)) {
            if (builder.length() > 0) builder.append(" · ");
            builder.append(nivel);
        }
        return builder.toString();
    }

    private String buildIncidenciaCardTitle(String tipo, String horario) {
        String safeTipo = hasText(tipo) ? tipo.trim() : getString(R.string.home_ultima_incidencia);
        String fecha = extractIncidenciaFecha(horario);
        if (!hasText(fecha)) {
            return safeTipo;
        }
        return safeTipo + " · " + fecha;
    }

    private String buildIncidenciaCardSubtitle(String descripcion) {
        if (hasText(descripcion)) {
            return descripcion.trim();
        }
        return "";
    }

    private String extractIncidenciaFecha(String horario) {
        if (!hasText(horario)) {
            return "";
        }
        String value = horario.trim();
        int separatorIndex = value.indexOf("·");
        if (separatorIndex > 0) {
            return value.substring(0, separatorIndex).trim();
        }
        return value;
    }

    private void sharePdf(Uri uri, String fileName, String subject) {
        if (uri == null) {
            showUiMessage(getString(R.string.incidencias_pdf_generate_error));
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, fileName != null ? fileName : subject);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.pdf_share_chooser)));
    }

    private EditText createField(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        field.setTextColor(0xFF1B1B1B);
        field.setHintTextColor(0xFF7A8695);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xFFF8FBFF);
        bg.setStroke(dp(1), 0xFFD7E2EF);
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
        field.setInputType(android.text.InputType.TYPE_NULL);
        return field;
    }

    private TextView createSectionLabel(int textRes) {
        TextView label = new TextView(this);
        label.setText(textRes);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(10);
        params.bottomMargin = dp(8);
        label.setLayoutParams(params);
        return label;
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) ->
                target.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
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

    private void showGroupSelector(List<GroupMember> memberships, boolean mandatory) {
        if (memberships == null || memberships.isEmpty()) {
            goToGroupEntry();
            return;
        }

        String[] nombres = new String[memberships.size()];
        String[] ids = new String[memberships.size()];

        for (int i = 0; i < memberships.size(); i++) {
            GroupMember member = memberships.get(i);
            String name = member.getGroupName();
            nombres[i] = (name != null && !name.trim().isEmpty()) ? name : getString(R.string.home_group_default);
            ids[i] = member.getGroupId();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.home_group_selector_title)
                .setItems(nombres, (dialog, which) -> viewModel.onGroupSelected(ids[which]))
                .setNegativeButton(R.string.home_group_selector_join, (d, w) -> viewModel.onJoinWithCodeClicked())
                .setPositiveButton(R.string.home_group_selector_create, (d, w) -> viewModel.onCreateNewGroupClicked());

        if (mandatory) {
            builder.setCancelable(false);
        }

        builder.show();
    }

    private void goToGroupEntry() {
        startActivity(new Intent(this, GroupEntryActivity.class));
    }

    private void goToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void abrirPantallaMedicacion() {
        startActivity(new Intent(this, MedicacionActivity.class));
    }

    private void abrirPantallaCitas() {
        startActivity(new Intent(this, CitasActivity.class));
    }

    private void openSeguimiento(String groupId) {
        if (!hasText(groupId)) {
            showUiMessage(getString(R.string.home_health_empty));
            return;
        }
        Intent intent = new Intent(this, SeguimientoActivity.class);
        intent.putExtra(SeguimientoActivity.EXTRA_GROUP_ID, groupId);
        startActivity(intent);
    }

    private void openPatologias(String groupId) {
        if (!hasText(groupId)) {
            showUiMessage(getString(R.string.patologias_empty));
            return;
        }
        Intent intent = new Intent(this, PatologiasActivity.class);
        intent.putExtra(PatologiasActivity.EXTRA_GROUP_ID, groupId);
        startActivity(intent);
    }

    private void openMedicationFromAlert() {
        if (viewModel.hasAuthenticatedUser()) {
            abrirPantallaMedicacion();
            return;
        }
        goToWelcome();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        launchedFromMedicationAlert = isMedicationAlertIntent(intent);
        handleMedicationAlertIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAlertSound();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!launchedFromMedicationAlert) {
            viewModel.refreshHomeCards();
        }
        reopenCareProfileIfNeeded();
    }

    private void handleMedicationAlertIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_SHOW_MEDICATION_ALERT, false)) {
            return;
        }

        String nombre = intent.getStringExtra(EXTRA_ALERT_MEDICATION_NAME);
        String hora = intent.getStringExtra(EXTRA_ALERT_MEDICATION_TIME);
        String fecha = intent.getStringExtra(EXTRA_ALERT_MEDICATION_DATE);

        startAlertSound();

        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.home_medicacion_alert_body,
                safeText(nombre, getString(R.string.home_medicacion_alert_fallback))));
        if (hora != null && !hora.trim().isEmpty()) {
            message.append(getString(R.string.home_medicacion_alert_at_time, hora));
        }
        if (fecha != null && !fecha.trim().isEmpty()) {
            message.append("\n").append(fecha);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.home_medicacion_alert_title)
                .setMessage(message.toString())
                .setCancelable(false)
                .setNegativeButton(R.string.home_medicacion_alert_close, (dialog, which) -> stopAlertSound())
                .setPositiveButton(R.string.home_medicacion_alert_open, (dialog, which) -> {
                    stopAlertSound();
                    openMedicationFromAlert();
                })
                .setOnDismissListener(dialog -> {
                    stopAlertSound();
                    launchedFromMedicationAlert = false;
                })
                .show();

        intent.removeExtra(EXTRA_SHOW_MEDICATION_ALERT);
        launchedFromMedicationAlert = false;
    }

    private boolean isMedicationAlertIntent(Intent intent) {
        if (intent == null) return false;
        if (intent.getBooleanExtra(EXTRA_SHOW_MEDICATION_ALERT, false)) return true;
        return hasText(intent.getStringExtra(EXTRA_ALERT_MEDICATION_ID))
                || hasText(intent.getStringExtra(EXTRA_ALERT_MEDICATION_NAME))
                || hasText(intent.getStringExtra(EXTRA_ALERT_MEDICATION_TIME))
                || hasText(intent.getStringExtra(EXTRA_ALERT_MEDICATION_DATE));
    }

    private void startAlertSound() {
        stopAlertSound();
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (alarmUri == null) return;
        activeAlertRingtone = RingtoneManager.getRingtone(this, alarmUri);
        if (activeAlertRingtone != null) activeAlertRingtone.play();
    }

    private void stopAlertSound() {
        if (activeAlertRingtone != null && activeAlertRingtone.isPlaying()) {
            activeAlertRingtone.stop();
        }
        activeAlertRingtone = null;
    }

    private String safeText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
    }

    private void bindCitaCardViews() {
        txtCitaTitulo = findViewById(R.id.txtCitaNombre);
        txtCitaHorario = findViewById(R.id.txtCitaFechaHora);
        if (txtCitaTitulo != null) {
            txtCitaTitulo.setText(R.string.home_proxima_cita_vacia);
            txtCitaTitulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            txtCitaTitulo.setTypeface(txtCitaTitulo.getTypeface(), Typeface.BOLD);
            txtCitaTitulo.setMaxLines(2);
            txtCitaTitulo.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }
        if (txtCitaHorario != null) {
            txtCitaHorario.setText(R.string.home_proxima_cita_sin_datos);
            txtCitaHorario.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            txtCitaHorario.setTextColor(Color.parseColor("#64748B"));
            txtCitaHorario.setPadding(0, dp(4), 0, 0);
            txtCitaHorario.setMaxLines(2);
            txtCitaHorario.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }
        txtCitaEncargada = null;
    }

    private void applyInitialHomeEmptyState() {
        updateCareProfileSummary(null, null);
        if (txtMedicacionNombre != null) {
            txtMedicacionNombre.setText(R.string.home_proxima_medicacion_vacia);
        }
        if (txtMedicacionHora != null) {
            txtMedicacionHora.setText(R.string.home_proxima_medicacion_sin_datos);
        }
        if (txtCitaTitulo != null) {
            txtCitaTitulo.setText(R.string.home_proxima_cita_vacia);
        }
        if (txtCitaHorario != null) {
            txtCitaHorario.setText(R.string.home_proxima_cita_sin_datos);
        }
        if (txtCitaEncargada != null) {
            txtCitaEncargada.setText("");
            txtCitaEncargada.setVisibility(View.GONE);
        }
        if (txtIncidenciaTitulo != null) {
            txtIncidenciaTitulo.setText(R.string.home_ultima_incidencia_vacia);
        }
        if (txtIncidenciaHorario != null) {
            txtIncidenciaHorario.setText(R.string.home_ultima_incidencia_sin_datos);
        }
        applyIncidenciaNivelStyle(null);
    }

    private void bindIncidenciaCardViews() {
        txtIncidenciaTitulo = findViewById(R.id.txtIncidenciaDesc);
        txtIncidenciaHorario = findViewById(R.id.txtIncidenciaFechaHora);
        if (txtIncidenciaTitulo != null) {
            txtIncidenciaTitulo.setText(R.string.home_ultima_incidencia_vacia);
            txtIncidenciaTitulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            txtIncidenciaTitulo.setTypeface(txtIncidenciaTitulo.getTypeface(), Typeface.BOLD);
            txtIncidenciaTitulo.setMaxLines(2);
            txtIncidenciaTitulo.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }
        if (txtIncidenciaHorario != null) {
            txtIncidenciaHorario.setText(R.string.home_ultima_incidencia_sin_datos);
            txtIncidenciaHorario.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            txtIncidenciaHorario.setTextColor(Color.parseColor("#64748B"));
            txtIncidenciaHorario.setMaxLines(2);
            txtIncidenciaHorario.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }
    }

    private void bindActividadCardViews() {
        txtActividadRecienteCount = findViewById(R.id.txtActividadRecienteCount);
        txtActividadRecienteHeadline = findViewById(R.id.txtActividadRecienteHeadline);
        txtActividadRecienteDetail = findViewById(R.id.txtActividadRecienteDetail);
        txtActividadRecienteFooter = findViewById(R.id.txtActividadRecienteFooter);
        TextView title = findViewById(R.id.txtActividadRecienteTitulo);
        if (title != null) {
            title.setText(R.string.activity_recent_title);
        }
        if (txtActividadRecienteCount != null) {
            txtActividadRecienteCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            txtActividadRecienteCount.setText(R.string.activity_recent_subtitle_zero);
        }
        if (txtActividadRecienteHeadline != null) {
            txtActividadRecienteHeadline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtActividadRecienteHeadline.setMaxLines(3);
            txtActividadRecienteHeadline.setEllipsize(android.text.TextUtils.TruncateAt.END);
            txtActividadRecienteHeadline.setText(R.string.activity_empty);
            txtActividadRecienteHeadline.setVisibility(View.GONE);
        }
        if (txtActividadRecienteDetail != null) {
            txtActividadRecienteDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            txtActividadRecienteDetail.setMaxLines(2);
            txtActividadRecienteDetail.setEllipsize(android.text.TextUtils.TruncateAt.END);
            txtActividadRecienteDetail.setText("");
            txtActividadRecienteDetail.setVisibility(View.GONE);
        }
        if (txtActividadRecienteFooter != null) {
            txtActividadRecienteFooter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            txtActividadRecienteFooter.setText(R.string.activity_view_full_history);
        }
    }

    private void bindSeguimientoSaludCardViews() {
        txtSeguimientoSaludSubtitulo = findViewById(R.id.txtSeguimientoSaludSubtitulo);
        txtSeguimientoSaludDetalle = findViewById(R.id.txtSeguimientoSaludDetalle);
    }

    private void renderHealthSummary(HomeViewModel.HealthSummaryState state) {
        if (txtSeguimientoSaludSubtitulo != null) {
            if (state != null && state.patologiasCount > 0) {
                txtSeguimientoSaludSubtitulo.setText(getHealthPathologiesSummary(state.patologiasCount));
            } else {
                txtSeguimientoSaludSubtitulo.setText(R.string.home_health_subtitle);
            }
        }
        if (txtSeguimientoSaludDetalle != null) {
            if (state != null && hasText(state.seguimientoResumen)) {
                txtSeguimientoSaludDetalle.setText(state.seguimientoResumen);
                txtSeguimientoSaludDetalle.setVisibility(View.VISIBLE);
            } else {
                txtSeguimientoSaludDetalle.setText("");
                txtSeguimientoSaludDetalle.setVisibility(View.GONE);
            }
        }
    }

    private String getHealthPathologiesSummary(int count) {
        if (count <= 0) {
            return getString(R.string.home_health_subtitle);
        }
        if (count == 1) {
            return getString(R.string.home_health_pathologies_one);
        }
        return getString(R.string.home_health_pathologies_many, count);
    }

    private void renderRecentActivity(ActividadItem item, int todayCount) {
        if (txtActividadRecienteCount != null) {
            if (todayCount <= 0) {
                txtActividadRecienteCount.setText(R.string.activity_recent_subtitle_zero);
            } else if (todayCount == 1) {
                txtActividadRecienteCount.setText(R.string.activity_recent_subtitle_one);
            } else {
                txtActividadRecienteCount.setText(getString(R.string.activity_recent_subtitle_many, todayCount));
            }
        }

        if (txtActividadRecienteHeadline == null || txtActividadRecienteDetail == null) {
            return;
        }

        if (item == null) {
            txtActividadRecienteHeadline.setText(R.string.activity_empty);
            txtActividadRecienteHeadline.setVisibility(View.GONE);
            txtActividadRecienteDetail.setText("");
            txtActividadRecienteDetail.setVisibility(View.GONE);
            return;
        }

        txtActividadRecienteHeadline.setText(buildActivityHeadline(item));
        txtActividadRecienteHeadline.setVisibility(View.GONE);
        txtActividadRecienteDetail.setText("");
        txtActividadRecienteDetail.setVisibility(View.GONE);
    }

    private String buildActivityHeadline(ActividadItem item) {
        if (item == null) {
            return getString(R.string.activity_empty);
        }
        String actor = textOrFallback(item.getActorName(), getString(R.string.activity_actor_fallback));
        switch (textOrFallback(item.getEntityType(), "")) {
            case ActividadItem.TYPE_MEDICACION:
                return getString(
                        ActividadItem.ACTION_DELETED.equals(item.getActionType())
                                ? R.string.activity_headline_medicacion_deleted
                                : (ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_medicacion_updated
                                : R.string.activity_headline_medicacion_created),
                        actor
                );
            case ActividadItem.TYPE_CITA:
                return getString(
                        ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_cita_updated
                                : R.string.activity_headline_cita_created,
                        actor
                );
            case ActividadItem.TYPE_PATOLOGIA:
                return getString(
                        ActividadItem.ACTION_DELETED.equals(item.getActionType())
                                ? R.string.activity_headline_patologia_deleted
                                : (ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_patologia_updated
                                : R.string.activity_headline_patologia_created),
                        actor
                );
            case ActividadItem.TYPE_SEGUIMIENTO:
                return getString(
                        ActividadItem.ACTION_DELETED.equals(item.getActionType())
                                ? R.string.activity_headline_seguimiento_deleted
                                : (ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_seguimiento_updated
                                : R.string.activity_headline_seguimiento_created),
                        actor
                );
            case ActividadItem.TYPE_INCIDENCIA:
            default:
                return getString(
                        ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_incidencia_updated
                                : R.string.activity_headline_incidencia_created,
                        actor
                );
        }
    }

    private void applyIncidenciaNivelStyle(String nivel) {
        if (txtIncidenciaNivel == null) return;
        if (!hasText(nivel)) {
            txtIncidenciaNivel.setVisibility(View.GONE);
            txtIncidenciaNivel.setText("");
            return;
        }

        String normalized = nivel.trim().toLowerCase(Locale.getDefault());
        int bgColor;
        switch (normalized) {
            case "leve":
                bgColor = 0xFF2E8B57;
                break;
            case "moderada":
                bgColor = 0xFFD97706;
                break;
            case "urgente":
                bgColor = 0xFFDC2626;
                break;
            default:
                bgColor = 0xFF4B5563;
                break;
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(999));
        txtIncidenciaNivel.setBackground(bg);
        txtIncidenciaNivel.setText(nivel.trim());
        txtIncidenciaNivel.setTextColor(Color.WHITE);
        txtIncidenciaNivel.setVisibility(View.VISIBLE);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private void ensureViewHasId(View view) {
        if (view != null && view.getId() == View.NO_ID) {
            view.setId(View.generateViewId());
        }
    }
}
