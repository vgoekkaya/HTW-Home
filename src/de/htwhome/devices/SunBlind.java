package de.htwhome.devices;

import com.google.gson.reflect.TypeToken;
import de.htwhome.gui.StatusChangeEvent;
import de.htwhome.gui.StatusChangeListener;
import de.htwhome.transmission.Message;
import de.htwhome.transmission.MessageType;
import de.htwhome.utils.Config;
import java.lang.reflect.Type;
import java.net.SocketException;

/**
 *
 * @author Christian Rech, Tim Bartsch
 */
public class SunBlind extends Actor<Integer> {

    public static final Type cfgType = new TypeToken<Config<Integer>>() {
    }.getType();
    public static final DeviceType deviceType = DeviceType.Sunblind;
    private static final int MIN_STATUS = 0;
    private static final int MAX_STATUS = 100;

    public SunBlind(int id, int status, String location, String description, int[] gidTab) throws SocketException {
        super(id, status, location, description, gidTab);
    }

    public SunBlind(int id) {
        this.id = id;
        loadConfig(deviceType);
    }

    @Override
    public void handleMsg(String msg) {
        super.handleMsg(msg, deviceType, cfgType);
    }

    @Override
    public void handleWeatherAlarm() {
        setStatus(MIN_STATUS);
    }

    @Override
    public void handleFireAlarm() {
        setStatus(MIN_STATUS);
    }

    @Override
    public void setStatus(Integer status) {
        this.status = status;
        fireChangeEvent();
        Message msg = new Message();
        msg.setMsgType(MessageType.statusResponse);
        msg.setSenderId(this.id);
        msg.setSenderDevice(deviceType);
        msg.setContent(String.valueOf(this.status));
        sendMsg(msg);
    }

    @Override
    public void setStatus(String status) {
        int i = Integer.valueOf(status);
        this.setStatus(i);
    }

    @Override
    protected void fireChangeEvent() {
        StatusChangeEvent<Integer> evt = new StatusChangeEvent<Integer>(this, this.status);
        for (StatusChangeListener l : listeners) {
            l.changeEventReceived(evt);
        }
    }
}
