package top.ourisland.creepersiarena.core.command.message;

import java.util.ArrayList;
import java.util.List;

/**
 * Small immutable MiniMessage panel model for command output.
 */
public record CommandPanel(
        String title,
        List<String> rows
) {

    public CommandPanel {
        if (title == null || title.isBlank()) title = "CreepersIArena";
        rows = List.copyOf(rows == null ? List.of() : rows);
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    public static final class Builder {

        private final String title;
        private final List<String> rows = new ArrayList<>();

        private Builder(String title) {
            this.title = title;
        }

        public Builder rows(List<String> rows) {
            if (rows != null) rows.forEach(this::row);
            return this;
        }

        public Builder row(String row) {
            rows.add(row == null ? "" : row);
            return this;
        }

        public CommandPanel build() {
            return new CommandPanel(title, rows);
        }

    }

}
