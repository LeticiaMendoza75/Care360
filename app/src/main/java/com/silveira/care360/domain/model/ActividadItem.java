package com.silveira.care360.domain.model;

public class ActividadItem {

    public static final String TYPE_MEDICACION = "medicacion";
    public static final String TYPE_CITA = "cita";
    public static final String TYPE_INCIDENCIA = "incidencia";
    public static final String TYPE_PATOLOGIA = "patologia";
    public static final String TYPE_SEGUIMIENTO = "seguimiento";

    public static final String ACTION_CREATED = "created";
    public static final String ACTION_UPDATED = "updated";
    public static final String ACTION_DELETED = "deleted";

    private final String id;
    private final String entityType;
    private final String actionType;
    private final String actorUserId;
    private final String actorName;
    private final String entityTitle;
    private final long timestamp;

    public ActividadItem(String id,
                         String entityType,
                         String actionType,
                         String actorUserId,
                         String actorName,
                         String entityTitle,
                         long timestamp) {
        this.id = id;
        this.entityType = entityType;
        this.actionType = actionType;
        this.actorUserId = actorUserId;
        this.actorName = actorName;
        this.entityTitle = entityTitle;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getActionType() {
        return actionType;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public String getActorName() {
        return actorName;
    }

    public String getEntityTitle() {
        return entityTitle;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
