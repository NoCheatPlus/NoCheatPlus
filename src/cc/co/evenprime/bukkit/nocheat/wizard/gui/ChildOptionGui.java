package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import javax.swing.JPanel;

/**
 * 
 * @author Evenprime
 * 
 */
public abstract class ChildOptionGui extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = -8647116398870984111L;

    private boolean           active           = true;

    public ChildOptionGui(boolean isActive) {
        this.active = isActive;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
