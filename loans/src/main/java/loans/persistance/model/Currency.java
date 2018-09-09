package loans.persistance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Optional;

public enum Currency {
    @JsonProperty("EUR")
    EUR,
    @JsonProperty("USD")
    USD;

    @JsonCreator
    public static Currency forValue(String value) {
        Optional<Currency> status = Arrays.stream(values())
                .filter(item -> value.equalsIgnoreCase(item.name())).findFirst();
        return status.orElse(null);
    }
}
