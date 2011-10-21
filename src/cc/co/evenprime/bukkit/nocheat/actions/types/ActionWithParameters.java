package cc.co.evenprime.bukkit.nocheat.actions.types;

import java.util.ArrayList;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.data.LogData;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

public abstract class ActionWithParameters extends Action {

    private enum WildCard {
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
    protected String getMessage(final LogData data) {
        StringBuilder log = new StringBuilder(100); // Should be big enough most
                                                    // of the time

        for(Object part : messageParts) {
            if(part instanceof String) {
                log.append((String) part);
            } else {
                log.append(getParameter((WildCard) part, data));
            }
        }

        return log.toString();
    }

    private String getParameter(WildCard wildcard, LogData data) {
        // The == is correct here, as these really are identical objects, not
        // only equal
        switch (wildcard) {

        case PLAYER:
            return data.playerName;

        case CHECK:
            return data.check;

        case LOCATION: {
            Player player = Bukkit.getPlayer(data.playerName);
            if(player != null) {
                Location l = player.getLocation();
                return String.format(Locale.US, "%.2f,%.2f,%.2f", l.getX(), l.getY(), l.getZ());
            }
            return "unknown";
        }

        case WORLD: {
            Player player = Bukkit.getPlayer(data.playerName);
            if(player != null) {
                return player.getWorld().getName();
            }
            return "unknown";
        }
        case VIOLATIONS:
            return String.format(Locale.US, "%d", data.violationLevel);

        case MOVEDISTANCE: {
            Player player = Bukkit.getPlayer(data.playerName);
            if(player != null) {
                Location l = player.getLocation();
                PreciseLocation t = data.toLocation;
                if(t.isSet()) {
                    return String.format(Locale.US, "%.2f,%.2f,%.2f", t.x - l.getX(), t.y - l.getY(), t.z - l.getZ());
                } else {
                    return "null";
                }
            }
            return "unknown";
        }

        case REACHDISTANCE:
            return String.format(Locale.US, "%.2f", data.reachdistance);

        case FALLDISTANCE:
            return String.format(Locale.US, "%.2f", data.falldistance);

        case LOCATION_TO:
            PreciseLocation to = data.toLocation;
            if(to.isSet()) {
                return String.format(Locale.US, "%.2f,%.2f,%.2f", to.x, to.y, to.z);
            } else {
                return "unknown";
            }

        case PACKETS:
            return String.valueOf(data.packets);

        case TEXT:
            return data.text;

        case PLACE_LOCATION: {
            SimpleLocation l = data.placedLocation;
            if(l.isSet()) {
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            } else {
                return "null";
            }
        }

        case PLACE_AGAINST: {
            SimpleLocation l = data.placedAgainstLocation;
            if(l.isSet()) {
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            } else {
                return "null";
            }
        }

        case BLOCK_TYPE: {
            Material type = data.placedType;
            if(type == null) {
                return "null";
            }
            return type.toString();
        }

        default:
            return "Evenprime was lazy and forgot to define " + wildcard + ".";
        }
    }
}
