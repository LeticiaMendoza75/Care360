package com.silveira.care360.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silveira.care360.R;
import com.silveira.care360.domain.model.ActividadItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    private final List<ActividadItem> items = new ArrayList<>();

    public void submitList(List<ActividadItem> actividad) {
        items.clear();
        if (actividad != null) {
            items.addAll(actividad);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        TextView txtHeadline = text(context, 15, true, "#173A6A");
        TextView txtDetail = text(context, 13, false, "#405A7A");
        TextView txtMeta = text(context, 12, false, "#7B8794");
        txtMeta.setPadding(0, dp(context, 4), 0, 0);

        root.addView(txtHeadline);
        root.addView(txtDetail);
        root.addView(txtMeta);

        return new ActividadViewHolder(root, txtHeadline, txtDetail, txtMeta);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        ActividadItem item = items.get(position);
        Context context = holder.itemView.getContext();
        holder.txtHeadline.setText(buildHeadline(context, item));
        holder.txtDetail.setText(textOrFallback(item != null ? item.getEntityTitle() : null, context.getString(R.string.activity_empty_item)));
        holder.txtMeta.setText(buildMeta(context, item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String buildHeadline(Context context, ActividadItem item) {
        if (item == null) return context.getString(R.string.activity_empty_item);
        String actor = textOrFallback(item.getActorName(), context.getString(R.string.activity_actor_fallback));
        switch (textOrFallback(item.getEntityType(), "")) {
            case ActividadItem.TYPE_MEDICACION:
                return context.getString(
                        ActividadItem.ACTION_DELETED.equals(item.getActionType())
                                ? R.string.activity_headline_medicacion_deleted
                                : (ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_medicacion_updated
                                : R.string.activity_headline_medicacion_created),
                        actor
                );
            case ActividadItem.TYPE_CITA:
                return context.getString(
                        ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_cita_updated
                                : R.string.activity_headline_cita_created,
                        actor
                );
            case ActividadItem.TYPE_PATOLOGIA:
                return context.getString(
                        ActividadItem.ACTION_DELETED.equals(item.getActionType())
                                ? R.string.activity_headline_patologia_deleted
                                : (ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_patologia_updated
                                : R.string.activity_headline_patologia_created),
                        actor
                );
            case ActividadItem.TYPE_SEGUIMIENTO:
                return context.getString(
                        ActividadItem.ACTION_DELETED.equals(item.getActionType())
                                ? R.string.activity_headline_seguimiento_deleted
                                : (ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_seguimiento_updated
                                : R.string.activity_headline_seguimiento_created),
                        actor
                );
            case ActividadItem.TYPE_INCIDENCIA:
            default:
                return context.getString(
                        ActividadItem.ACTION_UPDATED.equals(item.getActionType())
                                ? R.string.activity_headline_incidencia_updated
                                : R.string.activity_headline_incidencia_created,
                        actor
                );
        }
    }

    private String buildMeta(Context context, ActividadItem item) {
        if (item == null || item.getTimestamp() <= 0L) {
            return context.getString(R.string.activity_date_pending);
        }
        java.text.DateFormat formatter = DateFormat.getMediumDateFormat(context);
        java.text.DateFormat timeFormatter = DateFormat.getTimeFormat(context);
        Date date = new Date(item.getTimestamp());
        return formatter.format(date) + " · " + timeFormatter.format(date);
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        final TextView txtHeadline;
        final TextView txtDetail;
        final TextView txtMeta;

        ActividadViewHolder(@NonNull LinearLayout itemView,
                            TextView txtHeadline,
                            TextView txtDetail,
                            TextView txtMeta) {
            super(itemView);
            this.txtHeadline = txtHeadline;
            this.txtDetail = txtDetail;
            this.txtMeta = txtMeta;
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
        params.bottomMargin = dp(context, 2);
        textView.setLayoutParams(params);
        return textView;
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
