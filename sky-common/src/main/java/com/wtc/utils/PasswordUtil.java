package com.wtc.utils;

import com.password4j.Hash;
import com.password4j.Password;

public class PasswordUtil {
    public static String hashPassword(String password) {
        Hash hash = Password.hash(password).addRandomSalt(16).withArgon2();
        return hash.getResult();
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return Password.check(password, hashedPassword).withArgon2();
    }
}
