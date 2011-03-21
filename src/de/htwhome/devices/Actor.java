package de.htwhome.devices;
import de.htwhome.transmission.Message;
import de.htwhome.transmission.MessageType;
import de.htwhome.utils.ActorConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketException;
import javax.xml.bind.JAXB;

/**
 *
 * @author Christian Rech, Tim Bartsch
 */
public abstract class Actor<T> extends AbstractDevice<T>{

    private int[] gidTab;

    public Actor() {}

    public Actor(int id, T status, String location, String description, int[] gidTab) throws SocketException {
        super(id, status,location, description);
        this.gidTab = gidTab;
    }

   public static ActorConfig getConfig(){  //TODO Config file + config als attribut
        ActorConfig config = JAXB.unmarshal(new File("ActorConfig.xml"), ActorConfig.class);
        return config;
    }

    public static void setConfig(ActorConfig config) {
        FileWriter filewriter = null;
        try {
            filewriter = new FileWriter(("ActorConfig.xml"));
            JAXB.marshal(config, filewriter);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                filewriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        ActorConfig ac = new ActorConfig();
        save(ac);
        ac.setGidTab(gidTab);
        setConfig(ac);
    }

    public void load() {
        ActorConfig ac = this.getConfig();
        load(ac);
        this.gidTab = ac.getGidTab();
    }

    private boolean isReceiver(int id) {
	for (int i = 0; i < this.gidTab.length; i++) {
	    if (this.gidTab[i] == id) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public void handleMsg(String jsonMsg, Type msgType){
	Message<T> msg = gson.fromJson(jsonMsg, msgType);
        System.out.println("Verarbeite Nachricht vom Typ: " + msg.getMsgType());
	switch (msg.getMsgType()) {
	    case statusChange:
		if(isReceiver(msg.getReceiverId())){
                    System.out.println("betrifft mich");
		    setStatus(msg.getStatus());
		} else {
                    System.out.println("bestrifft mich nicht");
                }
		break;
	    case statusRequest:
		if(isReceiver(id)){
		    setStatus(this.status);
		}
		break;
	    case configChange:
		//TODO implement
		break;
	    case configRequest:
                Message reply = new Message(MessageType.configResponse, this.id, ALLDEVICES, null, this.toString());
                sendMsg(reply, null);
                break;
	}
    }
}
