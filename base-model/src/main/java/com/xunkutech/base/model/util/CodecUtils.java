package com.xunkutech.base.model.util;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Formatter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

public abstract class CodecUtils {

    private static final int BUFFER_SIZE = 2048;

    public static ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * "*.-_~"
     * base64 [0-9a-zA-Z+/=]
     * urlSafe[0-9a-zA-Z-_~]
     * crypt  [0-9a-zA-Z./$]
     * urlSafe[0-9a-zA-Z._*]
     */
    public static String urlSafeEncode(String value) {
        if (null == value) return null;
        return value
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "~")
                .replace("$", "*");
    }

    public static String urlSafeDecode(String value) {
        if (null == value) return null;
        return value
                .replace("-", "+")
                .replace("_", "/")
                .replace("~", "=")
                .replace("*", "$");
    }

    public static String toHex(byte[] value) {
        return DatatypeConverter.printHexBinary(value).toLowerCase();
    }

    public static byte[] fromHex(String value) throws CodecException {
        try {
            return DatatypeConverter.parseHexBinary(value);
        } catch (IllegalArgumentException e) {
            throw new CodecException(e);
        }
    }

    public static String toBase64(byte[] value) {
        return DatatypeConverter.printBase64Binary(value);
    }

    public static byte[] fromBase64(String value) throws CodecException {
        try {
            return DatatypeConverter.parseBase64Binary(value);
        } catch (IllegalArgumentException e) {
            throw new CodecException(e);
        }
    }

    public static byte[] toGzip(byte[] value) {
        if (null == value) return new byte[0];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gos = new GZIPOutputStream(baos);
            gos.write(value);
            gos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] fromGzip(byte[] value) throws CodecException {
        if (null == value) return new byte[0];
        try {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(value);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 GZIPInputStream gis = new GZIPInputStream(bais)) {
                int count;
                byte buf[] = new byte[BUFFER_SIZE];
                while ((count = gis.read(buf, 0, BUFFER_SIZE)) != -1) {
                    baos.write(buf, 0, count);
                }
                return baos.toByteArray();
            }
        } catch (ZipException e) {
            throw new CodecException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toAscii(String value) {
        if (null == value) return null;
        if (value.isEmpty()) return "";
        char[] chars = value.toCharArray();
        Formatter formatter = new Formatter();
        for (int i = 0; i < chars.length; i++) {
            formatter.format("%04x", (int) chars[i]);
        }
        return formatter.toString();
    }

    private static int parseInt(char[] value, int offset) {
        final int limit = -Integer.MAX_VALUE;
        final int radix = 16;
        final int multmin = limit / radix;
        int result = 0;
        boolean negative = false;
        if (value.length - offset >= 4) {
            int digit;
            for (int i = 0; i < 4; i++) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(value[offset++], radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + value + "\"");
                }
                if (result < multmin) {
                    throw new NumberFormatException("For input string: \"" + value + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + value + "\"");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + value + "\"");
        }
        return negative ? result : -result;
    }

    public static String fromAscii(String value) {
        if (null == value) return null;
        if (value.isEmpty()) return "";
        char[] in = value.toCharArray();
        char[] out = new char[in.length / 4];
        int offset = 0;
        try {
            for (int i = 0; i < out.length; i++) {
                out[i] = (char) (parseInt(in, offset));
                offset += 4;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return String.valueOf(out);
    }

    public static String toGzipBase64(byte[] value) {
        return toBase64(toGzip(value));
    }

    public static byte[] fromGzipBase64(String value) throws CodecException {
        return fromGzip(fromBase64(value));
    }

    public static void main(String[] args) throws Exception {
        System.out.println(toAscii("123"));
        System.out.println(toAscii("你好，中国"));
        System.out.println(fromAscii("4f60597dff0c4e2d56fd"));
        System.out.println(DatatypeConverter.printHexBinary("你好，中国".getBytes()));
//        return ZonedDateTime.of(value, ZONE_ID)
//                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    }
}