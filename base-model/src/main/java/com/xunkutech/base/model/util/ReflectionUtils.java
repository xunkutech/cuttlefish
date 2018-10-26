/*
 * @(#)ReflectionUtils.java     7 Nov 2008
 * 
 * Copyright © 2009 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xunkutech.base.model.util;

import java.lang.reflect.Field;

/**
 * Utility methods to retrieve and set field values via reflection.
 *
 * @author anph
 * @since 7 Nov 2008
 */
public abstract class ReflectionUtils {

    /**
     * Attempts to set the <i>instance</i> field of the target object with the given name
     * to the given value. The field may be declared in the target object's class or any
     * if its parent classes.
     * <p>
     * Makes the field accessible if necessary, e.g. if it is declared private.
     * <p>
     * Supports bean property names of the form <code>property.childProperty</code>.
     *
     * @param target       the (non-<code>null</code>) object whose field is to be set
     * @param propertyName the bean property name of the field to set
     * @param value        the value to set the field to
     * @throws IllegalAccessException if the value could not be retrieved
     * @see #setValue(Class, String, Object)
     * @see #trySetValue(Object, String, Object)
     * @see #trySetValue(Class, String, Object)
     */
    public static void setValue(Object target, String propertyName, Object value)
            throws IllegalAccessException {
        setPropertyValue(target, null, propertyName, value);
    }

    // support bean property names of the form property.childProperty
    private static void setPropertyValue(Object target, Class<? extends Object> targetClass,
                                         String propertyName, Object value) throws IllegalAccessException {
        int separatorIndex = propertyName.indexOf('.');

        // for "plain" properties, simply set them
        if (separatorIndex == -1) {
            setFieldValue(target, targetClass, propertyName, value);
            return;
        }
        
        /*
         * Otherwise, retrieve the first property and recurse, ensuring *instance* fields
         * are used in the recursion.
         */
        setPropertyValue(getFieldValue(target, targetClass,
                propertyName.substring(0, separatorIndex)),
                null, propertyName.substring(separatorIndex + 1), value);
    }

    // for an instance (static) field, targetClass (target) may be null
    private static void setFieldValue(Object target, Class<? extends Object> targetClass,
                                      String fieldName, Object value) throws IllegalAccessException {

        try {
            Field field = getAccessibleField(
                    (target != null) ? target.getClass() : targetClass, fieldName);
            field.set(target, value);
        } catch (Exception exception) {
            boolean instanceFieldRequested = (target != null);
            throw new IllegalAccessException(String.format(
                    "Unable to set field '%s' on %s '%s' due to %s: %s",
                    fieldName, instanceFieldRequested ? "object" : "class",
                    instanceFieldRequested ? target : targetClass.getName(),
                    exception.getClass().getSimpleName(), exception.getMessage()));
        }

    }

    public static Field getAccessibleField(Class<? extends Object> targetClass,
                                           String fieldName) throws SecurityException, NoSuchFieldException {
        Class<?> currentClass = targetClass;
        Field field = null;

        // the loop will be exited if the current class is Object.class and nothing is found
        do {

            try {
                field = currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException exception) {

                // try the superclass
                currentClass = currentClass.getSuperclass();
            }

        } while ((field == null) && (currentClass != null));

        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        return field;
    }

    /**
     * Attempts to set the <i>static</i> field of the target class with the given name
     * to the given value. The field may be declared in the target class or any
     * if its parent classes.
     * <p>
     * Makes the field accessible if necessary, e.g. if it is declared private.
     * <p>
     * Supports bean property names of the form <code>property.childProperty</code>.
     * Here, <code>property</code> is expected to be a static field of the target class,
     * whereas all further properties are assumed to be <u>instance</u> fields of their
     * parent objects.
     *
     * @param targetClass  the class whose static field is to be set
     * @param propertyName the bean property name of the field to set
     * @param value        the value to set the field to
     * @throws IllegalAccessException if the value could not be retrieved
     * @see #setValue(Object, String, Object)
     * @see #trySetValue(Object, String, Object)
     * @see #trySetValue(Class, String, Object)
     */
    public static void setValue(Class<? extends Object> targetClass, String propertyName,
                                Object value) throws IllegalAccessException {
        setPropertyValue(null, targetClass, propertyName, value);
    }

    /**
     * Attempts to <u>quietly</u> (i.e. without throwing exceptions) set the
     * <em>instance</em> field of the target object with the given name
     * to the given value. The field may be declared in the target object's class or any
     * if its parent classes.
     * <p>
     * Makes the field accessible if necessary, e.g. if it is declared private.
     * <p>
     * Supports bean property names of the form <code>property.childProperty</code>.
     *
     * @param target       the (non-<code>null</code>) object whose field is to be set
     * @param propertyName the bean property name of the field to set
     * @param value        the value to set the field to
     * @return <code>true</code> iff the value was successfully set, i.e. no exceptions
     * were thrown
     * @see #trySetValue(Class, String, Object)
     * @see #setValue(Object, String, Object)
     * @see #setValue(Class, String, Object)
     */
    public static boolean trySetValue(Object target, String propertyName, Object value) {
        return trySetPropertyValue(target, null, propertyName, value);
    }

    private static boolean trySetPropertyValue(Object target,
                                               Class<? extends Object> targetClass, String propertyName, Object value) {

        try {
            setPropertyValue(target, targetClass, propertyName, value);
            return true;
        } catch (IllegalAccessException exception) {
            return false;
        }

    }

    /**
     * Attempts to <u>quietly</u> (i.e. without throwing exceptions) set the
     * <em>static</em> field of the target class with the given name
     * to the given value. The field may be declared in the target class or any
     * if its parent classes.
     * <p>
     * Makes the field accessible if necessary, e.g. if it is declared private.
     * <p>
     * Supports bean property names of the form <code>property.childProperty</code>.
     * Here, <code>property</code> is expected to be a static field of the target class,
     * whereas all further properties are assumed to be <u>instance</u> fields of their
     * parent objects.
     *
     * @param targetClass  the class whose static field is to be set
     * @param propertyName the bean property name of the field to set
     * @param value        the value to set the field to
     * @return <code>true</code> iff the value was successfully set, i.e. no exceptions
     * were thrown
     * @see #trySetValue(Object, String, Object)
     * @see #setValue(Object, String, Object)
     * @see #setValue(Class, String, Object)
     */
    public static boolean trySetValue(Class<? extends Object> targetClass, String propertyName,
                                      Object value) {
        return trySetPropertyValue(null, targetClass, propertyName, value);
    }

    /**
     * Attempts to get the value of specified <i>instance</i> field from the given target
     * object. The field may be declared in the target object's class or any if its
     * parent classes.
     * <p>
     * Supports bean property names of the form <code>property.childProperty</code>.
     *
     * @param <T>          the type of the object to be returned
     * @param target       the (non-<code>null</code>) object from which to retrieve the value
     * @param propertyName the bean property name of the field whose value is required
     * @return the field's value
     * @throws IllegalAccessException if the value could not be retrieved
     * @see #getValue(Class, String)
     */
    public static <T> T getValue(Object target, String propertyName) throws IllegalAccessException {
        return ReflectionUtils.<T>getPropertyValue(target, null, propertyName);
    }

    // support bean property names of the form property.childProperty
    private static <T> T getPropertyValue(Object target,
                                          Class<? extends Object> targetClass, String propertyName)
            throws IllegalAccessException {
        String[] fieldNames = propertyName.split("\\.");
        T value = ReflectionUtils.<T>getFieldValue(target, targetClass, fieldNames[0]);
        int numFieldNames = fieldNames.length;

        // if the requested property was not a nested property, return it
        if (numFieldNames == 1) {
            return ReflectionUtils.<T>getFieldValue(target, targetClass, propertyName);
        }

        // recursively retrieve the properties, as *instance* properties
        for (int i = 1; i < numFieldNames; i++) {

            // will throw an NPE if one of the nested properties cannot be found
            value = ReflectionUtils.<T>getFieldValue(value, null, fieldNames[i]);
        }

        return value;
    }

    // for an instance (static) field, targetClass (target) may be null    
    @SuppressWarnings("unchecked")
    private static <T> T getFieldValue(Object target, Class<? extends Object> targetClass,
                                       String fieldName) throws IllegalAccessException {
        boolean instanceFieldRequested = (target != null);

        try {
            return (T) getAccessibleField(
                    instanceFieldRequested ? target.getClass() : targetClass, fieldName)
                    .get(instanceFieldRequested ? target : null);
        } catch (Exception exception) {
            throw new IllegalAccessException(String.format(
                    "Unable to get value of field '%s' of %s '%s' due to %s: %s",
                    fieldName, instanceFieldRequested ? "object" : "class",
                    instanceFieldRequested ? target : targetClass.getName(),
                    exception.getClass().getSimpleName(), exception.getMessage()));
        }

    }

    /**
     * Attempts to get the value of specified <i>static</i> field of the given target
     * class. The field may be declared in the target class or any if its parent classes.
     * <p>
     * Supports bean property names of the form <code>property.childProperty</code>.
     * Here, <code>property</code> is expected to be a static field of the target class,
     * whereas all further properties are assumed to be <u>instance</u> fields of their
     * parent objects.
     *
     * @param <T>          the type of the object to be returned
     * @param targetClass  the (non-<code>null</code>) object from which to retrieve the value
     * @param propertyName the bean property name of the field whose value is required
     * @return the field's value
     * @throws IllegalAccessException if the value could not be retrieved
     * @see #getValue(Object, String)
     */
    public static <T> T getValue(Class<? extends Object> targetClass, String propertyName)
            throws IllegalAccessException {
        return ReflectionUtils.<T>getPropertyValue(null, targetClass, propertyName);
    }

}
