package com.xunkutech.base.model.component.security;

import com.xunkutech.base.model.AbstractBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;


@Getter
@Setter
@Entity
@Table(name = "base_role")
public class SimpleRoleEntity extends AbstractBaseEntity
        implements IRoleEntity<SimplePrivilegeEntity> {

    @ManyToMany
    @JoinTable(name = "roles_privileges",
            joinColumns = @JoinColumn(name = "role_id",
                    referencedColumnName = "primary_code"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "primary_code"))
    private Collection<SimplePrivilegeEntity> privileges;

    private String name;
}
