package com.xunkutech.base.dao.component.fs;

import com.xunkutech.base.dao.repo.ModelEntityRepository;
import com.xunkutech.base.model.component.fs.FileMeta;
import com.xunkutech.base.model.component.fs.FileMetaEntity;

public interface FileMetaEntityRepository extends ModelEntityRepository<FileMetaEntity, FileMeta, Void> {

    FileMetaEntity findByModelHash(String hash);
}
