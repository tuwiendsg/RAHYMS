package at.ac.tuwien.dsg.smartcom.adapters.rest;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;

import java.io.Serializable;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class JsonMessageDTO implements Serializable {

    private String id;
    private String content;
    private String type;
    private String subtype;
    private String sender;
    private String receiver;
    private String conversation;
    private String language;
    private String securityToken;

    public JsonMessageDTO() {
    }

    public JsonMessageDTO(Message message) {
        if (message.getId() != null) {
            this.id = message.getId().getId();
        }
        this.content = message.getContent();
        this.type = message.getType();
        this.subtype = message.getSubtype();
        if (message.getSenderId() != null) {
            this.sender = message.getSenderId().getId();
        }
        if (message.getReceiverId() != null) {
            this.receiver = message.getReceiverId().getId();
        }
        this.conversation = message.getConversationId();
        this.language = message.getLanguage();
        this.securityToken = message.getSecurityToken();
    }

    public Message createMessage() {
        Message.MessageBuilder builder = new Message.MessageBuilder()
                .setContent(content)
                .setType(type)
                .setSubtype(subtype)
                .setConversationId(conversation)
                .setLanguage(language)
                .setSecurityToken(securityToken);

        if (id != null) {
            builder.setId(Identifier.message(id));
        }
        if (sender != null) {
            builder.setSenderId(Identifier.peer(sender));
        }
        if (receiver != null) {
            builder.setReceiverId(Identifier.peer(receiver));
        }

        return builder.create();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getConversation() {
        return conversation;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonMessageDTO that = (JsonMessageDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (subtype != null ? !subtype.equals(that.subtype) : that.subtype != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JsonMessageDTO{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", conversation='" + conversation + '\'' +
                ", language='" + language + '\'' +
                ", securityToken='" + securityToken + '\'' +
                '}';
    }
}
