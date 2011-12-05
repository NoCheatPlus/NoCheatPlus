package cc.co.evenprime.bukkit.nocheat.actions.types;

public enum WildCard {
    PLAYER("player"), LOCATION("location"), WORLD("world"), VIOLATIONS("violations"), MOVEDISTANCE("movedistance"), REACHDISTANCE("reachdistance"), FALLDISTANCE("falldistance"), LOCATION_TO("locationto"), CHECK("check"), PACKETS("packets"), TEXT("text"), PLACE_LOCATION("placelocation"), PLACE_AGAINST("placeagainst"), BLOCK_TYPE("blocktype");

    private final String s;

    private WildCard(String s) {
        this.s = s;
    }

    public static final WildCard get(String s) {
        for(WildCard c : WildCard.values()) {
            if(c.s.equals(s)) {
                return c;
            }
        }

        return null;
    }
}