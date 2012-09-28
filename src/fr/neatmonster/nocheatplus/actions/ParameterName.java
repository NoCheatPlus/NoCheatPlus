package fr.neatmonster.nocheatplus.actions;

/*
 * MM"""""""`YM                                                  dP                     
 * MM  mmmmm  M                                                  88                     
 * M'        .M .d8888b. 88d888b. .d8888b. 88d8b.d8b. .d8888b. d8888P .d8888b. 88d888b. 
 * MM  MMMMMMMM 88'  `88 88'  `88 88'  `88 88'`88'`88 88ooood8   88   88ooood8 88'  `88 
 * MM  MMMMMMMM 88.  .88 88       88.  .88 88  88  88 88.  ...   88   88.  ... 88       
 * MM  MMMMMMMM `88888P8 dP       `88888P8 dP  dP  dP `88888P'   dP   `88888P' dP       
 * MMMMMMMMMMMM                                                                         
 * 
 * M"""""""`YM                              
 * M  mmmm.  M                              
 * M  MMMMM  M .d8888b. 88d8b.d8b. .d8888b. 
 * M  MMMMM  M 88'  `88 88'`88'`88 88ooood8 
 * M  MMMMM  M 88.  .88 88  88  88 88.  ... 
 * M  MMMMM  M `88888P8 dP  dP  dP `88888P' 
 * MMMMMMMMMMM                                                                       
 */
/**
 * Some wildcards that are used in commands and log messages.
 */
public enum ParameterName {
    CHECK("check"),
    BLOCK_ID("blockid"),
    DISTANCE("distance"),
    FALL_DISTANCE("falldistance"),
    FOOD("food"),
    IP("ip"),
    LIMIT("limit"),
    LOCATION_FROM("locationfrom"),
    LOCATION_TO("locationto"),
    PACKETS("packets"),
    PLAYER("player"),
    REACH_DISTANCE("reachdistance"),
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
