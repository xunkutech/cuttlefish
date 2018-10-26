package com.xunkutech.base.dao.component.security;

import com.xunkutech.base.dao.repo.BaseEntityRepository;
import com.xunkutech.base.model.component.security.IRoleEntity;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface RoleEntityRepository<E extends IRoleEntity<?>>
        extends BaseEntityRepository<E> {
}
