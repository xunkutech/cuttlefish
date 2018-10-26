package com.xunkutech.base.dao.repo;

import com.xunkutech.base.model.AbstractJoinBean;
import com.xunkutech.base.model.IJoinEntity;
import com.xunkutech.base.model.IModelEntity;
import com.xunkutech.base.model.SimpleJoinObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Jason Han on 5/20/2017.
 */
@NoRepositoryBean
public interface JoinEntityRepository<
        E extends IJoinEntity<M, P, ?, EA, EB>,
        M extends AbstractJoinBean<P>,
        P,
        EA extends IModelEntity<?, ?, ?>,
        EB extends IModelEntity<?, ?, ?>
        > extends ModelEntityRepository<E, M, P> {

    @Query(" select new com.xunkutech.base.model.SimpleJoinObject(primaryCode, entityA.primaryCode, entityB.primaryCode) " +
            "from #{#entityName} " +
            "where enable=true " +
            "and entityA.primaryCode in :ids")
    Stream<SimpleJoinObject> findSimpleByACodeIn(@Param("ids") Iterable<String> aCodes, Pageable pageable);

    @Query(" select new com.xunkutech.base.model.SimpleJoinObject(primaryCode, entityA.primaryCode, entityB.primaryCode) " +
            "from #{#entityName} " +
            "where enable=true " +
            "and entityB.primaryCode in :ids")
    Stream<SimpleJoinObject> findSimpleByBCodeIn(@Param("ids") Iterable<String> bCodes, Pageable pageable);

    @Query(" select new com.xunkutech.base.model.SimpleJoinObject(primaryCode, entityA.primaryCode, entityB.primaryCode) " +
            "from #{#entityName} " +
            "where enable=true " +
            "and primaryCode in :ids")
    Stream<SimpleJoinObject> findSimpleByPrimaryCodeIn(@Param("ids") Iterable<String> primaryCodes);

    @Override
    @Deprecated
    default <T extends M> E newEntity(T model) {
        throw new IllegalStateException("Bad usage");
    }

    @Override
    @Deprecated
    default <T extends M> E newEntity(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        throw new IllegalStateException("Bad usage");
    }

    @Override
    @Deprecated
    default <T extends M> E getEntity(T model) {
        throw new IllegalStateException("Bad usage.");
    }

    @Override
    @Deprecated
    default <T extends M> E getEntity(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        throw new IllegalStateException("Bad usage.");
    }

    @Override
    @Deprecated
    @Transactional
    default <T extends M> E persistAndFlush(T model) {
        throw new IllegalStateException("Bad usage.");
    }

    @Override
    @Deprecated
    @Transactional
    default <T extends M> E persistAndFlush(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        throw new IllegalStateException("Bad usage.");
    }

    @Override
    @Deprecated
    @Transactional
    default <T extends M> E persist(T model) {
        throw new IllegalStateException("Bad usage.");
    }

    @Override
    @Deprecated
    @Transactional
    default <T extends M> E persist(T model, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        throw new IllegalStateException("Bad usage.");
    }

    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    newEntity(T model, RA repoA, RB repoB) {
        return newEntity(model, repoA, repoB, m -> {
        }, e -> {
        });
    }

    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    newEntity(T model, RA repoA, RB repoB, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = ModelEntityRepository.super.newEntity(model, preConsumer, postConsumer);
        repoA.findById(model.getCodeA()).ifPresent(entity::setEntityA);
        repoB.findById(model.getCodeB()).ifPresent(entity::setEntityB);
        return entity;
    }

    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    getEntity(T model, RA repoA, RB repoB) {
        return getEntity(model, repoA, repoB, m -> {
        }, e -> {
        });
    }

    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    getEntity(T model, RA repoA, RB repoB, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = ModelEntityRepository.super.getEntity(model, preConsumer, postConsumer);
        if (null != model.getCodeA()
                && (null == entity.getEntityA() || !model.getCodeA().equals(entity.getEntityA().getPrimaryCode()))) {
            repoA.findById(model.getCodeA()).ifPresent(entity::setEntityA);
        }

        if (null != model.getCodeB()
                && (null == entity.getEntityB() || !model.getCodeB().equals(entity.getEntityB().getPrimaryCode()))) {
            repoB.findById(model.getCodeB()).ifPresent(entity::setEntityB);
        }
        return entity;
    }

    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    acquireEntity(T model, RA repoA, RB repoB, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        Objects.requireNonNull(model, "model is null");
        return null == model.getEntityCode() ?
                newEntity(model, repoA, repoB, preConsumer, postConsumer)
                : getEntity(model, repoA, repoB, preConsumer, postConsumer);
    }

    @Transactional
    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    persistAndFlush(T model, RA repoA, RB repoB) {
        return persistAndFlush(model, repoA, repoB, m -> {
        }, e -> {
        });
    }

    @Transactional
    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    persistAndFlush(T model, RA repoA, RB repoB, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = acquireEntity(model, repoA, repoB, preConsumer, postConsumer);
        entity = saveAndFlush(entity);
        return entity;
    }

    @Transactional
    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    persist(T model, RA repoA, RB repoB) {
        return persist(model, repoA, repoB, m -> {
        }, e -> {
        });
    }

    @Transactional
    default <T extends M, RA extends BaseEntityRepository<? extends EA>, RB extends BaseEntityRepository<? extends EB>> E
    persist(T model, RA repoA, RB repoB, Consumer<? super T> preConsumer, Consumer<? super E> postConsumer) {
        E entity = acquireEntity(model, repoA, repoB, preConsumer, postConsumer);
        entity = persist(entity);
        return entity;
    }

}
