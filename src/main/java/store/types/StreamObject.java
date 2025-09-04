package store.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class StreamObject
{
    // for now we'll have stream key -> simple string key
    private List<Map<String, String>> value;
}
