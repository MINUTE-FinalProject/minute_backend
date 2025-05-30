package com.minute.auth.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CertificationStorage {

    private final Map<String, String> storage = new ConcurrentHashMap<>();

    public void save(String email, String number) {
        storage.put(email, number);
    }

    public String get(String email) {
        return storage.get(email);
    }

    public void remove(String email) {
        storage.remove(email);
    }

    public boolean matches(String email, String number) {
        return number.equals(storage.get(email));
    }
}

