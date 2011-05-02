package cc.co.evenprime.bukkit.nocheat.wizard;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

public class ParentOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3162246550749560727L;

	private LinkedList<Option> children = new LinkedList<Option>();

	public ParentOption(String identifier) {
		super(identifier);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setAlignmentX(1.0F);

		if(identifier.length() > 0) {
			this.setBorder(
					BorderFactory.createCompoundBorder(
							BorderFactory.createTitledBorder(identifier),
							BorderFactory.createEmptyBorder(5,5,5,5)));
		}
		else
			this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	}

	public final Collection<Option> getChildOptions() {
		return Collections.unmodifiableCollection(children);
	}

	public final void add(Option option) {
		children.addLast(option);

		super.add(option);
	}
}
