package top.ourisland.creepersiarena.core.extension.loading;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Parses and evaluates the version expression stored in {@code cia-version}.
 */
final class CiaVersionRequirement {

    private final String expression;
    private final Bound lower;
    private final Bound upper;
    private final Version exact;

    private CiaVersionRequirement(
            String expression,
            Bound lower,
            Bound upper,
            Version exact
    ) {
        this.expression = expression;
        this.lower = lower;
        this.upper = upper;
        this.exact = exact;
    }

    static CiaVersionRequirement parse(@lombok.NonNull String raw) {
        var expression = raw.trim();
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("cia-version must not be blank");
        }

        var rangeStart = expression.startsWith("[") || expression.startsWith("(");
        var rangeEnd = expression.endsWith("]") || expression.endsWith(")");
        if (rangeStart || rangeEnd) {
            if (!rangeStart || !rangeEnd) {
                throw new IllegalArgumentException("Invalid CIA version range: " + expression);
            }
            var body = expression.substring(1, expression.length() - 1);
            var comma = body.indexOf(',');
            if (comma < 0 || comma != body.lastIndexOf(',')) {
                throw new IllegalArgumentException("CIA version range must contain one comma: " + expression);
            }
            var lowerText = body.substring(0, comma).trim();
            var upperText = body.substring(comma + 1).trim();
            if (lowerText.isEmpty() && upperText.isEmpty()) {
                throw new IllegalArgumentException("CIA version range must have at least one bound: " + expression);
            }
            var lower = lowerText.isEmpty()
                    ? null
                    : new Bound(Version.parse(lowerText), expression.charAt(0) == '[');
            var upper = upperText.isEmpty()
                    ? null
                    : new Bound(Version.parse(upperText), expression.charAt(expression.length() - 1) == ']');
            if (lower != null && upper != null && lower.version.compareTo(upper.version) > 0) {
                throw new IllegalArgumentException("CIA version range lower bound exceeds upper bound: " + expression);
            }
            return new CiaVersionRequirement(expression, lower, upper, null);
        }

        return new CiaVersionRequirement(expression, null, null, Version.parse(expression));
    }

    boolean accepts(String runtimeVersion) {
        var current = Version.parse(runtimeVersion);
        if (exact != null) return exact.equals(current);
        if (lower != null) {
            int comparison = current.compareTo(lower.version);
            if (comparison < 0 || (comparison == 0 && !lower.inclusive)) return false;
        }
        if (upper != null) {
            int comparison = current.compareTo(upper.version);
            return comparison <= 0 && (comparison != 0 || upper.inclusive);
        }
        return true;
    }

    @Override
    public String toString() {
        return expression;
    }

    private record Bound(
            Version version,
            boolean inclusive
    ) {

    }

    private record Version(
            List<Integer> numbers,
            List<String> qualifier
    ) implements Comparable<Version> {

        private static Version parse(String raw) {
            var text = Objects.requireNonNull(raw, "raw").trim();
            if (text.isEmpty()) throw new IllegalArgumentException("Version must not be blank");

            var plus = text.indexOf('+');
            if (plus >= 0) text = text.substring(0, plus);

            String core;
            String suffix;
            var dash = text.indexOf('-');
            if (dash >= 0) {
                core = text.substring(0, dash);
                suffix = text.substring(dash + 1);
            } else {
                core = text;
                suffix = "";
            }

            var numbers = new ArrayList<Integer>();
            for (var token : core.split("\\.")) {
                if (token.isEmpty() || !token.chars().allMatch(Character::isDigit)) {
                    throw new IllegalArgumentException("Invalid version: " + raw);
                }
                try {
                    numbers.add(Integer.parseInt(token));
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Invalid numeric version component: " + raw, exception);
                }
            }
            while (numbers.size() > 1 && numbers.getLast() == 0) numbers.removeLast();

            var qualifier = new ArrayList<String>();
            if (!suffix.isEmpty()) {
                for (var token : suffix.split("[.-]")) {
                    if (token.isEmpty() || !token.matches("[0-9A-Za-z]+")) {
                        throw new IllegalArgumentException("Invalid version qualifier: " + raw);
                    }
                    qualifier.add(token.toLowerCase(Locale.ROOT));
                }
            }
            return new Version(List.copyOf(numbers), List.copyOf(qualifier));
        }

        @Override
        public int compareTo(Version other) {
            int max = Math.max(numbers.size(), other.numbers.size());
            for (int index = 0; index < max; index++) {
                int left = index < numbers.size() ? numbers.get(index) : 0;
                int right = index < other.numbers.size() ? other.numbers.get(index) : 0;
                int result = Integer.compare(left, right);
                if (result != 0) return result;
            }

            if (qualifier.isEmpty() && other.qualifier.isEmpty()) return 0;
            if (qualifier.isEmpty()) return 1;
            if (other.qualifier.isEmpty()) return -1;

            max = Math.max(qualifier.size(), other.qualifier.size());
            for (int index = 0; index < max; index++) {
                if (index >= qualifier.size()) return -1;
                if (index >= other.qualifier.size()) return 1;

                var left = qualifier.get(index);
                var right = other.qualifier.get(index);
                var leftNumber = left.chars().allMatch(Character::isDigit);
                var rightNumber = right.chars().allMatch(Character::isDigit);

                int result;
                if (leftNumber && rightNumber) {
                    result = Integer.compare(Integer.parseInt(left), Integer.parseInt(right));
                } else if (leftNumber) {
                    result = -1;
                } else if (rightNumber) {
                    result = 1;
                } else {
                    result = left.compareTo(right);
                }
                if (result != 0) return result;
            }
            return 0;
        }

    }

}
