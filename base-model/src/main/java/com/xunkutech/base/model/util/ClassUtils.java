package com.xunkutech.base.model.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

/**
 * Utility methods for dealing with {@link Class Classes}.
 *
 * @author anph
 * @since 9 Feb 2009
 */
public abstract class ClassUtils {

    /**
     * Returns <u>one</u> of the possible chains of superclasses and/or interfaces joining the
     * specified class to the given superclass, as returned by {@link #getSuperclassChains(Class, Class)}.
     * <i>Which</i> of the possible chains will be returned is not defined.
     * <p>
     * If <code>superclass</code> is <i>not</i> a superclass or -interface of <code>class</code>,
     * the method returns <code>null</code>. This may happen (in spite of the signature) if the
     * method is called with non-generic arguments.
     *
     * @param <S>        the type of the superclass at the &quot;end&quot; of the chain
     * @param clazz      the class at the &quot;start&quot; of the superclass chain
     * @param superclass the class at the &quot;end&quot; of the superclass chain
     * @return <u>one</u> superclass chain linking <code>class</code> to <code>superclass</code>,
     * where successive elements of the list are immediate superclasses or -interfaces. If
     * <code>class</code> is not a subclass of <code>superclass</code>, returns <code>null</code>.
     * @throws IllegalArgumentException if either argument is null
     * @see #getSuperclassChains(Class, Class)
     */
    public static <S> List<Class<? extends S>> getSuperclassChain(Class<? extends S> clazz,
                                                                  Class<S> superclass) {
        Set<List<Class<? extends S>>> superclassChains = getSuperclassChainsInternal(clazz, superclass, true);
        return (superclassChains.isEmpty() ? null : superclassChains.iterator().next());
    }

    /**
     * Returns the chain of superclass and/or interfaces from the specified class to the given
     * superclass. Either parameter may be an interface.
     * <p>
     * Each list in the resulting set contains immediate superclass elements in order, i.e. for
     * classes
     * <p>
     * <pre>
     * class Foo {}
     * class Bar extends Foo {}
     * class Baz extends Bar {}
     * </pre>
     * <p>
     * <code>getSuperclassChains(Baz.class, Foo.class)</code> will return one list, <code>[Baz.class,
     * Bar.class, Foo.class]</code>.
     * <p>
     * If both parameters are classes, there can only be one possible chain. However, if the superclass
     * is an interface, there may be multiple possible inheritance chains. For instance, for
     * <p>
     * <pre>
     * interface Foo {}
     * interface Bar1 extends Foo {}
     * interface Bar2 extends Foo {}
     * interface Baz extends Bar1, Bar2 {}
     * </pre>
     * <p>
     * both <code>[Baz.class, Bar1.class, Foo.class]</code> and <code>[Baz.class, Bar2.class, Foo.class]</code>
     * are valid inheritance chains, and the method returns both.
     * <p>
     * If <code>superclass</code> is <i>not</i> a superclass or -interface of <code>class</code>,
     * the method returns an empty set. This may happen (in spite of the signature) if the
     * method is called with non-generic arguments.
     *
     * @param <S>        the type of the superclass at the &quot;end&quot; of the chain
     * @param clazz      the class at the &quot;start&quot; of the superclass chain
     * @param superclass the class at the &quot;end&quot; of the superclass chain
     * @return all possible superclass chains linking <code>class</code> to <code>superclass</code>,
     * where successive elements of the list are immediate superclasses or -interfaces. If
     * <code>class</code> is not a subclass of <code>superclass</code>, returns an empty set.
     * @throws IllegalArgumentException if either argument is null
     * @see #getSuperclassChain(Class, Class)
     */
    public static <S> Set<List<Class<? extends S>>> getSuperclassChains(Class<? extends S> clazz,
                                                                        Class<S> superclass) {
        return getSuperclassChainsInternal(clazz, superclass, false);
    }

    private static <S> Set<List<Class<? extends S>>> getSuperclassChainsInternal(Class<? extends S> clazz,
                                                                                 Class<S> superclass,
                                                                                 boolean oneChainSufficient) {
        checkNotNull("'clazz' and 'superclass' may not be non-null", clazz, superclass);

        if (!superclass.isAssignableFrom(clazz)) {
            return Collections.emptySet();
        }

        // interfaces only need to be considered if the superclass is an interface
        return getSuperclassSubchains(clazz, superclass, oneChainSufficient,
                superclass.isInterface());
    }

    // recursive method: gets the subchains from the given class to the target class
    @SuppressWarnings("unchecked")
    private static <S> Set<List<Class<? extends S>>> getSuperclassSubchains(Class<? extends S> subclass,
                                                                            Class<S> superclass,
                                                                            boolean oneChainSufficient,
                                                                            boolean considerInterfaces) {

        // base case: the subclass *is* the target class
        if (subclass.equals(superclass)) {

            // since the list will be built from the *head*, entityA linked list is entityA good choice
            List<Class<? extends S>> subchain = new LinkedList<Class<? extends S>>();
            subchain.add(subclass);
            return singleton(subchain);
        }

        // recursive case: get all superclasses and, if required, interfaces and recurse
        Set<Class<? extends S>> supertypes = new HashSet<Class<? extends S>>();

        Class<? extends S> immediateSuperclass = (Class<? extends S>) subclass.getSuperclass();

        // interfaces and Object don't have a superclass
        if (immediateSuperclass != null) {
            supertypes.add(immediateSuperclass);
        }

        if (considerInterfaces) {
            supertypes.addAll(asList((Class<? extends S>[]) subclass.getInterfaces()));
        }

        Set<List<Class<? extends S>>> subchains = new HashSet<List<Class<? extends S>>>();

        for (Class<? extends S> supertype : supertypes) {
            Set<List<Class<? extends S>>> subchainsFromSupertype =
                    getSuperclassSubchains(supertype, superclass, oneChainSufficient,
                            considerInterfaces);

            // each chain from the supertype results in a chain [current, subchain-from-super]
            if (!subchainsFromSupertype.isEmpty()) {

                if (oneChainSufficient) {
                    ClassUtils.<S>addSubchain(subchains, subclass,
                            subchainsFromSupertype.iterator().next());
                    return subchains;
                } else {

                    for (List<Class<? extends S>> subchainFromSupertype : subchainsFromSupertype) {
                        ClassUtils.<S>addSubchain(subchains, subclass, subchainFromSupertype);
                    }

                }

            }

        }

        return subchains;
    }

    // adds the class to the beginning of the subchain and stores this extended subchain
    private static <T> void addSubchain(Set<List<Class<? extends T>>> subchains,
                                        Class<? extends T> clazz,
                                        List<Class<? extends T>> subchainFromSupertype) {
        subchainFromSupertype.add(0, clazz);
        subchains.add(subchainFromSupertype);
    }

    /**
     * Determines if <i>any</i> of the given superclasses is a superclass or -interface of the
     * specified class. See {@link Class#isAssignableFrom(Class)}.
     *
     * @param superclasses the superclasses and -interfaces to be tested against
     * @param clazz        the class to be to be checked
     * @return <code>true</code> iff <i>any</i> of the given classes is a superclass or -interface
     * of the specified class
     * @throws IllegalArgumentException if either of the arguments is null
     */
    public static boolean isAnyAssignableFrom(Collection<Class<?>> superclasses,
                                              Class<?> clazz) {
        checkNotNull("All arguments must be non-null", superclasses, clazz);

        return superclasses.parallelStream()
                .anyMatch(superclass -> superclass.isAssignableFrom(clazz));

    }

    /**
     * Determines if the specified object is assignment-compatible
     * with <em>any</em> of the specified {@code Class Classes}. class.
     * See {@link Class#isInstance(Object)}.
     *
     * @param superclasses the superclasses and -interfaces to be tested against
     * @param instance     the object to be to be checked
     * @return <code>true</code> iff the object is an instance of <i>any</i> of the
     * given classes interfaces
     * @throws IllegalArgumentException if either of the arguments is null
     */
    public static boolean isInstance(Collection<Class<?>> superclasses,
                                     Object instance) {
        checkNotNull("Superclasses non-null", superclasses);

        return superclasses.parallelStream()
                .anyMatch(superclass -> superclass.isInstance(instance));

    }

    /**
     * Collects all fields declared in the given class <u>and</u> inherited from
     * parents.
     *
     * @param clazz the class to parse
     * @return a list of all fields declared in the class or its  parents, in the
     * order determined by successive {@link Class#getDeclaredFields()}
     * calls
     * @see #getAllDeclaredFields(Class, Class)
     */
    public static List<Field> getAllDeclaredFields(Class<?> clazz) {
        return getAllDeclaredFields(clazz, Object.class);
    }

    /**
     * Collects all fields declared in the given class <u>and</u> inherited from
     * parents <strong>up to and including the given parent class</strong>.
     *
     * @param <T>        the type of the class to parse
     * @param clazz      the class to parse
     * @param superclass the superclass of the class to parse at which traversal should be
     *                   stopped
     * @return a list of all fields declared in the class or its parents up to and including
     * the given parent class, in the order determined by successive
     * {@link Class#getDeclaredFields()} calls
     * @see #getAllDeclaredFields(Class)
     */
    public static <T> List<Field> getAllDeclaredFields(Class<T> clazz,
                                                       Class<? super T> superclass) {
        final List<Field> fields = new ArrayList<Field>();

        for (Class<?> immediateSuperclass : getSuperclassChain(clazz, superclass)) {
            fields.addAll(Arrays.asList(immediateSuperclass.getDeclaredFields()));
        }

        return fields;
    }

    /**
     * Collects all fields declared in the given class <u>and</u> inherited from
     * parents that are annotated with an annotation of the given type.
     *
     * @param clazz          the class to parse
     * @param annotationType the non-{@code null} type (class) of the annotation required
     * @return a list of all fields declared in the class or its  parents, in the
     * order determined by successive {@link Class#getDeclaredFields()}
     * calls
     * @throws IllegalArgumentException if {@code clazz} or {@code annotationType} is {@code null}
     */
    public static List<Field> getAllAnnotatedDeclaredFields(Class<?> clazz,
                                                            Class<? extends Annotation> annotationType) {
        checkNotNull("All arguments must be non-null", clazz, annotationType);

        final List<Field> annotatedFields = new ArrayList<Field>();

        for (Field field : getAllDeclaredFields(clazz)) {

            if (field.isAnnotationPresent(annotationType)) {
                annotatedFields.add(field);
            }

        }

        return annotatedFields;
    }

    /**
     * Collects all methods of the given class (as returned by {@link Class#getMethods()} that
     * are annotated with the given annotation.
     *
     * @param clazz          the class whose methods should be returned
     * @param annotationType the annotation that the returned methods should be annotated with
     * @return the methods of the given class annotated with the given annotation
     * @throws IllegalArgumentException if {@code clazz} is {@code null}
     */
    public static Set<Method> getAnnotatedMethods(Class<?> clazz,
                                                  Class<? extends Annotation> annotationType) {
        checkNotNull("'clazz' must be non-null", clazz);

        // perhaps this case should throw an exception, but an empty list also seems sensible
        if (annotationType == null) {
            return new HashSet<Method>();
        }

        Set<Method> annotatedMethods = new HashSet<Method>();

        for (Method method : clazz.getMethods()) {

            if (method.isAnnotationPresent(annotationType)) {
                annotatedMethods.add(method);
            }

        }

        return annotatedMethods;
    }

    /**
     * Retrieves the type arguments of a class when regarded as an subclass of the
     * given typed superclass or interface. The order of the runtime type classes matches the order
     * of the type variables in the declaration of the typed superclass or interface.
     * <p>
     * For example, for the classes
     * <p>
     * <pre>
     * class Foo&lt;U, V&gt; {}
     * class Bar&lt;W&gt; extends Foo&lt;String, W&gt; {}
     * class Baz extends Bar&lt;Long&gt;
     * </pre>
     * <p>
     * and a <code>typedClass</code> argument of <code>Baz.class</code>, the method should return
     * <p>
     * <ul>
     * <li><code>[String, Long]</code> for a <code>typedSuperclass</code> argument of <code>Foo.class</code>,
     * and
     * <li><code>[Long]</code> if <code>typedSuperclass</code> is <code>Bar.class</code>.
     * </ul>
     * For type parameters that cannot be determined, <code>null</code> is returned.
     * <p>
     * <entityB>Note:</entityB> It is <u>not</u> possible to retrieve type information that is not available
     * in the (super)class hierarchy at <u>compile</u>-time. Calling
     * <code>getActualTypeArguments(new ArrayList&lt;String&gt;().getClass(), List.class)</code> will,
     * for instance, return <code>[null]</code> because the specification of the actual type
     * (<code>String</code>, in this example) did not take place either in the superclass {@link AbstractList}
     * or the interface {@link List}.
     * <p>
     * If {@code superclass} is <em>not</em> a superclass or -interface of {@code class},
     * the method returns {@code null}. This may happen (in spite of the signature) if the
     * method is called with non-generic arguments.
     *
     * @param <S>             the type of the object
     * @param typedClass      the class for which type information is required
     * @param typedSuperclass the typed class or interface of which the object is to be regarded a
     *                        subclass
     * @return the type arguments for the given class when regarded as a subclass of the
     * given typed superclass, in the order defined in the superclass. If
     * {@code class} is not a subclass of {@code superclass}, returns {@code null}.
     * @throws IllegalArgumentException if <code>typedSuperclass</code> or <code>typedClass</code>
     *                                  is <code>null</code>
     */
    public static <S> List<Type> getActualTypeArguments(Class<? extends S> typedClass,
                                                        Class<S> typedSuperclass) {
        checkNotNull("All arguments must be non-null", typedSuperclass, typedClass);

        /*
         * The type signature should ensure that the class really *is* an subclass of
         * typedSuperclass, but this can be circumvented by using "generic-less" arguements.
         */
        if (!typedSuperclass.isAssignableFrom(typedClass)) {
            return null;
        }

        TypeVariable<?>[] typedClassTypeParams = typedSuperclass.getTypeParameters();

        // if the class has no parameters, return
        if (typedClassTypeParams.length == 0) {
            return new ArrayList<>(0);
        }

        /*
         * It would be nice if the parent class simply "aggregated" all the type variable
         * assignments that happen in subclasses. In other words, it would be nice if, in the
         * example in the JavaDoc, new Baz().getClass().getSuperclass().getGenericSuperclass()
         * would return [String, Long] as actual type arguments.
         * Unfortunately, though, it returns [String, W], because the assignment of Long to W
         * isn't accessible to Bar. W's value is available from new Baz().getClass().getGenericSuperclass(),
         * and must be "remembered" as we traverse the object hierarchy.
         * Note, though, that the "variable substitution" of W (the variable used in Bar) for V (the
         * equivalent variable in Foo) *is* propagated, but only to the immediate parent!
         */
        Map<TypeVariable<?>, Type> typeAssignments =
                new HashMap<>(typedClassTypeParams.length);

        /*
         * Get one possible path from the typed class to the typed superclass. For classes, there
         * is only one (the superclass chain), but for interfaces there may be multiple. We only
         * need one, however (and it doesn't matter which one) since the compiler does not allow
         * inheritance chains with conflicting generic type information.
         */
        List<Class<? extends S>> superclassChain = getSuperclassChain(typedClass, typedSuperclass);

        assert (superclassChain != null) : Arrays.<Class<?>>asList(typedSuperclass, typedClass);

        /*
         * The list is ordered so that successive elements are immediate superclasses. The iteration
         * stops with the class whose *superclass* is the last element, because type information
         * is collected from the superclass.
         */
        for (int i = 0; i < superclassChain.size() - 1; i++) {
            collectAssignments(superclassChain.get(i), superclassChain.get(i + 1), typeAssignments);
        }

        // will contain null for entries for which no class could be resolved
        return getActualAssignments(typedClassTypeParams, typeAssignments);
    }

    private static void collectAssignments(Class<?> clazz, Class<?> supertype,
                                           Map<TypeVariable<?>, Type> typeAssignments) {
        TypeVariable<?>[] typeParameters = supertype.getTypeParameters();

        // the superclass is not necessarily a generic class
        if (typeParameters.length == 0) {
            return;
        }

        Type[] actualTypeAttributes = getActualTypeAttributes(clazz, supertype);

        assert (typeParameters.length == actualTypeAttributes.length)
                : Arrays.asList(typeParameters, typeAssignments);

        // matches up type parameters with their actual assignments, assuming the order is the same!
        for (int i = 0; i < actualTypeAttributes.length; i++) {
            Type type = actualTypeAttributes[i];


            if (type instanceof TypeVariable) {
                if (typeAssignments.containsKey(type)) {
                    typeAssignments.put(typeParameters[i], typeAssignments.get(type));
                }
            } else {
                typeAssignments.put(typeParameters[i], type);
            }
//
//            /*
//             * type will be entityA Class or ParameterizedType if the actual type is known,
//             * and entityA TypeVariable if not.
//             */
//            if (type instanceof Class) {
//                typeAssignments.put(typeParameters[i], (Class<?>) type);
//            } else if (type instanceof ParameterizedType) {
//                assert (((ParameterizedType) type).getRawType() instanceof Class) : type;
//                typeAssignments.put(typeParameters[i],
//                        (Class<?>) ((ParameterizedType) type).getRawType());
//            } else {
//                assert (type instanceof TypeVariable<?>) : type;
//
//                /*
//                 * The actual type arguments consist of classes and type variables from the
//                 * immediate child class. So if the type assignment mapping is updated to
//                 * contain the mapping of all type variables of the *current* class to
//                 * their classes, then these can be used in the *next* iteration to resolve
//                 * any variable "left over" from this round.
//                 *
//                 * Any variables that cannot be resolved in this round are not resolvable, otherwise
//                 * the would have been resolved in the previous round.
//                 */
//                if (typeAssignments.containsKey(type)) {
//                    typeAssignments.put(typeParameters[i], typeAssignments.get(type));
//                }
//
//            }

        }

    }


    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();

            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            // we could use the variable's bounds, but that won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);

        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + className);
        }
    }

    private static Type[] getActualTypeAttributes(Class<?> clazz, Class<?> supertype) {
        /*
         * The superclass is not necessarily entityA ParameterizedType even if it has type
         * parameters! This happens if entityA user fails to specify type parameters for entityA
         * class and ignores the warning, e.g.
         *
         * class MyList extends ArrayList
         *
         * In this case, the superclass ArrayList.class has one type parameter, but
         * MyList.class.getGenericSuperclass() returns entityA simple type object!
         *
         * In this case, no type assignments take place, so the actual arguments are
         * simply the type parameters.
         */
        Type genericSupertype = tryGetGenericSupertype(clazz, supertype);
        return ((genericSupertype instanceof ParameterizedType)
                ? ((ParameterizedType) genericSupertype).getActualTypeArguments()
                : supertype.getTypeParameters());
    }

    private static Type tryGetGenericSupertype(Class<?> clazz, Class<?> supertype) {

        if (!supertype.isInterface()) {
            return clazz.getGenericSuperclass();
        } else {
            Type[] genericInterfaces = clazz.getGenericInterfaces();

            for (int i = 0; i < genericInterfaces.length; i++) {
                Type interfaceType = genericInterfaces[i];

                // there is no guarantee that *all* the interfaces are generic
                if ((interfaceType instanceof ParameterizedType)
                        && (((ParameterizedType) interfaceType).getRawType().equals(supertype))) {
                    return interfaceType;
                } else {
                    assert (interfaceType instanceof Class) : interfaceType;

                    if (interfaceType.equals(supertype)) {
                        return interfaceType;
                    }

                }

            }

        }

        throw new AssertionError("Unable to find generic superclass information for class '"
                + clazz + "' and superclass/-interface '" + supertype + "'");
    }

    private static List<Type> getActualAssignments(
            TypeVariable<?>[] typedClassTypeParams,
            Map<TypeVariable<?>, Type> typeAssignments) {
        int numTypedClassTypeParams = typedClassTypeParams.length;
        List<Type> actualAssignments =
                new ArrayList<>(numTypedClassTypeParams);

        // for entries that could not be resolved, null should be returned
        for (int i = 0; i < numTypedClassTypeParams; i++) {
            actualAssignments.add(typeAssignments.get(typedClassTypeParams[i]));
        }

        return actualAssignments;
    }

    /**
     * Checks that objects are not {@code null}.
     *
     * @param errorMessage the message of the {@link IllegalArgumentException} that will be
     *                     thrown if any of the objects is {@code null}
     * @param objs         the objects to be checked
     * @throws IllegalArgumentException if any of the objects is {@code null}
     */
    public static void checkNotNull(String errorMessage, Object... objs) {
        Arrays.stream(objs).forEach(obj -> Objects.requireNonNull(obj, errorMessage));
    }

    /**
     * Gets an array of all methods in a class hierarchy walking up to parent classes
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAllDeclaredMethods(Class<?> clazz) {
        return getAllDeclaredMethods(clazz, Object.class);
    }

    /**
     * Gets an array of all methods in a class hierarchy walking up to parent classes
     *
     * @param clazz
     * @param superclass
     * @param <T>
     * @return
     */
    public static <T> List<Method> getAllDeclaredMethods(Class<T> clazz,
                                                         Class<? super T> superclass) {
        List<Method> methods = new ArrayList<>();

        for (Class<?> immediateSuperclass : getSuperclassChain(clazz, superclass)) {
            methods.addAll(Arrays.asList(immediateSuperclass.getDeclaredMethods()));
        }

        return methods;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Suffix for array class names: "[]"
     */
    public static final String ARRAY_SUFFIX = "[]";

    /**
     * Prefix for internal array class names: "["
     */
    private static final String INTERNAL_ARRAY_PREFIX = "[";

    /**
     * Prefix for internal non-primitive array class names: "[L"
     */
    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

    /**
     * The package separator character: '.'
     */
    private static final char PACKAGE_SEPARATOR = '.';

    /**
     * The path separator character: '/'
     */
    private static final char PATH_SEPARATOR = '/';

    /**
     * The inner class separator character: '$'
     */
    private static final char INNER_CLASS_SEPARATOR = '$';

    /**
     * The CGLIB class separator: "$$"
     */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /**
     * The ".class" file suffix
     */
    public static final String CLASS_FILE_SUFFIX = ".class";


    /**
     * Map with primitive wrapper type as key and corresponding primitive
     * type as value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

    /**
     * Map with primitive type as key and corresponding wrapper
     * type as value, for example: int.class -> Integer.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    /**
     * Map with primitive type name as key and corresponding primitive
     * type as value, for example: "int" -> "int.class".
     */
    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<>(32);

    /**
     * Map with common "java.lang" class name as key and corresponding Class as value.
     * Primarily for efficient deserialization of remote invocations.
     */
    private static final Map<String, Class<?>> commonClassCache = new HashMap<>(32);


    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        primitiveWrapperTypeMap.forEach((key, value) -> {
            primitiveTypeToWrapperMap.put(value, key);
            registerCommonClasses(key);
        });

        Set<Class<?>> primitiveTypes = new HashSet<>(32);
        primitiveTypes.addAll(primitiveWrapperTypeMap.values());
        primitiveTypes.addAll(Arrays.asList(boolean[].class, byte[].class, char[].class,
                double[].class, float[].class, int[].class, long[].class, short[].class));
        primitiveTypes.add(void.class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
        }

        registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class,
                Float[].class, Integer[].class, Long[].class, Short[].class);
        registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
                Object.class, Object[].class, Class.class, Class[].class);
        registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
                Error.class, StackTraceElement.class, StackTraceElement[].class);
    }


    /**
     * Register the given common classes with the ClassUtils cache.
     */
    private static void registerCommonClasses(Class<?>... commonClasses) {
        for (Class<?> clazz : commonClasses) {
            commonClassCache.put(clazz.getName(), clazz);
        }
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
     * class will be used as fallback.
     * <p>Call this method if you intend to use the thread context ClassLoader
     * in a scenario where you clearly prefer a non-null ClassLoader reference:
     * for example, for class path resource loading (but not necessarily for
     * {@code Class.forName}, which accepts a {@code null} ClassLoader
     * reference as well).
     *
     * @return the default ClassLoader (only {@code null} if even the system
     * ClassLoader isn't accessible)
     * @see Thread#getContextClassLoader()
     * @see ClassLoader#getSystemClassLoader()
     */

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    /**
     * Override the thread context ClassLoader with the environment's bean ClassLoader
     * if necessary, i.e. if the bean ClassLoader is not equivalent to the thread
     * context ClassLoader already.
     *
     * @param classLoaderToUse the actual ClassLoader to use for the thread context
     * @return the original thread context ClassLoader, or {@code null} if not overridden
     */

    public static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
        Thread currentThread = Thread.currentThread();
        ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
        if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
            currentThread.setContextClassLoader(classLoaderToUse);
            return threadContextClassLoader;
        } else {
            return null;
        }
    }

    /**
     * Replacement for {@code Class.forName()} that also returns Class instances
     * for primitives (e.g. "int") and array class names (e.g. "String[]").
     * Furthermore, it is also capable of resolving inner class names in Java source
     * style (e.g. "java.lang.Thread.State" instead of "java.lang.Thread$State").
     *
     * @param name        the name of the Class
     * @param classLoader the class loader to use
     *                    (may be {@code null}, which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError           if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, ClassLoader classLoader)
            throws ClassNotFoundException, LinkageError {

        Objects.requireNonNull(name, "Name must not be null");

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz == null) {
            clazz = commonClassCache.get(name);
        }
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader clToUse = classLoader;
        if (clToUse == null) {
            clToUse = getDefaultClassLoader();
        }
        try {
            return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
            if (lastDotIndex != -1) {
                String innerClassName =
                        name.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
                try {
                    return (clToUse != null ? clToUse.loadClass(innerClassName) : Class.forName(innerClassName));
                } catch (ClassNotFoundException ex2) {
                    // Swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    /**
     * Resolve the given class name into a Class instance. Supports
     * primitives (like "int") and array class names (like "String[]").
     * <p>This is effectively equivalent to the {@code forName}
     * method with the same arguments, with the only difference being
     * the exceptions thrown in case of class loading failure.
     *
     * @param className   the name of the Class
     * @param classLoader the class loader to use
     *                    (may be {@code null}, which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws IllegalArgumentException if the class name was not resolvable
     *                                  (that is, the class could not be found or the class file could not be loaded)
     * @see #forName(String, ClassLoader)
     */
    public static Class<?> resolveClassName(String className, ClassLoader classLoader) throws IllegalArgumentException {
        try {
            return forName(className, classLoader);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Cannot find class [" + className + "]", ex);
        } catch (LinkageError ex) {
            throw new IllegalArgumentException(
                    "Error loading class [" + className + "]: problem with class file or dependent class.", ex);
        }
    }

    /**
     * Resolve the given class name as primitive class, if appropriate,
     * according to the JVM's naming rules for primitive classes.
     * <p>Also supports the JVM's internal class names for primitive arrays.
     * Does <i>not</i> support the "[]" suffix notation for primitive arrays;
     * this is only supported by {@link #forName(String, ClassLoader)}.
     *
     * @param name the name of the potentially primitive class
     * @return the primitive class, or {@code null} if the name does not denote
     * a primitive class or primitive array class
     */

    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = primitiveTypeNameMap.get(name);
        }
        return result;
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return {@code false} if either the class or
     * one of its dependencies is not present or cannot be loaded.
     *
     * @param className   the name of the class to check
     * @param classLoader the class loader to use
     *                    (may be {@code null}, which indicates the default class loader)
     * @return whether the specified class is present
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            forName(className, classLoader);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Return the user-defined class for the given instance: usually simply
     * the class of the given instance, but the original class in case of a
     * CGLIB-generated subclass.
     *
     * @param instance the instance to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Object instance) {
        Objects.requireNonNull(instance, "Instance must not be null");
        return getUserClass(instance.getClass());
    }

    /**
     * Return the user-defined class for the given class: usually simply the given
     * class, but the original class in case of a CGLIB-generated subclass.
     *
     * @param clazz the class to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && Object.class != superclass) {
                return superclass;
            }
        }
        return clazz;
    }

    /**
     * Check whether the given class is cache-safe in the given context,
     * i.e. whether it is loaded by the given ClassLoader or a parent of it.
     *
     * @param clazz       the class to analyze
     * @param classLoader the ClassLoader to potentially cache metadata in
     */
    public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader) {
        Objects.requireNonNull(clazz, "Class must not be null");
        try {
            ClassLoader target = clazz.getClassLoader();
            if (target == null) {
                return true;
            }
            ClassLoader cur = classLoader;
            if (cur == target) {
                return true;
            }
            while (cur != null) {
                cur = cur.getParent();
                if (cur == target) {
                    return true;
                }
            }
            return false;
        } catch (SecurityException ex) {
            // Probably from the system ClassLoader - let's consider it safe.
            return true;
        }
    }


    /**
     * Get the class name without the qualified package name.
     *
     * @param className the className to get the short name for
     * @return the class name of the class without the package name
     * @throws IllegalArgumentException if the className is empty
     */
    public static String getShortName(String className) {
        Objects.requireNonNull(className, "Class name must not be empty");
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
        return shortName;
    }

    /**
     * Get the class name without the qualified package name.
     *
     * @param clazz the class to get the short name for
     * @return the class name of the class without the package name
     */
    public static String getShortName(Class<?> clazz) {
        return getShortName(getQualifiedName(clazz));
    }

    /**
     * Return the short string name of a Java class in uncapitalized JavaBeans
     * property format. Strips the outer class name in case of an inner class.
     *
     * @param clazz the class
     * @return the short name rendered in a standard JavaBeans property format
     * @see java.beans.Introspector#decapitalize(String)
     */
    public static String getShortNameAsProperty(Class<?> clazz) {
        String shortName = getShortName(clazz);
        int dotIndex = shortName.lastIndexOf(PACKAGE_SEPARATOR);
        shortName = (dotIndex != -1 ? shortName.substring(dotIndex + 1) : shortName);
        return Introspector.decapitalize(shortName);
    }

    /**
     * Determine the name of the class file, relative to the containing
     * package: e.g. "String.class"
     *
     * @param clazz the class
     * @return the file name of the ".class" file
     */
    public static String getClassFileName(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

    /**
     * Determine the name of the package of the given class,
     * e.g. "java.lang" for the {@code java.lang.String} class.
     *
     * @param clazz the class
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return getPackageName(clazz.getName());
    }

    /**
     * Determine the name of the package of the given fully-qualified class name,
     * e.g. "java.lang" for the {@code java.lang.String} class name.
     *
     * @param fqClassName the fully-qualified class name
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(String fqClassName) {
        Objects.requireNonNull(fqClassName, "Class name must not be null");
        int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
    }

    /**
     * Return the qualified name of the given class: usually simply
     * the class name, but component type class name + "[]" for arrays.
     *
     * @param clazz the class
     * @return the qualified name of the class
     */
    public static String getQualifiedName(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return clazz.getTypeName();
    }

    /**
     * Return the qualified name of the given method, consisting of
     * fully qualified interface/class name + "." + method name.
     *
     * @param method the method
     * @return the qualified name of the method
     */
    public static String getQualifiedMethodName(Method method) {
        return getQualifiedMethodName(method, null);
    }

    /**
     * Return the qualified name of the given method, consisting of
     * fully qualified interface/class name + "." + method name.
     *
     * @param method the method
     * @param clazz  the clazz that the method is being invoked on
     *               (may be {@code null} to indicate the method's declaring class)
     * @return the qualified name of the method
     * @since 4.3.4
     */
    public static String getQualifiedMethodName(Method method, Class<?> clazz) {
        Objects.requireNonNull(method, "Method must not be null");
        return (clazz != null ? clazz : method.getDeclaringClass()).getName() + '.' + method.getName();
    }

    /**
     * Return a descriptive name for the given object's type: usually simply
     * the class name, but component type class name + "[]" for arrays,
     * and an appended list of implemented interfaces for JDK proxies.
     *
     * @param value the value to introspect
     * @return the qualified name of the class
     */

    public static String getDescriptiveType(Object value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (Proxy.isProxyClass(clazz)) {
            StringBuilder result = new StringBuilder(clazz.getName());
            result.append(" implementing ");
            Class<?>[] ifcs = clazz.getInterfaces();
            for (int i = 0; i < ifcs.length; i++) {
                result.append(ifcs[i].getName());
                if (i < ifcs.length - 1) {
                    result.append(',');
                }
            }
            return result.toString();
        } else {
            return clazz.getTypeName();
        }
    }

    /**
     * Check whether the given class matches the user-specified type name.
     *
     * @param clazz    the class to check
     * @param typeName the type name to match
     */
    public static boolean matchesTypeName(Class<?> clazz, String typeName) {
        return (typeName != null &&
                (typeName.equals(clazz.getTypeName()) || typeName.equals(clazz.getSimpleName())));
    }


    /**
     * Determine whether the given class has a public constructor with the given signature.
     * <p>Essentially translates {@code NoSuchMethodException} to "false".
     *
     * @param clazz      the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding constructor
     * @see Class#getMethod
     */
    public static boolean hasConstructor(Class<?> clazz, Class<?>... paramTypes) {
        return (getConstructorIfAvailable(clazz, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public constructor with the given signature,
     * and return it if available (else return {@code null}).
     * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
     *
     * @param clazz      the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @return the constructor, or {@code null} if not found
     * @see Class#getConstructor
     */

    public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz, Class<?>... paramTypes) {
        Objects.requireNonNull(clazz, "Class must not be null");
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Determine whether the given class has a public method with the given signature.
     * <p>Essentially translates {@code NoSuchMethodException} to "false".
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding method
     * @see Class#getMethod
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public method with the given signature,
     * and return it if available (else throws an {@code IllegalStateException}).
     * <p>In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>Essentially translates {@code NoSuchMethodException} to {@code IllegalStateException}.
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be {@code null} to indicate any signature)
     * @return the method (never {@code null})
     * @throws IllegalStateException if the method has not been found
     * @see Class#getMethod
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(methodName, "Method name must not be null");
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Expected method not found: " + ex);
            }
        } else {
            Set<Method> candidates = new HashSet<>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (candidates.isEmpty()) {
                throw new IllegalStateException("Expected method not found: " + clazz.getName() + '.' + methodName);
            } else {
                throw new IllegalStateException("No unique method found: " + clazz.getName() + '.' + methodName);
            }
        }
    }

    /**
     * Determine whether the given class has a public method with the given signature,
     * and return it if available (else return {@code null}).
     * <p>In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be {@code null} to indicate any signature)
     * @return the method, or {@code null} if not found
     * @see Class#getMethod
     */

    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(methodName, "Method name must not be null");
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        } else {
            Set<Method> candidates = new HashSet<>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    /**
     * Return the number of methods with a given name (with any argument types),
     * for the given class and/or its superclasses. Includes non-public methods.
     *
     * @param clazz      the clazz to check
     * @param methodName the name of the method
     * @return the number of methods with the given name
     */
    public static int getMethodCountForName(Class<?> clazz, String methodName) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(methodName, "Method name must not be null");
        int count = 0;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (methodName.equals(method.getName())) {
                count++;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            count += getMethodCountForName(ifc, methodName);
        }
        if (clazz.getSuperclass() != null) {
            count += getMethodCountForName(clazz.getSuperclass(), methodName);
        }
        return count;
    }

    /**
     * Does the given class or one of its superclasses at least have one or more
     * methods with the supplied name (with any argument types)?
     * Includes non-public methods.
     *
     * @param clazz      the clazz to check
     * @param methodName the name of the method
     * @return whether there is at least one method with the given name
     */
    public static boolean hasAtLeastOneMethodWithName(Class<?> clazz, String methodName) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(methodName, "Method name must not be null");
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            if (hasAtLeastOneMethodWithName(ifc, methodName)) {
                return true;
            }
        }
        return (clazz.getSuperclass() != null && hasAtLeastOneMethodWithName(clazz.getSuperclass(), methodName));
    }

    /**
     * Given a method, which may come from an interface, and a target class used
     * in the current reflective invocation, find the corresponding target method
     * if there is one. E.g. the method may be {@code IFoo.bar()} and the
     * target class may be {@code DefaultFoo}. In this case, the method may be
     * {@code DefaultFoo.bar()}.
     *
     * @param method      the method to be invoked, which may come from an interface
     * @param targetClass the target class for the current invocation.
     *                    May be {@code null} or may not even implement the method.
     * @return the specific target method, or the original method if the
     * {@code targetClass} doesn't implement it or is {@code null}
     */
    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        if (isOverridable(method, targetClass) &&
                targetClass != null && targetClass != method.getDeclaringClass()) {
            try {
                if (Modifier.isPublic(method.getModifiers())) {
                    try {
                        return targetClass.getMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException ex) {
                        return method;
                    }
                } else {
                    Method specificMethod =
                            findMethod(targetClass, method.getName(), method.getParameterTypes());
                    return (specificMethod != null ? specificMethod : method);
                }
            } catch (SecurityException ex) {
                // Security settings are disallowing reflective access; fall back to 'method' below.
            }
        }
        return method;
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied name
     * and parameter types. Searches all superclasses up to {@code Object}.
     * <p>Returns {@code null} if no {@link Method} can be found.
     *
     * @param clazz      the class to introspect
     * @param name       the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be {@code null} to indicate any signature)
     * @return the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(name, "Method name must not be null");
        Class<?> searchType = clazz;

        return getAllDeclaredMethods(clazz).parallelStream()
                .filter(method ->
                        name.equals(method.getName()) &&
                                (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))
                ).findAny().orElse(null);
    }

    /**
     * Determine whether the given method is declared by the user or at least pointing to
     * a user-declared method.
     * <p>Checks {@link Method#isSynthetic()} (for implementation methods) as well as the
     * {@code GroovyObject} interface (for interface methods; on an implementation class,
     * implementations of the {@code GroovyObject} methods will be marked as synthetic anyway).
     * Note that, despite being synthetic, bridge methods ({@link Method#isBridge()}) are considered
     * as user-level methods since they are eventually pointing to a user-declared generic method.
     *
     * @param method the method to check
     * @return {@code true} if the method can be considered as user-declared; [@code false} otherwise
     */
    public static boolean isUserLevelMethod(Method method) {
        Objects.requireNonNull(method, "Method must not be null");
        return (method.isBridge() || (!method.isSynthetic() && !isGroovyObjectMethod(method)));
    }

    private static boolean isGroovyObjectMethod(Method method) {
        return method.getDeclaringClass().getName().equals("groovy.lang.GroovyObject");
    }

    /**
     * Determine whether the given method is overridable in the given target class.
     *
     * @param method      the method to check
     * @param targetClass the target class to check against
     */
    private static boolean isOverridable(Method method, Class<?> targetClass) {
        if (Modifier.isPrivate(method.getModifiers())) {
            return false;
        }
        if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
            return true;
        }
        return (targetClass == null ||
                getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass)));
    }

    /**
     * Return a public static method of a class.
     *
     * @param clazz      the class which defines the method
     * @param methodName the static method name
     * @param args       the parameter types to the method
     * @return the static method, or {@code null} if no static method was found
     * @throws IllegalArgumentException if the method name is blank or the clazz is null
     */

    public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>... args) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(methodName, "Method name must not be null");
        try {
            Method method = clazz.getMethod(methodName, args);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }


    /**
     * Check if the given class represents a primitive wrapper,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper class
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return primitiveWrapperTypeMap.containsKey(clazz);
    }

    /**
     * Check if the given class represents a primitive (i.e. boolean, byte,
     * char, short, int, long, float, or double) or a primitive wrapper
     * (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive or primitive wrapper class
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * Check if the given class represents an array of primitives,
     * i.e. boolean, byte, char, short, int, long, float, or double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive array class
     */
    public static boolean isPrimitiveArray(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }

    /**
     * Check if the given class represents an array of primitive wrappers,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper array class
     */
    public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
    }

    /**
     * Resolve the given class if it is a primitive class,
     * returning the corresponding primitive wrapper type instead.
     *
     * @param clazz the class to check
     * @return the original class, or a primitive wrapper for the original primitive type
     */
    public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap.get(clazz) : clazz);
    }

    /**
     * Check if the right-hand side type may be assigned to the left-hand side
     * type, assuming setting by reflection. Considers primitive wrapper
     * classes as assignable to the corresponding primitive types.
     *
     * @param lhsType the target type
     * @param rhsType the value type that should be assigned to the target type
     * @return if the target type is assignable from the value type
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        Objects.requireNonNull(lhsType, "Left-hand side type must not be null");
        Objects.requireNonNull(rhsType, "Right-hand side type must not be null");
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
            if (lhsType == resolvedPrimitive) {
                return true;
            }
        } else {
            Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
            if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the given type is assignable from the given value,
     * assuming setting by reflection. Considers primitive wrapper classes
     * as assignable to the corresponding primitive types.
     *
     * @param type  the target type
     * @param value the value that should be assigned to the type
     * @return if the type is assignable from the value
     */
    public static boolean isAssignableValue(Class<?> type, Object value) {
        Objects.requireNonNull(type, "Type must not be null");
        return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
    }


    /**
     * Convert a "/"-based resource path to a "."-based fully qualified class name.
     *
     * @param resourcePath the resource path pointing to a class
     * @return the corresponding fully qualified class name
     */
    public static String convertResourcePathToClassName(String resourcePath) {
        Objects.requireNonNull(resourcePath, "Resource path must not be null");
        return resourcePath.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    /**
     * Convert a "."-based fully qualified class name to a "/"-based resource path.
     *
     * @param className the fully qualified class name
     * @return the corresponding resource path, pointing to the class
     */
    public static String convertClassNameToResourcePath(String className) {
        Objects.requireNonNull(className, "Class name must not be null");
        return className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * Return a path suitable for use with {@code ClassLoader.getResource}
     * (also suitable for use with {@code Class.getResource} by prepending a
     * slash ('/') to the return value). Built by taking the package of the specified
     * class file, converting all dots ('.') to slashes ('/'), adding a trailing slash
     * if necessary, and concatenating the specified resource name to this.
     * <br/>As such, this function may be used to build a path suitable for
     * loading a resource file that is in the same package as a class file,
     * although {org.springframework.core.io.ClassPathResource} is usually
     * even more convenient.
     *
     * @param clazz        the Class whose package will be used as the base
     * @param resourceName the resource name to append. A leading slash is optional.
     * @return the built-up resource path
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
        Objects.requireNonNull(resourceName, "Resource name must not be null");
        if (!resourceName.startsWith("/")) {
            return classPackageAsResourcePath(clazz) + '/' + resourceName;
        }
        return classPackageAsResourcePath(clazz) + resourceName;
    }

    /**
     * Given an input class object, return a string which consists of the
     * class's package name as a pathname, i.e., all dots ('.') are replaced by
     * slashes ('/'). Neither a leading nor trailing slash is added. The result
     * could be concatenated with a slash and the name of a resource and fed
     * directly to {@code ClassLoader.getResource()}. For it to be fed to
     * {@code Class.getResource} instead, a leading slash would also have
     * to be prepended to the returned value.
     *
     * @param clazz the input class. A {@code null} value or the default
     *              (empty) package will result in an empty string ("") being returned.
     * @return a path which represents the package name
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String classPackageAsResourcePath(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        if (packageEndIndex == -1) {
            return "";
        }
        String packageName = className.substring(0, packageEndIndex);
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * Build a String that consists of the names of the classes/interfaces
     * in the given array.
     * <p>Basically like {@code AbstractCollection.toString()}, but stripping
     * the "class "/"interface " prefix before every class name.
     *
     * @param classes an array of Class objects
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see java.util.AbstractCollection#toString()
     */
    public static String classNamesToString(Class<?>... classes) {
        return classNamesToString(Arrays.asList(classes));
    }

    /**
     * Build a String that consists of the names of the classes/interfaces
     * in the given collection.
     * <p>Basically like {@code AbstractCollection.toString()}, but stripping
     * the "class "/"interface " prefix before every class name.
     *
     * @param classes a Collection of Class objects (may be {@code null})
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see java.util.AbstractCollection#toString()
     */
    public static String classNamesToString(Collection<Class<?>> classes) {
        if (null == classes || classes.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Iterator<Class<?>> it = classes.iterator(); it.hasNext(); ) {
            Class<?> clazz = it.next();
            sb.append(clazz.getName());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Copy the given Collection into a Class array.
     * The Collection must contain Class elements only.
     *
     * @param collection the Collection to copy
     * @return the Class array ({@code null} if the passed-in
     * Collection was {@code null})
     */

    public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
        if (collection == null) {
            return null;
        }
        return collection.toArray(new Class<?>[collection.size()]);
    }

    /**
     * Return all interfaces that the given instance implements as an array,
     * including ones implemented by superclasses.
     *
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as an array
     */
    public static Class<?>[] getAllInterfaces(Object instance) {
        Objects.requireNonNull(instance, "Instance must not be null");
        return getAllInterfacesForClass(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as an array,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as an array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
        return getAllInterfacesForClass(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as an array,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz       the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     *                    (may be {@code null} when accepting all declared interfaces)
     * @return all interfaces that the given object implements as an array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader classLoader) {
        Set<Class<?>> ifcs = getAllInterfacesForClassAsSet(clazz, classLoader);
        return ifcs.toArray(new Class<?>[ifcs.size()]);
    }

    /**
     * Return all interfaces that the given instance implements as a Set,
     * including ones implemented by superclasses.
     *
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesAsSet(Object instance) {
        Objects.requireNonNull(instance, "Instance must not be null");
        return getAllInterfacesForClassAsSet(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as a Set,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz) {
        return getAllInterfacesForClassAsSet(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as a Set,
     * including ones implemented by superclasses.
     * <p>If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz       the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     *                    (may be {@code null} when accepting all declared interfaces)
     * @return all interfaces that the given object implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz, ClassLoader classLoader) {
        Objects.requireNonNull(clazz, "Class must not be null");
        if (clazz.isInterface() && isVisible(clazz, classLoader)) {
            return Collections.<Class<?>>singleton(clazz);
        }
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = clazz;
        while (current != null) {
            Class<?>[] ifcs = current.getInterfaces();
            for (Class<?> ifc : ifcs) {
                interfaces.addAll(getAllInterfacesForClassAsSet(ifc, classLoader));
            }
            current = current.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Create a composite interface Class for the given interfaces,
     * implementing the given interfaces in one single Class.
     * <p>This implementation builds a JDK proxy class for the given interfaces.
     *
     * @param interfaces  the interfaces to merge
     * @param classLoader the ClassLoader to create the composite Class in
     * @return the merged interface as Class
     * @see java.lang.reflect.Proxy#getProxyClass
     */
    @SuppressWarnings("deprecation")
    public static Class<?> createCompositeInterface(Class<?>[] interfaces, ClassLoader classLoader) {
        if (null == interfaces || interfaces.length == 0) {
            throw new IllegalArgumentException("Interfaces must not be empty");
        }
        return Proxy.getProxyClass(classLoader, interfaces);
    }

    /**
     * Determine the common ancestor of the given classes, if any.
     *
     * @param clazz1 the class to introspect
     * @param clazz2 the other class to introspect
     * @return the common ancestor (i.e. common superclass, one interface
     * extending the other), or {@code null} if none found. If any of the
     * given classes is {@code null}, the other class will be returned.
     * @since 3.2.6
     */

    public static Class<?> determineCommonAncestor(Class<?> clazz1, Class<?> clazz2) {
        if (clazz1 == null) {
            return clazz2;
        }
        if (clazz2 == null) {
            return clazz1;
        }
        if (clazz1.isAssignableFrom(clazz2)) {
            return clazz1;
        }
        if (clazz2.isAssignableFrom(clazz1)) {
            return clazz2;
        }
        Class<?> ancestor = clazz1;
        do {
            ancestor = ancestor.getSuperclass();
            if (ancestor == null || Object.class == ancestor) {
                return null;
            }
        }
        while (!ancestor.isAssignableFrom(clazz2));
        return ancestor;
    }

    /**
     * Check whether the given class is visible in the given ClassLoader.
     *
     * @param clazz       the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against (may be {@code null},
     *                    in which case this method will always return {@code true})
     */
    public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
        if (classLoader == null) {
            return true;
        }
        try {
            Class<?> actualClass = classLoader.loadClass(clazz.getName());
            return (clazz == actualClass);
            // Else: different interface class found...
        } catch (ClassNotFoundException ex) {
            // No interface class found...
            return false;
        }
    }

    /**
     * Check whether the given object is a CGLIB proxy.
     *
     * @param object the object to check
     * @see #isCglibProxyClass(Class)
     */
    public static boolean isCglibProxy(Object object) {
        return isCglibProxyClass(object.getClass());
    }

    /**
     * Check whether the specified class is a CGLIB-generated class.
     *
     * @param clazz the class to check
     * @see #isCglibProxyClassName(String)
     */
    public static boolean isCglibProxyClass(Class<?> clazz) {
        return (clazz != null && isCglibProxyClassName(clazz.getName()));
    }

    /**
     * Check whether the specified class name is a CGLIB-generated class.
     *
     * @param className the class name to check
     */
    public static boolean isCglibProxyClassName(String className) {
        return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Copy properties from one bean to another bean
     *
     * @param from
     * @param to
     * @param <P>
     * @param <M>
     * @Deprecated
     */
//    @Deprecated
//    public static <P, M> void beanCopy(M from, P to) {
//        if (null == from || null == to) return;
//        getAllDeclaredFields(from.getClass(), Object.class).stream()
//                .filter(field -> !Modifier.isTransient(field.getModifiers())
//                        && !Modifier.isStatic(field.getModifiers()))
//                .forEach(field -> {
//                    try {
//                        Optional.ofNullable(ReflectionUtils.getValue(from, field.getName()))
//                                .ifPresent(value -> {
//                                    try {
//                                        ReflectionUtils.setValue(to, field.getName(), value);
//                                    } catch (IllegalAccessException e) {
//                                    }
//                                });
//
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                });
//    }

}