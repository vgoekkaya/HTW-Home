package de.htwhome.devices;
import com.google.gson.Gson;
import de.htwhome.transmission.MessageSender;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Rech, Tim Bartsch
 */
public abstract class Actor<T> extends AbstractDevice<T>{

    private int[] gidTab;
  
    public Actor(int id, T status, String location, String type, String hint, int[] gidTab) {
        super(id, status,location, type, hint);
        this.gidTab = gidTab;
    }

    public void sendMsg(AckMessage<T> ackMsg, Type ackMsgTyp){
        try {
            String json = new Gson().toJson(ackMsg, ackMsgTyp);
            System.out.println("JSON:" + json); //TODO aufraeumen
            MessageSender.sendMsg(json);
        } catch (IOException ex) {
            Logger.getLogger(Actor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleMsg(String msg, Type msgType){
	ActionMessage<T> actionMsg = gson.fromJson(msg, msgType);
        for(int i=0; i< this.gidTab.length; i++){
            if(this.gidTab[i] == actionMsg.getGid()){
                switch(actionMsg.getAction()){
                    case changeStatus:
                        setStatus(actionMsg.getStatus());
                        break;
                    case getStatus:
                        setStatus(this.status);
                        break;
                }
            }
        }
    }
}
