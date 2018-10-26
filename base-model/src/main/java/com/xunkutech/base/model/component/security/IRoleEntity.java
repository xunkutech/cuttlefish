package com.xunkutech.base.model.component.security;

import com.xunkutech.base.model.IBaseEntity;

import java.util.Collection;

public interface IRoleEntity<P extends IPrivilegeEntity> extends IBaseEntity {

    String getName();

    void setName(String name);

    Collection<P> getPrivileges();

    void setPrivileges(Collection<P> privileges);
}
