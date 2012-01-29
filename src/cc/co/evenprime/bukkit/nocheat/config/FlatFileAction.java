package cc.co.evenprime.bukkit.nocheat.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.ConsolecommandAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.SpecialAction;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionMapper;

public class FlatFileAction {

    private final File file;

    public FlatFileAction(File file) {
        this.file = file;
    }

    public void read(ActionMapper mapper) {

        List<Action> actions = new ArrayList<Action>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while((line = reader.readLine()) != null) {
                try {
                    if(line.trim().length() > 0 && !line.startsWith("#")) {
                        actions.add(parseLine(line));
                    }
                } catch(IllegalArgumentException e) {
                    System.out.println("NoCheat: " + e.getMessage());
                }
            }
            reader.close();

        } catch(FileNotFoundException e) {
            e.printStackTrace();

        } catch(IOException e) {
            e.printStackTrace();
        }

        for(Action a : actions) {
            mapper.addAction(a);
        }

    }

    private Action parseLine(String line) {

        // Split the line into some parts
        String parts[] = line.split("\\s+", 3);

        // four pieces is the minimum we need, no matter what it is
        if(parts.length < 3) {
            throw new IllegalArgumentException("The line " + line + " of the file " + file.getName() + " is malformed. It has not enough parts.");
        }

        String type = parts[0];
        String name = parts[1];
        String message = parts[2];

        if(type.equalsIgnoreCase("log")) {
            // A log action, it seems
            return new LogAction(name, message);
        } else if(type.equalsIgnoreCase("consolecommand")) {
            // A consolecommand action, it seems
            return new ConsolecommandAction(name, message);
        } else if(type.equalsIgnoreCase("special")) {
            return new SpecialAction(name, message);
        } else {
            throw new IllegalArgumentException("Unknown action type " + type + " of action with name " + name + ".");
        }
    }
}
