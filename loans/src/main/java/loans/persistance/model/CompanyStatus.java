package loans.persistance.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Optional;

public enum CompanyStatus {

    @JsonProperty("active")
    ACTIVE,

    @JsonProperty("blocked")
    BLOCKED;

    @JsonCreator
    public static CompanyStatus forValue(String value) {
        Optional<CompanyStatus> status = Arrays.stream(values())
                .filter(item -> value.equalsIgnoreCase(item.name())).findFirst();
        return status.orElse(null);
    }

}
