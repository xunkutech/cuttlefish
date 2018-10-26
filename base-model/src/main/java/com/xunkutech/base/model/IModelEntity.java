package com.xunkutech.base.model;

import com.xunkutech.base.model.util.ClassUtils;
import com.xunkutech.base.model.util.JsonUtils;
import com.xunkutech.base.model.util.TypeHolder;

import java.lang.reflect.Type;

/**
 * Created by jason on 7/16/17.
 */
public interface IModelEntity<
        M extends AbstractModelBean<P>,
        P,
        EP extends IPayloadEntity
        > extends IBaseEntity {

    String getPrimaryCode();

    M getModel();

    void setModel(M bean);

    EP getPayloadEntity();

    void setPayloadEntity(EP payloadEntity);

    Class<EP> getPayloadEntityClass();

    void setPayloadEntityClass(Class<EP> payloadEntityClass);

    Type getPayloadType();

    void setPayloadType(Type payloadType);

    Type getModelType();

    void setModelType(Type modelType);

    String getPayloadTypeHolder();

    void setPayloadTypeHolder(String payloadTypeHolder);

    @SuppressWarnings("unchecked")
    default Class<EP> resolvePayloadEntityClass() {
        // try cache
        Class<EP> clz = getPayloadEntityClass();
        if (null != clz) return clz;

        // obtain from parameterized type
        Type type = ClassUtils
                .getActualTypeArguments(this.getClass(), IModelEntity.class)
                .get(2);
        clz = (Class<EP>) ClassUtils.getRawType(type);
        setPayloadEntityClass(clz);
        return clz;
    }

    default Type resolveModelType() {
        // try cache
        Type modelType = getModelType();
        if (null != modelType) return modelType;

        // try parameter type
        modelType = ClassUtils
                .getActualTypeArguments(this.getClass(), IModelEntity.class)
                .get(0);

        setModelType(modelType);
        return modelType;
    }

    /**
     * For jpa implementation. The entity class (including embedded class) can not be the generic type. However,
     * the payload object can be generic if we could resolve its type at runtime. The best practise is that the
     * payload object implement @link TypeProvide interface that provide its type itself.
     *
     * @return
     */
    default Type resolvePayloadType() {

        // Try cache
        Type payloadType = getPayloadType();
        if (null != payloadType) return payloadType;

        // Try parameter type
        payloadType = ClassUtils
                .getActualTypeArguments(this.getClass(), IModelEntity.class)
                .get(1);
        if (null != payloadType) {
            setPayloadType(payloadType);
            return payloadType;
        }

        // Try TypeHolder
        TypeHolder typeHolder = JsonUtils.fromJson(getPayloadTypeHolder(), TypeHolder.class);
        if (null != typeHolder) {
            payloadType = typeHolder.getType();
            setPayloadType(payloadType);
            return payloadType;
        }

        return null;
    }

    default M detach() {
        M model = getModel();
        model.setEntityCode(getPrimaryCode());
        model.setPayload(getPayload());
        return model;
    }

    default P getPayload() {
        Class<EP> payloadEntityClass = resolvePayloadEntityClass();
        if (NonPersistent.class.isAssignableFrom(payloadEntityClass)) return null;

        if (null == getPayloadEntity()) return null;
        Type payloadType = resolvePayloadType();
        if (null == payloadType) return null;
        return JsonUtils.fromJson(getPayloadEntity().getJsonText(), payloadType);
    }


    default void setPayload(P payload) {
        Class<EP> payloadEntityClass = resolvePayloadEntityClass();
        if (NonPersistent.class.isAssignableFrom(payloadEntityClass)) return;

        // This is the last chance we can resolve payload type
        Type payloadType = resolvePayloadType();

        if (null == payloadType && null != payload) {
            if (payload instanceof TypeProvider) {
                payloadType = ((TypeProvider) payload).getType();
            } else {
                // Warning: if payload is parameterized type, we cannot resolve it.
                payloadType = payload.getClass();
            }

            setPayloadType(payloadType);
            setPayloadTypeHolder(TypeHolder.fromType(payloadType).toJson());
        }

        if (null == payload) {
            setPayloadEntity(null);
            return;
        }

        EP payloadEntity = getPayloadEntity();

        if (null == payloadEntity) {
            payloadEntity = IPayloadEntity.newEntity(payloadEntityClass, getPrimaryCode());
            setPayloadEntity(payloadEntity);
        }

        payloadEntity.setJsonText(JsonUtils.toJson(payload));
    }

}
