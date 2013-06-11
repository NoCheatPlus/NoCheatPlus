package fr.neatmonster.nocheatplus.actions;

/**
 * Namse subject to change.
 * @author mc_dev
 *
 */
public interface ParameterHolder extends ActionData{
	
	/**
	 * 
	 * @param parameterName
	 * @return Will always return some string, if not set: "<?PARAMETERNAME>".
	 */
	public String getParameter(final ParameterName parameterName);
	
	/**
	 * This might not set any parameters, if needsParameters() returns false.
	 * @param parameterName
	 * @param value
	 */
	public void setParameter(final ParameterName parameterName, String value);
	
    /**
     * Check if any of the actions needs parameters.
     * @return If true, actions are likely to contian command or logging actions.
     */
    boolean needsParameters();
}
