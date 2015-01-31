package org.zt.cachecontrol.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Digests {

    private static final String MD5_ALGORITHM_NAME = "MD5";

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f'};


    /**
     * Calculate the MD5 digest of the given bytes.
     * 
     * @param bytes the bytes to calculate the digest over
     * @return the digest
     */
    public static byte[] md5(byte[] bytes) {
        return digest(MD5_ALGORITHM_NAME, bytes);
    }

    public static byte[] md5(String text) {
        return md5(text.getBytes());
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given bytes.
     * 
     * @param bytes the bytes to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String md5AsHex(byte[] bytes) {
        return digestAsHexString(MD5_ALGORITHM_NAME, bytes);
    }

    public static String md5AsHex(CharSequence text) {
        return md5AsHex(text.toString().getBytes());
    }

    public static String md5AsHexTruncate(CharSequence text, int length) {
        if (length >= 32) {
            return md5AsHex(text);
        } else {
            return md5AsHex(text).substring(0, length);
        }
    }

    /**
     * Append a hexadecimal string representation of the MD5 digest of the given bytes to the given
     * {@link StringBuilder}.
     * 
     * @param bytes the bytes to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     */
    public static StringBuilder appendMd5DigestAsHex(byte[] bytes, StringBuilder builder) {
        return appendDigestAsHex(MD5_ALGORITHM_NAME, bytes, builder);
    }

    /**
     * Creates a new {@link MessageDigest} with the given algorithm. Necessary because
     * {@code MessageDigest} is not thread-safe.
     */
    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not find MessageDigest with algorithm \""
                    + algorithm + "\"", ex);
        }
    }

    private static byte[] digest(String algorithm, byte[] bytes) {
        return getDigest(algorithm).digest(bytes);
    }

    private static String digestAsHexString(String algorithm, byte[] bytes) {
        char[] hexDigest = digestAsHexChars(algorithm, bytes);
        return new String(hexDigest);
    }

    private static StringBuilder appendDigestAsHex(String algorithm, byte[] bytes,
            StringBuilder builder) {
        char[] hexDigest = digestAsHexChars(algorithm, bytes);
        return builder.append(hexDigest);
    }

    private static char[] digestAsHexChars(String algorithm, byte[] bytes) {
        byte[] digest = digest(algorithm, bytes);
        return encodeHex(digest);
    }

    private static char[] encodeHex(byte[] bytes) {
        char chars[] = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }

    public static String fixedHexString(byte[] hashBytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
            sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

}
