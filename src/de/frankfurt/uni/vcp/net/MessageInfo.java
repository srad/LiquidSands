package de.frankfurt.uni.vcp.net;


/**
 * <h3> This class holds data about the chat messages in the game </h3>
 * 
 * <p> It contains fields for every parameter, the server reports
 *     along with the message itself. </p>
 * 
 * <p> To avoid confusion between field-names and protocol-paramter-names
 *     the naming-scheme follows the scheme used in the client-server protocol
 * </p>
 *
 */
public class MessageInfo extends Info implements Comparable<MessageInfo> {

    /**
     * The time the server received this message
     */
    public String incomingtime;

    /**
     * The if of the sender of this message
     */    
    public String senderid;
    
    /**
     * The playerid of this message
     */        
    public String senderplayerid;
    /**
     * The message contents
     */
    public String message;
    
    
    public MessageInfo (String incomingtime, String senderid, String message, String info) {
        super (info);
        
        tags.remove("message");
        
        this.incomingtime = incomingtime;
        this.senderid = senderid;
        this.message = message;
        
        senderplayerid = tags.get("senderplayerid");
    }


    @Override
    public int compareTo(MessageInfo o) {
        long a = Long.parseLong(incomingtime);
        long b = Long.parseLong(o.incomingtime);
        return ((a - b) > 0)? -1 : 1;
    }
}
