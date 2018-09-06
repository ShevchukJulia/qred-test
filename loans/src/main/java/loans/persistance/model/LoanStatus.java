package loans.persistance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LoanStatus {

    @JsonProperty("rejected")
    REJECTED,

    @JsonProperty("confirmed")
    CONFIRMED,

    @JsonProperty("validated")
    VALID,

    @JsonProperty("invalid")
    INVALID,

    @JsonProperty("new")
    NEW

}
