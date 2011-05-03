package cc.co.evenprime.bukkit.nocheat.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cc.co.evenprime.bukkit.nocheat.wizard.gui.ParentOptionGui;
import cc.co.evenprime.bukkit.nocheat.wizard.options.BooleanOption;
import cc.co.evenprime.bukkit.nocheat.wizard.options.ChildOption;
import cc.co.evenprime.bukkit.nocheat.wizard.options.IntegerOption;
import cc.co.evenprime.bukkit.nocheat.wizard.options.LogLevelOption;
import cc.co.evenprime.bukkit.nocheat.wizard.options.MediumStringOption;
import cc.co.evenprime.bukkit.nocheat.wizard.options.Option;
import cc.co.evenprime.bukkit.nocheat.wizard.options.ParentOption;
import cc.co.evenprime.bukkit.nocheat.wizard.options.LongStringOption;
import cc.co.evenprime.bukkit.nocheat.wizard.options.ShortStringOption;

public class Wizard extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8798111079958779207L;

	private static final String pre = "    ";
	
	public Wizard() {
		
		JScrollPane scrollable = new JScrollPane();

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setContentPane(scrollable);
		
		JPanel inside = new JPanel();
		scrollable.setViewportView(inside);
		
		inside.setLayout(new BoxLayout(inside,BoxLayout.Y_AXIS));
		
		final ParentOption root = new ParentOption("");
		
		ParentOption loggingNode = new ParentOption("logging");
		root.add(loggingNode);

		loggingNode.add(new MediumStringOption("filename", "plugins/NoCheat/nocheat.log"));
		loggingNode.add(new LogLevelOption("logtofile", LogLevelOption.Options.LOW));
		loggingNode.add(new LogLevelOption("logtoconsole", LogLevelOption.Options.HIGH));
		loggingNode.add(new LogLevelOption("logtochat", LogLevelOption.Options.MED));
		loggingNode.add(new LogLevelOption("logtoirc", LogLevelOption.Options.MED));
		loggingNode.add(new ShortStringOption("logtoirctag", "nocheat"));

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

		speedhackNode.add(new LongStringOption("logmessage", "\"%1$s sent %2$d move events, but only %3$d were allowed. Speedhack?\""));

		{
			ParentOption speedhackLimitsNode = new ParentOption("limits");
			speedhackNode.add(speedhackLimitsNode);

			speedhackLimitsNode.add(new IntegerOption("low", 30));
			speedhackLimitsNode.add(new IntegerOption("med", 45));
			speedhackLimitsNode.add(new IntegerOption("high", 60));

			ParentOption speedhackActionNode = new ParentOption("action");
			speedhackNode.add(speedhackActionNode);

			speedhackActionNode.add(new MediumStringOption("low", "loglow cancel"));
			speedhackActionNode.add(new MediumStringOption("med", "logmed cancel"));
			speedhackActionNode.add(new MediumStringOption("high", "loghigh cancel"));
		}
		
		ParentOption movingNode = new ParentOption("moving");
		root.add(movingNode);
		
		movingNode.add(new LongStringOption("logmessage", "\"Moving violation: %1$s from %2$s (%4$.1f, %5$.1f, %6$.1f) to %3$s (%7$.1f, %8$.1f, %9$.1f)\""));
		movingNode.add(new LongStringOption("summarymessage", "\"Moving summary of last ~%2$d seconds: %1$s total Violations: (%3$d,%4$d,%5$d)\""));
		movingNode.add(new BooleanOption("allowflying", false));
		movingNode.add(new BooleanOption("allowfakesneak", true));
		
		{
			ParentOption movingActionNode = new ParentOption("action");
			movingNode.add(movingActionNode);
			
			movingActionNode.add(new MediumStringOption("low", "loglow cancel"));
			movingActionNode.add(new MediumStringOption("med", "logmed cancel"));
			movingActionNode.add(new MediumStringOption("high", "loghigh cancel"));
		}
		
		ParentOption airbuildNode = new ParentOption("airbuild");
		root.add(airbuildNode);
		
		{
			ParentOption airbuildLimitsNode = new ParentOption("limits");
			airbuildNode.add(airbuildLimitsNode);

			airbuildLimitsNode.add(new IntegerOption("low", 30));
			airbuildLimitsNode.add(new IntegerOption("med", 45));
			airbuildLimitsNode.add(new IntegerOption("high", 60));

			ParentOption airbuildActionNode = new ParentOption("action");
			airbuildNode.add(airbuildActionNode);

			airbuildActionNode.add(new MediumStringOption("low", "loglow cancel"));
			airbuildActionNode.add(new MediumStringOption("med", "logmed cancel"));
			airbuildActionNode.add(new MediumStringOption("high", "loghigh cancel"));
		}
		
		ParentOption bedteleportNode = new ParentOption("bedteleport");
		root.add(bedteleportNode);
		
		
		ParentOption itemdupeNode = new ParentOption("itemdupe");
		root.add(itemdupeNode);
		
		
		ParentOption bogusitemsNode = new ParentOption("bogusitems");
		root.add(bogusitemsNode);
		
		ParentOptionGui root2 = new ParentOptionGui(root);
		
		inside.add(root2);
		
		JButton b = new JButton("TEST");

		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String s = parseParent(root, "");

				JOptionPane.showMessageDialog(null, s);
			} });

		b.setAlignmentY(0.0F);
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
