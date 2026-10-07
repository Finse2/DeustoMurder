# DeustoMurder

DeustoMurder es un juego de misterio y asesinato multijugador desarrolado en Java e inspirado en **Cluedo**.

El juego utiliza personajes, armas y habitaciones personalizados basados en la Universidad de Deusto. Los jugadores podrán unirse a la misma partida desde diferentes ordenadores, moverse por el tablero, hacer sugerencias, reunir información e intentar descubrir la solución oculta del asesinato. 

## Objetivo del juego

Al principio de la partida, se selecciona una carta aleatoria de cada categoría:

* 1 Actor
* 1 Arma
* 1 Habitación

Estas tres cartas forman la solución oculta del asesinato.

Las cartas restantes se barajan y se reparten entre los jugadores. Las cartas sobrantes se muestran visibles para todos los jugadores.

El objetivo es determinar:

**¿Quién cometió el asesinato, con qué arma y en qué habitación?**

## Funcionalidades actuales

* Sistema de cartas de Actor
* Sistema de cartas de Arma
* Sistema de cartas de Habitación
* Generación aleatoria de la solución del asesinato
* Barajado aleatorio de cartas
* Reparto de cartas entre los jugadores
* Cartas sobrantes visibles
* Gestión de la mano de cada jugador
* Sistema de posiciones X/Y de los jugadores
* Movimiento básico del jugador hacia las cuatro direcciones posibles

## Funcionalidades planeadas

* Interfaz gráfica utilizando Java Swing
* Tablero de juego completo
* Validación de movimientos
* Habitaciones y entradas a las habitaciones
* Sistema de turnos con dados / movimiento
* Sugerencias
* Acusaciones
* Sistema de revelación de cartas
* Gestión de turnos
* Condiciones de victoria/derrota
* Selección de personaje
* Red para multijugador
* Sistema para hostear/unirse a una partida
* Múltiples ordenadores conectados a la misma partida

## Personajes

Los personajes actuales son: 

* Garaizar — Rosa
* Luka — Morado
* David — Azul
* Andrada — Rojo
* Antal — Verde
* Bringas — Amarillo

## Armas

* Machete
* Tiza 
* Teclado
* Cable
* Silla
* Destornillador

## Habitaciones

* CRAI
* Baños
* Cafeteria
* Sala de máquinas
* Laboratorios
* DeustoTech
* Aula E208
* Decanato
* Polideportivo

## Tecnologías empleadas

* Java
* Java Swing
* Java Collections
* Programación Orientada a Objetos
* Git
* GitHub

Más adelante se añadirá red para permitir que varios jugadores participen desde diferentes ordenadores.

## Estructura del proyecto

```text
DeustoMurder/
│
├── innerProy/
│   └── src/
│       ├── activation/
│       │   └── Main.java
│       │
│       ├── model/
│       │   ├── Card.java
│       │   ├── Actor.java
│       │   ├── Weapon.java
│       │   ├── Room.java
│       │   └── Player.java
│       │
│       └── ui/
│
└── README.md
```

La estructura del proyecto irá creciendo a medida que se implementen la red, la lógica del tablero, la interfaz de usuario y los sistemas de gestión del juego.

## Configuración actual del juego

Cuando comienza la partida: 

1. Se crean los actores, las armas y las habitaciones.
2. Se baraja cada categoría.
3. Se extrae un Actor, un Arma y una Habitación para crear la solución oculta.
4. Todas las cartas restantes se combinan en el mazo jugable.
5. Se baraja el mazo.
6. Cada jugador recibe 4 cartas.
7. Las cartas sobrantes se vuelven visibles para todos los jugadores.

## Estado de desarrollo

DeustoMurder se encuentra actualmente en desarrollo.

En primer lugar se está implementando el modelo base de cartas y jugadores. Los siguientes sistemas principales incluirán el tablero, las reglas de movimiento, la interfaz de usuario y la red multijugador.

## Contribuidores

Desarrollado como un proyecto grupal por estudiantes de la Universidad de Deusto.
