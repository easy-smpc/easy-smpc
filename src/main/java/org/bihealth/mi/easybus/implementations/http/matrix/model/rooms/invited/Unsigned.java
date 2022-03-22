
package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.invited;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for unsigned in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "age"
})

public class Unsigned {

    @JsonProperty("age")
    private long age;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Unsigned() {
    }

    /**
     * 
     * @param age
     */
    public Unsigned(long age) {
        super();
        this.age = age;
    }

    @JsonProperty("age")
    public long getAge() {
        return age;
    }

    @JsonProperty("age")
    public void setAge(long age) {
        this.age = age;
    }

}
