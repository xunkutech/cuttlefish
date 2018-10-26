package com.xunkutech.base.app.component.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class QETag {
    private static final Logger logger = LoggerFactory.getLogger(QETag.class);
    private static final int CHUNK_SIZE = 1 << 22;
    private static final int BUFFER_SIZE = 1 << 16;

    public static String urlSafeBase64Encode(byte[] data) {
        String encodedString = DatatypeConverter.printBase64Binary(data);
        encodedString = encodedString.replace('+', '-').replace('/', '_');
        return encodedString;
    }

    public static String calcETag(String fileName) throws IOException {
        if (null == fileName) return "";
        return calcETag(new File(fileName));
    }

    public static String calcETag(File file) throws IOException {
        if (null == file || !(file.exists() && file.isFile() && file.canRead())) {
            logger.error("Error: File not found or not readable");
            return "";
        }
        logger.debug("Calculate etag for file: {}", file.getAbsolutePath());
        FileInputStream inputStream = new FileInputStream(file);
        return calcETag(inputStream, null);
    }

    public static String calcETag(InputStream is, OutputStream os) throws IOException {
        String etag = "";
        if (null == is) return etag;
        MessageDigest chunkDataDigest, allDigestDigest;

        try {
            chunkDataDigest = MessageDigest.getInstance("sha1");
            allDigestDigest = MessageDigest.getInstance("sha1");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return etag;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int multi = CHUNK_SIZE / BUFFER_SIZE;
        int count = 0;
        int bytesRead;
        boolean bigFlag = false;
        while ((bytesRead = is.read(buffer)) > 0) {
            if (null != os) os.write(buffer, 0, bytesRead);
            if (++count > multi) {
                bigFlag = true;
                allDigestDigest.update(chunkDataDigest.digest());
                chunkDataDigest.reset();
                count = 0;
            }
            chunkDataDigest.update(buffer, 0, bytesRead);
        }
        byte[] hashData = new byte[21];
        if (bigFlag) {
            hashData[0] = (byte) 0x96;
            System.arraycopy(allDigestDigest.digest(chunkDataDigest.digest()), 0, hashData, 1, 20);
        } else {
            hashData[0] = 0x16;
            System.arraycopy(chunkDataDigest.digest(), 0, hashData, 1, 20);
        }
        etag = urlSafeBase64Encode(hashData);
        logger.debug("File etag: {}", etag);
        return etag;
    }
}