package com.xunkutech.base.dao.repo;

import com.xunkutech.base.dao.exception.DataNotExistException;
import com.xunkutech.base.model.AbstractModelBean;
import com.xunkutech.base.model.IModelEntity;
import com.xunkutech.base.model.util.BeanUtils;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by jason on 16-2-13.
 */
@NoRepositoryBean
public interface ModelEntityRepository<
        E extends IModelEntity<M, P, ?>,
        M extends AbstractModelBean<P>,
        P> extends BaseEntityRepository<E> {

    default Function<M, E> newEntityByModel() {
        return m -> {
            Objects.requireNonNull(m);
            E entity = newEntity(m.getEntityCode());
            entity.setModel(m);
            entity.setPayload(m.getPayload());
            return entity;
        };
    }

    default Function<M, E> attachEntityByModel() {
        return m -> {
            E entity = findById(Objects.requireNonNull(m).getEntityCode())
                    .orElseThrow(() -> new DataNotExistException(m.getEntityCode()));
            // replica model properties
            BeanUtils.deepCopy(m, entity.getModel(), entity.resolveModelType());

            if (null != m.getPayload()) {
                P payload = entity.getPayload();
                if (null == payload) {
                    entity.setPayload(m.getPayload());
                } else {
                    BeanUtils.deepCopy(m.getPayload(), payload, entity.resolvePayloadType());
                    entity.setPayload(payload);
                }
            }
            return entity;
        };
    }

    /**
     * 创建实体类对象
     *
     * @param model
     * @return
     */
    default <T extends M> E newEntity(T model) {
        return newEntity(model, m -> {
        }, e -> {
        });
    }

    /**
     * 创建实体类对象
     *
     * @param model
     * @return
     */
    default <T extends M> E newEntity(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        Objects.requireNonNull(preConsumer, "preConsumer").accept(model);
        E entity = newEntityByModel().apply(model);
        Objects.requireNonNull(postConsumer, "postConsumer").accept(entity);
        return entity;
    }

    /**
     * 取回实体类对象，并对实体类对象进行赋值
     *
     * @param model
     * @return
     */
    default <T extends M> E getEntity(T model) {
        return getEntity(model, m -> {
        }, e -> {
        });
    }

    default <T extends M> E getEntity(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        Objects.requireNonNull(preConsumer, "preConsumer").accept(model);
        E entity = attachEntityByModel().apply(model);
        Objects.requireNonNull(postConsumer, "postConsumer").accept(entity);
        return entity;
    }

    default <T extends M> E acquireEntity(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        Objects.requireNonNull(model, "model is null");
        return null == model.getEntityCode() ?
                newEntity(model, preConsumer, postConsumer)
                : getEntity(model, preConsumer, postConsumer);
    }

    @Transactional
    default <T extends M> E persistAndFlush(T model) {
        return persistAndFlush(model, m -> {
        }, e -> {
        });
    }

    @Transactional
    default <T extends M> E persistAndFlush(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = acquireEntity(model, preConsumer, postConsumer);
        entity = saveAndFlush(entity);
        return entity;
    }

    @Transactional
    default <T extends M> E persist(T model) {
        return persist(model, m -> {
        }, e -> {
        });
    }

    @Transactional
    default <T extends M> E persist(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = acquireEntity(model, preConsumer, postConsumer);
        entity = persist(entity);
        return entity;
    }

}
