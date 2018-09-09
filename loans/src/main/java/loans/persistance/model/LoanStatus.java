package loans.persistance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Optional;

public enum LoanStatus {

    @JsonProperty("rejected")
    REJECTED,
    @JsonProperty("confirmed")
    CONFIRMED,
    @JsonProperty("valid")
    VALID,
    @JsonProperty("invalid")
    INVALID,
    @JsonProperty("new")
    NEW;

    @JsonCreator
    public static LoanStatus forValue(String value) {
        Optional<LoanStatus> status = Arrays.stream(values())
                .filter(item -> value.equalsIgnoreCase(item.name())).findFirst();
        return status.orElse(null);
    }

}
