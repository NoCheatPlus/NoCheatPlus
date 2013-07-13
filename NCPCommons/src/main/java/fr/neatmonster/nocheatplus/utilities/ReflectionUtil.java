package fr.neatmonster.nocheatplus.utilities;

import java.lang.reflect.Method;

public class ReflectionUtil {
	
	/**
	 * Convenience method to check if members exist and fail if not. This checks getField(...) == null.
	 * @param prefix
	 * @param specs
	 * @throws RuntimeException If any member is not present.
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
	 * Check for the given names if the method returns the desired type of result (exact check).
	 * @param methodNames
	 * @param returnType
	 * @throws RuntimeException If one method is not existing or not matching return type or has arguments.
	 */
	public static void checkMethodReturnTypesNoArgs(Class<?> objClass, String[] methodNames, Class<?> returnType){
		// TODO: Add check: boolean isStatic.
		try {
			for (String methodName : methodNames){
				Method m = objClass.getMethod(methodName);
				if (m.getParameterTypes().length != 0){
					throw new RuntimeException("Expect method without arguments for " + objClass.getName() + "." + methodName);
				}
				if (m.getReturnType() != returnType){
					throw new RuntimeException("Wrong return type for: " + objClass.getName() + "." + methodName);
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
	 * Dirty method to call a declared method with a generic parameter type. Does try+catch for method invocation and should not throw anything for the normal case. Purpose for this is generic factory registration, having methods with type Object alongside methods with more specialized types.
	 * @param obj
	 * @param methodName
	 * @param arg Argument or invoke the method with.
	 * @return null in case of errors (can not be distinguished).
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
	 * Invoke a method without arguments, get the method matching the return types best, i.e. first type is preferred. At present a result is returned, even if the return type does not match at all.
	 * @param obj
	 * @param methodName
	 * @param returnTypePreference Most preferred return type first, might return null, might return a method with a completely different return type, comparison with ==, no isAssignableForm. TODO: really ?
	 * @return
	 */
	public static Object invokeMethodNoArgs(final Object obj, final String methodName, final Class<?> ...  returnTypePreference){
		// TODO: Isn't there a one-line-call for this ??
		final Class<?> objClass = obj.getClass();
		
		// Collect methods that might work.
		Method methodFound = null;
		int returnTypeIndex = returnTypePreference.length; // This can be 0 for no preferences given.
		// TODO: Does there exist an optimized method for getting all by name?
		for (final Method method : objClass.getMethods()){
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
