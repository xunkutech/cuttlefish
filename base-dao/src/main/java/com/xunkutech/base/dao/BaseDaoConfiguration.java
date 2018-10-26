package com.xunkutech.base.dao;

import com.xunkutech.base.model.IBaseEntity;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by wtjs_fpf on 4/24/2017.
 */
@Configuration
@EnableJpaRepositories(
        basePackageClasses = BaseDaoConfiguration.class)
@EntityScan(basePackageClasses = IBaseEntity.class)
public class BaseDaoConfiguration {
}
