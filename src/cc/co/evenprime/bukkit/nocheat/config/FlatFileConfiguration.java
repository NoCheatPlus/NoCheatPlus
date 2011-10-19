package cc.co.evenprime.bukkit.nocheat.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import cc.co.evenprime.bukkit.nocheat.actions.types.Action;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionMapper;
import cc.co.evenprime.bukkit.nocheat.config.util.OptionNode;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

public class FlatFileConfiguration extends Configuration {

    private final File file;

    public FlatFileConfiguration(Configuration defaults, boolean copyDefaults, File file) {
        super(defaults, copyDefaults);

        this.file = file;
    }

    public void load(ActionMapper action) throws IOException {

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

        String line = null;

        while((line = r.readLine()) != null) {
            parse(line, action);
        }

        r.close();

    }

    private void parse(String line, ActionMapper action) throws IOException {

        line = line.trim();

        // Is it a key/value pair?
        if(line.startsWith("#") || !line.contains("=")) {
            return;
        }

        String pair[] = line.split("=", 2);

        String key = pair[0].trim();
        String value = pair[1].trim();

        // Find out which option we have in front of us
        OptionNode node = getOptionNodeForString(ROOT, key);

        if(node == null) {
            return;
        }

        switch (node.getType()) {
        case ACTIONLIST:
            this.set(node, parseActionList(node, key, removeQuotationMarks(value), action));
            break;
        case STRING:
            this.set(node, removeQuotationMarks(value));
            break;
        case INTEGER:
            this.set(node, Integer.valueOf(removeQuotationMarks(value)));
            break;
        case LOGLEVEL:
            this.set(node, LogLevel.getLogLevelFromString(removeQuotationMarks(value)));
            break;
        case BOOLEAN:
            this.set(node, Boolean.valueOf(removeQuotationMarks(value)));
            break;
        default:
            throw new IllegalArgumentException("Unknown node type " + node.getType());
        }
    }

    private ActionList parseActionList(OptionNode node, String key, String value, ActionMapper action) {

        String[] s = key.split("\\.");
        String treshold = s[s.length - 1];

        // See if we already got that actionlist created
        ActionList al = (ActionList) this.get(node);

        if(al == null || al == this.getDefaults().getRecursive(node)) {
            al = new ActionList();
        }
        int th = Integer.parseInt(treshold);
        al.setActions(th, action.getActions(value.split("\\s+")));

        return al;
    }

    private OptionNode getOptionNodeForString(OptionNode root, String key) {
        String parts[] = key.split("\\.", 2);

        for(OptionNode node : root.getChildren()) {
            // Found the correct node?
            if(node.getName().equals(parts[0])) {
                if(node.isLeaf()) {
                    return node;
                } else {
                    return getOptionNodeForString(node, parts[1]);
                }
            }
        }

        return null;
    }

    public void save() {

        try {
            if(file.getParentFile() != null)
                file.getParentFile().mkdirs();

            file.createNewFile();
            BufferedWriter w = new BufferedWriter(new FileWriter(file));

            w.write("# Want to know what these options do? Read at the end of this file.\r\n");

            saveRecursive(w, ROOT);

            w.write("\r\n\r\n");

            saveDescriptionsRecursive(w, ROOT);

            w.flush();
            w.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDescriptionsRecursive(BufferedWriter w, OptionNode node) throws IOException {
        if(!node.isLeaf()) {
            for(OptionNode o : node.getChildren()) {
                saveDescriptionsRecursive(w, o);
            }
        }

        // Save a leaf node, if it's really stored here
        Object object = this.get(node);

        if(object == null) {
            return;
        }

        // Get the full id of the node
        String id = node.getName();
        OptionNode i = node;

        while((i = i.getParent()) != null && i.getName() != null && i.getName().length() > 0) {
            id = i.getName() + "." + id;
        }

        w.write("\r\n\r\n# " + id + ":\r\n#\r\n");

        String explaination = Explainations.get(node);

        w.write("#    " + explaination.replaceAll("\n", "\r\n#    "));
    }

    private void saveRecursive(BufferedWriter w, OptionNode node) throws IOException {

        if(!node.isLeaf()) {

            for(OptionNode o : node.getChildren()) {

                if(node == ROOT) {
                    w.write("\r\n");
                }

                saveRecursive(w, o);
            }

            return;
        } else {
            saveLeaf(w, node);
        }

    }

    private void saveLeaf(BufferedWriter w, OptionNode node) throws IOException {
        // Save a leaf node, if it's really stored here
        Object object = this.get(node);

        if(object == null) {
            return;
        }
        // Get the full id of the node
        String id = node.getName();
        OptionNode i = node;

        while((i = i.getParent()) != null && i.getName() != null && i.getName().length() > 0) {
            id = i.getName() + "." + id;
        }

        switch (node.getType()) {
        case ACTIONLIST:
            saveActionList(w, id, (ActionList) object);
            break;
        case STRING:
            saveString(w, id, (String) object);
            break;
        case INTEGER:
            saveValue(w, id, object.toString());
            break;
        case LOGLEVEL:
            saveValue(w, id, object.toString());
            break;
        case BOOLEAN:
            saveValue(w, id, object.toString());
            break;
        default:
            throw new IllegalArgumentException("Unknown node type " + node.getType());
        }
    }

    private void saveActionList(BufferedWriter w, String id, ActionList actionList) throws IOException {
        for(Integer treshold : actionList.getTresholds()) {
            StringBuilder s = new StringBuilder("");
            for(Action s2 : actionList.getActions(treshold)) {
                s.append(" ").append(s2.name);
            }
            saveValue(w, id + "." + treshold, s.toString().trim());
        }

    }

    private void saveString(BufferedWriter w, String id, String value) throws IOException {
        saveValue(w, id, addQuotationMarks(value));
    }

    private void saveValue(BufferedWriter w, String id, String value) throws IOException {
        w.write(id + " = " + value + "\r\n");
    }

    private String removeQuotationMarks(String s) {

        s = s.trim();

        if(s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        } else if(s.startsWith("\'") && s.endsWith("\'")) {
            return s.substring(1, s.length() - 1);
        }

        return s;
    }

    private String addQuotationMarks(String s) {

        return "\"" + s + "\"";
    }
}
