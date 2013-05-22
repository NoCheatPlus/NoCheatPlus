package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;


public interface WordProcessor{
	
	/**
	 * For debugging purposes.
	 * @return
	 */
	public String getProcessorName(); 
	
	/**
	 * Configured weight.
	 * @return
	 */
	public float getWeight();
	
	/**
	 * 
	 * @param message
	 * @return A number ranging from 0 to 1. 0 means no matching, 1 means high repetition score.
	 */
	public float process(MessageLetterCount message);
	
	/**
	 * Clear all held data.
	 */
	public void clear();
}
