package de.htwhome.devices;

import com.google.gson.Gson;
import de.htwhome.transmission.Message;
import de.htwhome.transmission.MessageReceiver;
import de.htwhome.transmission.MessageSender;
import de.htwhome.utils.DeviceConfig;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Rech, Tim Bartsch
 */
public abstract class AbstractDevice<T> {
    protected int id;
    protected  T status;
    protected  String location;
    protected  String description;
    protected static Gson gson = new Gson();
    private MessageReceiver msgReceiver;
    protected static int ALLDEVICES = 999;
    
    public AbstractDevice() {}

    public AbstractDevice(int id, T status, String location, String description) throws SocketException {
        this.id = id;
        this.status = status;
        this.location = location;
        this.description = description;
        msgReceiver = new MessageReceiver(this);
        msgReceiver.start();
    }

    protected  void load(DeviceConfig dc){
        this.id = dc.getId();
        this.status = (T) dc.getStatus();
        this.location = dc.getLocation();
        this.description = dc.getDescription();
    }

    protected void save (DeviceConfig dc){
        dc.setId(id);
        dc.setStatus(status);
        dc.setLocation(location);
        dc.setDescription(description);
    }


    public abstract void handleMsg(String msg);

    public abstract void handleMsg(String jsonMsg, DeviceType devType, Type cfgType);

    public void sendMsg(Message msg){
        try {
            String json = new Gson().toJson(msg);
            // System.out.println("JSON:" + json); //TODO aufraeumen
            MessageSender.sendMsg(json);
        } catch (IOException ex) {
            Logger.getLogger(Actor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public T getStatus() {
        return status;
    }
    
    public abstract void setStatus(String status);

    public abstract void setStatus(T status);

    @Override
    public String toString() {
	return "AbstractDevice{" + "id=" + id + "status=" + status + "location=" + location + "description=" + description + "msgReceiver=" + msgReceiver + '}';
    }

}
