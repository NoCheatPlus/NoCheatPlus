package me.neatmonster.nocheatplus.checks.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.neatmonster.nocheatplus.DataItem;

/**
 * Player specific data for the chat checks
 * 
 */
public class ChatData implements DataItem {

    // Keep track of the violation levels for the two checks
    public int       spamVL;
    public int       spamJoinsVL;
    public int       colorVL;

    // Count messages and commands
    public int       messageCount = 0;
    public int       commandCount = 0;

    // Remember when the last check time period started
    public long      spamLastTime = 0;

    // Remember the last chat message or command for logging purposes
    public String    message      = "";

    // Remember the question
    private String[] question     = null;

    public String[] getQuestion() {
        if (question != null)
            return question;
        try {
            // The URL of the .txt file containing the questions and the hashed answers
            final URL dropBox = new URL("http://dl.dropbox.com/u/34835222/NoCheatPlus_Questions.txt");

            // Reading the file
            String line;
            final List<String> lines = new ArrayList<String>();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(dropBox.openStream()));
            while ((line = reader.readLine()) != null)
                lines.add(line);
            reader.close();

            // Choosing a random question and answer
            final Random random = new Random();
            line = lines.get(random.nextInt(lines.size()));
            final String question = line.split("--")[0];
            final String answer = line.split("--")[1];

            // Save the question
            this.question = new String[] {question, answer};

            // Returning the selected question and answer
            return this.question;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
