package fr.neatmonster.nocheatplus.utilities;

public class ReflectionUtil {
	
	/**
	 * Convenience method to check if members exist and fail if not.
	 * @param prefix
	 * @param specs
	 * @throws RuntimeException
	 */
	public static void checkMembers(String prefix, String[]... specs){
		try {
			for (String[] spec : specs){
				Class<?> clazz = Class.forName(prefix + spec[0]);
				for (int i = 1; i < spec.length; i++){
					if (clazz.getField(spec[i]) == null) throw new NoSuchFieldException(prefix + spec[0] + " : " + spec[i]);
				}
			}
		} catch (SecurityException e) {
			// Let this one pass.
			//throw new RuntimeException(e);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
}
