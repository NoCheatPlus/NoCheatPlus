package cc.co.evenprime.bukkit.nocheat.wizard.options;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;


public class ParentOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3162246550749560727L;

	private LinkedList<Option> children = new LinkedList<Option>();

	public ParentOption(String identifier) {
		super(identifier);
	}

	public final Collection<Option> getChildOptions() {
		return Collections.unmodifiableCollection(children);
	}

	public final void add(Option option) {

		children.addLast(option);


	}
}
