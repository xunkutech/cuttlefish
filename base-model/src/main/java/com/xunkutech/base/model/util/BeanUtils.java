/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xunkutech.base.model.util;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.xunkutech.base.model.annotation.Immutable;

import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public abstract class BeanUtils {

    /**
     * Find a method with the given method name and the given parameter types,
     * declared on the given class or one of its superclasses. Prefers public methods,
     * but will return a protected, package access, or private method too.
     * <p>Checks {@code Class.getMethod} first, falling back to
     * {@code findDeclaredMethod}. This allows to find public methods
     * without issues even in environments with restricted Java security settings.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @param paramTypes the parameter types of the method to find
     * @return the Method object, or {@code null} if not found
     * @see Class#getMethod
     * @see #findDeclaredMethod
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return findDeclaredMethod(clazz, methodName, paramTypes);
        }
    }

    /**
     * Find a method with the given method name and the given parameter types,
     * declared on the given class or one of its superclasses. Will return a public,
     * protected, package access, or private method.
     * <p>Checks {@code Class.getDeclaredMethod}, cascading upwards to all superclasses.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @param paramTypes the parameter types of the method to find
     * @return the Method object, or {@code null} if not found
     * @see Class#getDeclaredMethod
     */
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null) {
                return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
            }
            return null;
        }
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none),
     * declared on the given class or one of its superclasses. Prefers public methods,
     * but will return a protected, package access, or private method too.
     * <p>Checks {@code Class.getMethods} first, falling back to
     * {@code findDeclaredMethodWithMinimalParameters}. This allows for finding public
     * methods without issues even in environments with restricted Java security settings.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     *                                  could not be resolved to a unique method with minimal parameters
     * @see Class#getMethods
     * @see #findDeclaredMethodWithMinimalParameters
     */
    public static Method findMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
        if (targetMethod == null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
        }
        return targetMethod;
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none),
     * declared on the given class or one of its superclasses. Will return a public,
     * protected, package access, or private method.
     * <p>Checks {@code Class.getDeclaredMethods}, cascading upwards to all superclasses.
     *
     * @param clazz      the class to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     *                                  could not be resolved to a unique method with minimal parameters
     * @see Class#getDeclaredMethods
     */
    public static Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
        if (targetMethod == null && clazz.getSuperclass() != null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
        }
        return targetMethod;
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none)
     * in the given list of methods.
     *
     * @param methods    the methods to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     *                                  could not be resolved to a unique method with minimal parameters
     */
    public static Method findMethodWithMinimalParameters(Method[] methods, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = null;
        int numMethodsFoundWithCurrentMinimumArgs = 0;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                int numParams = method.getParameterCount();
                if (targetMethod == null || numParams < targetMethod.getParameterCount()) {
                    targetMethod = method;
                    numMethodsFoundWithCurrentMinimumArgs = 1;
                } else if (!method.isBridge() && targetMethod.getParameterCount() == numParams) {
                    if (targetMethod.isBridge()) {
                        // Prefer regular method over bridge...
                        targetMethod = method;
                    } else {
                        // Additional candidate with same length
                        numMethodsFoundWithCurrentMinimumArgs++;
                    }
                }
            }
        }
        if (numMethodsFoundWithCurrentMinimumArgs > 1) {
            throw new IllegalArgumentException("Cannot resolve method '" + methodName +
                    "' to a unique method. Attempted to resolve to overloaded method with " +
                    "the least number of parameters but there were " +
                    numMethodsFoundWithCurrentMinimumArgs + " candidates.");
        }
        return targetMethod;
    }

    /**
     * Parse a method signature in the form {@code methodName[([arg_list])]},
     * where {@code arg_list} is an optional, comma-separated list of fully-qualified
     * type names, and attempts to resolve that signature against the supplied {@code Class}.
     * <p>When not supplying an argument list ({@code methodName}) the method whose name
     * matches and has the least number of parameters will be returned. When supplying an
     * argument type list, only the method whose name and argument types match will be returned.
     * <p>Note then that {@code methodName} and {@code methodName()} are <strong>not</strong>
     * resolved in the same way. The signature {@code methodName} means the method called
     * {@code methodName} with the least number of arguments, whereas {@code methodName()}
     * means the method called {@code methodName} with exactly 0 arguments.
     * <p>If no method can be found, then {@code null} is returned.
     *
     * @param signature the method signature as String representation
     * @param clazz     the class to resolve the method signature against
     * @return the resolved Method
     * @see #findMethod
     * @see #findMethodWithMinimalParameters
     */
    public static Method resolveSignature(String signature, Class<?> clazz) {
        Objects.requireNonNull(signature, "'signature' must not be empty");
        Objects.requireNonNull(clazz, "Class must not be null");
        int firstParen = signature.indexOf("(");
        int lastParen = signature.indexOf(")");
        if (firstParen > -1 && lastParen == -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected closing ')' for args list");
        } else if (lastParen > -1 && firstParen == -1) {
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected opening '(' for args list");
        } else if (firstParen == -1 && lastParen == -1) {
            return findMethodWithMinimalParameters(clazz, signature);
        } else {
            String methodName = signature.substring(0, firstParen);
            String[] parameterTypeNames =
                    StringUtils.commaDelimitedListToStringArray(signature.substring(firstParen + 1, lastParen));
            Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                String parameterTypeName = parameterTypeNames[i].trim();
                try {
                    parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
                } catch (Throwable ex) {
                    throw new IllegalArgumentException("Invalid method signature: unable to resolve type [" +
                            parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
                }
            }
            return findMethod(clazz, methodName, parameterTypes);
        }
    }


    /**
     * Check if the given type represents a "simple" property:
     * a primitive, a String or other CharSequence, a Number, a Date,
     * a URI, a URL, a Locale, a Class, or a corresponding array.
     * <p>Used to determine properties to check for a "simple" dependency-check.
     *
     * @param clazz the type to check
     * @return whether the given type represents a "simple" property
     */
    public static boolean isSimpleProperty(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }

    /**
     * Check if the given type represents a "simple" value type:
     * a primitive, a String or other CharSequence, a Number, a Date,
     * a URI, a URL, a Locale or a Class.
     *
     * @param clazz the type to check
     * @return whether the given type represents a "simple" value type
     */
    public static boolean isSimpleValueType(Class<?> clazz) {
        return (ClassUtils.isPrimitiveOrWrapper(clazz) ||
                clazz.isEnum() ||
                CharSequence.class.isAssignableFrom(clazz) ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) || Instant.class.isAssignableFrom(clazz) ||
                URI.class == clazz || URL.class == clazz ||
                Locale.class == clazz ||
                Class.class == clazz);
    }

    /**
     * This copier supports Bean to Bean, Map to Bean deep coping. It does not required the source Object be compatible
     * with the target Object. It will automatically convert (customer convert adaptor will be added  in the future
     * release) compatible types(e.g. String to primitive, String to Date/Instant, long/Long to Date/Instant, primitive
     * to wrapped primitive and vice-versa).
     * <p>
     * This copier also supports imperative filters, for example:
     * <p>
     * "-$.a.b.c: XXX, XXX, XXX", include ALL properties except ... in reference path
     * "[+]$.a.b.d: XXX, XXX, XXX", only include ... in reference path
     * <p>
     * The include filters has higher priority than exclude filters. If not provided, it will copy all fields/properties
     * it could to the target object.
     *
     * @param source
     * @param target
     * @param filterMap
     */
    private static void deepCopy(Object source, Object target, final Type context, String path, Map<String, List<String>> filterMap) {
        Objects.requireNonNull(source, "source is null");
        Objects.requireNonNull(target, "target is null");

        if (null == path || !path.startsWith("$")) {
            throw new IllegalArgumentException("Bad path prefix: " + path);
        }

        final List<String> includeFields, excludeFields;

        if (null != filterMap) {
            includeFields = (null != filterMap.get(path)) ? filterMap.get(path) : filterMap.get("+" + path);
            excludeFields = (null == includeFields) ? filterMap.get("-" + path) : null;
        } else {
            includeFields = excludeFields = null;
        }

        //Deal with Map
        if (source instanceof Map) {
            ((Map<?, ?>) source).entrySet().stream()
                    .filter(entry -> checkSource(source, entry.getKey().toString(), includeFields, excludeFields))
                    .filter(entry -> checkTarget(target, entry.getKey().toString()))
                    .forEach(entry -> copyValue(target, context, entry.getKey().toString(), entry.getValue(), path, filterMap));
            return;
        }

        ClassUtils.getAllDeclaredFields(source.getClass()).parallelStream()
                .filter(sourceField -> !Modifier.isTransient(sourceField.getModifiers())
                        && !Modifier.isStatic(sourceField.getModifiers()))
                .filter(sourceField -> checkSource(source, sourceField.getName(), includeFields, excludeFields))
                .filter(sourceField -> checkTarget(target, sourceField.getName()))
                .forEach(sourceField -> {
                    try {
                        copyValue(target, context, sourceField.getName(), ReflectionUtils.getValue(source, sourceField.getName()), path, filterMap);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * @param context   The generic information about target object.
     * @param target    Target object instance.
     * @param fieldName The field name of target object.
     * @param value     The value need to be copied.
     */
    private static void copyValue(Object target, Type context, String fieldName, Object value, String path, Map<String, List<String>> filterMap) {
        Objects.requireNonNull(value, "value is null");
        Objects.requireNonNull(target, "target is null");

        // target is a map instance
        if (target instanceof Map) {
            // we only support entry key is String.
            Map<Object, Object> mapTarget = (Map) target;
            Type[] mapTypes = $Gson$Types.getMapKeyAndValueTypes(context, target.getClass());

            // check the target map class is assignable for the given value;
            if (ClassUtils.isAssignable($Gson$Types.getRawType(mapTypes[0]), String.class)
                    && ClassUtils.isAssignableValue($Gson$Types.getRawType(mapTypes[1]), value)) {
                mapTarget.put(fieldName, value);
            }
            return;
        }

        try {
            Field targetField = ReflectionUtils.getAccessibleField(target.getClass(), fieldName);
            Class<?> targetFieldClass = targetField.getType();
            Type targetFieldType = $Gson$Types.resolve(context, target.getClass(), targetField.getGenericType());

            // target field is primitive type
            if (ClassUtils.isPrimitiveOrWrapper(targetFieldClass)) {
                try {
                    if (ClassUtils.isAssignableValue(targetFieldClass, value)) {
                        ReflectionUtils.setValue(target, fieldName, value);
                    } else {
                        Constructor<?> constructor = ClassUtils.resolvePrimitiveIfNecessary(targetFieldClass).getDeclaredConstructor(value.getClass());
                        ReflectionUtils.setValue(target, fieldName, constructor.newInstance(value));
                    }
                } catch (NoSuchMethodException | InstantiationException | InvocationTargetException ignore) {
                }

                return;
            }

            // target field is String
            if (String.class.isAssignableFrom(targetFieldClass)) {
                ReflectionUtils.setValue(target, fieldName, value.toString());
                return;
            }

            // target field is Instant
            if (Instant.class.isAssignableFrom(targetFieldClass)) {
                evaluateInstantType(value).ifPresent(FunctionUtils.wrapConsumer(v -> ReflectionUtils.setValue(target, fieldName, v)));
                return;
            }

            // target field is Date
            if (Date.class.isAssignableFrom(targetFieldClass)) {
                evaluateDateType(value).ifPresent(FunctionUtils.wrapConsumer(v -> ReflectionUtils.setValue(target, fieldName, v)));
                return;
            }

            // target field is enum
            if (targetFieldClass.isEnum()) {
                for (Enum<?> constant : (Enum[]) targetFieldClass.getEnumConstants()) {
                    String name = constant.name();
                    if (name.toLowerCase().equals(value.toString().toLowerCase())) {
                        ReflectionUtils.setValue(target, fieldName, constant);
                    }
                }
                return;
            }

            // target field is a Map
            if (Map.class.isAssignableFrom(targetFieldClass)) {
                // construct new map instance
                ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
                Map<Object, Object> mapInstance = (Map) cc.get(TypeToken.get(targetFieldType)).construct();
                deepCopy(value, mapInstance, targetFieldType, path + "." + fieldName, filterMap);
                ReflectionUtils.setValue(target, fieldName, mapInstance);
                return;
            }

            // target field is a collection
            if (Collection.class.isAssignableFrom(targetFieldClass)) {
                ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
                Collection<Object> collectionInstance = (Collection) cc.get(TypeToken.get(targetFieldType)).construct();
                collectionInstance.addAll(wrapToList(value, $Gson$Types.getRawType($Gson$Types.getCollectionElementType(targetFieldType, targetFieldClass))));
                ReflectionUtils.setValue(target, fieldName, collectionInstance);
                return;
            }

            // target field is an array
            if (targetFieldClass.isArray()) {
                Class<?> componentType = $Gson$Types.getRawType($Gson$Types.getArrayComponentType(targetFieldType));
                List<?> list = wrapToList(value, componentType);
                int size = list.size();
                Object array = Array.newInstance(componentType, size);
                for (int i = 0; i < size; i++) {
                    Array.set(array, i, list.get(i));
                }
                ReflectionUtils.setValue(target, fieldName, array);
                return;
            }

            // target field is an object
            Object targetFieldValueInstance = ReflectionUtils.getValue(target, fieldName);
            if (null == targetFieldValueInstance) {
                ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
                targetFieldValueInstance = cc.get(TypeToken.get(targetFieldType)).construct();
            }

            deepCopy(value, targetFieldValueInstance, targetFieldType, path + "." + fieldName, filterMap);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ClassUtils.isPrimitiveOrWrapper(value.getClass());
    }

    /**
     * Try to evaluate the Instant instance
     *
     * @param value
     * @return the according Instant instance object. if failed, return empty.
     */
    private static Optional<Instant> evaluateInstantType(Object value) {
        if (value instanceof Instant) {
            return Optional.of((Instant) value);
        }

        if (value instanceof Date) {
            return Optional.of(((Date) value).toInstant());
        }

        if (value instanceof String) {
            try {
                Long epochMilli = Long.valueOf((String) value);
                if (epochMilli > 0) {
                    return Optional.of(Instant.ofEpochMilli(epochMilli));
                }
            } catch (Throwable ignored) {
            }

            try {
                return Optional.of(Instant.parse((String) value));
            } catch (Throwable ignored) {
            }

            try {
                DateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
                Date date = format.parse((String) value);
                return Optional.of(date.toInstant());
            } catch (Throwable ignored) {
            }

            return Optional.empty();
        }

        if (value.getClass().equals(long.class) || value.getClass().equals(Long.class)) {
            return Optional.of(Instant.ofEpochMilli((Long) value));
        }

        if (value.getClass().equals(int.class) || value.getClass().equals(Integer.class)) {
            return Optional.of(Instant.ofEpochMilli(Integer.toUnsignedLong((Integer) value)));
        }

        return Optional.empty();
    }

    private static Optional<Date> evaluateDateType(Object value) {
        if (value instanceof Instant) {
            return Optional.of(Date.from((Instant) value));
        }

        if (value instanceof Date) {
            return Optional.of((Date) value);
        }

        if (value instanceof String) {
            // try long value
            try {
                Long epochMilli = Long.valueOf((String) value);
                if (epochMilli > 0) {
                    return Optional.of(new Date(epochMilli));
                }
            } catch (Throwable ignored) {
            }

            try {
                DateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
                Date date = format.parse((String) value);
                return Optional.of(date);
            } catch (Throwable ignored) {
            }

            try {
                return Optional.of(Date.from(Instant.parse((String) value)));
            } catch (Throwable ignored) {
            }

            return Optional.empty();
        }

        // long value
        if (value.getClass().equals(long.class) || value.getClass().equals(Long.class)) {
            return Optional.of(new Date((Long) value));
        }

        // int value
        if (value.getClass().equals(int.class) || value.getClass().equals(Integer.class)) {
            return Optional.of(new Date(Integer.toUnsignedLong((Integer) value)));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> wrapToList(Object obj, Class<?> componentType) {
        if (obj.getClass().isArray()) {

            Class<?> rawType;
            if (obj instanceof GenericArrayType) {
                rawType = $Gson$Types.getRawType(((GenericArrayType) obj).getGenericComponentType());
            } else {
                rawType = obj.getClass().getComponentType();
            }

            if (ClassUtils.isAssignable(componentType, rawType)) {
                int length = Array.getLength(obj);
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(obj, i));
                }
                return list;
            }

        }

        if (obj instanceof Collection && !((Collection) obj).isEmpty()) {
            List<Object> list = new LinkedList<>();
            ((Collection<Object>) obj).stream()
                    .filter(v -> ClassUtils.isAssignableValue(componentType, v))
                    .forEach(list::add);
            return list;
        }

        return new ArrayList<>();
    }

    private static boolean checkTarget(Object target, String fieldName) {

        if (target instanceof Map) {
            return true;
        }

        try {
            Field targetField = ReflectionUtils.getAccessibleField(target.getClass(), fieldName);
            //Ignore if target Element is immutable;
            if (targetField.isAnnotationPresent(Immutable.class)) {
                return false;
            }

            if (Modifier.isStatic(targetField.getModifiers())) {
                return false;
            }
        } catch (NoSuchFieldException e) {
            return false;
        }

        return true;
    }

    // source field is not null, not in the excludeFields or in the includeFields
    private static boolean checkSource(Object source, String fieldName, List<String> includeFields, List<String> excludeFields) {

        boolean isValid = false;

        if (null != includeFields) {
            isValid = includeFields.contains(fieldName);
        }

        if (!isValid && null != excludeFields) {
            isValid = !excludeFields.contains(fieldName);
        }

        if (null == includeFields && null == excludeFields) {
            isValid = true;
        }

        if (isValid && !(source instanceof Map)) {
            try {
                Object value = ReflectionUtils.getValue(source, fieldName);
                if (null == value) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }


        return isValid;
    }


    public static void deepCopy(Object source, Object target) {
        deepCopy(source, target, null);
    }

    public static void deepCopy(Object source, Object target, String... filters) {
        deepCopy(source, target, target.getClass(), filters);
    }


    public static void deepCopy(Object source, Object target, Type type, String... filters) {
        Map<String, List<String>> filterMap = null;

        if (null != filters && filters.length > 0) {
            filterMap = Arrays.stream(filters)
                    .map(i -> {
                        String[] item = i.split(":");
                        if (item.length != 2) {
                            throw new IllegalArgumentException("Bad filter format: " + i);
                        }

                        List<String> fields = Arrays.stream(item[1].split(","))
                                .map(f -> f.trim())
                                .collect(toList());

                        Map.Entry<String, List<String>> entry = new AbstractMap.SimpleEntry<>(item[0].trim(), fields);
                        return entry;
                    })
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        deepCopy(source, target, type, "$", filterMap);
    }

}
