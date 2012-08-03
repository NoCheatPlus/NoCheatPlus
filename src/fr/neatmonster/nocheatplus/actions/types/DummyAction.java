package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.Action;

/*
 * M""""""'YMM                                         MMP"""""""MM            dP   oo                   
 * M  mmmm. `M                                         M' .mmmm  MM            88                        
 * M  MMMMM  M dP    dP 88d8b.d8b. 88d8b.d8b. dP    dP M         `M .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMM  M 88    88 88'`88'`88 88'`88'`88 88    88 M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 
 * M  MMMM' .M 88.  .88 88  88  88 88  88  88 88.  .88 M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 
 * M       .MM `88888P' dP  dP  dP dP  dP  dP `8888P88 M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM                                     .88 MMMMMMMMMMMM                                      
 *                                             d8888P                                                    
 */
/**
 * If an action can't be parsed correctly, at least keep it stored in this form to not lose it when loading/storing the
 * configuration file.
 */
public class DummyAction extends Action {
    /** The original string used for this action definition. */
    private final String definition;

    /**
     * Instantiates a new dummy.
     * 
     * @param definition
     *            the definition
     */
    public DummyAction(final String definition) {
        super("dummyAction", 10000, 10000);
        this.definition = definition;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return definition;
    }
}
