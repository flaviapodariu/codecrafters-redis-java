package store;

import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@AllArgsConstructor
public class KeyValueStore {

    private final Map<String, String> simpleKeyValueStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> keysAdditionTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Expiry> keysExpiry = new ConcurrentHashMap<>();


    public String getValue(String key) {
        if (!containsKey(key)) {
            return null;
        }

        var additionInstant = this.keysAdditionTimestamps.get(key);
        var expiry = this.keysExpiry.getOrDefault(key, new NoExpiry());

        if (expiry.isExpired(additionInstant)) {
            removeKey(key);
            return null;
        }

        return this.simpleKeyValueStore.get(key);
    }


    public void addValue(String key, String value) {
        addValue(key, value, new NoExpiry());
    }

    public void addValue(String key, String value, Expiry expiry) {
        this.simpleKeyValueStore.put(key, value);
        this.keysAdditionTimestamps.put(key, Instant.now());
        this.keysExpiry.put(key, expiry);
    }

    public boolean containsKey(String key) {
        return this.simpleKeyValueStore.containsKey(key);
    }

    private void removeKey(String key) {
        this.simpleKeyValueStore.remove(key);
        this.keysAdditionTimestamps.remove(key);
        this.keysExpiry.remove(key);
    }


}
