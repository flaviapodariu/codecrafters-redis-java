package store.types;

public enum DataType {
    STRING("string"),
    INTEGER("integer"),
    LIST("list"),
    SET("set"),
    ZSET("zset"),
    STREAM("stream"),
    HASH("hash");

    private final String typeName;

    DataType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}

