package com.google.bitcoin.core;

/**
 * Message Identifier
 * @author Oscar Guindzberg
 *
 */
public class MessageIdentifier {
    
    private Class<? extends Message> messageClass;
    private Sha256Hash hash;
    
    public MessageIdentifier(Class<? extends Message> messageClass, Sha256Hash hash) {
        super();
        this.messageClass = messageClass;
        this.hash = hash;
    }
    public Class<? extends Message> getMessageClass() {
        return messageClass;
    }
    public Sha256Hash getHash() {
        return hash;
    }
    @Override
    public String toString() {
        return "MessageIdentifier [messageClass=" + messageClass + ", hash="
                + hash + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result
                + ((messageClass == null) ? 0 : messageClass.getName().hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageIdentifier other = (MessageIdentifier) obj;
        if (hash == null) {
            if (other.hash != null)
                return false;
        } else if (!hash.equals(other.hash))
            return false;
        if (messageClass == null) {
            if (other.messageClass != null)
                return false;
        } else if (!messageClass.getName().equals(other.messageClass.getName()))
            return false;
        return true;
    }
    
    

}
