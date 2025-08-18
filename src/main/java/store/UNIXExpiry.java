package store;

import lombok.AllArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
public class UNIXExpiry implements Expiry {

    private final Instant expiresAt;

    @Override
    public boolean isExpired(Instant addedAt) {
        return Instant.now().isAfter(this.expiresAt);
    }

    @Override
    public Instant getExpiryInstant(Instant addedAt) {
        return this.expiresAt;
    }
}
