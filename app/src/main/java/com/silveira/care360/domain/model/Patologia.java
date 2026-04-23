package com.silveira.care360.domain.model;

public class Patologia {

    private String id;
    private String nombre;
    private String descripcion;
    private String createdBy;
    private long createdAt;
    private String updatedBy;
    private long updatedAt;
    private boolean deleted;

    public Patologia() {
        this("", "", "", "", 0L, "", 0L, false);
    }

    public Patologia(String id,
                     String nombre,
                     String descripcion,
                     String createdBy,
                     long createdAt,
                     String updatedBy,
                     long updatedAt,
                     boolean deleted) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
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
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
