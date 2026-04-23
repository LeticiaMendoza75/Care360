package com.silveira.care360.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.domain.model.SeguimientoRegistro;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeguimientoAdapter extends RecyclerView.Adapter<SeguimientoAdapter.RegistroViewHolder> {

    public interface Listener {
        void onEdit(SeguimientoRegistro registro);
        void onDelete(SeguimientoRegistro registro);
    }

    private final List<SeguimientoRegistro> items = new ArrayList<>();
    private final Listener listener;

    public SeguimientoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<SeguimientoRegistro> registros) {
        items.clear();
        if (registros != null) {
            items.addAll(registros);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 14));
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(context, 12);
        root.setLayoutParams(params);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#FAFCFF"));
        background.setCornerRadius(dp(context, 18));
        background.setStroke(dp(context, 1), Color.parseColor("#D6E3F3"));
        root.setBackground(background);

        TextView title = text(context, 15, true, "#16324F");
        TextView value = text(context, 14, true, "#0F4BB3");
        TextView meta = text(context, 12, false, "#6B7280");
        TextView notes = text(context, 12, false, "#516072");

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(context, 10), 0, 0);

        MaterialButton btnEdit = button(context, context.getString(com.silveira.care360.R.string.medicacion_editar));
        MaterialButton btnDelete = button(context, context.getString(com.silveira.care360.R.string.medicacion_eliminar));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        deleteParams.leftMargin = dp(context, 10);
        btnDelete.setLayoutParams(deleteParams);

        actions.addView(btnEdit);
        actions.addView(btnDelete);
        root.addView(title);
        root.addView(value);
        root.addView(meta);
        root.addView(notes);
        root.addView(actions);
        return new RegistroViewHolder(root, title, value, meta, notes, btnEdit, btnDelete);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        SeguimientoRegistro item = items.get(position);
        Context context = holder.itemView.getContext();
        holder.title.setText(buildTypeLabel(item));
        holder.value.setText(buildValue(item));
        holder.meta.setText(buildDate(context, item));
        String notes = item != null ? item.getNotas() : null;
        holder.notes.setVisibility(isBlank(notes) ? View.GONE : View.VISIBLE);
        holder.notes.setText(notes);
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null && item != null) listener.onEdit(item);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null && item != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RegistroViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView value;
        final TextView meta;
        final TextView notes;
        final MaterialButton btnEdit;
        final MaterialButton btnDelete;

        RegistroViewHolder(@NonNull View itemView,
                           TextView title,
                           TextView value,
                           TextView meta,
                           TextView notes,
                           MaterialButton btnEdit,
                           MaterialButton btnDelete) {
            super(itemView);
            this.title = title;
            this.value = value;
            this.meta = meta;
            this.notes = notes;
            this.btnEdit = btnEdit;
            this.btnDelete = btnDelete;
        }
    }

    private static MaterialButton button(Context context, String text) {
        MaterialButton button = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private static TextView text(Context context, int sp, boolean bold, String color) {
        TextView view = new TextView(context);
        view.setTextColor(Color.parseColor(color));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    private static String buildTypeLabel(SeguimientoRegistro item) {
        if (item == null) return "-";
        String tipo = item.getTipo();
        if (SeguimientoRegistro.TIPO_TENSION.equals(tipo)) return "Tensión";
        if (SeguimientoRegistro.TIPO_GLUCOSA.equals(tipo)) return "Glucosa";
        if (SeguimientoRegistro.TIPO_TEMPERATURA.equals(tipo)) return "Temperatura";
        if (SeguimientoRegistro.TIPO_PESO.equals(tipo)) return "Peso";
        return "-";
    }

    private static String buildValue(SeguimientoRegistro item) {
        if (item == null) return "-";
        String tipo = item.getTipo();
        String principal = textOrDash(item.getValorPrincipal());
        String secundario = item.getValorSecundario();
        if (SeguimientoRegistro.TIPO_TENSION.equals(tipo) && !isBlank(secundario)) {
            return principal + " / " + secundario + " mmHg";
        }
        if (SeguimientoRegistro.TIPO_GLUCOSA.equals(tipo)) return principal + " mg/dL";
        if (SeguimientoRegistro.TIPO_TEMPERATURA.equals(tipo)) return principal + " °C";
        if (SeguimientoRegistro.TIPO_PESO.equals(tipo)) return principal + " kg";
        return principal;
    }

    private static String buildDate(Context context, SeguimientoRegistro item) {
        if (item == null || item.getRecordedAt() <= 0L) return "";
        java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(context);
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
        Date date = new Date(item.getRecordedAt());
        return dateFormat.format(date) + " · " + timeFormat.format(date);
    }

    private static int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String textOrDash(String value) {
        return isBlank(value) ? "-" : value.trim();
    }
}
