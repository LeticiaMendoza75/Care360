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
import com.silveira.care360.domain.model.Documento;

import java.util.ArrayList;
import java.util.List;

public class DocumentosAdapter extends RecyclerView.Adapter<DocumentosAdapter.DocumentoViewHolder> {

    public interface Listener {
        void onVerMasClicked(Documento documento);
        void onGestionarClicked(Documento documento);
    }

    private final Listener listener;
    private final List<Documento> items = new ArrayList<>();

    public DocumentosAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Documento> documentos) {
        items.clear();
        if (documentos != null) {
            items.addAll(documentos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        TextView txtTitulo = text(context, 18, true, "#173A6A");
        TextView txtArchivo = text(context, 13, true, "#405A7A");
        TextView txtNotas = text(context, 12, false, "#7B8794");

        LinearLayout rowActions = new LinearLayout(context);
        rowActions.setOrientation(LinearLayout.HORIZONTAL);
        rowActions.setGravity(Gravity.END);
        LinearLayout.LayoutParams rowActionsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowActionsParams.topMargin = dp(context, 8);
        rowActions.setLayoutParams(rowActionsParams);

        MaterialButton btnVerMas = actionButton(context, context.getString(R.string.docs_ver));
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
        root.addView(txtArchivo);
        root.addView(txtNotas);
        root.addView(rowActions);

        return new DocumentoViewHolder(root, txtTitulo, txtArchivo, txtNotas, btnVerMas, btnGestionar);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentoViewHolder holder, int position) {
        Documento documento = items.get(position);
        Context context = holder.itemView.getContext();

        holder.txtTitulo.setText(textOrFallback(documento.getTitulo(), context.getString(R.string.docs_generico)));
        holder.txtArchivo.setText(textOrFallback(documento.getFileName(), context.getString(R.string.docs_archivo_pendiente)));
        if (documento.getNotas() != null && !documento.getNotas().trim().isEmpty()) {
            holder.txtNotas.setVisibility(android.view.View.VISIBLE);
            holder.txtNotas.setText(documento.getNotas().trim());
        } else {
            holder.txtNotas.setVisibility(android.view.View.GONE);
            holder.txtNotas.setText("");
        }
        holder.btnVerMas.setOnClickListener(v -> listener.onVerMasClicked(documento));
        holder.btnGestionar.setOnClickListener(v -> listener.onGestionarClicked(documento));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DocumentoViewHolder extends RecyclerView.ViewHolder {
        final TextView txtTitulo;
        final TextView txtArchivo;
        final TextView txtNotas;
        final MaterialButton btnVerMas;
        final MaterialButton btnGestionar;

        DocumentoViewHolder(@NonNull LinearLayout itemView,
                            TextView txtTitulo,
                            TextView txtArchivo,
                            TextView txtNotas,
                            MaterialButton btnVerMas,
                            MaterialButton btnGestionar) {
            super(itemView);
            this.txtTitulo = txtTitulo;
            this.txtArchivo = txtArchivo;
            this.txtNotas = txtNotas;
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
