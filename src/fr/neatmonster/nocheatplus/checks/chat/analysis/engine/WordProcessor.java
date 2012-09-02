package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;


public interface WordProcessor{
	public String getProcessorName();
	/**
	 * 
	 * @param message
	 * @return A number ranging from 0 to 1. 0 means no matching, 1 means high repetition score.
	 */
	public float process(MessageLetterCount message);
}
