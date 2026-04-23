package com.silveira.care360.domain.model;

public class SeguimientoRegistro {

    public static final String TIPO_TENSION = "tension";
    public static final String TIPO_GLUCOSA = "glucosa";
    public static final String TIPO_TEMPERATURA = "temperatura";
    public static final String TIPO_PESO = "peso";

    private String id;
    private String tipo;
    private String valorPrincipal;
    private String valorSecundario;
    private String notas;
    private long recordedAt;
    private String createdBy;
    private long createdAt;
    private String updatedBy;
    private long updatedAt;
    private boolean deleted;

    public SeguimientoRegistro() {
        this("", "", "", "", "", 0L, "", 0L, "", 0L, false);
    }

    public SeguimientoRegistro(String id,
                               String tipo,
                               String valorPrincipal,
                               String valorSecundario,
                               String notas,
                               long recordedAt,
                               String createdBy,
                               long createdAt,
                               String updatedBy,
                               long updatedAt,
                               boolean deleted) {
        this.id = id;
        this.tipo = tipo;
        this.valorPrincipal = valorPrincipal;
        this.valorSecundario = valorSecundario;
        this.notas = notas;
        this.recordedAt = recordedAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getValorPrincipal() { return valorPrincipal; }
    public void setValorPrincipal(String valorPrincipal) { this.valorPrincipal = valorPrincipal; }
    public String getValorSecundario() { return valorSecundario; }
    public void setValorSecundario(String valorSecundario) { this.valorSecundario = valorSecundario; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public long getRecordedAt() { return recordedAt; }
    public void setRecordedAt(long recordedAt) { this.recordedAt = recordedAt; }
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
