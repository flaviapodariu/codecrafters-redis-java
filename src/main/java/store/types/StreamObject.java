package store.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@AllArgsConstructor
public class StreamObject
{
    private TreeMap<String, Map<String, String>> value;

    public StreamObject() {
        this.value = new TreeMap<>();
    }

    public void addStreamEntry(String streamId, Map<String, String> itemValue) {

        if (this.value.isEmpty()) {
            this.value = new TreeMap<>();
            this.value.put(streamId, itemValue);
        }

        this.value.put(streamId, itemValue);
    }

    public String getLast() {
        if (!this.value.isEmpty()) {
            return this.value.lastKey();
        }
        return null;
    }
}
