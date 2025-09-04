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
    private List<StreamValue> value;

    public void addStreamEntry(StreamValue streamValue) {
        if (this.value.isEmpty()) {
            this.value = new LinkedList<>();
        }
        this.value.add(streamValue);
    }

    public void addStreamEntries(List<StreamValue> streamValues) {
        if (this.value.isEmpty()) {
            this.value = new LinkedList<>();
        }
        this.value.addAll(streamValues);
    }

    public StreamValue getLast() {
        if (!this.value.isEmpty()) {
            return this.value.getLast();
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
