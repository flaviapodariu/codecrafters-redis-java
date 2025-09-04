package store.types;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class StreamObject
{
    // for now we'll have stream key -> simple string key
    private Map<String, List<StreamValue>> value;

    public void addStreamEntry(String key, StreamValue streamValue) {
        this.value.getOrDefault(key, new LinkedList<>()).add(streamValue);
    }

    public void addStreamEntries(String key, List<StreamValue> streamValues) {
        this.value.getOrDefault(key, new LinkedList<>()).addAll(streamValues);
    }

    public StreamValue getLast(String key) {
        var streamEntries = this.value.getOrDefault(key, new LinkedList<>());
        if (!streamEntries.isEmpty()) {
            return streamEntries.getLast();
        }
        return null;
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    @Setter
    public static class StreamValue {
        private String id;
        private String key;
        private String value;
    }
}
