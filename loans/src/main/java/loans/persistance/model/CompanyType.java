package loans.persistance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Optional;

public enum CompanyType {

    @JsonProperty("LTD")
    LTD,

    @JsonProperty("PUBLIC LIMITED COMPANY")
    PUBLIC_LIMITED_COMPANY,

    @JsonProperty("LIMITED PARTNERSHIP")
    LIMITED_PARTNERSHIP;

    @JsonCreator
    public static CompanyType forValue(String value) {
        Optional<CompanyType> status = Arrays.stream(values())
                .filter(item -> value.equalsIgnoreCase(item.name())).findFirst();
        return status.orElse(null);
    }

}
