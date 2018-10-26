package com.xunkutech.base.dao.component.security;

import com.xunkutech.base.dao.repo.BaseEntityRepository;
import com.xunkutech.base.model.component.security.IPrivilegeEntity;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PrivilegeEntityRepository<E extends IPrivilegeEntity<?>>
        extends BaseEntityRepository<E> {
}
