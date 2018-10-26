package com.xunkutech.base.model;

import com.xunkutech.base.model.util.ClassUtils;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Collections;
import java.util.List;

/**
 * Created by jason on 7/3/17.
 */
public class DefaultEntityListener {


    private <E extends AbstractBaseEntity> void logEvent(E entity, String phase) {

        List<Class<? extends AbstractBaseEntity>> chain = ClassUtils.getSuperclassChain(entity.getClass(), AbstractBaseEntity.class);

        Collections.reverse(chain);

        StringBuffer header = new StringBuffer(phase).append('\n');

        StringBuffer prefix = new StringBuffer("");

        for (int i = 0; i < chain.size(); i++) {
            header.append(prefix).append("\\- ").append(chain.get(i).getCanonicalName()).append("\n");
            prefix.append(" ");
        }
        header.append(entity.printJson());
        System.out.println(header);
    }

    @PrePersist
    private <E extends AbstractBaseEntity> void prePersist(E entity) {
        logEvent(entity, "========PrePersist========");
    }

    @PreUpdate
    private <E extends AbstractBaseEntity> void preUpdate(E entity) {
        logEvent(entity, "========PreUpdate========");
    }

}
