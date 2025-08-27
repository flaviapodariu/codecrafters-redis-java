package store;

import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@AllArgsConstructor
public class KeyValueStore {

    private final Map<String, Object> simpleKeyValueStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> keysAdditionTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Expiry> keysExpiry = new ConcurrentHashMap<>();


    public Object getValue(String key) {
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

    public void addValue(String key, Object value, Expiry expiry) {
        this.simpleKeyValueStore.put(key, value);
        this.keysAdditionTimestamps.put(key, Instant.now());
        this.keysExpiry.put(key, expiry);
    }

    /**
     * Method used with RPUSH command. Adds a value to an existing list at given key
     * or creates the list and adds the element there if the key does not exist
     * @param key given key
     * @param value value to add
     * @return the number of elements in the list
     */
    public int append(String key, String value) {
        var listObject = this.simpleKeyValueStore.get(key);
        List<String> list;

        if (listObject != null) {
//            assuming list<string> for now
            list = (LinkedList<String>) listObject;
            list.add(value);
        } else {
            list = new LinkedList<>();
            list.add(value);
            addValue(key, list, new NoExpiry());
        }
        return list.size();
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
