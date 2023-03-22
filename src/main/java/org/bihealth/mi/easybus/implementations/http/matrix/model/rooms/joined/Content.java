
package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model to create content in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content",
    "msgtype",
    "url",
    "body",
    "filename",
    "info",
    "name"
})

public class Content {

    @JsonProperty("content")
    private Content content;
    @JsonProperty("msgtype")
    private String msgtype;
    @JsonProperty("url")
    private String url;
    @JsonProperty("body")
    private String body;
    @JsonProperty("filename")
    private String filename;
    @JsonProperty("info")
    private Info info;
    @JsonProperty("name")
    private String name;
    @JsonProperty("org.bihealth.mi.scope")
    private String scope;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Content() {
    }

    /**
     * 
     * @param filename
     * @param body
     * @param msgtype
     * @param content
     * @param url
     * @param info
     */
    public Content(Content content, String msgtype, String url, String body, String filename, Info info, String name, String scope) {
        super();
        this.content = content;
        this.msgtype = msgtype;
        this.url = url;
        this.body = body;
        this.filename = filename;
        this.info = info;
        this.name = name;
        this.scope = scope;
    }

    @JsonProperty("content")
    public Content getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(Content content) {
        this.content = content;
    }

    @JsonProperty("msgtype")
    public String getMsgType() {
        return msgtype;
    }

    @JsonProperty("msgtype")
    public void setMsgType(String msgtype) {
        this.msgtype = msgtype;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonProperty("info")
    public Info getInfo() {
        return info;
    }

    @JsonProperty("info")
    public void setInfo(Info info) {
        this.info = info;
    }
    
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
    
    @JsonProperty("org.bihealth.mi.scope")
    public String getScope() {
        return scope;
    }

    @JsonProperty("org.bihealth.mi.scope")
    public void setScope(String scope) {
        this.scope = scope;
    }
}
