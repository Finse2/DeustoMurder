# DeustoMurder

DeustoMurder is a Java-based multiplayer murder mystery game inspired by **Cluedo / Clue**.

The game uses custom characters, weapons, and rooms based around Deusto. Players will be able to join the same game from different computers, move around the board, make suggestions, gather information, and attempt to discover the hidden murder solution.

## Game Objective

At the beginning of the game, one random card from each category is selected:

* 1 Actor
* 1 Weapon
* 1 Room

These three cards form the hidden murder solution.

The remaining cards are shuffled and distributed between the players. Any cards left over are made visible to all players.

The objective is to determine:

**Who committed the murder, with which weapon, and in which room?**

## Current Features

* Actor card system
* Weapon card system
* Room card system
* Random murder solution generation
* Random card shuffling
* Card distribution between players
* Visible leftover cards
* Player hand management
* Player X/Y position system
* Basic player movement

  * Up
  * Down
  * Left
  * Right

## Planned Features

* Graphical interface using Java Swing
* Full game board
* Movement validation
* Rooms and room entrances
* Dice / movement turn system
* Suggestions
* Accusations
* Card revealing system
* Turn management
* Win / lose conditions
* Character selection
* Multiplayer networking
* Host / join game system
* Multiple computers connected to the same match

## Characters

The current characters are:

* Garaizar — Pink
* Luka — Purple
* David — Blue
* Andrada — Red
* Antal — Green
* Bringas — Yellow

## Weapons

* Machete
* Tiza
* Teclado
* Cable
* Silla
* Destornillador

## Rooms

* Cry
* Baños
* Cafeteria
* Maquinas
* Laboratorio
* DeustoTech
* Aula 1
* Aula 2

## Technologies

* Java
* Java Swing
* Java Collections
* Object-Oriented Programming
* Git
* GitHub

Networking will later be added to allow multiple players to participate from different computers.

## Project Structure

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

The project structure will grow as networking, board logic, UI, and game-management systems are implemented.

## Current Game Setup

When a game starts:

1. Actors, weapons, and rooms are created.
2. Each category is shuffled.
3. One Actor, Weapon, and Room are removed to create the hidden solution.
4. All remaining cards are combined into the playable deck.
5. The deck is shuffled.
6. Each player receives 3 cards.
7. Remaining cards become visible to every player.

## Development Status

DeustoMurder is currently under development.

The core card and player model is being implemented first. The next major systems will include the board, movement rules, user interface, and multiplayer networking.

## Contributors

Developed as a group project by students at the University of Deusto.
