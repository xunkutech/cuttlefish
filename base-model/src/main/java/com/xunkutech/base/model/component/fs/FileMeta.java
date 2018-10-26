package com.xunkutech.base.model.component.fs;

import com.xunkutech.base.model.AbstractModelBean;
import com.xunkutech.base.model.converter.NativeStringAsciiConverter;
import com.xunkutech.base.model.util.CodecUtils;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Getter
@Setter
@Embeddable
public class FileMeta extends AbstractModelBean<Void> {

    public static String calHash(String namespace, String canonicalPath) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("sha1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return CodecUtils.toHex(digest.digest((namespace + canonicalPath).getBytes()));
    }

    /**
     * 文件的哈希值
     */
    @Column(name = "etag",
            nullable = false,
            length = 28,
            columnDefinition = "CHAR(28) COLLATE 'ascii_bin'")
    private String etag;

    @Column(name = "hash",
            nullable = false,
            length = 1022,
            columnDefinition = "VARCHAR(1022) COLLATE 'ascii_bin'")
    private String hash;

    @Column(name = "namespace",
            nullable = false,
            length = 1022,
            columnDefinition = "VARCHAR(1022) COLLATE 'ascii_bin'")
    @Convert(converter = NativeStringAsciiConverter.class)
    private String namespace;

    @Column(name = "canonical_path",
            nullable = false,
            length = 2044,
            columnDefinition = "VARCHAR(2044) COLLATE 'ascii_bin'")
    @Convert(converter = NativeStringAsciiConverter.class)
    private String canonicalPath;

    @Column(name = "content_type",
            nullable = false,
            length = 254,
            columnDefinition = "VARCHAR(254) COLLATE 'ascii_bin'")
    private String contentType;

    @Column(name = "origin_name",
            length = 510,
            columnDefinition = "VARCHAR(510) COLLATE 'ascii_bin'")
    @Convert(converter = NativeStringAsciiConverter.class)
    private String originName;

}
