# Log book: Week 10 (streams, lambdas and collectors)

> DRAFT. Rewrite this in your own words before submitting, add an AI use statement if your course requires one, and write your own entries for the other weeks (this file only covers week 10).

**Student:** Nicholas Fang
**Class worked on:** `Zombie`. All of this week's code is in `Zombie.java`, plus `test/ZombieStreamTest.java` and this log book.

## Timeline (from my commits)

| Date | What I did |
|---|---|
| 25 Oct | Stream version of `findTarget`, State pattern, the three collectors, the parallel stream, frenzy speed boost, infected humans becoming zombies, tests, and the PR for review |
| 25 Oct | Reviewed the team's code in a separate comment-only PR (#2) so teammates can reply on exact lines |
| 26 Oct | First draft of this log book |
| 5 Nov | Read the team's code again and added the siege behaviour and soldier avoidance, with tests |
| 6 Nov | Turned stalking and retreating into real states with a single `decideMode`, and added the horde alert |

## What I did

**1. Replaced the target-search loop with a stream.**
`findTarget()` used to loop over nearby entities, skipping soldiers and hidden humans and tracking the closest one by hand. I moved the filtering into `visibleCivilians(World)`, which returns a lazy `Stream<Human>`. `findTarget()` ends it with `min(comparingDouble(this::distanceTo))` and `orElseThrow`, so it now throws our own `TargetNotFoundException` instead of returning `null`. The same stream is reused for counting, so the "who can I see" rule lives in one place.

**2. The State pattern, as a lambda enum with four states.**
The zombie started with two hidden modes (wandering and chasing) inside one `update()`. It now has four: WANDERING, CHASING, STALKING and RETREATING. They are the constants of a private `Mode` enum, and each holds its behaviour as a `BiConsumer<Zombie, World>` lambda. `update()` calls `decideMode(world)` and then runs the chosen mode's lambda.
- *One place for transitions.* `decideMode` is the only method that changes the mode, so the priority order is easy to read: keep chasing, then new prey, then back away from soldiers, then gather at a building people are hiding in, otherwise wander. In the classic pattern each state decides its own next state. I moved that decision into one method on purpose, because with four states the rules interact and I wanted them side by side.
- *Alternatives I considered:* an if/else on a flag (the original, which gets messy as modes are added), an enum with a switch (still one big method), and Strategy as `Military` uses it (a strategy swaps an algorithm but doesn't model which mode the zombie is currently in).
- *What I did first:* I started with separate `ZombieState`, `WanderingState` and `ChasingState` classes, then folded them into `Zombie`. Each mode is only a few lines, so separate classes were mostly boilerplate, and the lambdas stand in for what would otherwise be a class hierarchy.
- *Trade-off:* `Zombie.java` is longer. If the zombie gets many more states, separate classes would be worth it again.

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
- *Siege (STALKING):* `occupiedBuildingNearby` streams over the world's `Building` objects and picks the one with the most people hiding inside (using `Building.getOccupantCount()` and its entrance position). A zombie with nobody in view walks to that entrance and waits, so humans who have to leave when they get hungry walk out into it.
- *Wary of soldiers (RETREATING):* `nearestSoldier` finds the closest `Military` unit. A zombie with nobody to chase backs away from one, and a chasing zombie breaks off if a soldier is within 190 units, unless the human is within 40 units (it commits to the kill). I did this because `Military` kills any zombie inside 140 units within a tick or two, and a zombie that chases a human straight into a soldier never gets to spread.

**7. Horde alert.**
`alertedTarget` streams over the zombies near this one and takes the target of any that is already chasing. A zombie that sees nobody itself joins that hunt, and chases keep going out to 450 units instead of 300. In a test, a zombie 316 units from a human (beyond its own 300 sight range) still joined a neighbour's chase and closed in to 243 units. A zombie with no chasing neighbour gets no alert. This is behaviour that only exists in groups.

## Testing
- `ZombieStreamTest` has eight checks: what a zombie can see, picking the closest target, throwing `TargetNotFoundException`, the prey score value, the parallel total matching the sequential one, a human turning into a zombie, zombies gathering at an occupied building, and finding the nearest soldier while ignoring civilians and far soldiers. I confirmed a test can fail by changing one expected value in a scratch copy.
- The existing `HumanFoodTest` still passes after every change.
- I ran the real `Main` under a virtual display for several seconds with no errors, and simulated its starting world for thousands of ticks, eight runs at a time.
- The horde alert has a manual check (a follower closing in on a human it cannot see) but no test in `ZombieStreamTest` yet.

## Trade-offs and what I learned
- **Game balance:** the frenzy, lunge and faster chase make zombies much deadlier in groups. That is more interesting, but I tuned it by feel and measurement, not against a target.
- **Spread is rare in the default game.** With `Main` unchanged there is one starting zombie, and it still dies at about tick 45 on average (0 of 8 test runs showed any spread), because its first target is next to a soldier. With two extra starting zombies the spread happened in 8 of 8 runs, and the average peak number of zombies was about 4 to 6 depending on the version. Eight runs is a small sample, so I treat the differences between my last few versions as run-to-run noise, not proof that one is better. I did not change `Main`, because it is a teammate's file. Khoi's unmerged `feature/configurable-population-settings` branch adds a `SimulationSettings` class with a zombie count, so the extra zombies could come from that setting instead.
- **Streams made the rules easier to read, but not always shorter,** and a lazy stream shared between methods needs care: each caller must finish it with exactly one terminal operation.
- **Moving the transition rules into `decideMode`** made the behaviour easier to follow and change, but it breaks from the textbook State pattern where each state picks its successor. I think the clarity was worth it, and I would explain that choice if asked.
- **What surprised me:** the biggest design problem was not code, it was balance. Humans outrun the original zombie speed and soldiers kill zombies in a tick, so the "spread" idea did nothing until I measured it and changed several things together.

## What could be improved
- A lone zombie still dies early in the default game. It needs to slide along walls when retreating so it does not get cornered, or the team needs to start with more zombies.
- `civiliansByDistanceBand` and `visibleCivilianEnergy` are only used by tests and the lunge. They could feed the on-screen display (`SimPanel`), which is another teammate's class.
- Add a test for `alertedTarget`, and add `ZombieStreamTest` to `ci.yml`, which only runs `HumanFoodTest` so far.
- The balance numbers should be agreed with the team, not set by one person.

## Git and teamwork
- Small commits with one-line messages in plain words, spread across 25 Oct, 26 Oct, 5 Nov and 6 Nov.
- My Zombie work is in PR #1, which I want a teammate to review before it is merged.
- I reviewed the team's code in PR #2 (comment-only), noting streams that could replace loops, duplicated image loading, and that infected humans used to just disappear.
- I kept to my own class and did not edit teammates' files, apart from the review comments in PR #2.
