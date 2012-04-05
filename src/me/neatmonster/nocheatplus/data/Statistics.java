package me.neatmonster.nocheatplus.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Statistics {

    public enum Id {

        BB_DIRECTION("blockbreak.direction"),
        BB_NOSWING("blockbreak.noswing"),
        BB_REACH("blockbreak.reach"),
        BP_DIRECTION("blockplace.direction"),
        BP_REACH("blockplace.reach"),
        CHAT_COLOR("chat.color"),
        CHAT_SPAM("chat.spam"),
        FI_DIRECTION("fight.direction"),
        FI_NOSWING("fight.noswing"),
        FI_REACH("fight.reach"),
        FI_SPEED("fight.speed"),
        INV_DROP("inventory.drop"),
        INV_BOW("inventory.instantbow"),
        INV_EAT("inventory.instanteat"),
        MOV_RUNNING("moving.running"),
        MOV_FLYING("moving.flying"),
        MOV_MOREPACKETS("moving.morepackets"),
        MOV_NOFALL("moving.nofall"),
        MOV_SNEAKING("moving.sneaking"),
        MOV_SWIMMING("moving.swimming"),
        FI_GODMODE("fight.godmode"),
        FI_INSTANTHEAL("fight.instantheal");

        private final String name;

        private Id(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    private final Map<Id, Double>  statisticVLs   = new HashMap<Id, Double>(Id.values().length);
    private final Map<Id, Integer> statisticFails = new HashMap<Id, Integer>(Id.values().length);

    public Statistics() {
        // Initialize statistic values
        for (final Id id : Id.values()) {
            statisticVLs.put(id, 0D);
            statisticFails.put(id, 0);
        }
    }

    public Map<String, Object> get() {
        final Map<String, Object> map = new TreeMap<String, Object>();

        for (final Entry<Id, Double> entry : statisticVLs.entrySet())
            map.put(entry.getKey().toString() + ".vl", entry.getValue().intValue());

        for (final Entry<Id, Integer> entry : statisticFails.entrySet())
            map.put(entry.getKey().toString() + ".failed", entry.getValue());

        return map;
    }

    public void increment(final Id id, final double vl) {
        Double stored = statisticVLs.get(id);
        if (stored == null)
            stored = 0D;
        statisticVLs.put(id, stored + vl);

        Integer failed = statisticFails.get(id);
        if (failed == null)
            failed = 0;
        statisticFails.put(id, failed + 1);
    }
}
