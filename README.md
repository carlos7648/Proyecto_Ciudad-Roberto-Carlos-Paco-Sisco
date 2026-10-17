

Proyecto desarrollado para la materia **Programación Gráfica**, utilizando **Java 17, LWJGL 3.3.3 y OpenGL 3.3**.

## Descripción

El proyecto representa una ciudad 3D interactiva en la que el usuario puede conducir un vehículo por diferentes calles y completar una serie de entregas.

El vehículo cuenta además con un **remolque articulado**, sistema de colisiones, faros y diferentes cámaras.

## Características

- Ciudad de 11 × 11 celdas.
- Calles, edificios y parques.
- Árboles y bancos.
- Ventanas iluminadas durante la noche.
- Pasos peatonales.
- Semáforos animados.
- 9 farolas.
- Modo día y noche.
- Vehículo controlable.
- 2 faros delanteros.
- Remolque articulado con enganche en V.
- Colisiones del vehículo y del remolque.
- Cámara aérea y cámara de seguimiento.
- Minimapa.
- 4 destinos de entrega.
- Sistema de reinicio del recorrido.

## Controles

| Tecla | Acción |
|------|------|
| W / ↑ | Avanzar |
| S / ↓ | Retroceder |
| A / ← | Girar a la izquierda |
| D / → | Girar a la derecha |
| ESPACIO | Frenar |
| C | Cambiar cámara |
| F | Encender/apagar faros |
| N | Día/Noche |
| M | Mostrar/ocultar minimapa |
| R / K | Reiniciar recorrido |
| ESC | Salir |

## Tecnologías utilizadas

- Java 17
- LWJGL 3.3.3
- OpenGL 3.3
- GLFW
- GLSL 330
- Apache Maven
- Visual Studio Code

## Ejecución

Primero compilar:

    mvn clean compile

Luego ejecutar:

    mvn exec:java '-Dexec.mainClass=com.graphics.AppVehiculo'

## Estructura principal

    src/main/java/com/graphics/
    ├── AppCiudad.java
    ├── AppVehiculo.java
    ├── Ciudad.java
    ├── Iluminacion.java
    ├── Minimapa.java
    ├── Semaforo.java
    └── Vehiculo.java

## Documentación

El repositorio incluye un documento PDF con la explicación detallada del proyecto, funcionamiento, controles, recursos utilizados y limitaciones.

## Autor

**Roberto Carlos Paco Sisco**

Proyecto académico de Programación Gráfica.
