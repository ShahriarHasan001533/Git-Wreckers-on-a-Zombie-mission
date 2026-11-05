# Log book: Week 10 (streams, lambdas and collectors)

> DRAFT. Rewrite this in your own words before submitting, and add an AI use statement if your course requires one.

**Student:** Nicholas Fang
**Class worked on:** `Zombie` (all of this week's changes are in `Zombie.java` and `test/ZombieStreamTest.java`)

## What I did

**1. Replaced the target-search loop with a stream.**
`findTarget()` used to loop over nearby entities, skipping soldiers and hidden humans and tracking the closest one by hand. I moved the filtering into `visibleCivilians(World)`, which returns a lazy `Stream<Human>`. `findTarget()` ends it with `min(comparingDouble(this::distanceTo))` and `orElseThrow`, so it now throws our own `TargetNotFoundException` instead of returning `null`. The same stream is reused for counting, so the "who can I see" rule lives in one place.

**2. Moved the State pattern into `Zombie` as a lambda enum.**
The zombie had two hidden modes (wandering and chasing) inside one `update()`. I made them a private `Mode` enum where each constant holds its behaviour as a `BiConsumer<Zombie, World>` lambda. `startChase()` and `stopChase()` handle the switches.
- *Alternatives I considered:* an if/else on a flag (the original, which gets messy as modes are added), an enum with a switch (still one big method), and Strategy as `Military` uses it (a strategy swaps an algorithm but doesn't decide when to switch, which is the core need here).
- *What I did first:* I started with separate `ZombieState`, `WanderingState` and `ChasingState` classes, then folded them into `Zombie`. With only two small states, separate classes were mostly boilerplate, and lambdas replace what would otherwise be a class hierarchy.
- *Trade-off:* `Zombie.java` is longer. If I add more states (fleeing, feeding), separate classes would be worth it again.

**3. Three collectors that describe the ecosystem.**
- `civiliansByDistanceBand` uses `Collectors.groupingBy` to count visible civilians in CLOSE, MEDIUM and FAR bands.
- `visibleCivilianEnergy` uses `Collectors.summarizingDouble` for min, max and average energy.
- `preyScore` uses a custom `Collector.of` (supplier, accumulator, combiner, finisher) that adds up how close each civilian is.

**4. A parallel stream, with reasoning about thread safety.**
`Zombie.totalPreyScore(World)` sums every zombie's prey score with `parallelStream()`. It is safe because each task only reads the world and its own zombie, the lambdas hold no shared state, and `DoubleStream.sum()` combines the results. `World` itself is not thread-safe, so this must only be called between ticks. I checked it against the sequential sum with 40 zombies and 30 humans, and again in a unit test with 20 and 15, and they matched. At this size the parallel version is probably not faster, because the overhead is larger than the work, so I'd describe it as a demonstration of the technique, not an optimisation.

**5. Using the metrics in behaviour.**
- `frenzyMultiplier` turns the prey score into a speed boost of up to 1.5x when a crowd is in view, plus a 0.25 lunge when someone is in the CLOSE band (1.75x at most).
- Infected humans now rise as new zombies (capped at 12, counted with a stream), so the population can grow.
- I raised the base chase speed to 2.4, because humans move at 2.0 and a slower zombie could never catch one.

**6. Reacting to the rest of the team's classes (added after reading their code).**
- *Siege:* `occupiedBuildingNearby` streams over the world's `Building` objects and picks the one with the most people hiding inside (using `Building.getOccupantCount()` and its entrance position). A zombie with nobody in view walks to that entrance and waits, so humans who have to leave when they get hungry walk out into it.
- *Wary of soldiers:* `nearestSoldier` finds the closest `Military` unit. A wandering zombie backs away from one, and a chasing zombie breaks off if a soldier is within 190 units, unless the human is within 40 units (it commits to the kill). I did this because `Military` kills any zombie inside 140 units within a tick or two, and a zombie that chases a human straight into a soldier never gets to spread.

## Testing
`ZombieStreamTest` has six checks: what a zombie can see, picking the closest target, throwing `TargetNotFoundException`, the prey score value, the parallel total matching the sequential one, and a human turning into a zombie. I confirmed a test can fail by changing one expected value in a scratch copy. I also ran `Main` under a virtual display for several seconds with no errors, and simulated its starting world for thousands of ticks.

## Trade-offs and what I learned
- **Game balance:** the frenzy, lunge and faster chase make zombies much deadlier in groups. That is more interesting, but I tuned it by feel and measurement, not against a target.
- **Spread is rare in the default game.** With `Main` unchanged there is one starting zombie, and it still dies at about tick 45 on average (0 of 8 test runs showed any spread), because its first target is next to a soldier and breaking off comes too late. With two extra starting zombies the spread happened in 8 of 8 runs, and the average peak number of zombies rose from 4.0 to 5.1 once I added the siege and soldier avoidance, and to 6.5 with the break-off. I did not change `Main`, because it is a teammate's file. Khoi's unmerged `feature/configurable-population-settings` branch adds a `SimulationSettings` class with a zombie count, so the extra zombies could come from that setting instead.
- **Streams made the rules easier to read, but not always shorter,** and a lazy stream shared between methods needs care: each caller must finish it with exactly one terminal operation.

## What could be improved
- `civiliansByDistanceBand` and `visibleCivilianEnergy` are only used by tests and the lunge. They could feed the on-screen display (`SimPanel`), which is another teammate's class.
- CI only runs `HumanFoodTest`, so `ZombieStreamTest` is not run on GitHub yet.
- `Main` needs the extra zombies for the spread to show up, and the balance numbers should be agreed with the team.

## Git and teamwork this week
Small commits with one-line messages, my Zombie work in PR #1, and a comment-only review of the team's code in PR #2 so teammates can reply on the exact lines.
