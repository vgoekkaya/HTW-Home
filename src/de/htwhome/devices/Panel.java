package de.htwhome.devices;

import com.google.gson.reflect.TypeToken;
import de.htwhome.transmission.Message;
import de.htwhome.transmission.MessageType;
import de.htwhome.utils.Config;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * @author Volkan Gökkaya, Tobias Lana
 */
public class Panel extends AbstractDevice<Boolean>{

    private ArrayList<AbstractDevice> deviceList;
    public static final DeviceType deviceType = DeviceType.Panel;
    public static final Type cfgType = new TypeToken<Config<Boolean>>(){}.getType();
    

    public Panel() {}

    public Panel(int id, boolean status, String location, String type, String description) throws SocketException{
        super(id, status, location, description);
        this.deviceList = new ArrayList<AbstractDevice>();

    }

    public void save() {
        Config pc = new Config();
        super.save(pc);
        pc.setDeviceList(deviceList);
        setConfig(pc, "Panel");
    }

    public void load(){
        Config pc = this.getConfig("Panel");
        load(pc);
        this.deviceList = pc.getDeviceList();
    }

    @Override
    public void handleMsg(String jsonMsg) {
	this.handleMsg(jsonMsg, deviceType, cfgType);
    }

    @Override
    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public void setStatus(String status) {
	boolean b = Boolean.parseBoolean(status);
	this.setStatus(b);
    }

    @Override
    public void handleMsg(String jsonMsg, DeviceType devType, Type cfgType){
	Message msg = gson.fromJson(jsonMsg, Message.class);
        System.out.println("Verarbeite Nachricht vom Typ: " + msg.getMsgType());
	switch (msg.getMsgType()) {
	    case statusResponse:
		//TODO implement or remove
		break;
	    case configChange:
		//TODO implement
		break;
	    case configRequest:
		//TODO implement
		break;
            case configResponse:
                updateDevicelist(msg.getContent(), msg.getSenderDevice());
                break;
	}
    }

    public void getAllConfigs() {
	//TODO implement
        Message msg = new Message();
        msg.setMsgType(MessageType.configRequest);
        msg.setSenderId(this.id);
        msg.setReceiverId(ALLDEVICES);
        sendMsg(msg);
//        sendMsg(msg, msgType); //TODO msgTyp ueberpruefen
    }

    public void useDeviceFunction(int receiverId) {
	//TODO implement
//        Message msg = new Message(MessageType.statusChange, this.id, receiverId, null, null);
//        sendMsg(msg, msgType); //TODO msgTyp ueberpruefen
    }

    /*
     * updateDevicelist
     * @author TL
     * @param String jsonMsg
     * 
     * Funktion wertet die Nachrichten des Typs configResponse aus
     * und schreibt die Devices in die DeviceList des Panels
     */
    private void updateDevicelist(String jsonCfg, DeviceType devType) {
        switch (devType) {
            case Anemometer:
                Config<Double> sc = gson.fromJson(jsonCfg, Anemometer.cfgType);
                System.out.println("ID " + sc.getId() + " hat sich gemeldet. >> " + sc.getDescription());
                sc.setDeviceType(deviceType);
                break;
            case Light:
                System.out.println("Light meldet sich");
                break;
            case Panel:
                System.out.println("Panel meldet sich");
                break;
            case PercentSwitch:
                System.out.println("PercentSwitch meldet sich");
                break;
            case Sunblind:
                System.out.println("Sunblind meldet sich");
                break;
            case Switch:
                System.out.println("Switch meldet sich");
                break;
        }
	//TODO implement
//        Message<T> msg = gson.fromJson(jsonMsg, msgType);
//        //Message content = gson.fromJson(msg.getJsonConfig(), msgType);
//        System.out.println("ID: " + msg.getSenderId() + " hat sich gemeldet");
//        System.out.println(msg.getJsonConfig());
//        String msg2 = gson.fromJson(msg.getJsonConfig(), msgType);
//        System.out.println(msg2);
    }

    public static void main(String[] args) throws SocketException {
        Panel p = new Panel(123, false, "Wohnzimmer", "Panel", "Megapanel");
        p.getAllConfigs();
    }


}
