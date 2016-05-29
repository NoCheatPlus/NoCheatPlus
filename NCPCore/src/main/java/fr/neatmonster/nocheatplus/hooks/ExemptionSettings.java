package fr.neatmonster.nocheatplus.hooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.NPC;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Encapsulate generic settings and checking functionality for exemption.
 * 
 * @author asofold
 *
 */
public class ExemptionSettings {

    /**
     * Check for meta data keys existence, allowing for multiple values to check
     * for.
     * 
     * @author asofold
     *
     */
    public static final class MetaDataListCheck {

        private static String[] getKeys(final Collection<String> keys) {
            if (keys == null) {
                return null;
            }
            else {
                final List<String> notNull = new ArrayList<String>(keys.size());
                for (final String key : keys) {
                    if (key != null) {
                        notNull.add(key);
                    }
                }
                if (notNull.isEmpty()) {
                    return null;
                }
                else {
                    return notNull.toArray(new String[notNull.size()]);
                }
            }
        }

        private final String[] metaDataKeys;

        public MetaDataListCheck(final ConfigFile config, final String pathActive, final String pathKeys) {
            this(config.getBoolean(pathActive) ? config.getStringList(pathKeys) : null);
        }

        public MetaDataListCheck(final Collection<String> keys) {
            this.metaDataKeys = getKeys(keys);
        }

        public boolean hasAnyMetaDataKey(final Entity entity) {
            if (metaDataKeys == null) {
                return false;
            }
            else {
                for (int i = 0; i < metaDataKeys.length; i++) {
                    if (entity.hasMetadata(metaDataKeys[i])) {
                        return true;
                    }
                }
                return false;
            }
        }

    }

    /** Default meta data check for exemption, not null. */
    public final MetaDataListCheck defaultMetaData;

    /** Always exempt NPCs from all checks. */
    public final boolean npcWildCardExempt;

    /**
     * Check for the Bukkit interface or not, in order to detect if a player is
     * an NPC.
     */
    public final boolean npcBukkitInterface;

    /**
     * Use meta data to check, if a player is an NPC, not null.
     */
    public final MetaDataListCheck npcMetaData;

    /**
     * Default constructor, containint the following settings:
     * <ul>
     * <li>Default wild card exemption by meta data key "nocheat.exempt".</li>
     * <li>Wild card exempt NPCs.</li>
     * <li>Check for the Bukkit NPC interface.</li>
     * <li>Regard entities with "NPC" meta data key as NPCs</li>
     * </ul>
     */
    public ExemptionSettings() {
        this(new MetaDataListCheck(Arrays.asList("nocheat.exempt")), 
                true, true, new MetaDataListCheck(Arrays.asList("NPC")));
    }

    /**
     * Read the settings from the given configuration file, using default paths,
     * assuming DefaultConfig as defaults.
     * 
     * @param config
     */
    public ExemptionSettings(final ConfigFile config) {
        this(
                new MetaDataListCheck(config, ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_DEFAULT_METADATA_ACTIVE, 
                        ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_DEFAULT_METADATA_KEYS), 
                config.getBoolean(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_ACTIVE), 
                config.getBoolean(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_BUKKITINTERFACE), 
                new MetaDataListCheck(config, ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_METADATA_ACTIVE, 
                        ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_METADATA_KEYS)
                );
    }

    /**
     * 
     * @param defaultMetaData
     *            Meta data keys to exempt for, currently servers as wild card,
     *            may be null.
     * @param npcWildCardExempt
     *            If to wild card exempt NPCs. If set to true,
     * @param npcBukkitInterface
     *            If to check for the NPC interface (Bukkit).
     * @param npcMetaData
     *            Meta data keys to exempt for, currently servers as wild card,
     *            may be null.
     */
    public ExemptionSettings(MetaDataListCheck defaultMetaData, boolean npcWildCardExempt, boolean npcBukkitInterface, MetaDataListCheck npcMetaData) {
        this.defaultMetaData = defaultMetaData == null ? new MetaDataListCheck(null) : defaultMetaData;
        this.npcWildCardExempt = npcWildCardExempt;
        this.npcBukkitInterface = npcBukkitInterface;
        this.npcMetaData = npcMetaData == null ? new MetaDataListCheck(null) : npcMetaData;
    }

    /**
     * Top level check for exemption by meta data, including NPCs. Meta data is
     * only checked if this is the primary thread (!).
     * 
     * @param entity
     * @return
     */
    public boolean isExemptedBySettings(final Entity entity) {
        return isExemptedBySettings(entity, Bukkit.isPrimaryThread());
    }

    /**
     * Test if according to this instance of settings, the player is regarded as
     * an NPC. Meta data is only checked if this is the primary thread (!).
     * 
     * @param entity
     * @param isPrimaryThread
     * @return
     */
    public boolean isExemptedBySettings(final Entity entity, final boolean isPrimaryThread) {
        return isPrimaryThread && defaultMetaData.hasAnyMetaDataKey(entity) 
                || npcWildCardExempt && isRegardedAsNpc(entity, isPrimaryThread);
    }

    /**
     * Test if according to this instance of settings, the player is regarded as
     * an NPC.Meta data is only checked if this is the primary thread (!).
     * 
     * @param entity
     * @return
     */
    public boolean isRegardedAsNpc(final Entity entity) {
        return isRegardedAsNpc(entity, Bukkit.isPrimaryThread());
    }

    /**
     * Test if according to this instance of settings, the player is regarded as
     * an NPC.Meta data is only checked if this is the primary thread (!).
     * 
     * @param entity
     * @param isPrimaryThread
     * @return
     */
    public boolean isRegardedAsNpc(final Entity entity, final boolean isPrimaryThread) {
        return npcBukkitInterface && (entity instanceof NPC) || isPrimaryThread && npcMetaData.hasAnyMetaDataKey(entity);
    }

}
