package com.xunkutech.base.model.util;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.xunkutech.base.model.AbstractBaseEntity;

import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

import static java.lang.reflect.Modifier.STATIC;
import static java.lang.reflect.Modifier.TRANSIENT;

/**
 * Created by jason on 7/17/17.
 */
public abstract class JsonUtils {

    public static Gson PRETTY_PRINT_GSON;
    public static Gson GSON;
    public static GsonBuilder GSON_BUILDER;

    private static class ByteArraySerializer
            implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return CodecUtils.fromBase64(json.getAsString());
            } catch (CodecException e) {
                return null;
            }
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(CodecUtils.toBase64(src));
        }
    }

    private static class LocaleSerializer
            implements JsonSerializer<Locale>, JsonDeserializer<Locale> {

        @Override
        public Locale deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Locale.forLanguageTag(json.getAsString());
        }

        @Override
        public JsonElement serialize(Locale src, Type typeOfSrc, JsonSerializationContext context) {
            if (null != src) {
                return new JsonPrimitive(src.toLanguageTag());
            } else {
                return null;
            }
        }
    }

//    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
//    private static class DateSerializer
//            implements JsonSerializer<Date>, JsonDeserializer<Date> {
//
//        private static final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
//
//        @Override
//        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
//                throws JsonParseException {
//            try {
//                return dateFormat.parse(json.getAsString());
//            } catch (ParseException e) {
//                throw new IllegalArgumentException();
//            }
//        }
//
//        @Override
//        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
//            if (null != src) {
//                return new JsonPrimitive(dateFormat.format(src));
//            } else {
//                return null;
//            }
//        }
//    }

    private static class InstantSerializer
            implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return Instant.parse(json.getAsString());
            } catch (DateTimeParseException e) {
                return null;
            }
        }

        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            if (null != src) {
                return new JsonPrimitive(src.toString());
            }
            return null;
        }
    }

    public static class EntityAdapterFactory implements TypeAdapterFactory {

        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (!AbstractBaseEntity.class.isAssignableFrom(rawType)) {
                return null;
            }

            return new TypeAdapter<T>() {
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(((AbstractBaseEntity) value).getPrimaryCode());
                    }
                }

                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return null;
                    } else {
                        T instance = null;
                        String id = reader.nextString();
                        try {
                            instance = rawType.newInstance();
                            ((AbstractBaseEntity) instance).setPrimaryCode(id);
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        return instance;
                    }
                }
            };
        }
    }

    private static class LowercaseEnumTypeAdapterFactory implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (!rawType.isEnum()) {
                return null;
            }

            final Map<String, T> lowercaseToConstant = new HashMap<String, T>();
            for (T constant : rawType.getEnumConstants()) {
                lowercaseToConstant.put(toLowercase(constant), constant);
            }

            return new TypeAdapter<T>() {
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(toLowercase(value));
                    }
                }

                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return null;
                    } else {
                        return lowercaseToConstant.get(reader.nextString());
                    }
                }
            };
        }

        private String toLowercase(Object o) {
            return o.toString().toLowerCase(Locale.US);
        }
    }

    private static class CustomerExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {

            OneToMany jpaOneToManyAnnotation = f.getAnnotation(OneToMany.class);
            if (null != jpaOneToManyAnnotation) {
                return true;
            }

            Transient jpaTransient = f.getAnnotation(Transient.class);
            if (null != jpaTransient) {
                return true;
            }
            return false;
        }
    }

    static {
        GSON_BUILDER = new GsonBuilder()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .excludeFieldsWithModifiers(TRANSIENT, STATIC)
                .setExclusionStrategies(new CustomerExclusionStrategy())
                .registerTypeAdapter(Instant.class, new InstantSerializer())
                .registerTypeAdapter(byte[].class, new ByteArraySerializer())
                .registerTypeAdapter(Locale.class, new LocaleSerializer())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
//                .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
//                    if (src == src.longValue())
//                        return new JsonPrimitive(src.longValue());
//                    return new JsonPrimitive(src);
//                })
                .registerTypeAdapter(Map.class, new JsonDeserializer<Map<String, Object>>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return (Map<String, Object>) read(json);
                    }

                    public Object read(JsonElement in) {
                        if (in.isJsonArray()) {
                            List<Object> list = new ArrayList<>();
                            JsonArray arr = in.getAsJsonArray();
                            for (JsonElement anArr : arr) {
                                list.add(read(anArr));
                            }
                            return list;
                        } else if (in.isJsonObject()) {
                            Map<String, Object> map = new LinkedTreeMap<>();
                            JsonObject obj = in.getAsJsonObject();
                            Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
                            for (Map.Entry<String, JsonElement> entry : entitySet) {
                                map.put(entry.getKey(), read(entry.getValue()));
                            }
                            return map;
                        } else if (in.isJsonPrimitive()) {
                            JsonPrimitive prim = in.getAsJsonPrimitive();
                            if (prim.isBoolean()) {
                                return prim.getAsBoolean();
                            } else if (prim.isString()) {
                                return prim.getAsString();
                            } else if (prim.isNumber()) {
                                Number num = prim.getAsNumber();
                                // here you can handle double int/long values
                                // and return any type you want
                                // this solution will transform 3.0 float to long values
                                if (Math.ceil(num.doubleValue()) == num.longValue())
                                    return num.longValue();
                                else {
                                    return num.doubleValue();
                                }
                            }
                        }
                        return null;
                    }
                })
        ;

        GSON = GSON_BUILDER.create();
        PRETTY_PRINT_GSON = GSON_BUILDER.setPrettyPrinting().create();
    }

    public static String toJson(Object src) {
        return GSON.toJson(src);
    }

    public static String printJson(Object src) {
        return PRETTY_PRINT_GSON.toJson(src);
    }

    public static <T> T fromJson(String src, Class<T> classOfT) {
        if (null == src || src.isEmpty() || null == classOfT) return null;
        return GSON.fromJson(src, classOfT);
    }

    public static <T> T fromJson(String src, Type type) {
        if (null == src || src.isEmpty() || null == type) return null;
        try {
            return GSON.fromJson(src, type);
        } catch (JsonParseException e) {
            return null;
        }
    }


    public static <T> String toAscii(T src) {
        return CodecUtils.toAscii(toJson(src));
    }

    public static <T> T fromAscii(String src, Class<T> classOfT) {
        return fromJson(CodecUtils.fromAscii(src), classOfT);
    }

    public static <T> T fromAscii(String src, Type type) {
        return fromJson(CodecUtils.fromAscii(src), type);
    }

    public static <T> byte[] toBin(T src) {
        if (null == src) return null;
        return toJson(src).getBytes();
    }

    public static <T> T fromBin(byte[] src, Class<T> classOfT) {
        if (null == src || src.length == 0) return null;
        return fromJson(new String(src), classOfT);
    }

    public static <T> T fromBin(byte[] src, Type type) {
        if (null == src || src.length == 0) return null;
        return fromJson(new String(src), type);
    }

    public static <T> String toBase64(T src) {
        return CodecUtils.toBase64(toBin(src));
    }

    public static <T> T fromBase64(String src, Class<T> classOfT) {
        try {
            return fromBin(CodecUtils.fromBase64(src), classOfT);
        } catch (CodecException e) {
            return null;
        }
    }

    public static <T> T fromBase64(String src, Type type) {
        try {
            return fromBin(CodecUtils.fromBase64(src), type);
        } catch (CodecException e) {
            return null;
        }
    }


    /**
     * 将对象转换成json，并用gzip进行压缩
     *
     * @param src
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> byte[] toGzip(T src) {
        if (null == src) return new byte[0];
        return CodecUtils.toGzip(toBin(src));
    }

    /**
     * 从Gzip压缩字节数组中返回对象
     *
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T fromGzip(byte[] data, Class<T> classOfT) {
        try {
            return fromBin(CodecUtils.fromGzip(data), classOfT);
        } catch (CodecException e) {
            return null;
        }
    }

    public static <T> T fromGzip(byte[] data, Type type) {
        try {
            return fromBin(CodecUtils.fromGzip(data), type);
        } catch (CodecException e) {
            return null;
        }
    }

    /**
     * 将对象转换成json，并用gzip进行压缩，再用base64进行编码
     *
     * @param src
     * @param <T>
     * @return
     */
    public static <T> String toGzipBase64(T src) {
        return CodecUtils.toBase64(toGzip(src));
    }

    /**
     * 从Gzip压缩字节数组且被编码成BASE64字符串中返回对象
     *
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> T fromGzipBase64(String data, Class<T> classOfT) {
        try {
            return fromGzip(CodecUtils.fromBase64(data), classOfT);
        } catch (CodecException e) {
            return null;
        }
    }

    public static <T> T fromGzipBase64(String data, Type type) {
        try {
            return fromGzip(CodecUtils.fromBase64(data), type);
        } catch (CodecException e) {
            return null;
        }
    }
}
