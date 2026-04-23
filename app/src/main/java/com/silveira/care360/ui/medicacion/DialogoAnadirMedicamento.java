package com.silveira.care360.ui.medicacion;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.silveira.care360.R;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.DiaMedicacionTemporal;
import com.silveira.care360.domain.model.Medicamento;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DialogoAnadirMedicamento {

    public interface Listener {
        void onMedicamentoConfirmado(Medicamento medicamento);
    }

    public interface EditorListener extends Listener {
        void onMedicamentoEliminar(Medicamento medicamento);
    }

    private final Context context;

    public DialogoAnadirMedicamento(Context context) {
        this.context = context;
    }

    public void show(Listener listener) {
        show(null, listener);
    }

    public void show(Medicamento initialMedicamento, Listener listener) {
        show(initialMedicamento, listener, null);
    }

    public void show(Medicamento initialMedicamento, Listener listener, Runnable onDeleteConfirmed) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_anadir_medicamento, null, false);

        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombreMedicamento);
        TextInputEditText etFechaInicio = dialogView.findViewById(R.id.etFechaInicioMedicamento);
        TextInputEditText etFechaFin = dialogView.findViewById(R.id.etFechaFinMedicamento);
        TextInputEditText etObservaciones = dialogView.findViewById(R.id.etObservacionesMedicamento);
        TextView txtResumenFechas = dialogView.findViewById(R.id.txtResumenFechasMedicamento);
        SwitchMaterial switchAlertas = dialogView.findViewById(R.id.switchAlertasMedicamento);
        MaterialButton btnAddDia = dialogView.findViewById(R.id.btnAddDiaDialog);
        LinearLayout layoutDias = dialogView.findViewById(R.id.layoutDiasDialog);

        List<DiaMedicacionTemporal> diasTemp = toTempList(initialMedicamento != null ? initialMedicamento.getDias() : null);
        prefillFields(initialMedicamento, etNombre, etObservaciones, switchAlertas);
        recalculateResumenFechas(diasTemp, etFechaInicio, etFechaFin, txtResumenFechas);
        renderDias(layoutDias, diasTemp, etFechaInicio, etFechaFin, txtResumenFechas);

        btnAddDia.setOnClickListener(v -> abrirSelectorFecha(fecha -> {
            if (existeDia(fecha, diasTemp, null)) {
                showDialogMessage(dialogView, "Día ya añadido");
                return;
            }

            diasTemp.add(new DiaMedicacionTemporal(fecha, new ArrayList<>()));
            recalculateResumenFechas(diasTemp, etFechaInicio, etFechaFin, txtResumenFechas);
            renderDias(layoutDias, diasTemp, etFechaInicio, etFechaFin, txtResumenFechas);
        }));

        boolean isEditing = initialMedicamento != null && !isBlank(initialMedicamento.getId());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(isEditing ? "Editar medicamento" : context.getString(R.string.medicacion_anadir_medicamento))
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        if (isEditing && onDeleteConfirmed != null) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Eliminar\nmedicamento", (dialogInterface, which) -> { });
        }

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nombre = getText(etNombre);
                if (isBlank(nombre)) {
                    etNombre.setError(context.getString(R.string.medicacion_nombre_obligatorio));
                    return;
                }
                etNombre.setError(null);

                if (diasTemp.isEmpty()) {
                    showDialogMessage(dialogView, context.getString(R.string.medicacion_dia_obligatorio));
                    return;
                }

                Runnable saveAction = () -> {
                    listener.onMedicamentoConfirmado(buildMedicamento(
                            initialMedicamento,
                            nombre,
                            getCalculatedText(etFechaInicio),
                            getCalculatedText(etFechaFin),
                            getText(etObservaciones),
                            switchAlertas.isChecked(),
                            convertir(diasTemp)
                    ));
                    dialog.dismiss();
                };

                if (!hasAnyHora(diasTemp)) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.medicacion_sin_horas_titulo)
                            .setMessage(R.string.medicacion_sin_horas_mensaje)
                            .setNegativeButton(R.string.medicacion_sin_horas_anadir, null)
                            .setPositiveButton(R.string.medicacion_sin_horas_guardar, (confirmDialog, which) -> saveAction.run())
                            .show();
                    return;
                }

                saveAction.run();
            });

            if (isEditing && onDeleteConfirmed != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setSingleLine(false);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setMaxLines(2);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> new AlertDialog.Builder(context)
                        .setTitle("Eliminar medicamento")
                        .setMessage("Se eliminara este medicamento. Esta accion no se puede deshacer.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar", (confirmDialog, which) -> {
                            dialog.dismiss();
                            onDeleteConfirmed.run();
                        })
                        .show());
            }
        });

        dialog.show();
    }

    private void prefillFields(Medicamento medicamento,
                               TextInputEditText etNombre,
                               TextInputEditText etObservaciones,
                               SwitchMaterial switchAlertas) {
        if (medicamento == null) {
            return;
        }

        etNombre.setText(medicamento.getNombre());
        etObservaciones.setText(medicamento.getObservaciones());
        switchAlertas.setChecked(medicamento.isAlertasActivas());
    }

    private void renderDias(LinearLayout container,
                            List<DiaMedicacionTemporal> dias,
                            TextInputEditText etFechaInicio,
                            TextInputEditText etFechaFin,
                            TextView txtResumenFechas) {
        container.removeAllViews();
        sortDias(dias);

        for (DiaMedicacionTemporal dia : dias) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_dia_medicacion, container, false);

            TextView txtFecha = view.findViewById(R.id.txtFechaDiaMedicacion);
            ImageButton btnEditar = view.findViewById(R.id.btnEditarDiaMedicacion);
            ImageButton btnEliminar = view.findViewById(R.id.btnEliminarDiaMedicacion);
            MaterialButton btnAddHora = view.findViewById(R.id.btnAddHoraDia);
            LinearLayout layoutHoras = view.findViewById(R.id.layoutHorasContainer);

            txtFecha.setText(dia.getFecha());
            btnEditar.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.VISIBLE);
            btnAddHora.setVisibility(View.VISIBLE);

            btnEditar.setOnClickListener(v -> abrirSelectorFecha(nuevaFecha -> {
                if (existeDia(nuevaFecha, dias, dia)) {
                    showDialogMessage(container, "Día ya añadido");
                    return;
                }
                dia.setFecha(nuevaFecha);
                recalculateResumenFechas(dias, etFechaInicio, etFechaFin, txtResumenFechas);
                renderDias(container, dias, etFechaInicio, etFechaFin, txtResumenFechas);
            }));

            btnEliminar.setOnClickListener(v -> {
                dias.remove(dia);
                recalculateResumenFechas(dias, etFechaInicio, etFechaFin, txtResumenFechas);
                renderDias(container, dias, etFechaInicio, etFechaFin, txtResumenFechas);
            });

            btnAddHora.setOnClickListener(v -> abrirSelectorHora(hora -> {
                if (dia.getHoras().contains(hora)) {
                    showDialogMessage(container, "Hora ya añadida");
                    return;
                }
                dia.addHora(hora);
                renderDias(container, dias, etFechaInicio, etFechaFin, txtResumenFechas);
            }));

            renderHoras(layoutHoras, dia, dias, container, etFechaInicio, etFechaFin, txtResumenFechas);
            container.addView(view);
        }
    }

    private void renderHoras(LinearLayout container,
                             DiaMedicacionTemporal dia,
                             List<DiaMedicacionTemporal> dias,
                             LinearLayout daysContainer,
                             TextInputEditText etFechaInicio,
                             TextInputEditText etFechaFin,
                             TextView txtResumenFechas) {
        MaterialButton addHourButton = null;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof MaterialButton && child.getId() == R.id.btnAddHoraDia) {
                addHourButton = (MaterialButton) child;
                break;
            }
        }

        container.removeAllViews();

        for (String hora : dia.getHoras()) {
            TextView chip = createHoraChip(hora);
            chip.setOnClickListener(v -> showHourOptions(container, dia, hora,
                    () -> renderDias(daysContainer, dias, etFechaInicio, etFechaFin, txtResumenFechas)));
            container.addView(chip);
        }

        if (addHourButton != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dpToPx(40)
            );
            if (container.getChildCount() > 0) {
                params.leftMargin = dpToPx(8);
            }
            addHourButton.setLayoutParams(params);
            container.addView(addHourButton);
        }
    }

    private TextView createHoraChip(String hora) {
        TextView chip = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chip.setLayoutParams(params);
        chip.setBackgroundResource(R.drawable.bg_chip_hora_medicacion);
        chip.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dot_small, 0, 0, 0);
        chip.setCompoundDrawablePadding(dpToPx(6));
        chip.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        chip.setText(hora);
        chip.setTextColor(0xFF1F2937);
        chip.setTextSize(13);
        return chip;
    }

    private void showHourOptions(View anchor, DiaMedicacionTemporal dia, String hora, Runnable onUpdated) {
        new AlertDialog.Builder(context)
                .setTitle(hora)
                .setItems(new CharSequence[]{"Editar hora", "Eliminar hora"}, (dialog, which) -> {
                    if (which == 0) {
                        abrirSelectorHora(nuevaHora -> {
                            if (dia.getHoras().contains(nuevaHora) && !nuevaHora.equals(hora)) {
                                showDialogMessage(anchor, "Hora ya añadida");
                                return;
                            }

                            List<String> horas = dia.getHoras();
                            int index = horas.indexOf(hora);
                            if (index >= 0) {
                                horas.set(index, nuevaHora);
                                onUpdated.run();
                            }
                        });
                    } else if (which == 1) {
                        dia.removeHora(hora);
                        onUpdated.run();
                    }
                })
                .show();
    }

    private List<DiaMedicacionTemporal> toTempList(List<DiaMedicacion> dias) {
        List<DiaMedicacionTemporal> result = new ArrayList<>();
        if (dias == null) {
            return result;
        }

        for (DiaMedicacion dia : dias) {
            if (dia == null) continue;
            result.add(new DiaMedicacionTemporal(
                    dia.getFecha(),
                    dia.getHoras() != null ? new ArrayList<>(dia.getHoras()) : new ArrayList<>()
            ));
        }

        return result;
    }

    private Medicamento buildMedicamento(Medicamento initialMedicamento,
                                         String nombre,
                                         String fechaInicio,
                                         String fechaFin,
                                         String observaciones,
                                         boolean alertasActivas,
                                         List<DiaMedicacion> dias) {
        Medicamento medicamento = new Medicamento();

        if (initialMedicamento != null) {
            medicamento.setId(initialMedicamento.getId());
            medicamento.setCreatedBy(initialMedicamento.getCreatedBy());
            medicamento.setCreatedAt(initialMedicamento.getCreatedAt());
            medicamento.setUpdatedBy(initialMedicamento.getUpdatedBy());
            medicamento.setUpdatedAt(initialMedicamento.getUpdatedAt());
        }

        medicamento.setNombre(nombre);
        medicamento.setFechaInicio(fechaInicio);
        medicamento.setFechaFin(fechaFin);
        medicamento.setObservaciones(observaciones);
        medicamento.setAlertasActivas(alertasActivas);
        medicamento.setDias(dias);
        return medicamento;
    }

    private void recalculateResumenFechas(List<DiaMedicacionTemporal> dias,
                                          TextInputEditText etFechaInicio,
                                          TextInputEditText etFechaFin,
                                          TextView txtResumenFechas) {
        sortDias(dias);

        if (dias == null || dias.isEmpty()) {
            etFechaInicio.setText(context.getString(R.string.medicacion_fecha_resumen_vacia));
            etFechaFin.setText(context.getString(R.string.medicacion_fecha_resumen_vacia));
            txtResumenFechas.setText(R.string.medicacion_fechas_calculadas);
            return;
        }

        String fechaInicio = safeText(dias.get(0).getFecha());
        String fechaFin = safeText(dias.get(dias.size() - 1).getFecha());

        etFechaInicio.setText(isBlank(fechaInicio) ? context.getString(R.string.medicacion_fecha_resumen_vacia) : fechaInicio);
        etFechaFin.setText(isBlank(fechaFin) ? context.getString(R.string.medicacion_fecha_resumen_vacia) : fechaFin);
        txtResumenFechas.setText(R.string.medicacion_fechas_calculadas);
    }

    private void sortDias(List<DiaMedicacionTemporal> dias) {
        if (dias == null) return;
        Collections.sort(dias, Comparator.comparing(this::fechaSortable));
    }

    private String fechaSortable(DiaMedicacionTemporal dia) {
        if (dia == null || isBlank(dia.getFecha())) {
            return "9999-99-99";
        }
        String[] parts = dia.getFecha().trim().split("/");
        if (parts.length == 3) {
            return parts[2] + "-" + pad(parts[1]) + "-" + pad(parts[0]);
        }
        return dia.getFecha().trim();
    }

    private String pad(String value) {
        return value != null && value.trim().length() == 1 ? "0" + value.trim() : safeText(value);
    }

    private List<DiaMedicacion> convertir(List<DiaMedicacionTemporal> temp) {
        List<DiaMedicacion> result = new ArrayList<>();
        for (DiaMedicacionTemporal d : temp) {
            DiaMedicacion dia = new DiaMedicacion();
            dia.setFecha(d.getFecha());
            dia.setHoras(new ArrayList<>(d.getHoras()));
            result.add(dia);
        }
        return result;
    }

    private boolean hasAnyHora(List<DiaMedicacionTemporal> dias) {
        if (dias == null) {
            return false;
        }
        for (DiaMedicacionTemporal dia : dias) {
            if (dia != null && dia.getHoras() != null && !dia.getHoras().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean existeDia(String fecha, List<DiaMedicacionTemporal> lista, DiaMedicacionTemporal currentDia) {
        for (DiaMedicacionTemporal d : lista) {
            if (d == currentDia) continue;
            if (safeText(d.getFecha()).equalsIgnoreCase(safeText(fecha))) {
                return true;
            }
        }
        return false;
    }

    private void abrirSelectorFecha(OnFecha l) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(context, (v, y, m, d) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
            l.onFecha(fecha);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void abrirSelectorHora(OnHora l) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(context, (v, h, m) -> {
            String hora = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            l.onHora(hora);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String getCalculatedText(TextInputEditText et) {
        String value = getText(et);
        String emptyLabel = context.getString(R.string.medicacion_fecha_resumen_vacia);
        return emptyLabel.equals(value) ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeText(String value) {
        return value != null ? value.trim() : "";
    }

    private void showDialogMessage(View anchor, String message) {
        if (anchor == null || isBlank(message)) {
            return;
        }
        Snackbar.make(anchor, message.trim(), Snackbar.LENGTH_LONG).show();
    }

    interface OnFecha { void onFecha(String f); }
    interface OnHora { void onHora(String h); }
}
