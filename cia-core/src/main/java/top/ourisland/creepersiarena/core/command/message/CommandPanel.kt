package top.ourisland.creepersiarena.core.command.message

import java.util.*

/**
 * Small immutable MiniMessage panel model for command output.
 */
class CommandPanel(
    title: String?,
    rows: List<String>?
) {

    private val titleValue: String = if (title.isNullOrBlank()) "CreepersIArena" else title
    private val rowsValue: List<String> = Collections.unmodifiableList(ArrayList(rows ?: emptyList()))

    fun title(): String = titleValue

    fun rows(): List<String> = rowsValue

    companion object {

        @JvmStatic
        fun builder(title: String?): Builder = Builder(title)

    }

    class Builder internal constructor(
        private val title: String?
    ) {

        private val rows = ArrayList<String>()

        fun rows(rows: List<String>?): Builder {
            rows?.forEach(this::row)
            return this
        }

        fun row(row: String?): Builder {
            rows.add(row ?: "")
            return this
        }

        fun build(): CommandPanel = CommandPanel(title, rows)

    }

}
