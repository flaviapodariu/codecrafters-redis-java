package store;

import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import store.types.StreamObject;

import java.nio.ByteBuffer;
import java.time.Instant;

import static commands.Errors.*;

@AllArgsConstructor
@Getter
public class StreamIdUtils {
    private final KeyValueStore kvStore;

    public String formatId(String streamId, String streamKey) {
        var streamObject = this.kvStore.getRedisObject(streamKey);
        if (streamObject == null) {
            return handleSequenceWildcard(streamId);
        }

        var stream = (StreamObject) streamObject.getValue();

        // this should only happen in blocking operations
        var lastId = stream.getLast();
        if (lastId == null) {
            return handleSequenceWildcard(streamId);
        }

        return handleSequenceWildcard(streamId, lastId);
    }


    public ByteBuffer checkIllegalStructure(String streamId) {
        if (streamId.equals("0-0")) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(STREAM_ID_NOT_ALLOWED).getBytes()
            );
        }

        if (!streamId.matches("^(\\d+)-(\\d+|\\*)$")) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(INVALID_STREAM_ID).getBytes()
            );
        }
        return null;
    }

    public ByteBuffer checkRangeIllegalStructure(String rangeLimit) {
        if (!rangeLimit.matches("^(\\d+)-(\\d+)|(\\d+)$")) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(INVALID_STREAM_ID).getBytes()
            );
        }
        return null;
    }

    public ByteBuffer getTimestampErrors(String streamKey, String streamId) {
        var streamObject = this.kvStore.getRedisObject(streamKey);
        if (streamObject == null) {
            return null;
        }

        var stream = (StreamObject) streamObject.getValue();

        // this should only happen in blocking operations
        var lastId = stream.getLast();
        if (lastId == null) {
            return null;
        }

        var lastEntrySplitId = lastId.split("-");
        var lastIdTimestamp = Long.parseLong(lastEntrySplitId[0]);
        var lastIdSequence = Long.parseLong(lastEntrySplitId[1]);

        var splitId = streamId.split("-");
        var idTimestamp = Long.parseLong(splitId[0]);
        long idSequence = Long.parseLong(splitId[1]);

        if (idTimestamp < lastIdTimestamp || ((idTimestamp == lastIdTimestamp) && (idSequence <= lastIdSequence)) ) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(STREAM_ID_LOWER).getBytes()
            );
        }

        return null;
    }

    public String generateFullId() {
        return Instant.now().toEpochMilli() + "-" + "*";
    }

    public String getExclusiveEndLimit(String end) {
        if (!end.contains("-")) {
            var endTime = Long.parseLong(end) + 1;
            end = String.valueOf(endTime);
            return end + "-0";
        } else {
            var splitEnd = end.split("-");
            var endSeq = Long.parseLong(splitEnd[1]) + 1;
            return splitEnd[0] + "-" + endSeq;
        }
    }

    private String handleSequenceWildcard(String streamId) {
        return handleSequenceWildcard(streamId, null);
    }

    private String handleSequenceWildcard(String streamId, String lastId) {
        var splitId = streamId.split("-");
        var timestamp = splitId[0];
        var seq = splitId[1];

        if (lastId == null) {
            if (seq.equals("*")) {
                seq = timestamp.equals("0") ? "1" : "0";
            }
            return streamId.replace("*", seq);
        }

        var splitLastId = lastId.split("-");
        var lastTimestamp = splitLastId[0];
        var lastSeq = splitLastId[1];

        if (seq.equals("*") && lastTimestamp.equals(timestamp)) {
            var last = Long.parseLong(lastSeq);
            seq = String.valueOf(last + 1);
            return streamId.replace("*", seq);
        } else if (!lastTimestamp.equals(timestamp)) {
            return streamId.replace("*", "0");
        }

        return streamId;
    }
}

