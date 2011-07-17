package cc.co.evenprime.bukkit.nocheat.data;


/**
 * per player storage for data persistence between events 
 * 
 * @author Evenprime
 *
 */
public class NoCheatData {

	/**
	 * Don't rely on any of these yet, they are likely going to change their name/functionality 
	 */
	public MovingData moving; 
	public SpeedhackData speedhack; 
	public AirbuildData airbuild;

	public PermissionData permission;
	public NukeData nuke;
	
	public NoCheatData() { }
}