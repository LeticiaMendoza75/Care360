package com.silveira.care360.data.remote.dto;

public class DocumentoDocumentDto {

    private String id;
    private String titulo;
    private String tipo;
    private String fechaDocumento;
    private String notas;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private String createdBy;
    private long createdAt;
    private String updatedBy;
    private long updatedAt;

    public DocumentoDocumentDto() {
    }

    public DocumentoDocumentDto(String id,
                                String titulo,
                                String tipo,
                                String fechaDocumento,
                                String notas,
                                String fileUrl,
                                String fileName,
                                String mimeType,
                                String createdBy,
                                long createdAt,
                                String updatedBy,
                                long updatedAt) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.fechaDocumento = fechaDocumento;
        this.notas = notas;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getFechaDocumento() { return fechaDocumento; }
    public void setFechaDocumento(String fechaDocumento) { this.fechaDocumento = fechaDocumento; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
