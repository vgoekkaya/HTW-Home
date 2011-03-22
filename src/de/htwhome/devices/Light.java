package de.htwhome.devices;

import com.google.gson.reflect.TypeToken;
import de.htwhome.gui.StatusChangeEvent;
import de.htwhome.gui.StatusChangeListener;
import de.htwhome.transmission.Message;
import de.htwhome.transmission.MessageType;
import de.htwhome.utils.ActorConfig;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Christian Rech, Tim Bartsch
 */
public class Light extends Actor<Boolean> {

    private final CopyOnWriteArrayList<StatusChangeListener> listeners = new CopyOnWriteArrayList<StatusChangeListener>();
    public static final DeviceType deviceType = DeviceType.Light;
    public static final Type cfgType = new TypeToken<ActorConfig<Boolean>>() {
    }.getType();

    public Light(int id, boolean status, String location, String description, int[] gidTab) throws SocketException {
        super(id, status, location, description, gidTab);
    }

    public Light() {
        super.load();
    }

    @Override
    public void setStatus(Boolean status) {
        this.status = status;
        fireChangeEvent();
        Message msg = new Message();
        msg.setMsgType(MessageType.statusResponse);
        msg.setSenderId(this.id);
        msg.setContent(String.valueOf(this.status));
        msg.setSenderDevice(deviceType);
        this.sendMsg(msg);
        System.out.println("Light.status:" + this.status);
    }

    @Override
    public void setStatus(String status) {
	boolean b = Boolean.valueOf(status);
	this.setStatus(b);
    }

    @Override
    public void handleMsg(String msg) {
        super.handleMsg(msg, deviceType, cfgType);
    }

    public void addStatusChangeListener(StatusChangeListener l) {
        this.listeners.add(l);
    }

    public void removeStatusChangeListener(StatusChangeListener l) {
        this.listeners.remove(l);
    }

    protected void fireChangeEvent() {
        StatusChangeEvent<Boolean> evt = new StatusChangeEvent<Boolean>(this, this.status);
        for (StatusChangeListener l : listeners) {
            l.changeEventReceived(evt);
        }
    }

    @Override
    public void handleFireAlarm() {
        setStatus(true);
    }

    public static void main(String[] args) throws SocketException {
        int[] gid  = {1};
	Light l = new Light(11, false, "haus", "Beschreibung", gid);
	l.handleMsg("{'msgType':'statusChange','senderId':0,'receiverId':1,'content':'true','senderDevice':'Switch'}");
//        Light l = new Light();
//	l.handleMsg("{'gid': '1', 'status': 'false', 'action': 'changeStatus'}");


        l.save();
//        ActorConfig sc2 = (ActorConfig) l.getConfig();
//        System.out.println("sc2 id= " + sc2.getId()
//                  + "\n" + "status= " + sc2.getStatus()
//                );
    }
}
