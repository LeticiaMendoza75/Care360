# Care360

## Documentacion Funcional

## Objetivo Del Producto

Permitir que una familia o pequeno grupo de cuidadores coordine el cuidado cotidiano de una persona dependiente o mayor desde una aplicacion unica y compartida.

## Usuarios Objetivo

- familiares cuidadores
- hijos o hijas que coordinan cuidados
- personas cercanas que apoyan en citas y medicacion
- pequenos entornos de cuidado informal

## Concepto Funcional Base

Cada usuario puede formar parte de uno o varios grupos. Cada grupo representa un caso de cuidado y se organiza alrededor de una persona cuidada. Dentro del grupo, los miembros comparten informacion relevante y gestionan acciones relacionadas con el cuidado.

## Modulos Principales

### 1. Acceso y autenticacion

Permite entrar en la aplicacion mediante autenticacion de usuario. El sistema identifica al usuario y carga sus grupos disponibles.

### 2. Entrada al sistema de grupos

El usuario puede:

- crear un nuevo grupo
- unirse a un grupo existente mediante codigo
- cambiar de grupo cuando pertenece a varios

### 3. Grupo familiar

Cada grupo contiene:

- nombre del grupo
- persona cuidada asociada
- miembros del grupo
- codigo de union

### 4. Home

La pantalla principal resume el estado del grupo activo. Muestra accesos y resumen de:

- persona cuidada
- proxima medicacion
- proxima cita
- ultima incidencia
- actividad reciente
- seguimiento y patologias
- acciones rapidas como nueva incidencia y SOS

### 5. Perfil de la persona cuidada

El perfil centraliza datos de contexto:

- nombre
- edad
- telefono
- direccion
- contacto de emergencia
- alergias e intolerancias
- patologias o diagnosticos relevantes

Desde perfil se puede acceder a:

- anadir medicacion
- anadir cita
- gestionar patologias
- acceder a seguimiento

Nota funcional:

- la foto de perfil no forma parte del alcance visible actual
- se deja planificada como mejora futura

### 6. Medicacion

Permite registrar y gestionar tratamientos.

Funciones actuales:

- alta de medicamento
- edicion
- eliminacion
- gestion de dias y horas
- recordatorios
- exportacion PDF
- visualizacion de proxima medicacion en Home

### 7. Citas

Permite gestionar citas medicas o de seguimiento.

Funciones actuales:

- alta de cita
- edicion
- eliminacion
- lugar
- profesional
- persona encargada de acompanar
- observaciones
- exportacion PDF
- visualizacion de proxima cita en Home

### 8. Incidencias

Permite registrar eventos o cambios relevantes.

Funciones actuales:

- nueva incidencia
- edicion
- eliminacion
- nivel de gravedad
- descripcion
- historial
- exportacion PDF
- visualizacion de ultima incidencia en Home

### 9. Actividad reciente

Muestra cambios recientes realizados en el grupo y resume la actividad diaria.

Actualmente contempla actividad relacionada con:

- medicacion
- citas
- incidencias
- patologias
- seguimiento

### 10. Patologias

Permite registrar patologias o diagnosticos relevantes de la persona cuidada.

Funciones actuales:

- alta
- edicion
- eliminacion
- notas asociadas

### 11. Seguimiento

Permite registrar valores de seguimiento de salud.

Tipos contemplados:

- tension
- glucosa
- temperatura
- peso

Funciones actuales:

- alta de registro
- edicion
- eliminacion
- notas

### 12. Familia

Gestiona la parte colaborativa del grupo.

Funciones actuales:

- ver codigo del grupo
- compartir codigo
- copiar codigo
- ver miembros
- acciones de grupo

### 13. SOS

Acceso rapido desde Home a acciones urgentes.

Funciones actuales:

- llamada al 112
- llamada al contacto de emergencia si existe

## Flujos Funcionales Principales

### Crear grupo

1. El usuario accede a crear grupo.
2. Introduce nombre del grupo y nombre de la persona cuidada.
3. El sistema crea grupo, membresia y grupo activo.
4. El usuario entra al Home del grupo.

### Unirse por codigo

1. El usuario introduce un codigo.
2. El sistema localiza el grupo.
3. Se crea la membresia del usuario.
4. El grupo pasa a estar disponible en su cuenta.

### Gestion de medicacion

1. El usuario accede al modulo de medicacion.
2. Crea o modifica un medicamento.
3. El sistema actualiza Home y actividad reciente.

### Gestion de citas

1. El usuario crea o edita una cita.
2. Puede asociar persona encargada.
3. La siguiente cita se refleja en Home.

### Gestion de incidencias

1. El usuario registra una incidencia.
2. Se actualiza el historial.
3. Home muestra la incidencia mas reciente.

### Seguimiento y patologias

1. El usuario registra contexto de salud relevante.
2. La informacion se integra en actividad reciente y resumen de salud.

## Alcance Actual Del MVP

Dentro del MVP actual, el foco esta en:

- coordinacion familiar
- seguimiento operativo del cuidado
- centralizacion de informacion relevante
- experiencia Android usable y demostrable

## Mejoras Futuras Funcionalmente Coherentes

- foto de perfil de la persona cuidada
- multimedia y adjuntos maduros
- notificaciones mas avanzadas
- comparticion entre plataformas
- dashboard o version web complementaria

