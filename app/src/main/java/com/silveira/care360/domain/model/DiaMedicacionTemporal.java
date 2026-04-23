package com.silveira.care360.domain.model;

import java.util.ArrayList;
import java.util.List;

public class DiaMedicacionTemporal {

    private String fecha;
    private List<String> horas;

    public DiaMedicacionTemporal() {
        this.horas = new ArrayList<>();
    }

    public DiaMedicacionTemporal(String fecha, List<String> horas) {
        this.fecha = fecha;
        this.horas = (horas != null) ? horas : new ArrayList<>();
    }

    public String getFecha() {
        return fecha;
    }

    public List<String> getHoras() {
        return horas;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setHoras(List<String> horas) {
        this.horas = (horas != null) ? horas : new ArrayList<>();
    }

    public void addHora(String hora) {
        if (hora != null && !hora.trim().isEmpty()) {
            this.horas.add(hora.trim());
        }
    }

    public void removeHora(String hora) {
        this.horas.remove(hora);
    }
}