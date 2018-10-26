package com.xunkutech.base.model;

import com.xunkutech.base.model.enums.EntityStatus;

import java.time.Instant;
import java.util.Date;

public interface IBaseEntity {

    String getPrimaryCode();

    void setPrimaryCode(String primaryCode);

    Date getLastModifiedTime();

    EntityStatus getEntityStatus();

    void setEntityStatus(EntityStatus entityStatus);

    Instant getCreatedDate();

    void setCreatedDate(Instant createdDate);

    Boolean getEnable();

    void setEnable(Boolean enable);
}
