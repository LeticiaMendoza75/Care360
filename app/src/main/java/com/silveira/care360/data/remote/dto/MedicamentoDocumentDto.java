package com.silveira.care360.data.remote.dto;

import java.util.ArrayList;
import java.util.List;

public class MedicamentoDocumentDto {

    private String id;
    private String nombre;
    private String fechaInicio;
    private String fechaFin;
    private String observaciones;
    private boolean alertasActivas;
    private List<DiaMedicacionDocumentDto> dias = new ArrayList<>();
    private String createdBy;
    private long createdAt;
    private String updatedBy;
    private long updatedAt;
    private boolean deleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isAlertasActivas() {
        return alertasActivas;
    }

    public void setAlertasActivas(boolean alertasActivas) {
        this.alertasActivas = alertasActivas;
    }

    public List<DiaMedicacionDocumentDto> getDias() {
        return dias;
    }

    public void setDias(List<DiaMedicacionDocumentDto> dias) {
        this.dias = dias != null ? dias : new ArrayList<>();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
