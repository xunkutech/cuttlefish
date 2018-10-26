package com.xunkutech.base.dao.repo;

import com.xunkutech.base.model.AbstractModelBeanExt;
import com.xunkutech.base.model.IModelEntityExt;
import com.xunkutech.base.model.util.BeanUtils;
import com.xunkutech.base.model.util.ClassUtils;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.function.Consumer;

@NoRepositoryBean
public interface ModelEntityExtRepository<
        E extends IModelEntityExt<M, P, ?>,
        M extends AbstractModelBeanExt<P>,
        P extends M
        > extends ModelEntityRepository<E, M, P> {

    @Override
    @SuppressWarnings("unchecked")
    default <T extends M> E newEntity(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = ModelEntityRepository.super.newEntity(model, preConsumer, postConsumer);
        if (ClassUtils.getRawType(entity.resolvePayloadType()).isAssignableFrom(model.getClass())) {
            entity.setPayload((P) model);
        }
        return entity;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <T extends M> E getEntity(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = ModelEntityRepository.super.getEntity(model, preConsumer, postConsumer);
        if (ClassUtils.getRawType(entity.resolvePayloadType()).isAssignableFrom(model.getClass())) {
            P payload = entity.getPayload();
            if (null == payload) {
                entity.setPayload((P) model);
            } else {
                BeanUtils.deepCopy(model, payload, entity.resolvePayloadType());
                entity.setPayload(payload);
            }
        }
        return entity;
    }

}
