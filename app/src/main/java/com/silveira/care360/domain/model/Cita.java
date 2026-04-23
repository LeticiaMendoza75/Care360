package com.silveira.care360.domain.model;

public class Cita {

    private String id;
    private String titulo;
    private String fecha;
    private String hora;
    private String lugar;
    private String profesional;
    private String personaEncargada;
    private String observaciones;
    private boolean recordatorioActivo;
    private String createdBy;
    private long createdAt;
    private String updatedBy;
    private long updatedAt;

    public Cita() {
    }

    public Cita(String id,
                String titulo,
                String fecha,
                String hora,
                String lugar,
                String profesional,
                String personaEncargada,
                String observaciones,
                boolean recordatorioActivo,
                String createdBy,
                long createdAt,
                String updatedBy,
                long updatedAt) {
        this.id = id;
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.lugar = lugar;
        this.profesional = profesional;
        this.personaEncargada = personaEncargada;
        this.observaciones = observaciones;
        this.recordatorioActivo = recordatorioActivo;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getProfesional() { return profesional; }
    public void setProfesional(String profesional) { this.profesional = profesional; }
    public String getPersonaEncargada() { return personaEncargada; }
    public void setPersonaEncargada(String personaEncargada) { this.personaEncargada = personaEncargada; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public boolean isRecordatorioActivo() { return recordatorioActivo; }
    public void setRecordatorioActivo(boolean recordatorioActivo) { this.recordatorioActivo = recordatorioActivo; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
