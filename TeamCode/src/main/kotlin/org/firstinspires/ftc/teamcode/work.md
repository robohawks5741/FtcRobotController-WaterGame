# DISCUSS WITH CLAY:

## How should we structure the Slide/Arm/Claw/Intake system?

Facts:
- Inter-module communication happens both in TeleOp and Auto

### Goals
- Each system is self-reliant
- Each system is structured as an API
  - Methods and properties that change state
  - Apply state to output every update

### Ideas

- Monolithic
  - Pros:
    - No "IPC" needed
  - Cons:
    - Harder to find specific problems
- IPC / ModuleConfig with reference to the ModuleHandler (DO THIS)
  - Pros:
    - Applies to all other modules
    - Modules can communicate properly
  - Cons:
    - Hard to debug an IPC chain
- Hierarchy
  - Pros:
    - IPC only one call up/down the chain (which is all we need)
  - Cons:
    - Hard to accurately model our system with inheritance

## How to use RR with the cardinal snap 

- I just don't know how to use RR very well
- It crashed with an ambiguous error when I tried the first time

## Voltage sensors

Voltage sensing (if the slides are lifting the slides up and pull to much then put them back down)  

## AprilTag driving

- Whether we use distance sensors or CV, we still need to figure out how to __quickly__ and __accurately__ adjust our location and orientation.
- Same problem with the cardinal snap

## Make it impossible to hit the board using the distance sensors

Idea: if distance is too close and input is forwards, "mute" the forward input

## Beam breaks to the claws so we know when the pixels are in and maybe to spin the intake

