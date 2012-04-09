package me.neatmonster.nocheatplus.checks.chat;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;

public class SpamJoinsCheck extends ChatCheck {

    // Used to know if the cooldown is enabled and since when
    private boolean cooldown          = false;
    private long    cooldownStartTime = 0L;

    // Used to remember the latest joins;
    private long[]  joins             = null;

    public SpamJoinsCheck(final NoCheatPlus plugin) {
        super(plugin, "chat.spamjoins");
    }

    public boolean check(final NoCheatPlusPlayer player, final ChatData data, final ChatConfig cc) {

        // Initialize the joins array
        if (joins == null)
            joins = new long[cc.spamJoinsPlayersLimit];

        boolean kick = false;

        // If the new players cooldown is over
        if (cooldown && System.currentTimeMillis() - cooldownStartTime > cc.spamJoinsCooldown) {
            // Stop the new players cooldown
            cooldown = false;
            cooldownStartTime = 0L;
        }

        // If the new players cooldown is active
        else if (cooldown)
            // Kick the player who joined
            kick = true;

        // If more than limit new players have joined in less than limit time
        else if (System.currentTimeMillis() - joins[0] < cc.spamJoinsTimeLimit) {
            // Enable the new players cooldown
            cooldown = true;
            cooldownStartTime = System.currentTimeMillis();
            // Kick the player who joined
            kick = true;
        }

        // Fill the joining times array
        for (int i = 0; i < cc.spamJoinsPlayersLimit - 1; i++)
            joins[i] = joins[i + 1];
        joins[cc.spamJoinsPlayersLimit - 1] = System.currentTimeMillis();

        return kick;
    }
}
