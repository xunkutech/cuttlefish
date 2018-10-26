package com.xunkutech.base.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by jason on 7/7/17.
 */
@Getter
@Setter
public final class SimpleJoinObject {

    private String primaryCode, aCode, bCode;

    public SimpleJoinObject(String primaryCode, String aCode, String bCode) {
        this.primaryCode = primaryCode;
        this.aCode = aCode;
        this.bCode = bCode;
    }
}
