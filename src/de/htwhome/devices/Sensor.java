package de.htwhome.devices;

/**
 *
 * @author Christian Rech, Tim Bartsch
 */
public abstract class Sensor<T> extends AbstractDevice<T>{

    private String[] actorList;
    private int gID;

     public Sensor (int id, T status,String location, String type, String hint, String[] aktorList, int gID) {
        super(id, status,location, type, hint);
        this.actorList = aktorList;
        this.gID = gID;
    }

}
