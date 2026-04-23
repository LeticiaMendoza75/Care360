package com.silveira.care360.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.Cita;

import java.util.ArrayList;
import java.util.List;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {

    public interface Listener {
        void onVerMasClicked(Cita cita);
        void onGestionarClicked(Cita cita);
    }

    private final Listener listener;
    private final List<Cita> items = new ArrayList<>();

    public CitasAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Cita> citas) {
        items.clear();
        if (citas != null) {
            items.addAll(citas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 12));
        RecyclerView.LayoutParams rootParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rootParams.bottomMargin = dp(context, 12);
        root.setLayoutParams(rootParams);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#FAFCFF"));
        background.setCornerRadius(dp(context, 16));
        background.setStroke(dp(context, 1), Color.parseColor("#D6E3F3"));
        root.setBackground(background);

        TextView txtTitulo = text(context, 20, true, "#173A6A");
        TextView txtFechaHora = text(context, 13, true, "#405A7A");
        TextView txtLugar = text(context, 13, false, "#6C7A8B");
        TextView txtProfesional = text(context, 12, false, "#7B8794");
        TextView txtPersonaEncargada = text(context, 12, false, "#7B8794");

        LinearLayout rowActions = new LinearLayout(context);
        rowActions.setOrientation(LinearLayout.HORIZONTAL);
        rowActions.setGravity(Gravity.END);
        LinearLayout.LayoutParams rowActionsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowActionsParams.topMargin = dp(context, 8);
        rowActions.setLayoutParams(rowActionsParams);

        MaterialButton btnVerMas = actionButton(context, context.getString(R.string.citas_ver_mas));
        MaterialButton btnGestionar = actionButton(context, context.getString(R.string.citas_gestionar));
        LinearLayout.LayoutParams btnGestionarParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnGestionarParams.leftMargin = dp(context, 8);
        btnGestionar.setLayoutParams(btnGestionarParams);

        rowActions.addView(btnVerMas);
        rowActions.addView(btnGestionar);

        root.addView(txtTitulo);
        root.addView(txtFechaHora);
        root.addView(txtLugar);
        root.addView(txtProfesional);
        root.addView(txtPersonaEncargada);
        root.addView(rowActions);

        return new CitaViewHolder(root, txtTitulo, txtFechaHora, txtLugar, txtProfesional, txtPersonaEncargada, btnVerMas, btnGestionar);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = items.get(position);
        Context context = holder.itemView.getContext();

        holder.txtTitulo.setText(textOrFallback(cita.getTitulo(), context.getString(R.string.citas_generica)));
        holder.txtFechaHora.setText(
                textOrFallback(cita.getFecha(), context.getString(R.string.citas_fecha_pendiente))
                        + " · "
                        + textOrFallback(cita.getHora(), context.getString(R.string.citas_hora_pendiente))
        );
        holder.txtLugar.setText(textOrFallback(cita.getLugar(), context.getString(R.string.citas_lugar_pendiente)));
        holder.txtProfesional.setText(textOrFallback(cita.getProfesional(), context.getString(R.string.citas_profesional_pendiente)));
        if (cita.getPersonaEncargada() != null && !cita.getPersonaEncargada().trim().isEmpty()) {
            holder.txtPersonaEncargada.setVisibility(android.view.View.VISIBLE);
            holder.txtPersonaEncargada.setText(context.getString(R.string.citas_persona_encargada_resumen, cita.getPersonaEncargada().trim()));
        } else {
            holder.txtPersonaEncargada.setVisibility(android.view.View.GONE);
            holder.txtPersonaEncargada.setText("");
        }
        holder.btnVerMas.setOnClickListener(v -> listener.onVerMasClicked(cita));
        holder.btnGestionar.setOnClickListener(v -> listener.onGestionarClicked(cita));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        final TextView txtTitulo;
        final TextView txtFechaHora;
        final TextView txtLugar;
        final TextView txtProfesional;
        final TextView txtPersonaEncargada;
        final MaterialButton btnVerMas;
        final MaterialButton btnGestionar;

        CitaViewHolder(@NonNull LinearLayout itemView,
                       TextView txtTitulo,
                       TextView txtFechaHora,
                       TextView txtLugar,
                       TextView txtProfesional,
                       TextView txtPersonaEncargada,
                       MaterialButton btnVerMas,
                       MaterialButton btnGestionar) {
            super(itemView);
            this.txtTitulo = txtTitulo;
            this.txtFechaHora = txtFechaHora;
            this.txtLugar = txtLugar;
            this.txtProfesional = txtProfesional;
            this.txtPersonaEncargada = txtPersonaEncargada;
            this.btnVerMas = btnVerMas;
            this.btnGestionar = btnGestionar;
        }
    }

    private static TextView text(Context context, int sp, boolean bold, String color) {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.parseColor(color));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        textView.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(context, 4);
        textView.setLayoutParams(params);
        return textView;
    }

    private static MaterialButton actionButton(Context context, String text) {
        MaterialButton button = new MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.parseColor("#0F4BB3"));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        return button;
    }

    private static String textOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()
        );
    }
}
