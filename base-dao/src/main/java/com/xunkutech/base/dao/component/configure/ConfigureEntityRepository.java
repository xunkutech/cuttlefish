package com.xunkutech.base.dao.component.configure;

import com.xunkutech.base.dao.repo.ModelEntityRepository;
import com.xunkutech.base.model.component.configure.Configure;
import com.xunkutech.base.model.component.configure.ConfigureEntity;
import com.xunkutech.base.model.enums.ConfigureType;

public interface ConfigureEntityRepository<P>
        extends ModelEntityRepository<ConfigureEntity<P>, Configure<P>, P> {

    ConfigureEntity<P> findByModelConfigureTypeAndModelConfigureName(ConfigureType type, String name);
}
