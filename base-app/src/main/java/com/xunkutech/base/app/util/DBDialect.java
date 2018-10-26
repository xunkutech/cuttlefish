package com.xunkutech.base.app.util;

import org.hibernate.dialect.MariaDBDialect;

/**
 * Created by Jason Han on 5/12/2017.
 * <p>
 * To support large varchar column up to 3072.
 */

/*
mysql.ini

character-set-server=utf8
innodb_large_prefix=true
innodb_file_format=barracuda
innodb_file_per_table=true
// */
public class DBDialect extends MariaDBDialect {

    /**
     * Call to super returns " type=innodb". 'Type' is a
     * deprecated MySQL synonym for 'engine'
     * so you may want to ignore the super call and use:
     * " engine=innodb ROW_FORMAT=DYNAMIC"
     */
    @Override
    public String getTableTypeString() {
        return super.getTableTypeString() + " ROW_FORMAT=DYNAMIC COLLATE 'utf8mb4_bin'";
    }
}