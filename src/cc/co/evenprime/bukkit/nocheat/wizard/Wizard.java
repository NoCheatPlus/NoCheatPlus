package cc.co.evenprime.bukkit.nocheat.wizard;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Wizard extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8798111079958779207L;

	private static final String pre = "    ";
	
	private static final int numberWidth = 4;
	private static final int wordWidth = 10;
	private static final int fileWidth = 20;
	private static final int textWidth = 60;

	public Wizard() {
		
		JScrollPane scrollable = new JScrollPane();

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setContentPane(scrollable);
		
		JPanel inside = new JPanel();
		scrollable.setViewportView(inside);
		
		inside.setLayout(new FlowLayout());


		final ParentOption root = new ParentOption("");
		inside.add(root);
		
		ParentOption loggingNode = new ParentOption("logging");
		root.add(loggingNode);

		loggingNode.add(new StringOption("filename", "plugins/NoCheat/nocheat.log", fileWidth));
		loggingNode.add(new LogLevelOption("logtofile", "low"));
		loggingNode.add(new LogLevelOption("logtoconsole", "high"));
		loggingNode.add(new LogLevelOption("logtochat", "med"));
		loggingNode.add(new LogLevelOption("logtoirc", "med"));
		loggingNode.add(new StringOption("logtoirctag", "nocheat", wordWidth));

		ParentOption activeNode = new ParentOption("active");
		root.add(activeNode);

		activeNode.add(new BooleanOption("speedhack", true));
		activeNode.add(new BooleanOption("moving", true));
		activeNode.add(new BooleanOption("airbuild", false));
		activeNode.add(new BooleanOption("bedteleport", true));
		activeNode.add(new BooleanOption("itemdupe", true));
		activeNode.add(new BooleanOption("bogusitems", false));

		ParentOption speedhackNode = new ParentOption("speedhack");
		root.add(speedhackNode);

		speedhackNode.add(new StringOption("logmessage", "\"%1$s sent %2$d move events, but only %3$d were allowed. Speedhack?\"", textWidth));

		{
			ParentOption speedhackLimitsNode = new ParentOption("limits");
			speedhackNode.add(speedhackLimitsNode);

			speedhackLimitsNode.add(new IntegerOption("low", 30, numberWidth));
			speedhackLimitsNode.add(new IntegerOption("med", 45, numberWidth));
			speedhackLimitsNode.add(new IntegerOption("high", 60, numberWidth));

			ParentOption speedhackActionNode = new ParentOption("action");
			speedhackNode.add(speedhackActionNode);

			speedhackActionNode.add(new StringOption("low", "loglow cancel", fileWidth));
			speedhackActionNode.add(new StringOption("med", "logmed cancel", fileWidth));
			speedhackActionNode.add(new StringOption("high", "loghigh cancel", fileWidth));
		}
		
		ParentOption movingNode = new ParentOption("moving");
		root.add(movingNode);
		
		movingNode.add(new StringOption("logmessage", "\"Moving violation: %1$s from %2$s (%4$.1f, %5$.1f, %6$.1f) to %3$s (%7$.1f, %8$.1f, %9$.1f)\"", textWidth));
		movingNode.add(new StringOption("summarymessage", "\"Moving summary of last ~%2$d seconds: %1$s total Violations: (%3$d,%4$d,%5$d)\"", textWidth));
		movingNode.add(new BooleanOption("allowflying", false));
		movingNode.add(new BooleanOption("allowfakesneak", true));
		
		{
			ParentOption movingActionNode = new ParentOption("action");
			movingNode.add(movingActionNode);
			
			movingActionNode.add(new StringOption("low", "loglow cancel", fileWidth));
			movingActionNode.add(new StringOption("med", "logmed cancel", fileWidth));
			movingActionNode.add(new StringOption("high", "loghigh cancel", fileWidth));
		}
		
		ParentOption airbuildNode = new ParentOption("airbuild");
		root.add(airbuildNode);
		
		{
			ParentOption airbuildLimitsNode = new ParentOption("limits");
			airbuildNode.add(airbuildLimitsNode);

			airbuildLimitsNode.add(new IntegerOption("low", 30, numberWidth));
			airbuildLimitsNode.add(new IntegerOption("med", 45, numberWidth));
			airbuildLimitsNode.add(new IntegerOption("high", 60, numberWidth));

			ParentOption airbuildActionNode = new ParentOption("action");
			airbuildNode.add(airbuildActionNode);

			airbuildActionNode.add(new StringOption("low", "loglow cancel", fileWidth));
			airbuildActionNode.add(new StringOption("med", "logmed cancel", fileWidth));
			airbuildActionNode.add(new StringOption("high", "loghigh cancel", fileWidth));
		}
		
		ParentOption bedteleportNode = new ParentOption("bedteleport");
		root.add(bedteleportNode);
		
		bedteleportNode.add(new EmptyOption());
		
		ParentOption itemdupeNode = new ParentOption("itemdupe");
		root.add(itemdupeNode);
		
		itemdupeNode.add(new EmptyOption());
		
		ParentOption bogusitemsNode = new ParentOption("bogusitems");
		root.add(bogusitemsNode);
		
		bogusitemsNode.add(new EmptyOption());
		
		JButton b = new JButton("TEST");

		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String s = parseParent(root, "");

				JOptionPane.showMessageDialog(null, s);
			} });


		inside.add(b);
		
		inside.doLayout();
		
		this.pack();
	}

	private String parseParent(ParentOption option, String prefix) {

		String s = "";
		if(option.getIdentifier().length() > 0) {
			s += prefix + option.getIdentifier() + ":\r\n";
		}

		for(Option o : option.getChildOptions()) {
			if(o instanceof ChildOption) 
				s += parseChild((ChildOption)o, prefix + pre);
			else if(o instanceof ParentOption)
				s += parseParent((ParentOption)o, prefix + pre);
			else
				parse(o, prefix + "  ");
		}

		return s;
	}

	private String parseChild(ChildOption option, String prefix) {

		return prefix + option.getIdentifier() + ": " + option.getValue() + "\r\n";
	}

	private String parse(Object option, String prefix) {

		return "";
	}
}
