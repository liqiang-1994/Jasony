package io.luxyva.jasony.gateway.security;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PersistentTokenCache<T> {
    
    private final long expireMillis;
    
    private final Map<String, Value> map;
    private long latestWriteTime;
    
    public PersistentTokenCache(long expireMillis) {
        if (expireMillis <= 0L) {
            throw new IllegalArgumentException();
        }
        this.expireMillis = expireMillis;
        
        map = new LinkedHashMap<>(64, 0.75f);
        latestWriteTime = System.currentTimeMillis();
    }
    
    public T get(String key) {
        purge();
        final Value val = map.get(key);
        final long time = System.currentTimeMillis();
        return val != null && time < val.expire ? val.token : null;
    }
    
    public void put(String key, T token) {
        purge();
        if (map.containsKey(key)) {
            map.remove(key);
        }
        final long time = System.currentTimeMillis();
        map.put(key, new Value(token, time + expireMillis));
        latestWriteTime = time;
    }
    
    public int size() {
        return map.size();
    }
    
    public void purge() {
        long time = System.currentTimeMillis();
        if (time - latestWriteTime > expireMillis) {
            map.clear();
        } else {
            Iterator<Value> values = map.values().iterator();
            while (values.hasNext()) {
                if (time >= values.next().expire) {
                    values.remove();
                } else {
                    break;
                }
            }
        }
    }
    
    
    private class Value {
        
        private final T token;
        private final long expire;
        
        Value(T token, long expire) {
            this.token = token;
            this.expire = expire;
        }
    }
    
}

