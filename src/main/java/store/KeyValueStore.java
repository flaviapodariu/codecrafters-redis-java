package store;

import lombok.AllArgsConstructor;
import store.expiry.Expiry;
import store.expiry.NoExpiry;

import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.*;
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
     * @param values values to add
     * @return the number of elements in the list
     */
    public int append(String key, List<String> values) {
        var listObject = this.simpleKeyValueStore.get(key);
        List<String> list;

        if (listObject != null) {
//            assuming list<string> for now
//            TODO ugly af
            list = (ArrayList<String>) listObject;
            list.addAll(values);
        } else {
            list = new ArrayList<>(values);
            addValue(key, list, new NoExpiry());
        }

        return list.size();
    }

    public int prepend(String key, List<String> values) {
        var listObject = this.simpleKeyValueStore.get(key);
        List<String> list;

        if (listObject != null) {
//            assuming list<string> for now
//            TODO ugly af
            list = (ArrayList<String>) listObject;
            for (var item: values) {
                list.addFirst(item);
            }
        } else {
            list = new ArrayList<>(values.reversed());
            addValue(key, list, new NoExpiry());
        }
        return list.size();
    }

    /**
     *  Get elements in a list given a range of indexes
     * @param key the key storing the list
     * @param start start index (inclusive)
     * @param stop stop index (inclusive)
     * @return a list of elements
     */
    public List<String> getRange(String key, int start, int stop) {
        if (!containsKey(key)) {
            return Collections.emptyList();
        }

        var list = (ArrayList<String>) this.simpleKeyValueStore.get(key);

        start = convertNegativeIndex(start,  list.size());
        stop = convertNegativeIndex(stop,  list.size());

        if (start >= list.size() || start > stop) {
            return Collections.emptyList();
        }
        if (stop >= list.size()) {
            stop = list.size() - 1;
        }

        return list.subList(start, stop+1);
    }

    public String removeFirst(String key) {
        var removedItem = removeItems(key, 1);
        return removedItem != null ? removedItem.getFirst() : null;
    }

    public List<String> removeItems(String key, int n) {
        if (!containsKey(key)) {
            return null;
        }

        var list = (ArrayList<String>) this.simpleKeyValueStore.get(key);

        if (list.isEmpty()) {
            return null;
        }

        if (n > list.size()) {
            return list;
        }

        var removedItems = new ArrayList<String>();

        for (int i = 0; i < n; i++) {
            removedItems.add(list.removeFirst());
        }

        return removedItems;
    }

    public boolean containsKey(String key) {
        return this.simpleKeyValueStore.containsKey(key);
    }


    private void removeKey(String key) {
        this.simpleKeyValueStore.remove(key);
        this.keysAdditionTimestamps.remove(key);
        this.keysExpiry.remove(key);
    }

    private Integer convertNegativeIndex(int index, int listSize) {
        var converted = index >= 0 ? index : index + listSize;
        return Math.max(converted, 0);
    }

}
