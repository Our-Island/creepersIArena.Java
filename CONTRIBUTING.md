# Contribution Guide

Thank you for contributing to **creepersIArena.Java**.

creepersIArena.Java is a Paper plugin implementation of Creeper's Imagination Arena. This document describes the
conventions we expect contributors to follow when writing code, changing gameplay behavior, editing configuration
defaults, and reviewing pull requests.

The goal of these rules is not to make code look clever. The goal is to make the plugin predictable: when someone opens
a file, they should quickly understand what the code does, how it affects the arena, where shared behavior belongs, and
how a change should be tested on a Paper server.

## Project Context

This repository is a Minecraft Paper plugin. Treat changes as plugin changes first, not as generic library changes.

The repository is a Gradle multi-module project. The current modules are:

| Module                  | Description                                                                                                                                                                     |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `cia-api`               | stable CreepersIArena Extension API: addon entrypoints, public annotations, content contracts, ids, sessions, mode/skill/job contracts, runtime views, and generic config views |
| `cia-core`              | plugin runtime implementation: bootstrap, discovery, command handling, game flow, managers, listeners, registries, extension loading, and utility code                          |
| `cia-paper-plugin`      | Paper-specific entrypoints, `paper-plugin.yml`, local Paper run task, and final plugin jar assembly                                                                             |
| `cia-default-content`   | built-in CreepersIArena jobs, skills, and game modes packaged as a CIA extension jar                                                                                            |
| `cia-example-extension` | minimal non-Paper CIA extension jar used to verify extension packaging and loader behavior                                                                                      |

The public extension package is:

```text
top.ourisland.creepersiarena.api
```

API types must not depend on `cia-core`. If an extension-facing type needs to expose runtime state, prefer moving a
small stable view or value type into `cia-api` instead of importing a core manager into the API module.

The runtime implementation package remains:

```text
top.ourisland.creepersiarena
```

Current top-level source areas inside `cia-core/src/main/java/top/ourisland/creepersiarena` include:

| Package   | Description                                                                         |
|-----------|-------------------------------------------------------------------------------------|
| `command` | command handling and admin/runtime command behavior                                 |
| `config`  | configuration loading, file orchestration, and core-owned typed config models       |
| `core`    | plugin bootstrap, internal components, permissions, extension loading, and services |
| `game`    | game flow, arena managers, lobby/player/protection/listener behavior                |
| `job`     | job and skill runtime, UI, and core-owned listeners                                 |
| `utils`   | shared helpers that are not tied to a single feature                                |

Paper-only bootstrap classes live in `cia-paper-plugin/src/main/java`. Shared gameplay/runtime code should stay in
`cia-core` unless it is being intentionally promoted to the public extension API in `cia-api`. Built-in gameplay content
belongs in `cia-default-content` and should register jobs, skills, modes, and content-specific listeners through
`CiaExtensionContext` rather than being wired straight into core managers.

Resource files are part of the plugin contract. Changes to `cia-paper-plugin/src/main/resources/paper-plugin.yml`,
`cia-core/src/main/resources/config.yml`, `cia-core/src/main/resources/arena.yml`,
`cia-core/src/main/resources/skill.yml`, `cia-core/src/main/resources/lang/`, or
`cia-default-content/src/main/resources/cia-extension.yml` should be reviewed as user-facing changes.

Default-content helpers such as built-in item factories, built-in persistent-data keys, built-in combat helpers, and
content-specific listeners should live in `cia-default-content`, not `cia-core` or `cia-api`. Public API configuration
types should be generic views only; mode-specific or job-specific typed configuration belongs with the implementation
that owns it.

CIA extension jars use a `cia-extension.yml` descriptor at the jar root. The public descriptor model lives in
`cia-api/src/main/java/top/ourisland/creepersiarena/api/extension`, while descriptor reading and validation live in
`cia-core/src/main/java/top/ourisland/creepersiarena/core/extension/metadata`. Descriptor reading must not load classes
or run extension lifecycle code.

CIA extension runtime loading lives in `cia-core/src/main/java/top/ourisland/creepersiarena/core/extension/loading`.
The loader may scan `extensions/*.cia.jar`, create one class loader per extension jar, discover
`CiaExtension` through `ServiceLoader`, and call `onLoad`, `onEnable`, and `onDisable`. Keep loading behavior separate
from descriptor parsing so metadata-only tools can inspect jars without executing extension code.

## Development Setup

Use the Gradle wrapper from the repository:

```bash
./gradlew build
```

For local Paper testing, use the run-paper task from the Paper plugin module when appropriate:

```bash
./gradlew :cia-paper-plugin:runServer
```

Do not commit generated server files, build outputs, local IDE metadata, logs, or temporary configuration files created
during manual testing.

The project uses Gradle Kotlin DSL and a Java toolchain configured from the version catalog. Do not lower the Java
target or change dependency versions unless the change is intentional and explained in the pull request.

## Commit Messages

This project uses the **Conventional Commit** format.

Use this shape:

```text
<type>(optional scope): <short summary>
```

Common types include:

```text
fix(arena): keep spectators outside active regions
feat(job): add cooldown to miner skill
refactor(config): split arena config loader
style(command): reformat admin command registration
chore(build): update Paper API
ci(build): run Gradle checks on pull requests
```

A good commit message explains the change from the user or maintainer's perspective. Prefer:

```text
fix(lobby): restore player inventory after failed arena start
```

over:

```text
fix stuff
```

When a change has a breaking behavior change, use a footer and an exclamation mark before the colon:

```text
feat(config)!: rename arena spawn settings

...

BREAKING CHANGE: `arena.spawn-points` has been replaced by `arena.spawns`.
```

## Pull Request Expectations

Keep pull requests focused. A gameplay change, a config migration, and a formatting cleanup should usually be separate
pull requests.

When opening a pull request, explain:

- what changed;
- why it changed;
- how the change was tested;
- which Paper, Minecraft, and plugin versions were used for testing;
- whether any configuration files, permissions, commands, or language keys changed.

For bug fixes, include reproduction steps when possible. For gameplay changes, describe the player-visible behavior
before and after the change.

## General Code Style

Write code that is easy to read in review. Avoid hiding intent behind overly compact formatting.

Class, enum, record, and object names use **PascalCase**:

```java
public final class ArenaManager {

}
```

Variable names, fields, parameters, and method names use **camelCase**:

```java
private int remainingTicks;

public void startArena(ArenaConfig arenaConfig) {
    var arenaName = arenaConfig.name();
}
```

```kotlin
val commandName = "join"
var retryCount = 0
```

Constants use **UPPER_SNAKE_CASE**:

```java
private static final int DEFAULT_COUNTDOWN_SECONDS = 10;
```

Package names stay lowercase and should remain under `top.ourisland.creepersiarena`.

Interfaces use the `I` + `Name` format. This is intentional and should be kept consistent across the project.

Good examples:

```java
public interface IArenaComponent {

}
```

```java
public interface IGamePhase {

}
```

Do not introduce interface names like `ArenaComponent`, `GamePhase`, or `ComponentLike` when the type is meant to be an
interface in the existing project style.

Do not introduce large unrelated rewrites. Use the same language as the surrounding code unless there is a clear reason
to change it. Java is the default choice for most Paper runtime, game, command, and listener code. Kotlin is acceptable
where the surrounding package already uses Kotlin or where it clearly improves configuration or data-model code.

## Visibility and Encapsulation

Private-purpose methods, variables, fields, and classes should not be exposed.

Use `private` for implementation details. Do not mark something `public` just because it is convenient during
development.

Good:

```java
private boolean canJoinArena(Player player, GameSession session) {
    return session.accepts(player) && !session.isRunning();
}
```

Bad:

```java
public boolean canJoinArena(Player player, GameSession session) {
    return session.accepts(player) && !session.isRunning();
}
```

In Java, avoid redundant exposure for helper methods that are not part of the public API:

```java
private String normalizeArenaName(String raw) {
    return raw.trim().toLowerCase(Locale.ROOT);
}
```

In Kotlin, the same rule applies:

```kotlin
private fun normalizeArenaName(raw: String): String {
    return raw.trim().lowercase()
}
```

Public APIs should be intentional. If another package needs shared behavior, first consider whether the behavior belongs
in `core`, `game`, or `utils` rather than exposing a feature-local helper.

## Braces and Spacing

Opening braces stay on the same line as the method, function, class, interface, enum, record, constructor, or
control-flow header.

Good:

```java
public final class Example {

}
```

```kotlin
class Example {

}
```

Bad:

```java
public final class Example
{

}
```

After a class or interface header, leave an extra blank line before the first member. Also leave a blank line before the
closing brace of the class or interface. This rule applies to type bodies, not method or function bodies.

Good Java class:

```java
public record Color(
        int r,
        int g,
        int b
) {

    public Color() {
        this(0, 0, 0);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", r, g, b);
    }

}
```

Good Kotlin class:

```kotlin
data class Color(
    val r: Int = 0,
    val g: Int = 0,
    val b: Int = 0,
) {

    override fun toString(): String {
        return "($r, $g, $b)"
    }

}
```

Methods and functions should **not** add extra blank lines immediately after the opening brace or immediately before the
closing brace.

Good Java method:

```java
public String trimText(String input) {
    return input.trim();
}
```

Good Kotlin function:

```kotlin
fun trimText(input: String): String {
    return input.trim()
}
```

Bad Java method:

```java
public String trimText(String input) {

    return input.trim();

}
```

Bad Kotlin function:

```kotlin
fun trimText(input: String): String {

    return input.trim()

}
```

This rule intentionally creates breathing room around type bodies while keeping method and function bodies compact.

Do not compress complete Java or Kotlin files onto one line.

Good:

```java
@Override
public void onEnable() {
    bootstrap.enable(this);
}
```

Bad:

```java
@Override public void onEnable() { bootstrap.enable(this); }
```

## Function and Method Parameters

When a method, constructor, or function has **three or more parameters**, place each parameter on its own line.

Good Java:

```java
public GameSession createSession(
        ArenaConfig arenaConfig,
        Collection<Player> players,
        GameMode mode
) {
    return new GameSession(arenaConfig, players, mode);
}
```

Good Kotlin:

```kotlin
fun createSession(
    arenaConfig: ArenaConfig,
    players: Collection<Player>,
    mode: GameMode
): GameSession {
    return GameSession(arenaConfig, players, mode)
}
```

Avoid this for three or more parameters:

```java
public GameSession createSession(ArenaConfig arenaConfig, Collection<Player> players, GameMode mode) {
    return new GameSession(arenaConfig, players, mode);
}
```

For two parameters, a single line is acceptable when it remains readable.

## Records and Data Classes

Records and data classes should always put fields on separate lines.

Java records:

```java
public record ArenaBounds(
        String worldName,
        Position min,
        Position max
) {

}
```

Kotlin data classes:

```kotlin
data class SkillConfig(
    val id: String,
    val cooldownTicks: Int,
    val enabled: Boolean = true,
)
```

Do not compress records or data classes with multiple fields into one line:

```kotlin
data class SkillConfig(val id: String, val cooldownTicks: Int)
```

The multi-line format keeps diffs smaller and makes future additions easier to review.

## Enums

Enum constants should always be placed one per line.

Good Java:

```java
public enum ArenaPhase {

    LOBBY,
    COUNTDOWN,
    RUNNING,
    FINISHED

}
```

Good Kotlin:

```kotlin
enum class ArenaPhase {

    LOBBY,
    COUNTDOWN,
    RUNNING,
    FINISHED

}
```

Avoid:

```java
public enum ArenaPhase {
    LOBBY,
    COUNTDOWN,
    RUNNING,
    FINISHED
}
```

Also avoid compressing the enum into one line:

```java
public enum ArenaPhase { LOBBY, COUNTDOWN, RUNNING, FINISHED }
```

## Annotation Formatting

When an annotation has more than three parameters, place each parameter on its own line.

Good:

```java
@ExampleAnnotation(
        name = "arena",
        enabled = true,
        timeoutTicks = 200,
        retries = 2
)
public final class ArenaExample {

}
```

Kotlin:

```kotlin
@ExampleAnnotation(
    name = "arena",
    enabled = true,
    timeoutTicks = 200,
    retries = 2
)
class ArenaExample {

}
```

For short annotations with one or two parameters, a single line is fine if it is readable.

Good:

```java
@EventHandler(ignoreCancelled = true)
public void onPlayerMove(PlayerMoveEvent event) {
    handleMove(event);
}
```

## Java `var` Usage

Prefer `var` for local variables when it reduces redundancy **and** improves readability. Do not use `var` mechanically.
Follow the intent of the
OpenJDK [Local Variable Type Inference Style Guidelines](https://openjdk.org/projects/amber/guides/lvti-style-guide):
code is read more often than it is written, and `var` should help future readers understand the code from local context.

`var` is recommended when the initializer clearly communicates the type or role.

Good, because the receiver and method name make the role clear:

```java
var pluginManager = plugin.getServer().getPluginManager();
pluginManager.registerEvents(listener, plugin);
```

Good, because the constructor already states the type:

```java
var session = new GameSession(arenaConfig, players, mode);
session.startCountdown();
```

Good, because it removes noisy generic repetition:

```java
for (var entry : skillConfig.skills().entrySet()) {
    registerSkill(entry.getKey(), entry.getValue());
}
```

Good, because it makes a resource declaration less repetitive:

```java
try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
    return reader.lines().toList();
}
```

Use explicit types when the initializer does not make the type or intent obvious.

Bad:

```java
var result = load(player);
```

Better:

```java
ArenaJoinResult result = load(player);
```

Avoid `var` when using an empty diamond or generic factory where the inferred type may be too broad.

Bad:

```java
var queue = new PriorityQueue<>();
var names = List.of();
```

Better:

```java
PriorityQueue<Player> queue = new PriorityQueue<>();
List<String> names = List.of();
```

Avoid `var` when the exact primitive type matters.

Bad:

```java
var delayTicks = 20;
```

Better, when the API expects a `long` tick value:

```java
long delayTicks = 20L;
```

Keep the variable's scope small when using `var`. If a variable is declared far away from where it is used, prefer an
explicit type or refactor the code so the declaration and usage are closer together.

Do not use `var` for fields, method parameters, method return types, or public API design. Java does not allow most of
these forms, and the style principle is the same: public contracts should be explicit.

## Stream API Formatting

When using Java Stream API, keep `.stream()` on the same line as the data source. After that, put every operation on its
own line.

Good:

```java
var onlinePlayers = Bukkit.getOnlinePlayers().stream()
        .filter(player -> player.hasPermission("creepersiarena.join"))
        .sorted(Comparator.comparing(Player::getName))
        .toList();
```

Bad:

```java
var onlinePlayers = Bukkit.getOnlinePlayers()
        .stream()
        .filter(player -> player.hasPermission("creepersiarena.join")).sorted(Comparator.comparing(Player::getName)).toList();
```

This style makes stream pipelines easier to scan and easier to modify during reviews.

Break up long chains when intermediate values have useful names.

Good:

```java
var eligiblePlayers = Bukkit.getOnlinePlayers().stream()
        .filter(this::canJoin)
        .toList();

var selectedPlayers = arenaSelector.selectPlayers(eligiblePlayers);
return createSession(arenaConfig, selectedPlayers, mode);
```

## Imports

Use normal explicit imports by default. However, when imports become repetitive, prefer broader imports according to the
following rules.

If more than four methods from the same class are used, consider importing all methods from that class.

```java
import static java.util.Comparator.*;
```

If one method from an external class is used more than four times, consider importing that method directly instead of
repeatedly qualifying it.

```java
import static java.util.Objects.requireNonNull;
```

If imports from the same package exceed four, consider importing the whole package.

```java
import java.util.*;
```

This is not a license to use wildcard imports everywhere. Use broader imports when they improve readability and reduce
noise. Do not add wildcard imports just because an IDE inserted them automatically.

Static imports are acceptable for well-known constants or helper methods when they improve readability, but avoid hiding
where important Paper, Bukkit, or project-specific behavior comes from.

## Static Utility Classes

If a Java class only contains static utility methods, it must prevent instantiation with a private constructor.

Good:

```java
public final class LocationUtils {

    private LocationUtils() {
    }

    public static boolean isSameWorld(Location first, Location second) {
        return first.getWorld().equals(second.getWorld());
    }

}
```

In Kotlin, use `object` instead of a class with a private constructor.

Good:

```kotlin
object LocationUtils {

    fun isSameWorld(first: Location, second: Location): Boolean {
        return first.world == second.world
    }

}
```

Bad:

```kotlin
class LocationUtils private constructor() {

    companion object {

        fun isSameWorld(first: Location, second: Location): Boolean {
            return first.world == second.world
        }

    }

}
```

## Unused Lambda, Catch, and Pattern Variables

For unused variables in lambda expressions, `catch` statements, and switch patterns, use `_` where the language supports
it. This follows the intent of [JEP 456](https://openjdk.org/jeps/456).

Good Kotlin:

```kotlin
items.forEach { _ ->
    reload()
}
```

```kotlin
try {
    runTask()
} catch (_: Exception) {
    fallback()
}
```

For Java versions that support unnamed variables and patterns, prefer `_` for intentionally unused values.

Good:

```java
try {
    runTask();
} catch (Exception _) {
    fallback();
}
```

```java
for (Player _ : players) reloadArena();
```

```java
return switch (event) {
    case ArenaEvent.Join(Player player, Arena _) -> "joined: " + player.getName();
    default -> "unknown";
};
```

```java
(key, _) -> logger.info(key)
```

If the current language level or toolchain does not support unnamed variables in a specific context, use a clearly named
ignored variable such as `ignored`, but do not use misleading names.

## Kotlin and Java Boundaries

This project intentionally uses both Kotlin and Java. Do not migrate packages blindly.

Java is the default choice for Paper runtime behavior, including most command, listener, game-loop, lifecycle, and
Bukkit/Paper API integration code. This keeps plugin behavior straightforward for Java-heavy APIs and reduces churn in
central runtime code.

Kotlin is acceptable when:

- the surrounding package is already Kotlin;
- the code is primarily a data or configuration model;
- Kotlin significantly simplifies a transformation or parser;
- the change is part of an approved refactor.

Bad reasons include:

- "Kotlin looks nicer";
- "I touched the file anyway";
- "I want all packages to be Kotlin".

### `utils` Package

The `utils` package is where reusable helpers belong. If you create a helper that is likely to be useful outside the
current command, listener, job, or game service, move it to `utils` or to a shared service in `core` or `game`.

Choose Java or Kotlin based on the surrounding code and call sites. Do not introduce Kotlin into a Java-heavy runtime
path unless the benefit is clear.

Good:

```java
package top.ourisland.creepersiarena.utils;

public final class ArenaNameUtils {

    private ArenaNameUtils() {
    }

    public static String normalize(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT);
    }

}
```

Then use it from feature code:

```java
var normalizedName = ArenaNameUtils.normalize(input);
```

Do not duplicate utility logic inside a command if an equivalent utility already exists.

Bad:

```java
private String normalizeArenaName(String raw) {
    return raw.trim().toLowerCase(Locale.ROOT);
}
```

Before writing a helper, check whether the behavior already exists in `utils`, `core`, or a shared game service. Unless
there is a specific reason not to, use the existing utility.

### `core` Package

The `core` package should remain Java unless there is a good reason to change it.

Core code is usually more stable and often interacts with Java-heavy APIs or project-level abstractions. Keeping it Java
reduces churn and preserves compatibility for central components.

Good reasons to introduce Kotlin in `core` might include:

- a new component is naturally coroutine-based;
- Kotlin significantly simplifies a complex data transformation;
- the surrounding code is already Kotlin;
- the change is part of an approved refactor.

Bad reasons include:

- "Kotlin looks nicer";
- "I touched the file anyway";
- "I want all packages to be Kotlin".

## Shared Utilities and Reuse

During development, if you write a method or function that has reusable value, **extract it** to `utils`, `core`, or a
shared game service.

A helper has reusable value when:

- it is not tied to one command's business logic;
- it handles coordinates, regions, locations, worlds, bounding boxes, text formatting, language messages, scheduling,
  permissions, parsing, validation, or similar infrastructure concerns;
- it is likely to be needed by another command, listener, job, mode, or service;
- it fixes a bug that could appear in more than one place.

For example, region and location checks should not be reimplemented inside every listener or command.

Good:

```java
var bounds = ArenaBounds.fromConfig(regionConfig);
if (!bounds.contains(player.getLocation())) {
    protectionService.handleOutsideArena(player);
}
```

Bad:

```java
var location = player.getLocation();
if (location.getBlockX() < minX || location.getBlockX() > maxX) {
    protectionService.handleOutsideArena(player);
}
```

The bad example duplicates behavior that belongs in a region or bounds helper. If a suitable utility exists, use it. Do
not write a second local version unless there is a documented reason.

The same rule applies to message formatting and configuration parsing. Command and listener code should not need to know
every low-level detail of YAML paths, language keys, coordinate parsing, or permission string construction.

## Command and Feature Code

Command code should focus on command behavior, not infrastructure.

Commands should validate sender type, permissions, arguments, and current game state before mutating arena state.

Good:

```java
if (!sender.hasPermission("creepersiarena.admin.reload")) {
    sender.sendMessage(Component.text("You do not have permission to reload creepersIArena."));
    return;
}

plugin.onReload();
sender.sendMessage(Component.text("creepersIArena reloaded."));
```

Bad:

```java
plugin.onReload();
```

Do not silently fail. Commands should return clear feedback for invalid sender type, missing permissions, invalid arena
names, invalid config, or unsupported game state.

When sending messages, use the project's established message or language system when one is available instead of
hardcoding repeated user-facing strings in command logic.

A command can decide *what* should happen. Shared conversion, permission, region, configuration, scheduling, game-state,
and language behavior should stay in the appropriate `utils`, `core`, `game`, `config`, or service package.

## Configuration Classes

Configuration classes should be easy to load from YAML and easy to inspect.

Java records are a good fit for immutable configuration values:

```java
public record ArenaConfig(
        String name,
        int minPlayers,
        int maxPlayers,
        List<String> spawnPoints
) {

}
```

Kotlin configuration models should generally use `data class` when they are primarily data holders:

```kotlin
data class SkillConfig(
    val id: String,
    val cooldownTicks: Int = 0,
    val enabled: Boolean = true,
)
```

Prefer clear defaults when a safe default exists. Do not use nullable types simply to avoid initializing a property.

Good:

```kotlin
var enabled: Boolean = false
var displayName: String = ""
```

Use nullable types when a value may truly be absent from configuration or runtime input:

```kotlin
var permission: String? = null
```

Nested config groups should also be records or data classes when they carry state. If a config group is intentionally
empty, use a Kotlin `object` or `data object` rather than adding meaningless fake fields.

Validate loaded values before using them in gameplay logic.

Good:

```java
if (arenaConfig.maxPlayers() < arenaConfig.minPlayers()) {
    throw new IllegalArgumentException("maxPlayers must be greater than or equal to minPlayers");
}
```

Bad:

```java
int maxPlayers = 16;
```

unless the value is a true constant of the game mode and is documented as such.

## Lombok and Nullability

The project uses Lombok. Use Lombok to remove boilerplate when it makes the code easier to maintain, not to hide
important behavior.

Respect the repository Lombok configuration. Accessors are fluent, so generated methods should follow the existing
style:

```java
arenaConfig.name();
arenaConfig.enabled();
```

not:

```java
arenaConfig.getName();
arenaConfig.isEnabled();
```

Where nullability matters, make it explicit with JSpecify annotations, Kotlin nullable types, or clear API design. Do
not rely on comments to describe whether a value may be `null`.

Prefer returning empty collections over `null` collections.

Good:

```java
public List<Player> players() {
    return players;
}
```

Bad:

```java
public List<Player> players() {
    return players == null ? null : players;
}
```

When Java callers use Kotlin code, remember that Kotlin nullability affects generated method contracts. Keep public APIs
conservative and predictable.

## Paper, Bukkit, and Minecraft API Usage

Prefer Paper and Bukkit APIs over internal Minecraft server classes. Avoid NMS, reflection into server internals, or
version-specific implementation details unless there is a documented reason and a clear fallback or compatibility plan.

Keep main-thread rules in mind. Most Bukkit entity, world, inventory, and player operations must happen on the server
thread. Do not perform blocking I/O, network requests, or long computations on the main thread.

Good:

```java
Bukkit.getScheduler().runTask(plugin, () -> player.teleport(spawnLocation));
```

Bad:

```java
new Thread(() -> player.teleport(spawnLocation)).start();
```

Register commands, listeners, permissions, and lifecycle components through the plugin bootstrap or the appropriate
core/component system. Clean up scheduled tasks, listeners, sessions, and temporary state when the plugin disables or
reloads.

`onReload()` is not the same as a full disable followed by a full enable. Reload-safe code should explicitly refresh
configuration and runtime state without assuming every object was recreated.

## Configuration and Resources

Configuration changes are user-facing changes.

When changing `paper-plugin.yml`, `config.yml`, `arena.yml`, `skill.yml`, or files under `lang/`:

- keep defaults valid;
- keep existing keys compatible when possible;
- document renamed or removed keys in the pull request;
- validate loaded values before using them in gameplay logic;
- avoid hardcoding server-specific values in Java or Kotlin code when they belong in configuration.

Resource defaults should be usable on a fresh server. For config migrations, test both a fresh default configuration and
an existing configuration from before the change.

## Game and Arena Behavior

Gameplay changes should be easy to reason about and easy to test.

Keep arena state transitions explicit. Do not let unrelated systems mutate session state indirectly without a named
method that describes the transition.

Good:

```java
session.startCountdown();
session.beginRound();
session.finishRound(winningTeam);
```

Bad:

```java
session.phase(2);
session.flag(true);
```

Avoid hidden global state. If a game component needs access to runtime state, pass the required dependency explicitly or
obtain it through the established component/bootstrap system.

Protect player data. Any code that changes inventory, location, health, potion effects, game mode, scoreboard,
permissions, or team membership should also define how the state is restored when a player leaves, the arena stops, the
plugin reloads, the plugin disables, or an error occurs.

## Error Handling and Logging

Do not swallow exceptions that affect plugin state, configuration loading, player data, or arena flow.

Good:

```java
try {
    configManager.reload();
} catch (IOException exception) {
    plugin.getLogger().severe("Failed to reload creepersIArena configuration: " + exception.getMessage());
    throw exception;
}
```

Bad:

```java
try {
    configManager.reload();
} catch (Exception ignored) {
}
```

Use clear log messages that identify the arena, player, config file, or component involved. Avoid noisy logs inside
high-frequency events such as movement, damage, or interaction handlers unless debugging is explicitly enabled.

## Testing and Manual Verification

Before opening a pull request, run:

```bash
./gradlew build
```

For gameplay, command, configuration, or listener changes, also test on a local Paper server with the relevant Minecraft
version.

Manual testing notes should include:

- server version;
- plugin version or commit;
- changed config files;
- commands used;
- expected behavior;
- observed behavior;
- relevant console logs if something failed.

For config migrations, test both a fresh default configuration and an existing configuration from before the change.

## Examples of Preferred Formatting

### Java Method

This example follows the project style: the method has three parameters, so each parameter is placed on its own line.
The stream source keeps `.stream()` on the same line as the data, and every stream operation after that gets its own
line.

```java
public static List<Player> eligiblePlayers(
        Collection<Player> players,
        String permission,
        ArenaSession session
) {
    if (players.isEmpty()) return List.of();

    return players.stream()
            .filter(player -> player.hasPermission(permission))
            .filter(session::accepts)
            .sorted(Comparator.comparing(Player::getName))
            .toList();
}
```

### Kotlin Function

This example uses one parameter per line, keeps defaults visible, and avoids extra blank lines after the opening brace.

```kotlin
fun loadSkillConfig(
    id: String,
    cooldownTicks: Int = 0,
    enabled: Boolean = true
): SkillConfig {
    return SkillConfig(
        id = id,
        cooldownTicks = cooldownTicks,
        enabled = enabled
    )
}
```

### Java Record

```java
public record PlayerSnapshot(
        UUID playerId,
        Location location,
        GameMode gameMode,
        ItemStack[] inventory
) {

}
```

### Kotlin Data Class

```kotlin
data class ArenaLanguageEntry(
    val key: String,
    val fallback: String,
    val placeholders: Map<String, String> = emptyMap()
)
```

### Java Stream

```java
List<Player> enabledPlayers = Bukkit.getOnlinePlayers().stream()
        .filter(player -> player.hasPermission("creepersiarena.play"))
        .sorted(Comparator.comparing(Player::getName))
        .toList();
```

### Java `var`

```java
var session = new GameSession(arenaConfig, players, mode);
session.startCountdown();
```

Use an explicit type instead when the initializer hides important information:

```java
ArenaJoinResult result = joinService.tryJoin(player, arenaName);
```

## Review Expectations

Before opening a pull request, check the following:

- The commit messages follow Conventional Commit format.
- The change is focused and does not mix unrelated rewrites.
- Public APIs are intentional.
- Private helpers are marked `private`.
- Interface names follow the `IName` convention.
- Java `var` is used only where it reduces redundancy and keeps local readability high.
- Exact primitive types, generic empty factories, and unclear method-call initializers use explicit types.
- Parameters, records, data classes, annotations, streams, enums, imports, static utility classes, and unused variables
  follow the formatting rules above.
- Opening braces stay on the same line as the declaration or control-flow header.
- Type bodies have breathing room, while method and function bodies stay compact.
- Reusable helpers are placed in `utils`, `core`, or a shared game service instead of being duplicated.
- Existing shared utilities are reused instead of duplicated.
- `core` remains Java unless a Kotlin migration is justified.
- Java/Kotlin boundaries are respected.
- Paper/Bukkit APIs are used safely on the correct thread.
- Commands validate permissions, sender type, arguments, and game state.
- Configuration/resource changes are documented and remain backward-compatible when possible.
- Feature code uses shared project utilities and services instead of duplicating infrastructure logic.
- Player state changed by arena code is restored on leave, stop, reload, disable, and error paths.
- Lombok fluent accessors and nullability expectations are respected.
- `./gradlew build` passes.
- Gameplay changes were tested on a Paper server when relevant.

When in doubt, optimize for maintainability and review clarity. Code is read more often than it is written.
