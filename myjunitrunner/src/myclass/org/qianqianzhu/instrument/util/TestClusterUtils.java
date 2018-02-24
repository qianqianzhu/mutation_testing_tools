package org.qianqianzhu.instrument.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class TestClusterUtils {
	
	/**
	 * Get the set of fields defined in this class and its superclasses
	 *
	 * @param clazz
	 * @return
	 */
	
	public static Set<Field> getFields(Class<?> clazz) {
		// TODO: Helper not necessary here!
		Map<String, Field> helper = new TreeMap<String, Field>();

		Set<Field> fields = new LinkedHashSet<Field>();
		if (clazz.getSuperclass() != null) {
			for (Field f : getFields(clazz.getSuperclass())) {
				helper.put(f.toGenericString(), f);
			}

		}
		for (Class<?> in : clazz.getInterfaces()) {
			for (Field f : getFields(in)) {
				helper.put(f.toGenericString(), f);
			}
		}

		for (Field f : clazz.getDeclaredFields()) {
			helper.put(f.toGenericString(), f);
		}
		fields.addAll(helper.values());

		return fields;
	}
	
	public static void makeAccessible(Field field) {
		if (!Modifier.isPublic(field.getModifiers())
		        || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

}
