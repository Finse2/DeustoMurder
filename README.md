<div align="center">

<img src="docs/assets/deustomurder-cover.png" alt="DeustoMurder — cinematic cover" width="100%">

<br>

**ONE MURDER. SIX SUSPECTS. ONE TRUTH.**

*An atmospheric multiplayer detective board game built with Java and Swing.*

[![Java](https://img.shields.io/badge/Java-171717?style=flat-square&logo=openjdk&logoColor=C9A46A)](https://www.java.com/)
![Swing](https://img.shields.io/badge/Java_Swing-171717?style=flat-square&logoColor=C9A46A)
![Multiplayer](https://img.shields.io/badge/TCP%2FIP_Multiplayer-171717?style=flat-square&logoColor=C9A46A)
![Status](https://img.shields.io/badge/Status-In_Development-761B30?style=flat-square&logoColor=white)

[The Case](#the-case) · [Gameplay](#gameplay) · [Gallery](#gallery) · [Architecture](#architecture) · [Installation](#installation) · [Roadmap](#roadmap) · [Team](#team)

</div>

---

<a id="the-case"></a>
<div align="center">
<img src="docs/assets/headers/the-case.png" alt="THE CASE" width="100%">

*Every suspect has a secret. Every clue tells a story.*
</div>

A mysterious murder has shaken the **University of Deusto**. Step into the role of a detective, investigate the board, examine the cards, and piece together the truth before your opponents.

Your mission is simple: discover **who** committed the crime, **where** it happened, and **which weapon** was used.

<table>
<tr>
<td width="33%" align="center"><strong>INVESTIGATE</strong><br><sub>Explore the board and examine the evidence.</sub></td>
<td width="33%" align="center"><strong>DEDUCE</strong><br><sub>Rule out suspects, rooms and weapons.</sub></td>
<td width="33%" align="center"><strong>ACCUSE</strong><br><sub>Make your final accusation.</sub></td>
</tr>
</table>

<div align="center"><img src="docs/assets/divider.png" alt="" width="85%"></div>

<a id="gameplay"></a>
<div align="center">
<img src="docs/assets/headers/how-to-play.png" alt="HOW TO PLAY" width="100%">

*Follow the clues. Eliminate the impossible. Uncover the truth.*
</div>

<table>
<tr>
<td width="50%" valign="top"><h3>01 · Join the Investigation</h3>Join a multiplayer session and prepare to investigate with other detectives.</td>
<td width="50%" valign="top"><h3>02 · Roll & Move</h3>Take turns, roll two dice, and move across the board.</td>
</tr>
<tr>
<td width="50%" valign="top"><h3>03 · Examine the Evidence</h3>Use your cards and detective notes to eliminate impossible combinations.</td>
<td width="50%" valign="top"><h3>04 · Solve the Case</h3>Identify the suspect, weapon and room, then make your accusation.</td>
</tr>
</table>

> **Development note:** The project is under active development. Some multiplayer and gameplay features may not yet be fully integrated or tested end to end.

<div align="center"><img src="docs/assets/divider.png" alt="" width="85%"></div>

<a id="gallery"></a>
<div align="center">
<img src="docs/assets/headers/inside-the-mystery.png" alt="INSIDE THE MYSTERY" width="100%">

*A glimpse behind the investigation.*
</div>

<!-- Add this gallery when all three real screenshots exist in docs/assets/screenshots/.
<table>
<tr>
<td width="50%" align="center"><img src="docs/assets/screenshots/main-menu.png" alt="Main menu" width="100%"><br><b>01 / MAIN MENU</b></td>
<td width="50%" align="center"><img src="docs/assets/screenshots/lobby.png" alt="Multiplayer lobby" width="100%"><br><b>02 / MULTIPLAYER LOBBY</b></td>
</tr>
</table>
<div align="center"><img src="docs/assets/screenshots/game-board.png" alt="Game board" width="95%"><br><b>03 / THE INVESTIGATION BOARD</b></div>
-->

<p align="center"><em>Gameplay screenshots will be added as the interface is finalized.</em></p>

<div align="center"><img src="docs/assets/divider.png" alt="" width="85%"></div>

<a id="architecture"></a>
<div align="center">
<img src="docs/assets/headers/under-the-hood.png" alt="UNDER THE HOOD" width="100%">

*The engineering behind the mystery.*

![Java](https://img.shields.io/badge/Java-171717?style=for-the-badge&logo=openjdk&logoColor=C9A46A)
![Swing](https://img.shields.io/badge/Swing-171717?style=for-the-badge&logoColor=C9A46A)
![Sockets](https://img.shields.io/badge/TCP_Sockets-171717?style=for-the-badge&logoColor=C9A46A)
![Threads](https://img.shields.io/badge/Multithreading-171717?style=for-the-badge&logoColor=C9A46A)
</div>

DeustoMurder uses an object-oriented, modular structure to separate user interface, game rules, networking and supporting services.

```mermaid
flowchart TD
    UI["Java Swing UI"] <--> CORE["Game Core"]
    CORE --> TURNS["Turn & Movement System"]
    CORE --> CARDS["Cards & Accusations"]
    CORE <--> NET["TCP Client / Server"]
    CORE --> SERVICES["Statistics & History"]
    classDef central fill:#761B30,stroke:#C9A46A,color:#fff;
    classDef module fill:#171717,stroke:#C9A46A,color:#fff;
    class CORE central;
    class UI,TURNS,CARDS,NET,SERVICES module;
```

### Main components

| Area | Examples | Responsibility |
|:--|:--|:--|
| Game core | `Game`, `GameState`, `GameSetup` | Game lifecycle and shared state |
| Turns | `RoundManager`, `MoveValidator` | Turn order and legal movement |
| Cards | `DeckManager`, `AccusationValidator` | Distribution and solution checking |
| Networking | `Client`, `GameServer` | Multiplayer communication |
| Services | `StatisticsManager`, `GameHistory` | Player statistics and event records |
| Interface | `MainScreen`, `LobbyScreen`, `InGameScreen` | Desktop screens |

<details>
<summary><strong>View a simplified source layout</strong></summary>

```text
innerProy/
└── src/
    ├── game/
    │   ├── core/
    │   ├── turns/
    │   ├── cards/
    │   ├── accusations/
    │   └── history/
    ├── networking/
    └── ui/
```

The directory layout is illustrative; consult the repository for the latest package structure.
</details>

<div align="center"><img src="docs/assets/divider.png" alt="" width="85%"></div>

<a id="installation"></a>
<div align="center">
<img src="docs/assets/headers/getting-started.png" alt="GETTING STARTED" width="100%">

*Every investigation begins somewhere.*
</div>

### Requirements

- A **JDK** compatible with the project source code.
- **IntelliJ IDEA** (recommended).
- **Git** to clone the repository.

### 01 · Clone

```bash
git clone https://github.com/Finse2/DeustoMurder.git
cd DeustoMurder
```

### 02 · Configure

1. Open the cloned folder in **IntelliJ IDEA**.
2. Configure the project SDK under **File → Project Structure**.
3. If necessary, mark `innerProy/src` as **Sources Root**.
4. Locate the class containing the application's `public static void main(String[] args)` entry point and run it.

> The exact JDK version and entry-point class should be verified against the current repository before publishing release instructions.

<details>
<summary><strong>Troubleshooting</strong></summary>

- **JDK not configured:** Select a compatible SDK in IntelliJ's Project Structure.
- **Sources not recognized:** Mark the source directory as *Sources Root*.
- **Resources missing:** Check the resource paths and working directory in the run configuration.
- **Multiplayer connection fails:** Verify the host is running, the network is reachable, and the firewall permits the required connection.
</details>

<div align="center"><img src="docs/assets/divider.png" alt="" width="85%"></div>

<a id="roadmap"></a>
<div align="center">
<img src="docs/assets/headers/development-roadmap.png" alt="DEVELOPMENT ROADMAP" width="100%">

*Building the mystery, one commit at a time.*
</div>

The following checklist describes the project's development areas. **Confirm the completion markers against the current code and tests before publishing.**

- [x] Core game model and state management components
- [x] Turn, movement, deck and accusation logic components
- [x] Initial Java Swing screens and networking foundations
- [ ] Fully integrated and tested multiplayer game flow
- [ ] Complete achievements integration
- [ ] Save/load and persistent game history
- [ ] End-to-end tests, stability improvements and release packaging

<div align="center"><img src="docs/assets/divider.png" alt="" width="85%"></div>

<a id="team"></a>
<div align="center">
<img src="docs/assets/headers/the-team.png" alt="THE MINDS BEHIND THE MYSTERY" width="100%">

*Four developers. One investigation.*
</div>

Developed as a student project at the **University of Deusto**.

| Developer | Primary focus |
|:--|:--|
| **David** | Game logic, backend and gameplay systems |
| **Andrada** | Multiplayer lobby and player experience |
| **Antal** | Board and interface components |
| **Luka** | Main interface and board visualization |

<div align="center">

[**View repository contributors**](https://github.com/Finse2/DeustoMurder/graphs/contributors)

</div>

<div align="center"><img src="docs/assets/divider.png" alt="" width="85%"></div>

<div align="center">
<img src="docs/assets/headers/final-verdict.png" alt="THE FINAL VERDICT" width="100%">

### THE CASE IS NEVER TRULY CLOSED.

*Every mystery leaves a trace.*

[![Source](https://img.shields.io/badge/SOURCE_CODE-171717?style=for-the-badge&logo=github&logoColor=C9A46A)](https://github.com/Finse2/DeustoMurder)
[![Issues](https://img.shields.io/badge/ISSUES-171717?style=for-the-badge&logo=github&logoColor=C9A46A)](https://github.com/Finse2/DeustoMurder/issues)
[![Contributors](https://img.shields.io/badge/CONTRIBUTORS-761B30?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Finse2/DeustoMurder/graphs/contributors)

<sub>DEUSTO MURDER · UNIVERSITY OF DEUSTO · 2026</sub>

</div>
