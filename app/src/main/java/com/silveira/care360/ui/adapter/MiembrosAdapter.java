package com.silveira.care360.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.silveira.care360.R;
import com.silveira.care360.domain.model.GroupMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter de la lista de miembros de la pantalla Familia.
 * Se encarga exclusivamente de pintar cada miembro en el RecyclerView.
 */
public class MiembrosAdapter extends RecyclerView.Adapter<MiembrosAdapter.MemberViewHolder> {

    public interface OnManageMemberClickListener {
        void onManageMemberClicked(GroupMember member);
    }

    private final List<GroupMember> items = new ArrayList<>();
    private final boolean canManageMembers;
    private final String currentUserId;
    private final String ownerUserId;
    private final OnManageMemberClickListener onManageMemberClickListener;

    public MiembrosAdapter() {
        this(false, null, null, null);
    }

    public MiembrosAdapter(boolean canManageMembers,
                           String currentUserId,
                           String ownerUserId,
                           OnManageMemberClickListener onManageMemberClickListener) {
        this.canManageMembers = canManageMembers;
        this.currentUserId = currentUserId;
        this.ownerUserId = ownerUserId;
        this.onManageMemberClickListener = onManageMemberClickListener;
    }

    /**
     * Sustituye la lista actual por la nueva y refresca el RecyclerView.
     */
    public void submitList(List<GroupMember> members) {
        items.clear();
        if (members != null) {
            items.addAll(members);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_miembro_familia, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        GroupMember member = items.get(position);

        String name = safeText(member.getName(), "Miembro");
        String email = safeText(member.getEmail(), "Sin correo");
        String role = mapRole(member.getRole());

        holder.txtNombreMiembro.setText(name);
        holder.txtRolMiembro.setText(role);
        holder.txtCorreoMiembro.setText(email);

        boolean canManageThisMember = canManageMembers
                && member.getUserId() != null
                && !member.getUserId().equals(currentUserId)
                && (ownerUserId == null || !ownerUserId.equals(member.getUserId()));

        holder.txtGestionarMiembro.setVisibility(canManageThisMember ? View.VISIBLE : View.GONE);
        holder.txtGestionarMiembro.setOnClickListener(canManageThisMember && onManageMemberClickListener != null
                ? v -> onManageMemberClickListener.onManageMemberClicked(member)
                : null);

        // Alternancia suave de color para diferenciar filas
        Context context = holder.itemView.getContext();
        int backgroundColor = ContextCompat.getColor(
                context,
                position % 2 == 0 ? R.color.miembro_fila_par : R.color.miembro_fila_impar
        );
        holder.itemView.setBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Devuelve un texto válido o un fallback si viene null o vacío.
     */
    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String mapRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "Miembro";
        }

        if ("admin".equalsIgnoreCase(role.trim())) {
            return "Admin";
        }

        return "Miembro";
    }

    /**
     * ViewHolder de un miembro.
     */
    static class MemberViewHolder extends RecyclerView.ViewHolder {

        final TextView txtNombreMiembro;
        final TextView txtRolMiembro;
        final TextView txtCorreoMiembro;
        final TextView txtGestionarMiembro;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreMiembro = itemView.findViewById(R.id.txtNombreMiembro);
            txtRolMiembro = itemView.findViewById(R.id.txtRolMiembro);
            txtCorreoMiembro = itemView.findViewById(R.id.txtCorreoMiembro);
            txtGestionarMiembro = itemView.findViewById(R.id.txtGestionarMiembro);
        }
    }
}
