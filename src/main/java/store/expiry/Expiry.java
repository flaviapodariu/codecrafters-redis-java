package store.expiry;

import java.time.Instant;

public interface Expiry {

    boolean isExpired(Instant addedAt);

    Instant getExpiryInstant(Instant addedAt);
}
