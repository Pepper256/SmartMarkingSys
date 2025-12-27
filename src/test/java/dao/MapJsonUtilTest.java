package dao;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class MapJsonUtilTest {

    @Test
    void stringMap_roundTrip() {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("1", "Q1");
        m.put("2", "Q2");

        String json = MapJsonUtil.toJson(m);
        HashMap<String, String> back = MapJsonUtil.toStringMap(json);

        assertEquals(m, back);
    }

    @Test
    void booleanMap_roundTrip() {
        HashMap<String, Boolean> m = new HashMap<String, Boolean>();
        m.put("1", true);
        m.put("2", false);

        String json = MapJsonUtil.toJson(m);
        HashMap<String, Boolean> back = MapJsonUtil.toBooleanMap(json);

        assertEquals(m, back);
    }

    @Test
    void emptyOrNullJson_returnsEmptyMap() {
        assertTrue(MapJsonUtil.toStringMap(null).isEmpty());
        assertTrue(MapJsonUtil.toStringMap("").isEmpty());
        assertTrue(MapJsonUtil.toBooleanMap(null).isEmpty());
        assertTrue(MapJsonUtil.toBooleanMap("   ").isEmpty());
    }
}
