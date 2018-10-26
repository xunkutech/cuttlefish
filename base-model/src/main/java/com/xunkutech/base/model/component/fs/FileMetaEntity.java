package com.xunkutech.base.model.component.fs;

import com.xunkutech.base.model.AbstractModelEntity;
import com.xunkutech.base.model.NonPersistentPayloadEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(
        name = "base_file_meta",
        indexes = {
                @Index(columnList = "hash", unique = true),
                @Index(columnList = "namespace,canonical_path", unique = true)
        }
)
public class FileMetaEntity extends AbstractModelEntity<FileMeta, Void, NonPersistentPayloadEntity> {

    @Embedded
    private FileMeta model;

    @Override
    public NonPersistentPayloadEntity getPayloadEntity() {
        return null;
    }

    @Override
    public void setPayloadEntity(NonPersistentPayloadEntity payloadEntity) {

    }
}
