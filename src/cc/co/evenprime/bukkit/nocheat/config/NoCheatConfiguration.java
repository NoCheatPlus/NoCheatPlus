package cc.co.evenprime.bukkit.nocheat.config;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;

public class NoCheatConfiguration extends YamlConfiguration {

    private ActionFactory factory;

    @Override
    public String saveToString() {
        // Some reflection wizardry to avoid having a lot of 
        // linebreaks in the yml file, and get a "footer" into the file
        try {
            Field op;
            op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            op.setAccessible(true);
            DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(200);
        } catch(Exception e) {}

        String result = super.saveToString();

        return result;
    }

    public static void writeInstructions(File rootConfigFolder) {
        InputStream fis = NoCheatConfiguration.class.getClassLoader().getResourceAsStream("Instructions.txt");

        StringBuffer result = new StringBuffer();
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while((i = fis.read(buf)) != -1) {
                result.append(new String(buf).substring(0, i));
            }

            File iFile = new File(rootConfigFolder, "Instructions.txt");
            if(iFile.exists()) {
                iFile.delete();
            }
            FileWriter output = new FileWriter(iFile);
            String nl = System.getProperty("line.separator");
            String instructions = result.toString();
            instructions = instructions.replaceAll("\r\n", "\n");
            String lines[] = instructions.split("\n");

            for(String line : lines) {
                output.append(line);
                output.append(nl);
            }

            output.flush();
            output.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Do this after reading new data
     */
    public void regenerateActionLists() {
        factory = new ActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false));
    }

    /**
     * A convenience method to get action lists from the config
     * @param path
     * @return
     */
    public ActionList getActionList(String path) {

        String value = this.getString(path);
        return factory.createActionList(value);
    }

    /**
     * Savely store ActionLists back into the yml file
     * @param path
     * @param list
     */
    public void set(String path, ActionList list) {
        StringBuffer string = new StringBuffer();

        for(int treshold : list.getTresholds()) {
            if(treshold > 0) {
                string.append(" vl>").append(treshold);
            }
            for(Action action : list.getActions(treshold)) {
                string.append(" ").append(action);
            }
        }

        set(path, string.toString().trim());
    }
}
