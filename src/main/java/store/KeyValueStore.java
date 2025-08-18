package store;

import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@AllArgsConstructor
public class KeyValueStore {

    private final Map<String, String> simpleKeyValueStore = new ConcurrentHashMap<>();

    public String getValue(String key) {
        return this.simpleKeyValueStore.get(key);
    }

    public void addValue(String key, String value) {
        this.simpleKeyValueStore.put(key, value);
    }

    public boolean containsKey(String key) {
        return this.simpleKeyValueStore.containsKey(key);
    }


}
