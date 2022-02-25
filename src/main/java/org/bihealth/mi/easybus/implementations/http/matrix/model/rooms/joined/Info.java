
package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model to create info in matrix model to create content in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mimetype",
    "size"
})

public class Info {

    @JsonProperty("mimetype")
    private String mimetype;
    @JsonProperty("size")
    private Integer size;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Info() {
    }

    /**
     * 
     * @param size
     * @param mimetype
     */
    public Info(String mimetype, Integer size) {
        super();
        this.mimetype = mimetype;
        this.size = size;
    }

    @JsonProperty("mimetype")
    public String getMimetype() {
        return mimetype;
    }

    @JsonProperty("mimetype")
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

}
