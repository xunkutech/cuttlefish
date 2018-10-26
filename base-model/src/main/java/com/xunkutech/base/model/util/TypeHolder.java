package com.xunkutech.base.model.util;

import com.xunkutech.base.model.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * The helper class help to store the class type in persistent layer to assist construct new instance object from
 * the class.
 */
@Getter
@Setter
public class TypeHolder implements JsonSerializable {

    public TypeHolder(String typeName, TypeType typeType, TypeHolder arg1, TypeHolder[] arg2, TypeHolder[] arg3) {

        Objects.requireNonNull(typeName, "typeName is null");
        Objects.requireNonNull(typeType, "typeType is null");
        this.typeName = typeName;
        this.typeType = typeType;

        switch (typeType) {
            case CLASS:
                break;
            case GENERIC_ARRAY_TYPE:
                componentType = arg1;
                break;
            case PARAMETERIZED_TYPE:
                ownerType = arg1;
                typeArguments = arg2;
                break;
            case WILDCARD_TYPE:
                upperBoundsTypes = arg2;
                lowerBoundsTypes = arg3;
                break;
            case TYPE_VARIABLE:
                break;
            default:
        }
    }

    private String typeName;
    private TypeType typeType;
    private TypeHolder ownerType;
    private TypeHolder componentType;
    private TypeHolder[] upperBoundsTypes;
    private TypeHolder[] lowerBoundsTypes;
    private TypeHolder[] typeArguments;

    @SuppressWarnings("unchecked")
    public Type getType() {
        Type resolvedType = null;
        try {
            switch (typeType) {
                case CLASS:
                    resolvedType = ClassUtils.getDefaultClassLoader().loadClass(typeName);
                    break;
                case GENERIC_ARRAY_TYPE:
                    resolvedType = new GenericArrayTypeMockImpl(componentType.getType());
                    break;
                case PARAMETERIZED_TYPE:
                    resolvedType = new ParameterizedTypeMockImpl(null == ownerType ? null : ownerType.getType(),
                            ClassUtils.getDefaultClassLoader().loadClass(typeName),
                            null == typeArguments ? null : Arrays.stream(typeArguments)
                                    .map(TypeHolder::getType)
                                    .toArray(Type[]::new));
                    break;
                case WILDCARD_TYPE:
                    resolvedType = new WildcardTypeMockImpl(null == upperBoundsTypes ? null : Arrays.stream(upperBoundsTypes)
                            .map(TypeHolder::getType)
                            .toArray(Type[]::new),
                            null == lowerBoundsTypes ? null : Arrays.stream(lowerBoundsTypes)
                                    .map(TypeHolder::getType)
                                    .toArray(Type[]::new));
                    break;
                case TYPE_VARIABLE:
                    resolvedType = Object.class;
                    break;
                default:
                    throw new IllegalStateException("can not happen here");
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
        return resolvedType;

    }

    public static TypeHolder fromType(Type type) {
        if (null == type) {
            return null;
        }

        String typeName;
        TypeType typeType;
        TypeHolder arg1 = null;
        TypeHolder[] arg2 = null;
        TypeHolder[] arg3 = null;

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            typeName = parameterizedType.getRawType().getTypeName();
            typeType = TypeType.PARAMETERIZED_TYPE;
            arg1 = (null != parameterizedType.getOwnerType()) ? fromType(parameterizedType.getOwnerType()) : null;
            arg2 = Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(TypeHolder::fromType)
                    .toArray(TypeHolder[]::new);
        } else if (type instanceof GenericArrayType) {
            typeName = type.getTypeName();
            typeType = TypeType.GENERIC_ARRAY_TYPE;
            arg1 = fromType(((GenericArrayType) type).getGenericComponentType());
        } else if (type instanceof TypeVariable) {
            //TODO: Skip GenericDeclaration
            typeName = type.getTypeName();
            typeType = TypeType.TYPE_VARIABLE;
        } else if (type instanceof WildcardType) {
            typeName = type.getTypeName();
            typeType = TypeType.WILDCARD_TYPE;
            arg2 = Arrays.stream(((WildcardType) type).getUpperBounds())
                    .map(TypeHolder::fromType)
                    .toArray(TypeHolder[]::new);
            arg3 = Arrays.stream(((WildcardType) type).getLowerBounds())
                    .map(TypeHolder::fromType)
                    .toArray(TypeHolder[]::new);
        } else if (type instanceof Class) {
            typeName = type.getTypeName();
            Class<?> c = (Class<?>) type;
            if (c.isArray()) {
                typeType = TypeType.GENERIC_ARRAY_TYPE;
                arg1 = fromType(c.getComponentType());
            } else {
                typeType = TypeType.CLASS;
            }
        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + className);
        }

        return new TypeHolder(typeName, typeType, arg1, arg2, arg3);
    }

    static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Returns true if {@code a} and {@code b} are equal.
     */
    static boolean equals(Type a, Type b) {
        if (a == b) {
            // also handles (a == null && b == null)
            return true;

        } else if (a instanceof Class) {
            // Class already specifies equals().
            return a.equals(b);

        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            // TODO: save a .clone() call
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equal(pa.getOwnerType(), pb.getOwnerType())
                    && pa.getRawType().equals(pb.getRawType())
                    && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) {
                return false;
            }

            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                    && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) {
                return false;
            }
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration()
                    && va.getName().equals(vb.getName());

        } else {
            // This isn't a type we support. Could be a generic array type, wildcard type, etc.
            return false;
        }
    }

    static int hashCodeOrZero(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }

    enum TypeType {
        CLASS, GENERIC_ARRAY_TYPE, PARAMETERIZED_TYPE, TYPE_VARIABLE, WILDCARD_TYPE
    }

    @AllArgsConstructor
    @Getter
    public static final class ParameterizedTypeMockImpl implements ParameterizedType, Serializable {
        private Type ownerType;
        private Type rawType;
        private Type[] actualTypeArguments;

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType
                    && TypeHolder.equals(this, (ParameterizedType) other);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualTypeArguments)
                    ^ rawType.hashCode()
                    ^ hashCodeOrZero(ownerType);
        }

        @Override
        public String toString() {
            int length = actualTypeArguments.length;
            if (length == 0) {
                return typeToString(rawType);
            }

            StringBuilder stringBuilder = new StringBuilder(30 * (length + 1));
            stringBuilder.append(typeToString(rawType)).append("<").append(typeToString(actualTypeArguments[0]));
            for (int i = 1; i < length; i++) {
                stringBuilder.append(", ").append(typeToString(actualTypeArguments[i]));
            }
            return stringBuilder.append(">").toString();
        }

        private static final long serialVersionUID = 0;
    }

    @AllArgsConstructor
    @Getter
    public static final class GenericArrayTypeMockImpl implements GenericArrayType, Serializable {
        private Type genericComponentType;

        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType
                    && TypeHolder.equals(this, (GenericArrayType) o);
        }

        @Override
        public int hashCode() {
            return genericComponentType.hashCode();
        }

        @Override
        public String toString() {
            return typeToString(genericComponentType) + "[]";
        }

        private static final long serialVersionUID = 0;
    }

    static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    static void checkNotPrimitive(Type type) {
        checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());
    }

    public static final class WildcardTypeMockImpl implements WildcardType, Serializable {
        final Type upperBound;
        private final Type lowerBound;

        public WildcardTypeMockImpl(Type[] upperBounds, Type[] lowerBounds) {
            checkArgument(lowerBounds.length <= 1);
            checkArgument(upperBounds.length == 1);

            if (lowerBounds.length == 1) {
                checkNotNull(lowerBounds[0]);
                checkNotPrimitive(lowerBounds[0]);
                checkArgument(upperBounds[0] == Object.class);
                this.lowerBound = lowerBounds[0];
                this.upperBound = Object.class;

            } else {
                checkNotNull(upperBounds[0]);
                checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = upperBounds[0];
            }
        }

        public Type[] getUpperBounds() {
            return new Type[]{upperBound};
        }

        public Type[] getLowerBounds() {
            return lowerBound != null ? new Type[]{lowerBound} : new Type[]{};
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType
                    && TypeHolder.equals(this, (WildcardType) other);
        }

        @Override
        public int hashCode() {
            // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1)
                    ^ (31 + upperBound.hashCode());
        }

        @Override
        public String toString() {
            if (lowerBound != null) {
                return "? super " + typeToString(lowerBound);
            } else if (upperBound == Object.class) {
                return "?";
            } else {
                return "? extends " + typeToString(upperBound);
            }
        }

        private static final long serialVersionUID = 0;
    }

    public static void main(String[] args) throws Exception {

        Test2 t2 = new Test2();
        System.out.println(fromType(t2.getClass()).printJson());
        System.out.println(fromType(t2.getClass().getDeclaredField("bbb").getGenericType()).printJson());

        TypeHolder tp = fromType(t2.getClass().getDeclaredField("bbb").getGenericType());

        System.out.println(fromType(tp.getType()).printJson());
        Test1<String> t1 = new Test1();
        System.out.println(fromType(t2.getClass().getSuperclass().getDeclaredField("x").getType()).printJson());
        Map mp = JsonUtils.<Map<?, ?>>fromJson("{}", t2.getClass().getSuperclass().getDeclaredField("ms").getType());
        mp.put("aa", "bb");
        System.out.println(JsonUtils.toJson(mp));
        t2.setBbb(new String[]{"aaa", "bbb", "ccc"});
        System.out.println(t2.printJson());
        System.out.println(JsonUtils.<Test2>fromJson(t2.printJson(), fromType(Test2.class).getType()).printJson());

    }

    static class Test1<X> {
        public X x;
        public Map<String, X> ms;
    }

    @Getter
    @Setter
    static class Test2 extends Test1<String> implements JsonSerializable {
        public Map mm;

        public String[] bbb;
    }
}
