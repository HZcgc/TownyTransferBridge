package net.townyreborn.transfer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

final class NpcIdParser {
    private NpcIdParser() {
    }

    static Set<Integer> parse(List<?> configuredValues, Consumer<String> warningSink) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (Object configuredValue : configuredValues) {
            Integer id = parseOne(configuredValue);
            if (id == null || id < 0) {
                warningSink.accept(String.valueOf(configuredValue));
                continue;
            }
            ids.add(id);
        }
        return Set.copyOf(ids);
    }

    private static Integer parseOne(Object value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return ((Number) value).intValue();
        }
        if (value instanceof Long longValue) {
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                return null;
            }
            return longValue.intValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Integer.valueOf(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
