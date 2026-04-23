# Care360

## Documentacion Tecnica

## Stack Tecnologico Actual

- Android nativo
- Java
- Arquitectura por capas inspirada en Clean Architecture
- MVVM con ViewModel y LiveData
- Hilt para inyeccion de dependencias
- Firebase Authentication
- Firebase Firestore
- Firebase Storage incorporado a nivel de dependencias, aunque no forma parte del flujo visible actual del producto
- Room incorporado como tecnologia disponible en el proyecto
- Material Components

## Configuracion Base Detectada

- `compileSdk`: 36
- `minSdk`: 26
- `targetSdk`: 36
- `applicationId`: `com.silveira.care360`
- `versionName`: `1.0`
- compatibilidad Java 11

## Estructura De Paquetes

Ruta base:

- `app/src/main/java/com/silveira/care360`

Capas principales detectadas:

- `ui`
- `domain`
- `data`
- `di`
- `core`
- `legacy`

## Capa UI

La capa `ui` contiene Activities, ViewModels y helpers de interfaz. Se observan pantallas principales como:

- `HomeActivity`
- `GroupEntryActivity`
- `CreateGroupActivity`
- `CitasActivity`
- `FamiliaActivity`
- `PatologiasActivity`
- `SeguimientoActivity`
- `DocsActivity`
- `WelcomeActivity`

ViewModels relevantes:

- `HomeViewModel`
- `FamiliaViewModel`
- `CreateGroupViewModel`
- `GroupEntryViewModel`
- `PatologiasViewModel`
- `SeguimientoViewModel`

## Capa Domain

Contiene modelos, repositorios abstractos y casos de uso.

Modelos detectados:

- `Group`
- `GroupMember`
- `User`
- `Medicamento`
- `Cita`
- `Incidencia`
- `ActividadItem`
- `Patologia`
- `SeguimientoRegistro`
- `Documento`

Casos de uso detectados incluyen operaciones como:

- crear grupo
- unirse por codigo
- cambiar grupo activo
- cargar Home
- cargar perfil
- cargar actividad
- cargar seguimiento
- cargar patologias
- eliminar entidades
- exportar PDFs

## Capa Data

Gestiona implementaciones concretas de acceso a datos y mapeo.

Data sources Firestore detectados:

- `FirebaseUserDataSource`
- `FirebaseGroupDataSource`
- `FirebaseMedicamentoDataSource`
- `FirebaseCitaDataSource`
- `FirebaseIncidenciaDataSource`
- `FirebaseDocumentoDataSource`
- `FirebasePatologiaDataSource`
- `FirebaseSeguimientoDataSource`

La capa data tambien incluye:

- repositorios concretos
- DTOs
- mappers

## Inyeccion De Dependencias

La carpeta `di` contiene modulos Hilt como:

- `RepositoryModule`
- `DataSourceModule`
- `NetworkModule`
- `DatabaseModule`
- modulos de repositorios especificos por dominio

## Persistencia Y Backend

### Firebase Authentication

Se usa para autenticar usuarios.

### Firestore

Se usa como base de datos principal remota para la mayor parte del estado compartido.

Dominios funcionales visibles en el codigo:

- usuarios
- grupos
- membresias
- medicacion
- citas
- incidencias
- documentos
- patologias
- seguimiento

### Firebase Storage

La dependencia existe en el proyecto, pero en el estado actual de producto no se utiliza como parte del flujo visible final para foto de perfil. La decision actual es priorizar estabilidad del MVP y dejar multimedia como mejora futura.

### Room

Room esta disponible a nivel tecnico y puede aprovecharse en una fase futura para cache local, modo offline o sincronizacion mas robusta.

## Seguridad Y Reglas De Firebase

Actualmente existe una regla abierta a cualquier usuario autenticado:

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

Esta configuracion es valida para desarrollo temprano, pero no es suficiente para un entorno de producto, porque cualquier usuario autenticado podria leer o escribir datos que no pertenecen a sus grupos.

Para el proyecto se recomienda documentar las reglas Firestore por separado y tratarlas como parte del hardening tecnico del producto. Se ha anadido un documento especifico en:

- [FIREBASE_RULES.md](./FIREBASE_RULES.md)

Ese documento recoge:

- la estructura real detectada en Firestore desde el codigo
- la regla actual
- una propuesta de reglas mas segura
- riesgos y puntos ambiguos del flujo actual

Nota tecnica importante:

- el flujo actual de union por codigo se resuelve desde cliente
- eso limita lo estricto que pueden ser las reglas sin mover parte del proceso a una Cloud Function o backend intermedio
- por tanto, la propuesta de reglas incluida debe entenderse como una mejora clara respecto al estado actual, pero no como el maximo nivel posible de cierre

## Arquitectura Aplicada En La Practica

La organizacion del proyecto sigue una separacion razonable entre:

- presentacion
- logica de negocio
- acceso a datos

Patron general observado:

1. La Activity delega en ViewModel.
2. El ViewModel orquesta casos de uso.
3. Los casos de uso delegan en repositorios.
4. Los repositorios utilizan data sources y mappers.

Esto hace que el proyecto sea mantenible y defendible tecnicamente para una continuidad futura.

## Fortalezas Tecnicas

- separacion por capas clara
- uso de casos de uso
- modularidad funcional razonable
- base adecuada para evolucionar
- integracion ya establecida con Firebase
- multilenguaje ya contemplado en resources

## Limitaciones Tecnicas Actuales

- la interfaz esta construida solo para Android nativo
- no existe todavia una estrategia cerrada de multiplataforma
- algunos flujos de UX todavia requieren pulido
- la parte multimedia no se considera cerrada como modulo de producto
- falta aun documentacion tecnica de handoff dentro del propio repo

## Decision Tecnica Relevante Sobre Foto De Perfil

La foto de perfil de la persona cuidada se ha retirado del flujo visible actual para evitar exponer una funcionalidad no prioritaria en estado intermedio. Esta decision mejora la coherencia del MVP y reduce riesgo de percepcion negativa durante una presentacion o venta.

## Posibles Evoluciones Tecnicas

- cierre de cache local y sincronizacion estable con Room
- consolidacion de estrategia multimedia
- futura migracion o replicacion a Flutter o web
- automatizacion de pruebas
- hardening de reglas y despliegue
