package com.silveira.care360.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.domain.model.Patologia;

import java.util.ArrayList;
import java.util.List;

public class PatologiasAdapter extends RecyclerView.Adapter<PatologiasAdapter.PatologiaViewHolder> {

    public interface Listener {
        void onEdit(Patologia patologia);
        void onDelete(Patologia patologia);
    }

    private final List<Patologia> items = new ArrayList<>();
    private final Listener listener;

    public PatologiasAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Patologia> patologias) {
        items.clear();
        if (patologias != null) {
            items.addAll(patologias);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatologiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        TextView description = text(context, 13, false, "#516072");
        description.setPadding(0, dp(context, 4), 0, 0);

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(context, 10), 0, 0);

        MaterialButton btnEdit = actionButton(context, context.getString(com.silveira.care360.R.string.medicacion_editar), "#0F4BB3", "#FFFFFF");
        MaterialButton btnDelete = actionButton(context, context.getString(com.silveira.care360.R.string.medicacion_eliminar), "#FFFFFF", "#D93A2F");
        btnDelete.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D93A2F")));
        btnDelete.setStrokeWidth(dp(context, 1));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        deleteParams.leftMargin = dp(context, 10);
        btnDelete.setLayoutParams(deleteParams);

        actions.addView(btnEdit);
        actions.addView(btnDelete);
        root.addView(title);
        root.addView(description);
        root.addView(actions);
        return new PatologiaViewHolder(root, title, description, btnEdit, btnDelete);
    }

    @Override
    public void onBindViewHolder(@NonNull PatologiaViewHolder holder, int position) {
        Patologia item = items.get(position);
        holder.title.setText(textOrDash(item != null ? item.getNombre() : null));
        String description = item != null ? item.getDescripcion() : null;
        holder.description.setVisibility(isBlank(description) ? View.GONE : View.VISIBLE);
        holder.description.setText(description);
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

    static class PatologiaViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final MaterialButton btnEdit;
        final MaterialButton btnDelete;

        PatologiaViewHolder(@NonNull View itemView,
                            TextView title,
                            TextView description,
                            MaterialButton btnEdit,
                            MaterialButton btnDelete) {
            super(itemView);
            this.title = title;
            this.description = description;
            this.btnEdit = btnEdit;
            this.btnDelete = btnDelete;
        }
    }

    private static TextView text(Context context, int sp, boolean bold, String color) {
        TextView view = new TextView(context);
        view.setTextColor(Color.parseColor(color));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    private static MaterialButton actionButton(Context context, String text, String background, String textColor) {
        MaterialButton button = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.parseColor(textColor));
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(background)));
        return button;
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
