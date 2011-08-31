package cc.co.evenprime.bukkit.nocheat.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * An extremely simple YAML config parser
 * 
 * @author Evenprime
 * 
 */
public class YamlConfigParser {

    private final String              prefix = "    ";

    private final Map<String, Object> root;

    public YamlConfigParser() {

        root = new HashMap<String, Object>();
    }

    public void read(File file) throws IOException {

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

        LinkedList<String> lines = new LinkedList<String>();

        String line = null;

        while((line = r.readLine()) != null) {
            lines.add(line);
        }

        r.close();

        parse(root, lines, "");

    }

    private void parse(Map<String, Object> root, LinkedList<String> lines, String prefix) throws IOException {

        String line = null;

        while(!lines.isEmpty()) {
            line = lines.getFirst();
            if(line.trim().startsWith("#")) {
                lines.removeFirst();
            } else if(line.trim().isEmpty()) {
                lines.removeFirst();
            } else if(line.startsWith(prefix)) {
                lines.removeFirst();
                if(line.contains(":")) {
                    String pair[] = line.split(":", 2);
                    if(pair[1].trim().isEmpty()) {
                        Map<String, Object> m = new HashMap<String, Object>();
                        parse(m, lines, prefix + this.prefix);
                        root.put(pair[0].trim(), m);
                    } else {
                        root.put(pair[0].trim(), removeQuotationMarks(pair[1].trim()));
                    }
                }
            } else
                break;
        }
    }

    private static String removeQuotationMarks(String s) {
        if(s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        } else if(s.startsWith("\'") && s.endsWith("\'")) {
            return s.substring(1, s.length() - 1);
        }

        return s;

    }

    /* Convenience methods for retrieving values start here */

    public Object getProperty(String path) {
        return getProperty(path, root);
    }

    @SuppressWarnings("unchecked")
    private static Object getProperty(String path, Map<String, Object> node) {

        if(node == null) {
            return null;
        }

        if(!path.contains(".")) {
            return node.get(path);
        } else {
            String[] parts = path.split("\\.", 2);
            return getProperty(parts[1], (Map<String, Object>) node.get(parts[0]));
        }
    }

    public String getString(String path, String defaultValue) {
        return getString(path, defaultValue, root);
    }

    private static String getString(String path, String defaultValue, Map<String, Object> node) {
        try {
            String result = (String) getProperty(path, node);
            if(result == null)
                return defaultValue;
            return result;
        } catch(Exception e) {
            return defaultValue;
        }
    }
}
