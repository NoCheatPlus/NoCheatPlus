package fr.neatmonster.nocheatplus.utilities;

import java.lang.reflect.Method;

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
	
	/**
	 * Dirty method. Does try.catch and return null for method invocation.
	 * @param obj
	 * @param methodName
	 * @param arg
	 * @return
	 */
	public static Object invokeGenericMethodOneArg(final Object obj, final String methodName, final Object arg){
		// TODO: Isn't there a one-line-call for this ??
		final Class<?> objClass = obj.getClass();
		final Class<?> argClass = arg.getClass();
		// Collect methods that might work.
		Method methodFound = null;
		boolean denyObject = false;
		for (final Method method : objClass.getDeclaredMethods()){
			if (method.getName().equals(methodName)){
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 1 ){
					// Prevent using Object as argument if there exists a method with a specialized argument.
					if (parameterTypes[0] != Object.class && !parameterTypes[0].isAssignableFrom(argClass)){
						denyObject = true;
					}
					// Override the found method if none found yet and assignment is possible, or if it has a specialized argument of an already found one.
					if ((methodFound == null && parameterTypes[0].isAssignableFrom(argClass) || methodFound != null && methodFound.getParameterTypes()[0].isAssignableFrom(parameterTypes[0]))){
						methodFound = method;
					}
				}
			}
		}
		if (denyObject && methodFound.getParameterTypes()[0] == Object.class){
			// TODO: Throw something !?
			return null;
		}
		else if (methodFound != null && methodFound.getParameterTypes()[0].isAssignableFrom(argClass)){
			try{
				final Object res = methodFound.invoke(obj, arg);
				return res;
			}
			catch (Throwable t){
				// TODO: Throw something !?
				return null;
			}
		}
		else{
			// TODO: Throw something !?
			return null;
		}
	}
	
	/**
	 * 
	 * @param obj
	 * @param methodName
	 * @param returnTypePreference This is  comparison with ==, no isAssignableForm. TODO: really ?
	 * @return
	 */
	public static Object invokeMethodVoid(final Object obj, final String methodName, final Class<?> ...  returnTypePreference){
		// TODO: Isn't there a one-line-call for this ??
		final Class<?> objClass = obj.getClass();
		
		// Collect methods that might work.
		Method methodFound = null;
		int returnTypeIndex = returnTypePreference.length; // This can be 0 for no preferences given.
		for (final Method method : objClass.getDeclaredMethods()){
			if (method.getName().equals(methodName)){
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 0){
					// Override the found method if none found yet or if the return type matches the preferred policy.
					final Class<?> returnType = method.getReturnType();
					if (methodFound == null){
						methodFound = method;
						for (int i = 0; i < returnTypeIndex; i++){
							if (returnTypePreference[i] == returnType){
								returnTypeIndex = i;
								break;
							}
						}
					}
					else{
						// Check if the return type is preferred over previously found ones.
						for (int i = 0; i < returnTypeIndex; i++){
							if (returnTypePreference[i] == returnType){
								methodFound = method;
								returnTypeIndex = i;
								break;
							}
						}
					}
					if (returnTypeIndex == 0){
						// "Quick" return.
						break;
					}
				}
			}
		}
		if (methodFound != null){
			try{
				final Object res = methodFound.invoke(obj);
				return res;
			}
			catch (Throwable t){
				// TODO: Throw something !?
				return null;
			}
		}
		else{
			// TODO: Throw something !?
			return null;
		}
	}
	
}
