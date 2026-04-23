package com.silveira.care360.data.remote.dto;

public class SeguimientoRegistroDocumentDto {

    private String id;
    private String tipo;
    private String valorPrincipal;
    private String valorSecundario;
    private String notas;
    private Long recordedAt;
    private String createdBy;
    private Long createdAt;
    private String updatedBy;
    private Long updatedAt;
    private Boolean deleted;

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
    public Long getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Long recordedAt) { this.recordedAt = recordedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
