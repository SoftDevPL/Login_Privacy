package lg.sec.loginprivacy.listeners.hashingUtils;

import lombok.SneakyThrows;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.SecureRandom;

public class PasswordHarsher implements PasswordEncoder {

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int SALT_BYTES = 32;
    private static final int HASH_BYTES = 32;
    private static final int PBKDF2_ITERATIONS = 342593;
    private static final int ITERATION_INDEX = 0;
    private static final int SALT_INDEX = 1;
    private static final int PBKDF2_INDEX = 2;


    @Override
    public String encode(CharSequence rawPassword) {
        return createHashedPassword(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return verifyHash(rawPassword, encodedPassword);
    }

    private String createHashedPassword(CharSequence password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = saltPassword(password.toString().toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTES);
        return PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
    }

    private boolean verifyHash(CharSequence password, String goodHash) {
        String[] params;
        int iterations;
        byte[] salt;
        byte[] hash;
        byte[] testHash;

        try {
            params = goodHash.split(":");
            iterations = Integer.parseInt(params[ITERATION_INDEX]);
            salt = fromHex(params[SALT_INDEX]);
            hash = fromHex(params[PBKDF2_INDEX]);
            testHash = saltPassword(password.toString().toCharArray(), salt, iterations, hash.length);
        } catch (Exception e) {
            params = password.toString().split(":");
            iterations = Integer.parseInt(params[ITERATION_INDEX]);
            salt = fromHex(params[SALT_INDEX]);
            hash = fromHex(params[PBKDF2_INDEX]);
            testHash = saltPassword(goodHash.toCharArray(), salt, iterations, hash.length);
        }
        return slowEquals(hash, testHash);
    }

    private byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }

    private String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    private boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    @SneakyThrows
    private byte[] saltPassword(char[] password, byte[] salt, int iterations, int bytes) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
}
