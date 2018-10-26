package com.xunkutech.base.model.component.configure;

import com.xunkutech.base.model.AbstractPayloadEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by jason on 7/18/17.
 */
@Entity
@Table(name = "base_configure_payload")
public class ConfigurePayloadEntity extends AbstractPayloadEntity {

    private static final long serialVersionUID = -478318661441233077L;
}
