package store;

import lombok.AllArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
public class TTLExpiry implements Expiry {

    private final Long ttlMillis;

    @Override
    public boolean isExpired(Instant addedAt) {
        return addedAt.plusMillis(ttlMillis).isBefore(Instant.now());
    }

    @Override
    public Instant getExpiryInstant(Instant addedAt) {
        return addedAt.plusMillis(ttlMillis);
    }
}
