package example.api.location.google.Models;

public class notification_model {

    String sender;
    String receiver;
    String status;
    String nodeAddress;
    Long time;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public notification_model() {
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getStatus() {
        return status;
    }


}
