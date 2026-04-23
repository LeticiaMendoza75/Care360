# Care360

## Firebase Rules

## Regla Actual Detectada

Actualmente la base de datos esta abierta a cualquier usuario autenticado:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Esta regla es util para desarrollo inicial, pero es demasiado permisiva para un producto real.

## Estructura Real Detectada En El Codigo

Colecciones y subcolecciones observadas en los data sources Firestore:

- `users/{uid}`
- `users/{uid}/memberships/{groupId}`
- `groups/{groupId}`
- `groups/{groupId}/members/{userId}`
- `groups/{groupId}/medicamentos/{medicamentoId}`
- `groups/{groupId}/medicamentos/{medicamentoId}/dias/{fechaKey}`
- `groups/{groupId}/citas/{citaId}`
- `groups/{groupId}/incidencias/{incidenciaId}`
- `groups/{groupId}/documentos/{documentoId}`
- `groups/{groupId}/patologias/{patologiaId}`
- `groups/{groupId}/seguimiento/{seguimientoId}`

## Campos Relevantes De Seguridad Detectados

En el codigo aparecen estos campos relacionados con identidad o autorizacion:

### En `users/{uid}`

- `uid`
- `activeGroupId`
- `email`
- `displayName`
- `photoUrl`
- `authProvider`
- `active`

### En `groups/{groupId}`

- `createdBy`
- `joinCode`
- `active`
- `name`
- `careName`

### En `groups/{groupId}/members/{userId}`

- `uid`
- `name`
- `email`
- `role`
- `joinedAt`
- `updatedAt`

### En `users/{uid}/memberships/{groupId}`

- `groupId`
- `groupName`
- `careName`
- `role`
- `joinedAt`
- `updatedAt`

## Principio De Seguridad Recomendado

- cada usuario solo debe poder modificar su propio documento en `users`
- un usuario solo debe poder leer y operar sobre grupos de los que es miembro
- la lectura y escritura de subcolecciones de grupo debe depender de pertenencia al grupo
- la administracion de miembros o eliminacion de grupo deberia restringirse a administradores o creador del grupo

## Propuesta De Reglas Mas Segura

La siguiente propuesta mejora mucho la seguridad respecto a la actual y esta alineada con la estructura real detectada. Se ha planteado para no inventar rutas que no aparezcan en el codigo.

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    function isSignedIn() {
      return request.auth != null;
    }

    function isSelf(userId) {
      return isSignedIn() && request.auth.uid == userId;
    }

    function isGroupMember(groupId) {
      return isSignedIn() &&
        exists(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid));
    }

    function isGroupAdmin(groupId) {
      return isSignedIn() &&
        get(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid)).data.role == "admin";
    }

    match /users/{userId} {
      allow read: if isSelf(userId);
      allow create: if isSelf(userId);
      allow update: if isSelf(userId);
      allow delete: if false;

      match /memberships/{groupId} {
        allow read: if isSelf(userId);
        allow create, update, delete: if isSelf(userId);
      }
    }

    match /groups/{groupId} {
      allow read: if isSignedIn();

      allow create: if isSignedIn()
        && request.resource.data.createdBy == request.auth.uid;

      allow update: if isGroupMember(groupId);

      allow delete: if isGroupAdmin(groupId);

      match /members/{memberId} {
        allow read: if isGroupMember(groupId);

        allow create: if isSignedIn()
          && memberId == request.auth.uid
          && request.resource.data.uid == request.auth.uid;

        allow update: if isGroupAdmin(groupId)
          || (isSignedIn() && memberId == request.auth.uid);

        allow delete: if isGroupAdmin(groupId)
          || (isSignedIn() && memberId == request.auth.uid);
      }

      match /medicamentos/{medicamentoId} {
        allow read, create, update, delete: if isGroupMember(groupId);

        match /dias/{fechaKey} {
          allow read, create, update, delete: if isGroupMember(groupId);
        }
      }

      match /citas/{citaId} {
        allow read, create, update, delete: if isGroupMember(groupId);
      }

      match /incidencias/{incidenciaId} {
        allow read, create, update, delete: if isGroupMember(groupId);
      }

      match /documentos/{documentoId} {
        allow read, create, update, delete: if isGroupMember(groupId);
      }

      match /patologias/{patologiaId} {
        allow read, create, update, delete: if isGroupMember(groupId);
      }

      match /seguimiento/{seguimientoId} {
        allow read, create, update, delete: if isGroupMember(groupId);
      }
    }
  }
}
```

## Explicacion Breve Por Bloque

### Bloque `users/{userId}`

- limita lectura y escritura al propio usuario
- evita que un usuario consulte o altere el perfil de otro

### Bloque `users/{userId}/memberships/{groupId}`

- permite que cada usuario lea y gestione solo sus membresias
- encaja con el flujo actual de cambio de grupo activo y membresias

### Bloque `groups/{groupId}`

- permite crear grupos a usuarios autenticados cuyo `createdBy` coincide con su `uid`
- permite leer grupos a usuarios autenticados
- limita borrado de grupo a administradores

### Bloque `groups/{groupId}/members/{memberId}`

- lectura solo para miembros del grupo
- alta permitida cuando el usuario se anade a si mismo
- actualizacion y borrado permitidos a admin o al propio miembro

### Subcolecciones funcionales

- `medicamentos`
- `dias`
- `citas`
- `incidencias`
- `documentos`
- `patologias`
- `seguimiento`

Todas quedan protegidas por pertenencia al grupo.

## Riesgos Y Puntos Ambiguos

### 1. Lectura de grupos para union por codigo

El flujo actual de union por codigo consulta `groups` desde cliente usando `joinCode`. Si se cierra demasiado la lectura de `groups`, el join por codigo puede romperse.

Por eso la propuesta anterior permite lectura de `groups` a usuarios autenticados. Esto es mas seguro que la regla actual, pero sigue siendo mas abierto de lo ideal.

### 2. Join por codigo idealmente deberia moverse al backend

La forma mas segura de cerrar este flujo seria:

- mover la logica de union por codigo a una Cloud Function o backend
- evitar que el cliente pueda consultar libremente grupos por `joinCode`
- centralizar validaciones y alta de membresia del lado servidor

### 3. Updates de grupo demasiado amplios

La propuesta deja `update` sobre `groups/{groupId}` a miembros del grupo para no romper el flujo actual, ya que varias operaciones escriben datos compartidos del grupo. En una fase posterior convendria restringir algunos cambios a administradores o a tipos de operacion mas concretos.

### 4. Storage no incluido aqui

Estas reglas cubren Firestore. Si en el futuro se utiliza Firebase Storage para archivos o imagenes, hara falta documentar y endurecer tambien las reglas de Storage.

## Recomendacion Practica

Para el estado actual del proyecto:

1. salir de la regla abierta `request.auth != null`
2. adoptar una regla intermedia basada en pertenencia a grupo
3. documentar claramente que el flujo de join por codigo es el principal punto a reforzar
4. si el producto evoluciona, mover el join por codigo a backend y cerrar mucho mas `groups`
