package generator;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.Random;

public class TokenGenerator implements IdentifierGenerator {
    public static final String name = "tokenGenerator";

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return randomAlphaNumericString(8);
    }

    @SuppressWarnings("SameParameterValue")
    private static String randomAlphaNumericString(int length) {
        final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        StringBuilder result = new StringBuilder();
        Random random = new Random();
        while (result.length() < length) {
            char nextChar = chars.charAt(random.nextInt(chars.length()));
            result.append(nextChar);
        }

        return result.toString();
    }
}
