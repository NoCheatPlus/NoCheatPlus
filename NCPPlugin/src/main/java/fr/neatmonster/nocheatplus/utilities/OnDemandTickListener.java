package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.components.TickListener;

/**
 * Auxiliary class for easier short term adding of TickListener. Override delegateTick.<br>
 * NOTES:
 * <li>The methods in this class are not thread-safe, despite partly delegating to thread-safe methods from TickTask.</li>
 * <li>Registering listeners while the TickTask is locked will fail, check isRegistered after calling register if that is important. Should only be the case while NCP is not enabled or not yet enabling.</li>
 * @author mc_dev
 *
 */
public abstract class OnDemandTickListener implements TickListener{
	
	protected boolean isRegistered = false;
	
	/**
	 * Override this to get called on a tick.
	 * @param tick See: TickListener.onTick
	 * @param timeLast See: TickListener.onTick
	 * @return true to stay registered, false to unregister.
	 */
	public abstract boolean delegateTick(final int tick, final long timeLast);

	@Override
	public void onTick(final int tick, final long timeLast) {
		if (!isRegistered){
			// Could happen due to concurrency.
			// (No extra unregister, to preserve order).
			return;
		}
		else if (!delegateTick(tick, timeLast)){
			// Remove from TickListenerS.
			unRegister();
		}
	}
	
	/**
	 * Register with TickTask, does check the isRegistered flag.
	 * @return This instance for chaining.
	 */
	public OnDemandTickListener register(){
		return register(false);
	}
	
	/**
	 * Register with the TickTask if force is true or if isRegistered is false.
	 * @param force Set to true to call TickTask.addTickListener.
	 * @return
	 */
	public OnDemandTickListener register(final boolean force){
		if (force || !isRegistered){
			// Flag is set in the TickTask.
			TickTask.addTickListener(this);
		}
		return this;
	}
	
	/**
	 * Unregister from TickTask, does check the isRegistered flag.
	 * @return This instance for chaining.
	 */
	public OnDemandTickListener unRegister(){
		return unRegister(false);
	}
	
	/**
	 * Unregister from TickTask, if force is true or isRegistered is true. 
	 * @param force
	 * @return This instance for chaining.
	 */
	public OnDemandTickListener unRegister(final boolean force){
		if (force || isRegistered){
			// Flag is set in the TickTask.
			TickTask.removeTickListener(this);
		}
		return this;
	}
	
	/**
	 * A way to set isRegistered without causing any further calls to TickTask (for call from TickTask itself).<br>
	 * This must not cause any calls that use the TickListener registry of the TickTask (deadlocks / concurrent modification etc.).<br>
	 * Used by the TickTask, called under lock of TickListenerS.
	 * @param registered
	 */
	public void setRegistered(final boolean registered){
		isRegistered = registered;
	}
	
	/**
	 * Test if this instance has been registered with the TickTask.
	 * @return
	 */
	public boolean isRegistered(){
		return isRegistered;
	}

}
