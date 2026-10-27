# Git Wreckers : Zombie Mission Simulation  

## Overview
What happens when a group of zombie infections spreads through a living community? Git Wreckers explores this through a 2D zombie outbreak simulation developed in Java, where humans search for food and shelter while zombies hunt and infect humans and military units attempt to control the outbreak. Without direct player control, the interactions between agents can create unexpected outcomes such as large outbreaks or naturally formed safe zones.

## Project Concepts
- **Zombies** pursue nearby humans and infect on contact
- **Humans** move, seek shelter, consume food, and become inactive when they starve
- **Military** patrol and can kill zombies
- Behaviour emerges from local agent interactions
- **No player input.** Every agent follows simple local rules, so the outbreak curve and the formation of safe zones emerge from the interactions themselves

## Features
- World simulation
- Tick Loop
- Human movement and survival
- Zombie infection and chasing
- Military patrol and shooting
- Buildings and shelter
- Food/resources
- Population statistics
- Start, pause, reset, and speed controls

## Class Structures

[View the UML diagram](https://drive.google.com/file/d/1-3FDkD7lQL_UJ8YTpuyqcfL1BkNXXclc/view?usp=sharing)

The generic design is demonstrated by `Human.findNearestEntity()`, which searches for the nearest entity of a requested subtype. The Food constructor uses the standard `double` type because it already accepts both integer and decimal literals.

## Team Responsibilities
Use the agreed division:

| Area | Responsibility | Member | Student ID |
|---|---|---|---|
| World/Main | Tick loop, spawning, and neighbour queries | Tran Khoi Nguyen (Kian) Nguyen | 48769266 |
| Human/Food | Energy, starvation, food consumption, and shelter | Abir Maya Bhowmick | ABIR92218 | 49125761
| Zombie | Infection, chasing, and target selection | Nicholas Fang | |
| Military/MovementBehaviour | Patrol, shooting, and strategy pattern | Neev Patel | 48521558 |
| Building/SimPanel | Occupancy, rendering, and statistics | | |

## Rules 
- Zombies chase nearby humans.
- Humans attempt to survive by moving, finding food, and seeking shelter. They lose energy on each update, gain energy by eating nearby food, and become inactive when their energy reaches zero.
- Zombies infect humans when they catch them.
- Military units patrol and shoot zombies.
- The simulation progresses automatically through repeated ticks.
- Population changes are caused by interactions between entities.

## Requirements
- Git
- Java Development Kit (JDK 11 or later)

### How to run
1. Clone this project and move to this project
```bash
git clone https://github.com/ShahriarHasan001533/Git-Wreckers-on-a-Zombie-mission

cd Git-Wreckers-on-a-Zombie-mission
cd COMP2000
```
2. Compile the Java source files.
```bash
javac -d out src/*.java
```

3. Run the `Main` class
```bash
java -cp "out;src" Main
```

On macOS or Linux, use a colon instead of a semicolon: `java -cp "out:src" Main`.
The `src` entry makes the PNG image assets available at runtime.

4. Check the Java version and confirm that the image folders are present.
```bash 
java -version
javac -version
```

## References
- COMP2000 course materials
- Image assets are the PNG files included in this repository under `COMP2000/src` and `COMP2000/src/HumanFoodAssets`.
- The simulation loads these assets from the classpath; no external image source or attribution is recorded in the submitted project.
