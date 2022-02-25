
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
    private Integer age;

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
    public Unsigned(Integer age) {
        super();
        this.age = age;
    }

    @JsonProperty("age")
    public Integer getAge() {
        return age;
    }

    @JsonProperty("age")
    public void setAge(Integer age) {
        this.age = age;
    }

}
