package com.xunkutech.base.dao.component.sequence;

import com.xunkutech.base.dao.exception.MaxRetryReachedException;
import com.xunkutech.base.dao.repo.BaseEntityRepository;
import com.xunkutech.base.model.component.sequence.SequenceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jason on 7/16/17.
 */
public interface SequenceEntityRepository extends BaseEntityRepository<SequenceEntity> {

    SequenceEntity findByNameAndEnableIsTrue(String name);

    Logger logger = LoggerFactory.getLogger(SequenceEntityRepository.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    default Long nextValue(String name) throws MaxRetryReachedException {

        SequenceEntity entity = findByNameAndEnableIsTrue(name);

        if (null == entity) {
            logger.info("Create new sequence {}", name);

            entity = newEntity(null);
            entity.setName(name);
            entity.setSequence(0L);
            save(entity);
            return 0L;
        }

        int retryCount = 0;

        while (retryCount++ < 10) {
            try {
                entity = findByNameAndEnableIsTrue(name);
                entity.setSequence(entity.getSequence() + 1);
                entity = save(entity);
                return entity.getSequence();
            } catch (OptimisticLockingFailureException e) {
                logger.warn("Version conflict detected, retrying - " + e.getMessage());
                try {
                    Thread.sleep(10 * retryCount);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        throw new MaxRetryReachedException("Can't get sequence.");
    }
}
