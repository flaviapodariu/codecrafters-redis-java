package store;

import java.time.Instant;

public class NoExpiry implements Expiry {
    @Override
    public boolean isExpired(Instant addedAt) {
        return false;
    }

    @Override
    public Instant getExpiryInstant(Instant addedAt) {
        return Instant.MAX;
    }
}
