package cl.lillo.demobluetooth44.Modelo;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Alexi on 04/07/2016.
 */
public class ConexionHelperSQLServer {
    String ip = "192.168.4.154:1433";
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "GESTIONPRODCAF";
    String user = "GESTIONPRODCAF";
    String pass = "GESTIONPRODCAF";
    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL;
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + "/" + db + ";user=" + user + ";password=" + pass + ";";

               /* ConnURL= "jdbc:jtds:sqlserver://IP_Address(for eg.192.168.5.60)/databaseName=Andro;user=username;password=pwd;";
               *
               * ConnURL = "jdbc:jtds:sqlserver://" + ip + "/"
                    + "databaseName=" + db + ";user=" + un + ";password="
                    + password + ";";
               *
               * */
            conn = DriverManager.getConnection(ConnURL);

        } catch (SQLException se) {
            Log.e("ERROR sql: ", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERROR noclass: ", e.getMessage());
        } catch (Exception e) {
            Log.e("ERROR ex: ", e.getMessage());
        }
        return conn;
    }
}
