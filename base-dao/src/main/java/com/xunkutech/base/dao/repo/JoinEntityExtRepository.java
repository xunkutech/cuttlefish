package com.xunkutech.base.dao.repo;

import com.xunkutech.base.model.AbstractJoinBeanExt;
import com.xunkutech.base.model.IJoinEntityExt;
import com.xunkutech.base.model.IModelEntity;
import com.xunkutech.base.model.util.BeanUtils;
import com.xunkutech.base.model.util.ClassUtils;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.function.Consumer;

@NoRepositoryBean
public interface JoinEntityExtRepository<
        E extends IJoinEntityExt<M, P, ?, EA, EB>,
        M extends AbstractJoinBeanExt<P>,
        P extends M,
        EA extends IModelEntity<?, ?, ?>,
        EB extends IModelEntity<?, ?, ?>
        > extends JoinEntityRepository<E, M, P, EA, EB> {

    @Override
    @SuppressWarnings("unchecked")
    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    newEntity(T model, RA repoA, RB repoB, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = JoinEntityRepository.super.newEntity(model, repoA, repoB, preConsumer, postConsumer);
        if (ClassUtils.getRawType(entity.resolvePayloadType()).isAssignableFrom(model.getClass())) {
            entity.setPayload((P) model);
        }
        return entity;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    getEntity(T model, RA repoA, RB repoB, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = JoinEntityRepository.super.getEntity(model, repoA, repoB, preConsumer, postConsumer);
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
