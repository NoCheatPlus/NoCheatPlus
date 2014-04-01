package fr.neatmonster.nocheatplus.actions;

/**
 * Some wildcards that are used in commands and log messages.
 */
public enum ParameterName {
	// TODO: Cleanup for some kind of policies: useful names, alternative names, prefer generic names.
    BLOCK_ID("blockid"), // TODO: Block name ?
    CHECK("check"),
    TAGS("tags"),
    DISTANCE("distance"),
    FALL_DISTANCE("falldistance"), // TODO: rather not deprecate ?
    FOOD("food"),
    IP("ip"),
    LIMIT("limit"),
    LOCATION_FROM("locationfrom"),
    LOCATION_TO("locationto"),
    PACKETS("packets"),
    PLAYER("player"), // TODO: playername rather ? + displayname ?
    REACH_DISTANCE("reachdistance"), // TODO: deprecate ?
    UUID("uuid"),
    VIOLATIONS("violations"),
    WORLD("world");

    /**
     * Gets the parameter associated to the text.
     * 
     * @param text
     *            the text
     * @return the parameter name
     */
    public static final ParameterName get(final String text) {
        for (final ParameterName parameterName : ParameterName.values())
            if (parameterName.text.equals(text))
                return parameterName;

        return null;
    }

    /** The text. */
    private final String text;

    /**
     * Instantiates a new parameter name.
     * 
     * @param text
     *            the text
     */
    private ParameterName(final String text) {
        this.text = text;
    }
}
