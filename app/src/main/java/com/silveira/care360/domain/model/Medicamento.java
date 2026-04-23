package com.silveira.care360.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Medicamento {

    private String id;
    private String nombre;
    private String fechaInicio;
    private String fechaFin;
    private String observaciones;
    private boolean alertasActivas;
    private List<DiaMedicacion> dias;

    private String createdBy;
    private long createdAt;
    private String updatedBy;
    private long updatedAt;
    private boolean deleted;

    public Medicamento() {
        this.dias = new ArrayList<>();
    }

    public Medicamento(String id,
                       String nombre,
                       String fechaInicio,
                       String fechaFin,
                       String observaciones,
                       boolean alertasActivas,
                       List<DiaMedicacion> dias,
                       String createdBy,
                       long createdAt,
                       String updatedBy,
                       long updatedAt) {
        this(id, nombre, fechaInicio, fechaFin, observaciones, alertasActivas, dias, createdBy, createdAt, updatedBy, updatedAt, false);
    }

    public Medicamento(String id,
                       String nombre,
                       String fechaInicio,
                       String fechaFin,
                       String observaciones,
                       boolean alertasActivas,
                       List<DiaMedicacion> dias,
                       String createdBy,
                       long createdAt,
                       String updatedBy,
                       long updatedAt,
                       boolean deleted) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.observaciones = observaciones;
        this.alertasActivas = alertasActivas;
        this.dias = (dias != null) ? dias : new ArrayList<>();
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public List<DiaMedicacion> getDias() {
        return dias;
    }

    public boolean isAlertasActivas() {
        return alertasActivas;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void setAlertasActivas(boolean alertasActivas) {
        this.alertasActivas = alertasActivas;
    }

    public void setDias(List<DiaMedicacion> dias) {
        this.dias = (dias != null) ? dias : new ArrayList<>();
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
