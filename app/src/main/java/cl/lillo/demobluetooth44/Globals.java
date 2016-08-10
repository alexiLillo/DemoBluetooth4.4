package cl.lillo.demobluetooth44;

/**
 * Created by Alexi on 06/07/2016.
 */
public class Globals {
    private static Globals instance;

    // Global variable
    private boolean puedeSync;

    // Restrict the constructor from being instantiated
    private Globals() {
    }

    public void setData(boolean d) {
        this.puedeSync = d;
    }

    public boolean getData() {
        return this.puedeSync;
    }

    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }
}
