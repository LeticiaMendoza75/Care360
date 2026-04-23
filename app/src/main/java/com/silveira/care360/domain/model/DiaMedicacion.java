package com.silveira.care360.domain.model;

import java.util.ArrayList;
import java.util.List;

public class DiaMedicacion {

    private String fecha;
    private List<String> horas;

    public DiaMedicacion() {
        this.horas = new ArrayList<>();
    }

    public DiaMedicacion(String fecha, List<String> horas) {
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
        this.horas = horas;
    }
}