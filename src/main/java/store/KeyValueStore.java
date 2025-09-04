package store;

import lombok.AllArgsConstructor;
import store.expiry.Expiry;
import store.expiry.NoExpiry;
import store.types.DataType;
import store.types.StreamObject;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@AllArgsConstructor
public class KeyValueStore {

    private final Map<String, RedisObject> keyValueStore = new ConcurrentHashMap<>();

    public RedisObject getRedisObject(String key) {
        if (!containsKey(key)) {
            return null;
        }

        var valueObject = this.keyValueStore.get(key);

        if (valueObject.isExpired()) {
            removeKey(key);
            return null;
        }

        return this.keyValueStore.get(key);
    }

    public void setValue(String key, Object value) {
        setValue(key, value, new NoExpiry());
    }

    public void setValue(String key, Object value, Expiry expiry) {
        var valueBuilder = RedisObject.builder()
                .value(value)
                .additionTime(Instant.now())
                .expiryType(expiry);
        switch (value) {
            case String ignored -> {
                valueBuilder.type(DataType.STRING);
            }
            case List<?> ignored -> {
                valueBuilder.type(DataType.LIST);
            }
            case StreamObject ignored -> {
                valueBuilder.type(DataType.STREAM);
            }
            default -> {}
        }
        this.keyValueStore.put(key, valueBuilder.build());
    }

    /**
     * Method used with RPUSH command. Adds a value to an existing list at given key
     * or creates the list and adds the element there if the key does not exist
     * @param key given key
     * @param values values to add
     * @return the number of elements in the list
     */
    public int append(String key, List<String> values) {
        var valueType = this.keyValueStore.get(key);
        List<String> list;

        if (valueType != null && valueType.getType().equals(DataType.LIST)) {

            list = valueType.getValue();
            list.addAll(values);
        } else {
            list = new ArrayList<>(values);
            setValue(key, list, new NoExpiry());
        }

        return list.size();
    }

    public int prepend(String key, List<String> values) {
        var valueType = this.keyValueStore.get(key);
        List<String> list;

        if (valueType != null && valueType.getType().equals(DataType.LIST)) {
            list = valueType.getValue();
            for (var item: values) {
                list.addFirst(item);
            }
        } else {
            list = new ArrayList<>(values.reversed());
            setValue(key, list, new NoExpiry());
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

        var valueType = this.keyValueStore.get(key);
        List<String> list;

        if (valueType != null && valueType.getType().equals(DataType.LIST)) {
            list = valueType.getValue();
            start = convertNegativeIndex(start,  list.size());
            stop = convertNegativeIndex(stop,  list.size());

            if (start >= list.size() || start > stop) {
                return Collections.emptyList();
            }
            if (stop >= list.size()) {
                stop = list.size() - 1;
            }
        } else {
            //  TODO should i throw something?
            list = Collections.emptyList();
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

        var valueType = this.keyValueStore.get(key);
        List<String> list;
        var removedItems = new ArrayList<String>();

        if (valueType != null && valueType.getType().equals(DataType.LIST)) {

            list = valueType.getValue();
            if (list.isEmpty()) {
                return null;
            }

            if (n > list.size()) {
                return list;
            }

            for (int i = 0; i < n; i++) {
                removedItems.add(list.removeFirst());
            }
        } else {
            //  TODO should i throw something?
            return null;
        }


        return removedItems;
    }

    public boolean containsKey(String key) {
        return this.keyValueStore.containsKey(key);
    }

    // TODO ugly
    public void addStreamValue(String key, StreamObject.StreamValue streamValue) {
        if (!containsKey(key)) {
            var streamList = new LinkedList<StreamObject.StreamValue>();
            streamList.add(streamValue);
            setValue(key, new StreamObject(streamList));
            return;
        }

        var stream = (StreamObject) this.keyValueStore.get(key).getValue();
        stream.addStreamEntry(streamValue);
    }

    private void removeKey(String key) {
        this.keyValueStore.remove(key);
    }

    private Integer convertNegativeIndex(int index, int listSize) {
        var converted = index >= 0 ? index : index + listSize;
        return Math.max(converted, 0);
    }

}
