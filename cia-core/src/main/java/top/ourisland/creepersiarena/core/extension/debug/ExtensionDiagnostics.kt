package top.ourisland.creepersiarena.core.extension.debug

import top.ourisland.creepersiarena.api.identity.ExtensionId
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.bootstrap.discovery.ComponentCatalog
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent
import top.ourisland.creepersiarena.core.extension.loading.CiaExtensionLoadFailure
import top.ourisland.creepersiarena.core.extension.loading.CiaExtensionManager
import top.ourisland.creepersiarena.core.extension.loading.LoadedCiaExtension
import top.ourisland.creepersiarena.core.game.GameManager
import top.ourisland.creepersiarena.core.job.JobManager
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRegistry
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.format.DateTimeFormatter

object ExtensionDiagnostics {

    @JvmStatic
    fun infoLines(rt: BootstrapRuntime, rawId: String): List<String> {
        val id = try {
            ExtensionId.parse(rawId)
        } catch (exception: IllegalArgumentException) {
            return listOf(exception.message ?: "Invalid extension id: $rawId")
        }

        val manager = rt.requireService(CiaExtensionManager::class.java)

        val loaded = manager.loadedExtension(id)
        if (loaded != null) return loadedInfoLines(rt, loaded)

        val failure = manager.loadFailure(id)
        if (failure != null) return failureInfoLines(failure)

        return listOf("CIA extension not found: ${id.value()}")
    }

    private fun loadedInfoLines(
        rt: BootstrapRuntime,
        loaded: LoadedCiaExtension
    ): List<String> {
        val descriptor = loaded.descriptor()
        val snapshot = loaded.registrations()
        val lines = ArrayList<String>()

        lines.add("id=${descriptor.id().value()}")
        lines.add("namespace=${descriptor.namespace().value()}")
        lines.add("status=${if (loaded.enabled()) "ENABLED" else "LOADED"}")
        lines.add("name=${descriptor.name()}")
        lines.add("version=${descriptor.version()}")
        lines.add("main=${descriptor.mainClass()}")
        lines.add("api-version=${descriptor.apiVersion()}")
        lines.add("cia-version=${descriptor.ciaVersion()}")
        lines.add("load-order=${descriptor.loadOrder()}")
        lines.add("authors=${descriptor.authors()}")
        lines.add("required-dependencies=${descriptor.requiredDependencyIds()}")
        lines.add("optional-dependencies=${descriptor.optionalDependencyIds()}")
        lines.add("jar=${loaded.jarPath()}")
        lines.add("data-folder=${loaded.context().dataFolder()}")
        lines.add("loaded-at=${loaded.loadedAt()}")

        appendList(lines, "registered-jobs", snapshot.jobs())
        appendList(lines, "registered-skills", snapshot.skills())
        appendList(lines, "registered-modes", snapshot.modes())
        appendList(lines, "registered-listeners", snapshot.listeners())
        appendList(lines, "installed-resources", snapshot.installedResources())
        appendList(lines, "merged-yaml-resources", snapshot.mergedYamlResources())
        appendList(lines, "merged-properties-resources", snapshot.mergedPropertiesResources())

        appendOwnerRuntimeView(rt, descriptor.id(), lines)
        return lines
    }

    private fun failureInfoLines(failure: CiaExtensionLoadFailure): List<String> {
        return listOf(
            "id=${failure.id().value()}",
            "status=FAILED",
            "jar=${failure.jarPath()}",
            "failed-at=${failure.failedAt()}",
            "message=${failure.message()}"
        )
    }

    private fun appendList(
        lines: MutableList<String>,
        label: String,
        values: List<String>
    ) {
        lines.add("$label=${sorted(values)}")
    }

    private fun appendOwnerRuntimeView(
        rt: BootstrapRuntime,
        extensionId: ExtensionId,
        lines: MutableList<String>
    ) {
        val catalog = rt.getService(ComponentCatalog::class.java) ?: return

        appendList(lines, "catalog-modules", ownedIds(catalog.registeredModules(), extensionId) { it.toString() })
        appendList(lines, "catalog-jobs", ownedIds(catalog.registeredJobs(), extensionId) { it.toString() })
        appendList(lines, "catalog-skills", ownedIds(catalog.registeredSkills(), extensionId) { it.toString() })
        appendList(lines, "catalog-modes", ownedIds(catalog.registeredModes(), extensionId) { it.toString() })
    }

    private fun sorted(values: List<String>): List<String> {
        return values.sorted()
    }

    private fun <K, T> ownedIds(
        components: Collection<RegisteredComponent<K, T>>,
        extensionId: ExtensionId,
        keyFn: (K) -> String
    ): List<String> {
        return components
            .asSequence()
            .filter { it.owner().extensionId() == extensionId }
            .map { keyFn(it.id()) }
            .sorted()
            .toList()
    }

    @JvmStatic
    fun writeDump(rt: BootstrapRuntime): Path {
        val target = rt.plugin()
            .dataFolder
            .toPath()
            .resolve("extension-cache/extensions-dump.txt")

        try {
            Files.createDirectories(target.parent)
            Files.writeString(
                target,
                dumpLines(rt).joinToString(System.lineSeparator()) + System.lineSeparator(),
                StandardCharsets.UTF_8
            )
            return target
        } catch (ex: IOException) {
            throw IllegalStateException("Failed to write extension dump: $target", ex)
        }
    }

    @JvmStatic
    fun dumpLines(rt: BootstrapRuntime): List<String> {
        val lines = ArrayList<String>()

        lines.add("# CreepersIArena extension dump")
        lines.add("generated-at=${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}")
        lines.add("plugin=${rt.plugin().name} version=${rt.plugin().pluginMeta.version}")
        lines.add("")
        lines.add("## Extensions")
        lines.addAll(listLines(rt))
        lines.add("")

        val manager = rt.requireService(CiaExtensionManager::class.java)

        manager.loadedExtensions()
            .sortedBy {
                it.descriptor().id().value()
            }
            .forEach {
                lines.add("## Extension ${it.descriptor().id().value()}")
                lines.addAll(loadedInfoLines(rt, it))
                lines.add("")
            }

        if (manager.loadFailures().isNotEmpty()) {
            lines.add("## Failed extensions")
            manager.loadFailures().forEach {
                lines.addAll(failureInfoLines(it))
                lines.add("")
            }
        }

        lines.add("## Runtime registry owners")
        appendRuntimeOwners(rt, lines)

        return lines
    }

    @JvmStatic
    fun listLines(rt: BootstrapRuntime): List<String> {
        val manager = rt.requireService(CiaExtensionManager::class.java)
        val lines = ArrayList<String>()

        lines.add(
            "CIA extensions: loaded=${manager.loadedExtensions().size} failed=${manager.loadFailures().size}"
        )

        manager.loadedExtensions()
            .sortedBy { it.descriptor().id().value() }
            .forEach {
                val descriptor = it.descriptor()
                val snapshot = it.registrations()

                lines.add(
                    "- ${descriptor.id().value()}" +
                            " namespace=${descriptor.namespace().value()}" +
                            " [${if (it.enabled()) "ENABLED" else "LOADED"}]" +
                            " version=${descriptor.version()}" +
                            " jobs=${snapshot.jobs().size}" +
                            " skills=${snapshot.skills().size}" +
                            " modes=${snapshot.modes().size}" +
                            " listeners=${snapshot.listeners().size}" +
                            " resources=${snapshot.totalResources()}"
                )
            }

        manager.loadFailures().forEach {
            lines.add("- ${it.id().value()} [FAILED] ${it.message()}")
        }

        if (manager.loadedExtensions().isEmpty() && manager.loadFailures().isEmpty()) {
            lines.add("No CIA extensions have been discovered.")
        }

        return lines
    }

    private fun appendRuntimeOwners(
        rt: BootstrapRuntime,
        lines: MutableList<String>
    ) {
        val catalog = rt.getService(ComponentCatalog::class.java)
        if (catalog != null) {
            appendGrouped(lines, "catalog.modules", catalog.registeredModules()) { it.toString() }
            appendGrouped(lines, "catalog.jobs", catalog.registeredJobs()) { it.toString() }
            appendGrouped(lines, "catalog.skills", catalog.registeredSkills()) { it.toString() }
            appendGrouped(lines, "catalog.modes", catalog.registeredModes()) { it.toString() }
        }

        val jobs = rt.getService(JobManager::class.java)
        if (jobs != null) {
            appendGrouped(lines, "runtime.jobs", jobs.registeredJobs()) { it.toString() }
        }

        val skills = rt.getService(SkillRegistry::class.java)
        if (skills != null) {
            appendGrouped(lines, "runtime.skills", skills.registeredSkills()) { it.toString() }
        }

        val games = rt.getService(GameManager::class.java)
        if (games != null) {
            appendGrouped(lines, "runtime.modes", games.registeredModes()) { it.toString() }
        }
    }

    private fun <K, T> appendGrouped(
        lines: MutableList<String>,
        title: String,
        components: Collection<RegisteredComponent<K, T>>,
        keyFn: (K) -> String
    ) {
        lines.add("$title:")

        val grouped = LinkedHashMap<String, MutableList<String>>()

        for (component in components) {
            grouped
                .getOrPut(component.owner().extensionId().value()) { ArrayList() }
                .add(keyFn(component.id()))
        }

        if (grouped.isEmpty()) {
            lines.add("  <empty>")
            return
        }

        grouped.entries
            .sortedBy { it.key }
            .forEach { lines.add("  ${it.key} -> ${sorted(it.value)}") }
    }
}
