package com.silveira.care360.data.remote.dto;

import java.util.ArrayList;
import java.util.List;

public class DiaMedicacionDocumentDto {

    private String fecha;
    private String fechaKey;
    private List<String> horas = new ArrayList<>();

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFechaKey() {
        return fechaKey;
    }

    public void setFechaKey(String fechaKey) {
        this.fechaKey = fechaKey;
    }

    public List<String> getHoras() {
        return horas;
    }

    public void setHoras(List<String> horas) {
        this.horas = horas != null ? horas : new ArrayList<>();
    }
}
