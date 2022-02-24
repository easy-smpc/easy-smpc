
package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "age",
    "transaction_id",
    "redacted_by",
    "redacted_because"
})

public class Unsigned {

    @JsonProperty("age")
    private Integer age;
    @JsonProperty("transaction_id")
    private String transactionId;
    @JsonProperty("redacted_by")
    private String redactedBy;
    @JsonProperty("redacted_because")
    private RedactedBecause redactedBecause;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Unsigned() {
    }

    /**
     * 
     * @param redactedBy
     * @param redactedBecause
     * @param age
     * @param transactionId
     */
    public Unsigned(Integer age, String transactionId, String redactedBy, RedactedBecause redactedBecause) {
        super();
        this.age = age;
        this.transactionId = transactionId;
        this.redactedBy = redactedBy;
        this.redactedBecause = redactedBecause;
    }

    @JsonProperty("age")
    public Integer getAge() {
        return age;
    }

    @JsonProperty("age")
    public void setAge(Integer age) {
        this.age = age;
    }

    @JsonProperty("transaction_id")
    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("transaction_id")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonProperty("redacted_by")
    public String getRedactedBy() {
        return redactedBy;
    }

    @JsonProperty("redacted_by")
    public void setRedactedBy(String redactedBy) {
        this.redactedBy = redactedBy;
    }

    @JsonProperty("redacted_because")
    public RedactedBecause getRedactedBecause() {
        return redactedBecause;
    }

    @JsonProperty("redacted_because")
    public void setRedactedBecause(RedactedBecause redactedBecause) {
        this.redactedBecause = redactedBecause;
    }

}
