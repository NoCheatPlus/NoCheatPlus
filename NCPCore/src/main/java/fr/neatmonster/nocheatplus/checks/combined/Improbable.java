/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.components.registry.feature.IDisableListener;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * This check  combines different other checks frequency and occurrecnces into one count.
 * (Intended for static access by other checks.) 
 *
 * @author mc_dev
 *
 */
public class Improbable extends Check implements IDisableListener{

    private static Improbable instance = null;

    /**
     * Return if t cancel.
     * @param player
     * @param weights
     * @param now
     * @return
     */
    public static final boolean check(final Player player, final float weight, final long now, final String tags){
        return instance.checkImprobable(player, weight, now, tags);
    }

    /**
     * Feed the check but no violations processing (convenience method).
     * @param player
     * @param weight
     * @param now
     */
    public static final void feed(final Player player, final float weight, final long now){
        CombinedData.getData(player).improbableCount.add(now, weight);
    }

    ////////////////////////////////////
    // Instance methods.
    ///////////////////////////////////

    public Improbable() {
        super(CheckType.COMBINED_IMPROBABLE);
        instance = this;
    }

    private boolean checkImprobable(final Player player, final float weight, final long now, final String tags) {
        if (!isEnabled(player)) return false;
        final CombinedData data = CombinedData.getData(player);
        final CombinedConfig cc = CombinedConfig.getConfig(player);
        data.improbableCount.add(now, weight);
        final float shortTerm = data.improbableCount.bucketScore(0);
        double violation = 0;
        boolean violated = false;
        if (shortTerm * 0.8f > cc.improbableLevel / 20.0){
            final float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration(), true) : 1f;
            if (shortTerm / lag > cc.improbableLevel / 20.0){
                violation += shortTerm * 2d / lag;
                violated = true;
            }
        }
        final double full = data.improbableCount.score(1.0f);
        if (full > cc.improbableLevel){
            final float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration() * data.improbableCount.numberOfBuckets(), true) : 1f;
            if (full / lag > cc.improbableLevel){
                violation += full / lag;
                violated = true;
            }
        }
        boolean cancel = false;
        if (violated){
            // Execute actions
            data.improbableVL += violation / 10.0;
            final ViolationData vd = new ViolationData(this, player, data.improbableVL, violation, cc.improbableActions);
            if (tags != null && !tags.isEmpty()) vd.setParameter(ParameterName.TAGS, tags);
            cancel = executeActions(vd).willCancel();
        }
        else
            data.improbableVL *= 0.95;
        return cancel;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

}
