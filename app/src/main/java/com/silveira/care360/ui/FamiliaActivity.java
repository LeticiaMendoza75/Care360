package com.silveira.care360.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.ui.adapter.MiembrosAdapter;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FamiliaActivity extends AppCompatActivity {

    private FamiliaViewModel viewModel;

    private ImageButton btnBackFamilia;
    private TextView txtJoinCode;
    private MaterialButton btnCopiarCodigo;
    private MaterialButton btnCompartirCodigo;
    private MaterialButton btnInvitarContactos;
    private MaterialButton btnCrearNuevoGrupo;
    private MaterialButton btnVerMiembros;
    private MaterialButton btnSalirGrupo;
    private MaterialButton btnEliminarGrupo;

    private MiembrosAdapter miembrosAdapter;
    private MiembrosAdapter dialogMiembrosAdapter;
    private AlertDialog miembrosDialog;
    private ActivityResultLauncher<Intent> pickPhoneContactLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familia);

        viewModel = new ViewModelProvider(this).get(FamiliaViewModel.class);

        initViews();
        setupRecycler();
        setupActivityLaunchers();
        observeViewModel();
        setupListeners();
        BottomNavManager.bind(this, BottomNavManager.Tab.FAMILIA);

        viewModel.loadFamilyData();
    }

    private void initViews() {
        btnBackFamilia = findViewById(R.id.btnBackFamilia);
        txtJoinCode = findViewById(R.id.txtJoinCode);
        btnCopiarCodigo = findViewById(R.id.btnCopiarCodigo);
        btnCompartirCodigo = findViewById(R.id.btnCompartirCodigo);
        btnInvitarContactos = findViewById(R.id.btnInvitarContactos);
        btnCrearNuevoGrupo = findViewById(R.id.btnCrearNuevoGrupo);
        btnVerMiembros = findViewById(R.id.btnVerMiembros);
        btnSalirGrupo = findViewById(R.id.btnSalirGrupo);
        btnEliminarGrupo = findViewById(R.id.btnEliminarGrupo);
    }

    private void setupRecycler() {
        miembrosAdapter = new MiembrosAdapter();
    }

    private void setupListeners() {
        btnBackFamilia.setOnClickListener(v -> finish());
        btnCopiarCodigo.setOnClickListener(v -> viewModel.onCopyCodeClicked());
        btnCompartirCodigo.setOnClickListener(v -> viewModel.onShareCodeClicked());
        btnInvitarContactos.setOnClickListener(v -> viewModel.onInviteContactsClicked());
        btnCrearNuevoGrupo.setOnClickListener(v -> viewModel.onCreateNewGroupClicked());
        btnVerMiembros.setOnClickListener(v -> viewModel.onViewMembersClicked());
        btnSalirGrupo.setOnClickListener(v -> viewModel.onLeaveGroupClicked());
        btnEliminarGrupo.setOnClickListener(v -> viewModel.onDeleteGroupClicked());
    }

    private void observeViewModel() {
        viewModel.state.observe(this, state -> {
            if (state == null) {
                return;
            }

            if (state.joinCode != null && !state.joinCode.trim().isEmpty()) {
                txtJoinCode.setText(state.joinCode);
            } else {
                txtJoinCode.setText("— — — — — —");
            }

            boolean enabled = !state.isLoading;
            btnCopiarCodigo.setEnabled(enabled);
            btnCompartirCodigo.setEnabled(enabled);
            btnInvitarContactos.setEnabled(enabled);
            btnCrearNuevoGrupo.setEnabled(enabled);
            btnVerMiembros.setEnabled(enabled);
            btnSalirGrupo.setEnabled(enabled && state.canLeaveGroup);
            btnEliminarGrupo.setEnabled(enabled && state.canDeleteGroup);
            btnEliminarGrupo.setVisibility(state.canDeleteGroup ? android.view.View.VISIBLE : android.view.View.GONE);

            if (miembrosDialog != null && miembrosDialog.isShowing() && dialogMiembrosAdapter != null) {
                dialogMiembrosAdapter.submitList(state.members);
            }

            if (state.errorMessage != null && !state.errorMessage.trim().isEmpty()) {
                UiMessageUtils.show(this, state.errorMessage);
            }
        });

        viewModel.action.observe(this, action -> {
            if (action == null) {
                return;
            }

            if (action instanceof FamiliaViewModel.CopyCodeAction) {
                copiarCodigoAlPortapapeles(((FamiliaViewModel.CopyCodeAction) action).code);
            } else if (action instanceof FamiliaViewModel.ShareTextAction) {
                compartirTexto(((FamiliaViewModel.ShareTextAction) action).text);
            } else if (action instanceof FamiliaViewModel.OpenContactPickerAction) {
                abrirSelectorContactos();
            } else if (action instanceof FamiliaViewModel.OpenSmsInviteAction) {
                FamiliaViewModel.OpenSmsInviteAction smsAction = (FamiliaViewModel.OpenSmsInviteAction) action;
                abrirInvitacionSms(smsAction.phoneNumber, smsAction.text);
            } else if (action instanceof FamiliaViewModel.OpenMembersDialogAction) {
                FamiliaViewModel.OpenMembersDialogAction membersAction = (FamiliaViewModel.OpenMembersDialogAction) action;
                abrirDialogoMiembros(
                        membersAction.members,
                        membersAction.canManageMembers,
                        membersAction.currentUserId,
                        membersAction.ownerUserId
                );
            } else if (action instanceof FamiliaViewModel.ShowManageMemberActionsAction) {
                mostrarAccionesMiembro(((FamiliaViewModel.ShowManageMemberActionsAction) action).member);
            } else if (action instanceof FamiliaViewModel.ConfirmRemoveMemberAction) {
                mostrarConfirmacionEliminarMiembro(((FamiliaViewModel.ConfirmRemoveMemberAction) action).member);
            } else if (action instanceof FamiliaViewModel.ConfirmDeleteGroupAction) {
                mostrarConfirmacionEliminarGrupo(((FamiliaViewModel.ConfirmDeleteGroupAction) action).groupName);
            } else if (action instanceof FamiliaViewModel.SelectNewOwnerAction) {
                mostrarSelectorNuevoResponsable(((FamiliaViewModel.SelectNewOwnerAction) action).candidates);
            } else if (action instanceof FamiliaViewModel.ConfirmLeaveGroupAction) {
                mostrarConfirmacionSalirDelGrupo(((FamiliaViewModel.ConfirmLeaveGroupAction) action).newOwnerUserId);
            } else if (action instanceof FamiliaViewModel.LeaveGroupCompletedAction) {
                UiMessageUtils.show(this, ((FamiliaViewModel.LeaveGroupCompletedAction) action).message);
                goToHomeAndFinish();
            } else if (action instanceof FamiliaViewModel.DeleteGroupCompletedAction) {
                UiMessageUtils.show(this, ((FamiliaViewModel.DeleteGroupCompletedAction) action).message);
                goToGroupEntryAndFinish();
            } else if (action instanceof FamiliaViewModel.NavigateToGroupEntryAction) {
                goToGroupEntry();
            } else if (action instanceof FamiliaViewModel.NavigateToWelcomeAction) {
                goToWelcome();
            } else if (action instanceof FamiliaViewModel.ShowMessageAction) {
                UiMessageUtils.show(this, ((FamiliaViewModel.ShowMessageAction) action).message);
            }

            viewModel.onActionHandled();
        });

        viewModel.groupSelectorEvent.observe(this, selectorState -> {
            if (selectorState == null) {
                return;
            }
            showGroupSelector(selectorState.memberships);
            viewModel.onGroupSelectorHandled();
        });
    }

    private void setupActivityLaunchers() {
        pickPhoneContactLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri phoneUri = result.getData().getData();
                        if (phoneUri != null) {
                            ContactSelection contact = obtenerContactoSeleccionado(phoneUri);
                            viewModel.onContactSelected(contact.displayName, contact.phoneNumber);
                        }
                    }
                });
    }

    private void abrirSelectorContactos() {
        Intent intent = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);
        pickPhoneContactLauncher.launch(intent);
    }

    private ContactSelection obtenerContactoSeleccionado(@NonNull Uri phoneUri) {
        String nombre = null;
        String telefono = null;

        Cursor cursor = getContentResolver().query(
                phoneUri,
                new String[]{Phone.DISPLAY_NAME, Phone.NUMBER},
                null,
                null,
                null
        );

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
                    int phoneIdx = cursor.getColumnIndex(Phone.NUMBER);
                    if (nameIdx >= 0) {
                        nombre = cursor.getString(nameIdx);
                    }
                    if (phoneIdx >= 0) {
                        telefono = cursor.getString(phoneIdx);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return new ContactSelection(nombre, telefono);
    }

    private void copiarCodigoAlPortapapeles(String code) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Código del grupo", code);
        clipboard.setPrimaryClip(clip);
        UiMessageUtils.show(this, "Código copiado");
    }

    private void compartirTexto(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Compartir con..."));
    }

    private void abrirInvitacionSms(String phoneNumber, String text) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
        smsIntent.putExtra("sms_body", text);

        if (smsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(smsIntent);
        } else {
            compartirTexto(text);
        }
    }

    private void abrirDialogoMiembros(List<GroupMember> members,
                                      boolean canManageMembers,
                                      String currentUserId,
                                      String ownerUserId) {
        if (miembrosDialog != null && miembrosDialog.isShowing()) {
            miembrosDialog.dismiss();
        }

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_miembros_grupo, null);
        RecyclerView rvMiembrosDialog = dialogView.findViewById(R.id.rvMiembrosDialog);
        rvMiembrosDialog.setLayoutManager(new LinearLayoutManager(this));

        dialogMiembrosAdapter = new MiembrosAdapter(
                canManageMembers,
                currentUserId,
                ownerUserId,
                member -> viewModel.onManageMemberClicked(member)
        );
        dialogMiembrosAdapter.submitList(members);
        rvMiembrosDialog.setAdapter(dialogMiembrosAdapter);

        miembrosDialog = new AlertDialog.Builder(this)
                .setTitle("Miembros del grupo")
                .setView(dialogView)
                .setPositiveButton("Cerrar", null)
                .show();

        if (miembrosDialog.getWindow() != null) {
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.92f);
            miembrosDialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        miembrosDialog.setOnDismissListener(dialog -> {
            miembrosDialog = null;
            dialogMiembrosAdapter = null;
        });
    }

    private void mostrarAccionesMiembro(GroupMember member) {
        if (member == null) {
            return;
        }

        String roleAction = "admin".equalsIgnoreCase(member.getRole()) ? "Quitar admin" : "Hacer admin";
        String memberName = member.getName() != null && !member.getName().trim().isEmpty() ? member.getName() : "este miembro";

        new AlertDialog.Builder(this)
                .setTitle(memberName)
                .setItems(new String[]{roleAction, "Eliminar del grupo"}, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.onChangeMemberRoleRequested(member);
                    } else if (which == 1) {
                        viewModel.onRemoveMemberRequested(member);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarConfirmacionEliminarMiembro(GroupMember member) {
        if (member == null) {
            return;
        }

        String memberName = member.getName() != null && !member.getName().trim().isEmpty() ? member.getName() : "este miembro";

        new AlertDialog.Builder(this)
                .setTitle("Eliminar miembro")
                .setMessage("Se eliminara a " + memberName + " del grupo.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.onRemoveMemberConfirmed(member))
                .show();
    }

    private void showGroupSelector(List<GroupMember> memberships) {
        if (memberships == null || memberships.isEmpty()) {
            goToGroupEntry();
            return;
        }

        String[] nombres = new String[memberships.size()];
        String[] ids = new String[memberships.size()];

        for (int i = 0; i < memberships.size(); i++) {
            GroupMember member = memberships.get(i);
            String name = member.getGroupName();
            nombres[i] = (name != null && !name.trim().isEmpty()) ? name : "Grupo Familiar";
            ids[i] = member.getGroupId();
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona tu grupo")
                .setItems(nombres, (dialog, which) -> viewModel.onGroupSelected(ids[which]))
                .setNegativeButton("Unirme con codigo", (d, w) -> goToGroupEntry())
                .setPositiveButton("Crear nuevo grupo", (d, w) -> goToGroupEntry())
                .show();
    }

    private void mostrarConfirmacionEliminarGrupo(String groupName) {
        String groupLabel = groupName != null && !groupName.trim().isEmpty() ? groupName : "este grupo";

        new AlertDialog.Builder(this)
                .setTitle("Eliminar grupo")
                .setMessage("Se eliminara " + groupLabel + " y toda su informacion asociada. Esta accion no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.onDeleteGroupConfirmed())
                .show();
    }

    private void mostrarSelectorNuevoResponsable(List<GroupMember> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            UiMessageUtils.show(this, "Antes de salir debe quedar otro admin en el grupo");
            return;
        }

        String[] nombres = new String[candidates.size()];
        String[] ids = new String[candidates.size()];

        for (int i = 0; i < candidates.size(); i++) {
            GroupMember candidate = candidates.get(i);
            nombres[i] = candidate.getName() != null && !candidate.getName().trim().isEmpty()
                    ? candidate.getName()
                    : (candidate.getEmail() != null ? candidate.getEmail() : "Admin");
            ids[i] = candidate.getUserId();
        }

        final int[] selectedIndex = {0};

        new AlertDialog.Builder(this)
                .setTitle("Elige nuevo responsable")
                .setSingleChoiceItems(nombres, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Continuar", (dialog, which) -> viewModel.onNewOwnerSelectedForLeave(ids[selectedIndex[0]]))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarConfirmacionSalirDelGrupo(String newOwnerUserId) {
        new AlertDialog.Builder(this)
                .setTitle("Salir del grupo")
                .setMessage("¿Quieres salir del grupo?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salir", (dialog, which) -> viewModel.onLeaveGroupConfirmed(newOwnerUserId))
                .show();
    }

    private void goToGroupEntry() {
        Intent intent = new Intent(this, GroupEntryActivity.class);
        startActivity(intent);
    }

    private void goToGroupEntryAndFinish() {
        Intent intent = new Intent(this, GroupEntryActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToHomeAndFinish() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static class ContactSelection {
        final String displayName;
        final String phoneNumber;

        ContactSelection(String displayName, String phoneNumber) {
            this.displayName = displayName;
            this.phoneNumber = phoneNumber;
        }
    }
}
