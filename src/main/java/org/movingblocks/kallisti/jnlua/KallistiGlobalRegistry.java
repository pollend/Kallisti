/*
 * Copyright 2018 Adrian Siekierka, MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.movingblocks.kallisti.jnlua;

import org.movingblocks.kallisti.oc.MultiArgReturn;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.NamedJavaFunction;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class KallistiGlobalRegistry {
	private static class Func implements NamedJavaFunction {
		private final String methodName;
		private final Object obj;
		private final List<Method> methods = new ArrayList<>();

		public Func(String methodName, Object obj) {
			this.methodName = methodName;
			this.obj = obj;
		}

		@Override
		public String getName() {
			return methodName;
		}

		@Override
		public int invoke(LuaState luaState) {
			int argCount = luaState.getTop();
			Method m = null;
			for (Method mm : methods) {
				if (mm.getParameterCount() == argCount || mm.isVarArgs()) {
					m = mm;
					break;
				}
			}

			if (m == null) {
				throw new RuntimeException("Could not find method!");
			}

			int parameterCount = m.getParameterCount();
			Object[] arguments = new Object[parameterCount];

			if (m.isVarArgs()) {
				for (int i = 0; i < parameterCount - 1; i++) {
					arguments[i] = luaState.toJavaObject(i + 1, m.getParameterTypes()[i]);
				}
				Class componentType = m.getParameterTypes()[parameterCount - 1].getComponentType();
				arguments[parameterCount - 1] = Array.newInstance(componentType, argCount - (parameterCount - 1));
				for (int i = parameterCount - 1; i < argCount; i++) {
					Object entry = luaState.toJavaObject(i + 1, componentType);
					Array.set(arguments[parameterCount - 1], i - (parameterCount - 1), entry);
				}
			} else {
				for (int i = 0; i < parameterCount; i++) {
					arguments[i] = luaState.toJavaObject(i + 1, m.getParameterTypes()[i]);
				}
			}

			try {
				Object o = m.invoke(obj, arguments);
				if (m.getReturnType() == Void.TYPE) {
					return 0;
				} else if (o instanceof MultiArgReturn) {
					Object[] values = ((MultiArgReturn) o).getValues();
					Arrays.asList(values).forEach(luaState::pushJavaObject);
					return values.length;
				} else {
					luaState.pushJavaObject(o);
					return 1;
				}
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getTargetException());
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private KallistiGlobalRegistry() {

	}

	public static void registerGlobal(Object obj, String name, LuaState state) {
		Class c = obj.getClass();

		Map<String, Func> funcMap = new HashMap<>();

		for (Method m : c.getMethods()) {
			// TODO: remove ugly __ hack
			final String methodName = m.getName().startsWith("__") ? m.getName().substring(2) : m.getName();
			funcMap.computeIfAbsent(methodName, (s) -> new Func(s, obj)).methods.add(m);
		}

		state.register(name, funcMap.values().toArray(new NamedJavaFunction[0]), true);
		state.newTable();
		state.pushJavaFunction(luaState -> {
			String key = luaState.toJavaObject(-1, String.class);
			throw new LuaRuntimeException("attempt to call " + name + "." + key + " (a nil value)");
		});
		state.setField(-2, "__index");
		state.setMetatable(-2);
	}
}
