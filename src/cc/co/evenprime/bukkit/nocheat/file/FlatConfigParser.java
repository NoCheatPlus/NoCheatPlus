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
 * An extremely simple flat config file parser
 * 
 * @author Evenprime
 * 
 */
public class FlatConfigParser {

    private final Map<String, Object> root;

    public FlatConfigParser() {

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
            } else {
                lines.removeFirst();
                if(line.contains("=")) {
                    String pair[] = line.split("=", 2);
                    putString(pair[0].trim(), root, removeQuotationMarks(pair[1].trim()));
                }
            }
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

    @SuppressWarnings("unchecked")
    private static void putString(String path, Map<String, Object> node, String property) {
        String[] pathParts = path.split("\\.");
        for(int i = 0; i < pathParts.length; i++) {
            if(i == pathParts.length - 1) { // last in the chain
                node.put(pathParts[i], property);
            } else if(node.containsKey(pathParts[i]) && node.get(pathParts[i]) instanceof Map) {
                node = (Map<String, Object>) node.get(pathParts[i]);
            } else {
                HashMap<String, Object> newMap = new HashMap<String, Object>();
                node.put(pathParts[i], newMap);
                node = newMap;
            }
        }
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
