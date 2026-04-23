package com.silveira.care360.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;

import java.util.ArrayList;
import java.util.List;

public class MedicamentosAdapter extends RecyclerView.Adapter<MedicamentosAdapter.MedicamentoViewHolder> {

    public interface Listener {
        void onVerMasClicked(Medicamento medicamento);
        void onGestionarClicked(Medicamento medicamento);
    }

    private final Listener listener;
    private final List<Medicamento> items = new ArrayList<>();

    public MedicamentosAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Medicamento> medicamentos) {
        items.clear();
        if (medicamentos != null) {
            items.addAll(medicamentos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = items.get(position);

        holder.txtNombreMedicamento.setText(safeText(medicamento.getNombre(), "Medicamento"));
        holder.txtFechaInicioMedicacion.setText(safeText(medicamento.getFechaInicio(), "-"));
        holder.txtFechaFinMedicacion.setText(safeText(medicamento.getFechaFin(), "-"));

        bindPrimerDia(holder, medicamento);

        holder.txtVerMasMedicamento.setOnClickListener(v -> listener.onVerMasClicked(medicamento));
        holder.btnGestionarMedicamento.setOnClickListener(v -> listener.onGestionarClicked(medicamento));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindPrimerDia(@NonNull MedicamentoViewHolder holder, @NonNull Medicamento medicamento) {
        holder.layoutDiasContainer.removeAllViews();

        List<DiaMedicacion> dias = medicamento.getDias();
        if (dias == null || dias.isEmpty()) {
            return;
        }

        DiaMedicacion primerDia = dias.get(0);
        if (primerDia == null) {
            return;
        }

        View diaView = LayoutInflater.from(holder.itemView.getContext())
                .inflate(R.layout.item_dia_medicacion, holder.layoutDiasContainer, false);

        TextView txtFechaDiaMedicacion = diaView.findViewById(R.id.txtFechaDiaMedicacion);
        ImageButton btnEditarDiaMedicacion = diaView.findViewById(R.id.btnEditarDiaMedicacion);
        ImageButton btnEliminarDiaMedicacion = diaView.findViewById(R.id.btnEliminarDiaMedicacion);
        LinearLayout layoutHorasContainer = diaView.findViewById(R.id.layoutHorasContainer);
        MaterialButton btnAddHoraDia = diaView.findViewById(R.id.btnAddHoraDia);

        txtFechaDiaMedicacion.setText(safeText(primerDia.getFecha(), "-"));
        btnEditarDiaMedicacion.setVisibility(View.GONE);
        btnEliminarDiaMedicacion.setVisibility(View.GONE);
        btnAddHoraDia.setVisibility(View.GONE);

        bindHoras(layoutHorasContainer, btnAddHoraDia, primerDia.getHoras());
        holder.layoutDiasContainer.addView(diaView);
    }

    private void bindHoras(@NonNull LinearLayout layoutHorasContainer,
                           @NonNull MaterialButton btnAddHoraDia,
                           List<String> horas) {

        layoutHorasContainer.removeAllViews();

        if (horas != null) {
            for (String hora : horas) {
                TextView chip = createHoraChip(layoutHorasContainer, safeText(hora, "--:--"));
                layoutHorasContainer.addView(chip);
            }
        }

        if (btnAddHoraDia.getVisibility() == View.VISIBLE) {
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(layoutHorasContainer, 40)
            );
            if (layoutHorasContainer.getChildCount() > 0) {
                buttonParams.leftMargin = dpToPx(layoutHorasContainer, 8);
            }

            btnAddHoraDia.setLayoutParams(buttonParams);
            layoutHorasContainer.addView(btnAddHoraDia);
        }
    }

    private TextView createHoraChip(@NonNull LinearLayout parent, @NonNull String hora) {
        TextView chip = new TextView(parent.getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (parent.getChildCount() > 0) {
            params.leftMargin = dpToPx(parent, 8);
        }
        chip.setLayoutParams(params);

        chip.setBackgroundResource(R.drawable.bg_chip_hora_medicacion);
        chip.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dot_small, 0, 0, 0);
        chip.setCompoundDrawablePadding(dpToPx(parent, 6));
        chip.setPadding(
                dpToPx(parent, 12),
                dpToPx(parent, 8),
                dpToPx(parent, 12),
                dpToPx(parent, 8)
        );
        chip.setText(hora);
        chip.setTextColor(0xFF1F2937);
        chip.setTextSize(13);

        return chip;
    }

    private int dpToPx(@NonNull View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    static class MedicamentoViewHolder extends RecyclerView.ViewHolder {

        final TextView txtNombreMedicamento;
        final TextView txtFechaInicioMedicacion;
        final TextView txtFechaFinMedicacion;
        final TextView txtVerMasMedicamento;
        final MaterialButton btnGestionarMedicamento;
        final LinearLayout layoutDiasContainer;

        MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombreMedicamento = itemView.findViewById(R.id.txtNombreMedicamento);
            txtFechaInicioMedicacion = itemView.findViewById(R.id.txtFechaInicioMedicacion);
            txtFechaFinMedicacion = itemView.findViewById(R.id.txtFechaFinMedicacion);
            txtVerMasMedicamento = itemView.findViewById(R.id.txtVerMasMedicamento);
            btnGestionarMedicamento = itemView.findViewById(R.id.btnGestionarMedicamento);
            layoutDiasContainer = itemView.findViewById(R.id.layoutDiasContainer);
        }
    }
}
