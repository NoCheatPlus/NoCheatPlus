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
        String parts[] = line.split("\\s+", 5);

        // four pieces is the minimum we need, no matter what it is
        if(parts.length < 4) {
            throw new IllegalArgumentException("The line " + line + " of the file " + file.getName() + " is malformed. It has not enough parts.");
        }

        String type = parts[0];
        String name = parts[1];

        int delay = 0;
        try {
            delay = Integer.parseInt(parts[2]);
        } catch(Exception e) {
            throw new IllegalArgumentException("Couldn't parse third parameter of action " + name + " from file " + file.getName() + ". It is " + parts[2] + " but should be a number.");
        }

        int repeat = 0;
        try {
            repeat = Integer.parseInt(parts[3]);
        } catch(Exception e) {
            throw new IllegalArgumentException("Couldn't parse fourth parameter of action " + name + " from file " + file.getName() + ". It is " + parts[2] + " but should be a number.");
        }

        if(type.equalsIgnoreCase("log")) {
            // A log action, it seems
            if(parts.length < 5) {
                throw new IllegalArgumentException("Missing fifth parameter of action " + name + " from file " + file.getName() + ".");
            }

            return new LogAction(name, delay, repeat, parts[4]);
        } else if(type.equalsIgnoreCase("consolecommand")) {
            // A consolecommand action, it seems
            if(parts.length < 5) {
                throw new IllegalArgumentException("Missing fifth parameter of action " + name + " from file " + file.getName() + ".");
            }

            return new ConsolecommandAction(name, delay, repeat, parts[4]);
        } else if(type.equalsIgnoreCase("special")) {
            // A "special" actions, it seems
            return new SpecialAction(name, delay, repeat);
        } else {
            throw new IllegalArgumentException("Unknown action type " + type + " of action with name " + name + ".");
        }
    }
}
