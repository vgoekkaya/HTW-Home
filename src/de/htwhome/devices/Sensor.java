package de.htwhome.devices;

import com.google.gson.reflect.TypeToken;
import de.htwhome.transmission.Message;
import de.htwhome.transmission.MessageType;
import de.htwhome.utils.ActorConfig;
import de.htwhome.utils.HTWhomeConfig;
import de.htwhome.utils.LittleHelpers;
import de.htwhome.utils.SensorConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.util.Timer;
import javax.xml.bind.JAXB;

/**
 *
* @author Christian Rech, Tim Bartsch
 */
public abstract class Sensor<T> extends AbstractDevice<T>{

    private Timer timer;

    protected int[] actorIdTab;
    protected T[] actorStatusTab;
    protected boolean[] actorAckTab; //TODO init
    protected int gid;
    private boolean timeSchedulerChangeStatus = false;


    public Sensor() {
    }

     public Sensor (int id, T status, String location, String description, int[] actorIdTab, T[] actorStatusTab, int gid) throws SocketException {
        super(id, status,location, description);
        this.actorIdTab = actorIdTab;
        this.actorStatusTab = actorStatusTab;
        this.gid = gid;
        actorAckTab = new boolean[actorIdTab.length];
        save();
    }

    public Sensor (int id, T status, String location, String description, int gid) throws SocketException {
        super(id, status,location, description);
        this.gid = gid;
    }

    public void startScheduler(T firstStatus, T secondStatus,long from, long till){
        timer = new Timer();
        long now = System.currentTimeMillis();
        long start = (from + now)* 1000;  //TODO Berechnung
        long intervall = till * 1000;
        timer.schedule(new TimeSchedulerTask<T>(this, firstStatus, secondStatus), start, intervall);
    }

    public void startRandomScheduler(long intervall){
        timer = new Timer();
        timer.schedule(new TimeSchedulerTask<T>(this), 0, intervall);
    }

    protected  T newTimeSchedulerStatus(T firstStatus, T secondStatus){
       if (timeSchedulerChangeStatus)
            status = secondStatus;
        else 
            status = firstStatus;
       timeSchedulerChangeStatus = (timeSchedulerChangeStatus) ? false : true;
       return status;
    }

    protected  T newTimeSchedulerStatus(){
       return (T) LittleHelpers.randomMeasurement();
    }
    
    public void stopScheduler(){
        timer.cancel(); //Terminate the timer thread
    }

//  public static SensorConfig getConfig(){  //TODO Config file + config als attribut
//        SensorConfig config = JAXB.unmarshal(new File("SensorConfig.xml"), SensorConfig.class);
//        // System.out.println(config);
//        return config;
//    }
//
//    public static void setConfig(SensorConfig config) {
//        FileWriter filewriter = null;
//        try {
//            filewriter = new FileWriter(("SensorConfig.xml"));
//            JAXB.marshal(config, filewriter);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                filewriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    
    public void startResponseThread() {
        actorRespThread art = new actorRespThread(this);
        art.start();
    }

    public void setActorStatus(T status, int pos){
	actorStatusTab[pos] = status;
    }

    public abstract void setActorStatus(String status, int pos);

    public void save(){
        HTWhomeConfig sc = new HTWhomeConfig();
        save(sc);
        sc.setActorIDTab(actorIdTab);
        sc.setActorStatusTab(actorStatusTab);
        sc.setActorAckTab(actorAckTab);
       setConfig(sc, "Sensor");
    }

    public void load(){
        HTWhomeConfig sc = Sensor.getConfig("Sensor");
        load(sc);
        this.actorIdTab = sc.getActorIDTab();
        this.actorStatusTab = (T[]) sc.getActorStatusTab();
        this.actorAckTab = sc.getActorAckTab();
    }

    @Override
    public void handleMsg(String jsonMsg, DeviceType devType, Type cfgType){
	Message msg = gson.fromJson(jsonMsg, Message.class);
        Message reply;
        HTWhomeConfig<T> sc;
	switch (msg.getMsgType()) {
	    case statusRequest: //denke ist fertig. TL
                reply = new Message();
                reply.setMsgType(MessageType.statusResponse);
		reply.setSenderId(this.id);
		reply.setReceiverId(ALLDEVICES);
		reply.setSenderDevice(devType);
                reply.setContent(String.valueOf(this.status));
                sendMsg(reply);
		break;
	    case statusResponse:
                if (actorIdTab != null) {
                    for (int i = 0; i < actorIdTab.length; i++) {
                        if (actorIdTab[i] == msg.getSenderId()) {
			    this.setActorStatus(msg.getContent(), i);
                            actorAckTab[i] = true;
                        }
                    }
                }
		break;
	    case configChange:
		if (msg.getReceiverId() == this.id) {
		    sc = gson.fromJson(msg.getContent(), cfgType);
		    setConfig(sc, "Sensor");
		    getConfig("Sensor");
		}
		break;
	    case configRequest: //TODO implement
                reply = new Message();
		reply.setMsgType(MessageType.configResponse);
		reply.setSenderId(this.id);
		reply.setReceiverId(ALLDEVICES);
		reply.setSenderDevice(devType);
//		SensorConfig<T> sc = new SensorConfig<T>();
////                sc.setDescription(this.description);
////                sc.setId(this.id);
////                sc.setLocation(this.location);
////                sc.setStatus(this.status);
//              save(sc);
//		sc.setActorIDTab(actorIdTab);
//		sc.setActorStatusTab(actorStatusTab);
//                save();
                sc = getConfig("Sensor");
		String content = gson.toJson(sc, cfgType);
                reply.setContent(content);
                sendMsg(reply);
                break;
	}
    }
    public boolean checkRespones(){
        for (int i = 0; i < actorAckTab.length; i++) {
            if(actorAckTab[i] == false)
                return false;
        }
        return true;
    }

    public boolean getTimeSchedulerChangeStatus() {
        return timeSchedulerChangeStatus;
    }

    public void setTimeSchedulerChangeStatus(boolean TimeSchedulerChangeStatus) {
        this.timeSchedulerChangeStatus = TimeSchedulerChangeStatus;
    }
}
