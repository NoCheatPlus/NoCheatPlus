package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;

/**
 * Ray tracing for block coordinates with entry point offsets.
 * @author mc_dev
 *
 */
public abstract class RayTracing {
	
//	/** End point coordinates (from, to) */
//	protected double x0, y0, z0, x1, y1, z1;
	
//	/** Total distance between end points. */
//	protected double d;
	
	/** Distance per axis. */
	protected double dX, dY, dZ;
	
	/** Current block. */
	protected int blockX, blockY, blockZ;
	
	/** Offset within current block. */
	protected double oX, oY, oZ;
	
	/** Current "time" in [0..1]. */
	protected double t = Double.MIN_VALUE;
	
	/** Tolerance for time, for checking the abort condition: 1.0 - t <= tol . */
	protected double tol = 0.0;
	
	public RayTracing(double x0, double y0, double z0, double x1, double y1, double z1){
		set(x0, y0, z0, x1, y1, z1);
	}
	
	public RayTracing(){
		set(0, 0, 0, 0, 0, 0);
	}
	
	public void set(double x0, double y0, double z0, double x1, double y1, double z1){
//		// TODO: Consider not using end-points at all.
//		this.x0 = x0;
//		this.y0 = y0;
//		this.z0 = z0;
//		this.x1 = x1;
//		this.y1 = y1;
//		this.z1 = z1;
//		// Set the "runtime" info.
//		d = CheckUtils.distance(x0, y0, z0, x1, y1, z1);
		dX = x1 - x0;
		dY = y1 - y0;
		dZ = z1 - z0;
		blockX = Location.locToBlock(x0);
		blockY = Location.locToBlock(y0);
		blockZ = Location.locToBlock(z0);
		oX = (double) (x0 - blockX);
		oY = (double) (y0 - blockY);
		oZ = (double) (z0 - blockZ);
		t = 0.0;
	}
	
	private static final double tDiff(final double dTotal, final double offset){
		if (dTotal > 0.0){
			return (1 - offset) / dTotal; 
		}
		else if (dTotal < 0.0){
			return offset / -dTotal; 
		}
		else{
			return Double.MAX_VALUE;
		}
	}
	
	/**
	 * Loop through blocks.
	 */
	public void loop(){
		// TODO: Might intercept 0 dist ?
		
		// Time to block edge.
		double tX, tY, tZ, tMin;
		while (1.0 - t > tol){
			// Determine smallest time to block edge.
			tX = tDiff(dX, oX);
			tY = tDiff(dY, oY);
			tZ = tDiff(dZ, oZ);
			tMin = Math.min(tX,  Math.min(tY, tZ));
			if (tMin == Double.MAX_VALUE || t + tMin > 1.0) tMin = 1.0 - t;
			// Call step with appropriate arguments.
			if (!step(blockX, blockY, blockZ, oX, oY, oZ, tMin)) break; // || tMin == 0) break;
			if (t + tMin >= 1.0 - tol) break;
			// Advance (add to t etc.).
			// x
			oX += tMin * dX;
			if (tX == tMin){
				if (dX < 0){
					oX = 1;
					blockX --;
				}
				else{
					oX = 0;
					blockX ++;
				}
			}
			else if (oX >= 1){
				oX -= 1;
				blockX ++;
			}
			else if (oX < 0){
				oX += 1;
				blockX --;
			}
			// y
			oY += tMin * dY;
			if (tY == tMin){
				if (dY < 0){
					oY = 1;
					blockY --;
				}
				else{
					oY = 0;
					blockY ++;
				}
			}
			else if (oY >= 1){
				oY -= 1;
				blockY ++;
			}
			else if (oY < 0){
				oY += 1;
				blockY --;
			}
			// z
			oZ += tMin * dZ;
			if (tZ == tMin){
				if (dZ < 0){
					oZ = 1;
					blockZ --;
				}
				else{
					oZ = 0;
					blockZ ++;
				}
			}
			else if (oZ >= 1){
				oZ -= 1;
				blockZ ++;
			}
			else if (oZ < 0){
				oZ += 1;
				blockZ --;
			}
			t += tMin;
		}
	}
	
	/**
	 * One step in the loop.
	 * @return If to continue loop.
	 */
	protected abstract boolean step(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dT);
	
}
