package com.xunkutech.base.model.component.security;

import com.xunkutech.base.model.AbstractBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "base_privilege")
public class SimplePrivilegeEntity extends AbstractBaseEntity
        implements IPrivilegeEntity<SimpleRoleEntity> {

    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<SimpleRoleEntity> roles;
}
