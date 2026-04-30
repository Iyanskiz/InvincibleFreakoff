A high-octane 2D Java Fighting Engine📝 
OverviewThis project is a custom-built 2D fighting game engine developed in Java.
It features characters from the Invincible universe, utilizing a specialized sprite-based animation system, dynamic environmental hazards, and a robust character selection UI.🕹️
Battle ControlsThe game is designed for local 1v1 combat.ActionPlayer 1 (WASD)Player 2 (Arrows)MovementA / D← / →JumpW↑Block (Hold)S↓Light AttackFNum 1Heavy AttackGNum 2Special AbilityHNum 3Restart GameENTERENTER🛡️
The RosterEach fighter features unique stats and custom attributes tailored to their lore.Invincible: The balanced hero. High speed, versatile air game, and quick recovery.Omni-Man: The heavy hitter. 
Massive damage output balanced with high tankiness.Thragg: The Grand Regent. Equipped with armor that reduces incoming damage by 50%.Conquest: The berserker. Extremely aggressive with high-impact, crushing attacks.
🚀 Key FeaturesDynamic Stages: Battle across four random locations including City Under Siege (falling meteors) and Storm's Wrath (random lightning).
Visual Feedback: Features screen flashes, hit particles, and "BLOCKED!" popups to enhance gameplay feel.Physics Engine: Custom logic for gravity, collision detection, and automatic sprite mirroring.
Animated UI: A character selection screen with "levitating" sprite previews and dynamic stat cards.📂 Technical Setup1. Source Files (src/)Frame.java: The main entry point to launch the application.
GamePanel.java: Manages the core game loop, stage hazards, and physics.Fighter.java: The abstract base class containing all combat logic.CharacterSelect.java: The selection menu logic and rendering.2. 
Image Assets (imgs/)Sprites must be placed in a folder named imgs at the project root. 
Required files for each character:[Name]Idle.png, [Name]Block.png, [Name]Forward.png, [Name]Backward.png, [Name]Punch.png, [Name]Hit.png, [Name]Levitating.png.




































