package com.xunkutech.base.dao.repo;

import com.xunkutech.base.dao.exception.DataDuplicatedException;
import com.xunkutech.base.model.IBaseEntity;
import com.xunkutech.base.model.enums.EntityStatus;
import com.xunkutech.base.model.util.ClassUtils;
import com.xunkutech.base.model.util.CodecUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Jason Han on 5/20/2017.
 */
@NoRepositoryBean
public interface BaseEntityRepository<E extends IBaseEntity>
        extends JpaRepository<E, String>, JpaSpecificationExecutor<E> {

    //Find last one
    Optional<E> findFirstByEnableIsTrueOrderByCreatedDateDesc();

    //Find first one
    Optional<E> findFirstByEnableIsTrueOrderByCreatedDate();

    //Replacement of findById
    Optional<E> findByPrimaryCodeAndEnableIsTrue(String primaryCode);

    //Get the enabled entity by given ids
    Stream<E> findByPrimaryCodeInAndEnableIsTrue(Iterable<String> ids);

    //Default pageable query method
    Stream<E> findAllByEnableIsTrue(Pageable pageable);

    default Optional<E> getFirstOne() {
        return getFirstOne(e -> e);
    }

    default <V> Optional<V> getFirstOne(Function<E, V> function) {
        Objects.requireNonNull(function, "function");
        return findFirstByEnableIsTrueOrderByCreatedDate().map(function);
    }

    default Optional<E> getLastOne() {
        return getLastOne(e -> e);
    }

    default <V> Optional<V> getLastOne(Function<E, V> function) {
        Objects.requireNonNull(function, "function");
        return findFirstByEnableIsTrueOrderByCreatedDateDesc().map(function);
    }

    default Optional<E> getById(String id) {
        return getById(id, e -> e);
    }

    default <V> Optional<V> getById(String id, Function<E, V> function) {
        Objects.requireNonNull(function, "function");
        return findByPrimaryCodeAndEnableIsTrue(id).map(function);
    }

    default Stream<E> getAllById(Stream<String> ids) {
        return getAllById(ids, e -> e);
    }

    default <V> Stream<V> getAllById(Stream<String> ids, Function<E, V> function) {
        Objects.requireNonNull(function, "function");
        return findByPrimaryCodeInAndEnableIsTrue(ids::iterator).map(function);
    }


    default Stream<E> getAll(Pageable pageable) {
        return getAll(pageable, e -> e);
    }

    default <V> Stream<V> getAll(Pageable pageable, Function<E, V> function) {
        Objects.requireNonNull(function, "function");
        return findAllByEnableIsTrue(pageable).map(function);
    }

    @Transactional
    default E persist(E entity) {
        return persist(entity, e -> {
        }, e -> e);
    }

    @Transactional
    default <V> V persist(E entity, Consumer<E> consumer, Function<E, V> function) {
        Objects.requireNonNull(consumer, "consumer").accept(entity);
        return Objects.requireNonNull(function, "function").apply(save(entity));
    }

    @Transactional
    default E persistAndFlush(E entity) {
        return persistAndFlush(entity, e -> {
        }, e -> e);
    }

    @Transactional
    default <V> V persistAndFlush(E entity, Consumer<E> consumer, Function<E, V> function) {
        Objects.requireNonNull(consumer, "consumer").accept(entity);
        return Objects.requireNonNull(function, "function").apply(saveAndFlush(entity));
    }

    @Transactional
    default int remove(E entity) {
        return remove(entity, e -> {
        });
    }

    @Transactional
    default int remove(E entity, Consumer<E> consumer) {
        Objects.requireNonNull(consumer, "consumer").accept(entity);
        return Optional.ofNullable(entity).filter(IBaseEntity::getEnable).map(e -> {
            consumer.accept(e);
            e.setEnable(false);
            save(entity);
            return 1;
        }).orElse(0);

    }

    @Transactional
    default int removeById(String id) {
        return removeById(id, e -> {
        });
    }

    //TODO: Could improve performance
    @Transactional
    default int removeById(String id, Consumer<E> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        return findById(id).map(entity -> remove(entity, consumer)).orElse(0);
    }

    default Class<E> getEntityClass() {
        //TODO: refine it.
        Type type = ClassUtils
                .getActualTypeArguments(this.getClass(), BaseEntityRepository.class)
                .get(0);

        Objects.requireNonNull(type, "Can not resolve entity class for " +
                Arrays.stream(this.getClass().getInterfaces()).map(Class::getName).collect(Collectors.joining(", ", "{", "}")) +
                ". Make sure you put the concrete entity class in the repository declaration");
        return (Class<E>) ClassUtils.getRawType(type);
    }

    static String newUUID() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(System.currentTimeMillis());
        return "0000000000000000" + CodecUtils.toHex(byteBuffer.array()).toLowerCase() + UUID.randomUUID().toString().replace("-", "");
    }

    default E newEntity(String id) {
        try {
            id = (null == id || id.length() != 64) ? newUUID() : id;
            if (existsById(id))
                throw new DataDuplicatedException(id);
            E entity = getEntityClass().getConstructor().newInstance();
            entity.setPrimaryCode(id);
            entity.setCreatedDate(Instant.now());
            entity.setEntityStatus(EntityStatus.ACTIVE);
            entity.setEnable(true);
            return entity;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Can not happen here.");
    }

    //TODO: assume first interface inheritance
    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass().getInterfaces()[0]);
    }

}
