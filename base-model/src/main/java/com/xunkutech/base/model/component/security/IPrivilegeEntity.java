package com.xunkutech.base.model.component.security;

import com.xunkutech.base.model.IBaseEntity;

import java.util.Collection;

public interface IPrivilegeEntity<R extends IRoleEntity> extends IBaseEntity {

    String getName();

    void setName(String name);

    Collection<R> getRoles();

    void setRoles(Collection<R> roles);
}
