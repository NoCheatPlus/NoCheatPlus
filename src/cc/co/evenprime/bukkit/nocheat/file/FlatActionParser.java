package cc.co.evenprime.bukkit.nocheat.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cc.co.evenprime.bukkit.nocheat.DefaultConfiguration;
import cc.co.evenprime.bukkit.nocheat.actions.ActionManager;

/**
 * Parses a action config file that was written in a flat style, one action per
 * line
 * 
 * @author Evenprime
 * 
 */
public class FlatActionParser {

    public void read(ActionManager manager, File file) {

        List<String[]> actionLines = readActionLinesFromFile(file, manager.getKnownTypes());

        for(String[] actionLine : actionLines) {
            manager.createActionFromStrings(actionLine[0], actionLine[1], actionLine[2], actionLine[3], actionLine.length > 4 ? actionLine[4] : null);
        }
    }

    private List<String[]> readActionLinesFromFile(File file, String[] knownTypes) {

        List<String[]> lines = new LinkedList<String[]>();

        if(!file.exists()) {
            DefaultConfiguration.writeActionFile(file);
            return lines;
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while((line = reader.readLine()) != null) {
                for(String s : knownTypes) {
                    if(line.startsWith(s)) {
                        // Split at whitespace characters
                        String parts[] = line.split("\\s+", 5);

                        if(parts.length < 4) {
                            System.out.println("NoCheat: Incomplete action definition found. Ignoring it: " + line);
                        } else {
                            lines.add(parts);
                        }
                    }
                }
            }
            reader.close();

        } catch(FileNotFoundException e) {
            e.printStackTrace();

        } catch(IOException e) {
            e.printStackTrace();
        }

        return lines;

    }
}
