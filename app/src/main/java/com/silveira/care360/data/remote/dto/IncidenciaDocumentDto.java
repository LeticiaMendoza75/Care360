package com.silveira.care360.data.remote.dto;

public class IncidenciaDocumentDto {

    private String id;
    private String tipo;
    private String fecha;
    private String fechaKey;
    private String hora;
    private String nivel;
    private String descripcion;
    private String createdBy;
    private long createdAt;
    private String updatedBy;
    private long updatedAt;

    public IncidenciaDocumentDto() {
    }

    public IncidenciaDocumentDto(String id,
                                 String tipo,
                                 String fecha,
                                 String fechaKey,
                                 String hora,
                                 String nivel,
                                 String descripcion,
                                 String createdBy,
                                 long createdAt,
                                 String updatedBy,
                                 long updatedAt) {
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.fechaKey = fechaKey;
        this.hora = hora;
        this.nivel = nivel;
        this.descripcion = descripcion;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getFechaKey() { return fechaKey; }
    public void setFechaKey(String fechaKey) { this.fechaKey = fechaKey; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
