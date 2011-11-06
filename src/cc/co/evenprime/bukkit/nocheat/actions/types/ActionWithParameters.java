package cc.co.evenprime.bukkit.nocheat.actions.types;

import java.util.ArrayList;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.Check;

public abstract class ActionWithParameters extends Action {

    public enum WildCard {
        PLAYER("player"), LOCATION("location"), WORLD("world"), VIOLATIONS("violations"), MOVEDISTANCE("movedistance"), REACHDISTANCE("reachdistance"), FALLDISTANCE("falldistance"), LOCATION_TO("locationto"), CHECK("check"), PACKETS("packets"), TEXT("text"), PLACE_LOCATION("placelocation"), PLACE_AGAINST("placeagainst"), BLOCK_TYPE("blocktype");

        private final String s;

        private WildCard(String s) {
            this.s = s;
        }

        private static final WildCard get(String s) {
            for(WildCard c : WildCard.values()) {
                if(c.s.equals(s)) {
                    return c;
                }
            }

            return null;
        }
    }

    private final ArrayList<Object> messageParts;

    public ActionWithParameters(String name, int delay, int repeat, String message) {
        super(name, delay, repeat);

        messageParts = new ArrayList<Object>();

        parseMessage(message);
    }

    private void parseMessage(String message) {
        String parts[] = message.split("\\[", 2);

        // No opening braces left
        if(parts.length != 2) {
            messageParts.add(message);
        }
        // Found an opening brace
        else {
            String parts2[] = parts[1].split("\\]", 2);

            // Found no matching closing brace
            if(parts2.length != 2) {
                messageParts.add(message);
            }
            // Found a matching closing brace
            else {
                WildCard w = WildCard.get(parts2[0]);

                if(w != null) {
                    // Found an existing wildcard inbetween the braces
                    messageParts.add(parts[0]);
                    messageParts.add(w);

                    // Go further down recursive
                    parseMessage(parts2[1]);
                } else {
                    messageParts.add(message);
                }
            }
        }
    }

    /**
     * Get a string with all the wildcards replaced with data from LogData
     * 
     * @param data
     * @return
     */

    protected String getMessage(NoCheatPlayer player, Check check) {

        StringBuilder log = new StringBuilder(100); // Should be big enough most
                                                    // of the time

        for(Object part : messageParts) {
            if(part instanceof String) {
                log.append((String) part);
            } else {
                log.append(check.getParameter((WildCard) part, player));
            }
        }

        return log.toString();
    }
}
