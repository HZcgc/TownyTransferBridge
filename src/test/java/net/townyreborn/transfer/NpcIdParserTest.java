package net.townyreborn.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NpcIdParserTest {
    @Test
    void acceptsNumericAndNumericStringIdsAndRemovesDuplicates() {
        List<String> warnings = new ArrayList<>();

        Set<Integer> ids = NpcIdParser.parse(List.of(0, 12, "27", 12L), warnings::add);

        assertEquals(Set.of(0, 12, 27), ids);
        assertEquals(List.of(), warnings);
    }

    @Test
    void rejectsNegativeMalformedAndNonIntegralIds() {
        List<String> warnings = new ArrayList<>();

        Set<Integer> ids = NpcIdParser.parse(List.of(-1, "camel", 4.5D), warnings::add);

        assertEquals(Set.of(), ids);
        assertEquals(List.of("-1", "camel", "4.5"), warnings);
    }
}
