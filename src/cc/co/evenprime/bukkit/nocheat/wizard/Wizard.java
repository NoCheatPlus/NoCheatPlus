package cc.co.evenprime.bukkit.nocheat.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cc.co.evenprime.bukkit.nocheat.DefaultConfiguration;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationTree;
import cc.co.evenprime.bukkit.nocheat.wizard.gui.ConfigurationTreeGui;

/**
 * The actual GUI for the configuration tool
 * 
 * @author Evenprime
 * 
 */
public class Wizard extends JFrame {

    public static void main(String[] args) {

        Wizard w = new Wizard();
        w.setVisible(true);
    }

    private static final long serialVersionUID = 8798111079958773207L;

    public static final Color disabled         = Color.GRAY;
    public static final Color enabled          = Color.BLACK;

    private class Wizard_JPanel extends JPanel {

        private static final long serialVersionUID = 5748088661296418403L;

        private final JPanel      inside;
        private ConfigurationTree tree;

        /**
         * Create a complete configuration screen based on a specific
         * configuration tree
         * 
         * @param tree
         */
        private Wizard_JPanel(ConfigurationTree tree) {

            JScrollPane scrollPane = new JScrollPane();
            inside = new JPanel();
            this.tree = tree;
            scrollPane.setViewportView(inside);

            this.setLayout(new BorderLayout());

            inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));

            // Recursively walk through "defaults" tree and move stuff to our
            // new real config tree, if it is not defined
            // in our real tree already
            final ConfigurationTree modelRoot = tree;

            final ConfigurationTreeGui guiRoot = new ConfigurationTreeGui(modelRoot);

            inside.add(guiRoot);
            this.add(scrollPane, BorderLayout.CENTER);
        }

        /**
         * Recreate the whole tree (e.g. in case the underlying model tree
         * changed
         */
        private void refresh() {
            this.inside.removeAll();
            this.inside.add(new ConfigurationTreeGui(tree));
        }
    }

    private final JTabbedPane                    tabs;
    private final Map<String, File>              worldFiles;
    private final Map<String, ConfigurationTree> worldTrees;

    public Wizard() {

        tabs = new JTabbedPane();
        worldFiles = ConfigurationManager.getWorldSpecificConfigFiles("NoCheat");
        worldTrees = new HashMap<String, ConfigurationTree>();

        final File globalConfig = ConfigurationManager.getGlobalConfigFile("NoCheat");
        ConfigurationTree globalTree;

        try {
            globalTree = ConfigurationManager.createFullConfigurationTree(DefaultConfiguration.buildDefaultConfigurationTree(), globalConfig);
        } catch(Exception e) {
            System.out.println("NoCheat: Couldn't use existing global config file " + globalConfig + ", creating a new file.");
            globalTree = DefaultConfiguration.buildDefaultConfigurationTree();
        }

        worldTrees.put(null, globalTree);

        for(String worldName : worldFiles.keySet()) {
            ConfigurationTree worldTree;
            try {
                worldTree = ConfigurationManager.createPartialConfigurationTree(globalTree, worldFiles.get(worldName));
                worldTrees.put(worldName, worldTree);
            } catch(Exception e) {
                System.out.println("NoCheat: Couldn't read existing world-specific config file for world " + worldName);
                worldFiles.remove(worldName);
            }
        }

        worldFiles.put(null, globalConfig);

        setup();
    }

    private void setup() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());

        JButton saveAllButton = new JButton("Save All");

        tabs.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                ((Wizard_JPanel) tabs.getSelectedComponent()).refresh();
            }
        });

        Wizard_JPanel global = new Wizard_JPanel(worldTrees.get(null));
        tabs.addTab("Global Settings", null, global, "The settings valid for all worlds, unless a specific setting overrides them.");

        for(String name : worldTrees.keySet()) {
            if(name != null) {
                Wizard_JPanel world = new Wizard_JPanel(worldTrees.get(name));
                tabs.addTab(name + " Settings", null, world, "Some world-specific settings.");
            }
        }

        saveAllButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                for(String worldName : worldTrees.keySet()) {
                    ConfigurationManager.writeConfigFile(worldFiles.get(worldName), worldTrees.get(worldName));
                }

                ConfigurationManager.writeDescriptionFile(ConfigurationManager.getDescriptionFile("NoCheat"), worldTrees.get(null));

                DefaultConfiguration.writeDefaultActionFile(ConfigurationManager.getDefaultActionFile("NoCheat"));

                DefaultConfiguration.writeActionFile(ConfigurationManager.getActionFile("NoCheat"));

                JOptionPane.showMessageDialog(null, "Saved All");
            }
        });

        saveAllButton.setAlignmentY(0.0F);
        this.add(saveAllButton, BorderLayout.SOUTH);
        this.pack();
        this.setSize(1000, 700);
        this.setTitle("NoCheat configuration utility");

        this.add(tabs, BorderLayout.CENTER);

    }
}
