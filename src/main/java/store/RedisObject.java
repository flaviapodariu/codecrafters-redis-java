package store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import store.expiry.Expiry;
import store.types.DataType;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class RedisObject {
   private Object value;
   private DataType type;
   private Instant additionTime;
   private Expiry expiryType;

    /**
     * This is a generic method.
     * The <T> before the return type 'T' declares it as generic.
     * The cast happens safely inside this method.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

   public boolean isExpired() {
       return this.expiryType.isExpired(this.additionTime);
   }
}
