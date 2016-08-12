package cl.lillo.demobluetooth44;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//import com.example.bluetooth2.R;

public class MainActivity extends Activity {
    private static final String TAG = "bluetooth2";

    TextView txtKL, txtLB;
    Handler h;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    //private static String address = "98:D3:31:30:69:E9";  //pesa 26
    //private static String address = "98:D3:31:20:73:87";  //pesa 17
    private static String address = getBluetoothMacAddress();
    private String mac;
    private String neg = "";
    private Button scannerButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seleccionarMac();

        //scanner qr
        scannerButton = (Button) findViewById(R.id.scannerButton);
        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), BarcodeScanner.class);
                startActivity(intent);
            }
        });
        //fin scanner qr

        //TABS
        Resources res = getResources();

        TabHost tabs = (TabHost) findViewById(R.id.tabHost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("mitab1");
        spec.setContent(R.id.linearLayout);
        spec.setIndicator("", res.getDrawable(R.drawable.weight));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab2");
        spec.setContent(R.id.linearLayout2);
        spec.setIndicator("", res.getDrawable(R.drawable.card));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab3");
        spec.setContent(R.id.linearLayout3);
        spec.setIndicator("", res.getDrawable(R.drawable.sync));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab4");
        spec.setContent(R.id.linearLayout4);
        spec.setIndicator("", res.getDrawable(R.drawable.menu));
        tabs.addTab(spec);

        tabs.setCurrentTab(0);
        //fin tabs

        txtKL = (TextView) findViewById(R.id.txtPesoKL);
        txtLB = (TextView) findViewById(R.id.txtPesoLB);

        mac = getBluetoothMacAddress();
        //txtLB.setText(mac);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String str = "";
                        String[] cadena;
                        str = Arrays.toString(readBuf);  // unica manera
                        cadena = str.split(",");
                        try {
                            int coma;
                            int peso;
                            int cienLibras;
                            int num = Integer.parseInt(cadena[0].replace(" ", "").replace("[", ""));
                            int num2 = Integer.parseInt(cadena[1].replace(" ", "").replace("[", ""));
                            if (num == 35) {
                                neg = "-";
                            }
                            if (num == 99) {
                                neg = "-";
                            }
                            if (num == 67) {
                                neg = "";
                            }
                            if (num == 3) {
                                neg = "";
                            }
                            if (num2 != 67) {
                                if (num == -1) {
                                    coma = converter(Integer.parseInt(cadena[1].replace(" ", "")));
                                    peso = converter(Integer.parseInt(cadena[2].replace(" ", "")));
                                    cienLibras = Integer.parseInt(cadena[3].replace(" ", ""));
                                    if (cienLibras > 0) {
                                        peso += (cienLibras * 100);
                                    }
                                    if (coma < 10) {
                                        txtKL.setText(neg + peso + ".0" + coma);
                                    } else {
                                        txtKL.setText(neg + peso + "." + coma);
                                    }
                                }
                            }
                            System.out.println("Array: " + str);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    public void seleccionarMac() {
        AlertDialog.Builder alertEliminarPlanta = new AlertDialog.Builder(this);
        alertEliminarPlanta.setTitle("Seleccionar Pesa");
        alertEliminarPlanta.setMessage("Seleccione la pesa a utilizar, asegurese de que la pesa esté vinculada.");
        final Spinner listaPesas = new Spinner(this);
        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Pesa 17");
        spinnerArray.add("Pesa 26");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerArray);
        listaPesas.setAdapter(adapter);
        alertEliminarPlanta.setView(listaPesas);
        alertEliminarPlanta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pesa = listaPesas.getSelectedItem().toString();
                if (pesa.equals("Pesa 17"))
                    address = "98:D3:31:20:73:87";
                if (pesa.equals("Pesa 26"))
                    address = "98:D3:31:30:69:E9";
            }
        });

        alertEliminarPlanta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertEliminarPlanta.show();
    }

    //retorna mac de dispositivo por el nombre
    public static String getBluetoothMacAddress() {
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // if device does not support Bluetooth
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "device does not support bluetooth");
            return null;
        }

        String mac = "";
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            // here you get the mac using device.getAddress()
            if (device.getName().toString().startsWith("HUBCROP")) {
                mac = device.getAddress();
            }
        }
        //return mBluetoothAdapter.getAddress();
        return mac;
    }

    public int converter(int libra) {
        if (libra >= 16 && libra <= 25) {
            libra -= 6;
        }
        if (libra >= 32 && libra <= 41) {
            libra -= 12;
        }
        if (libra >= 48 && libra <= 63) {
            libra -= 18;
        }
        if (libra >= 64 && libra <= 73) {
            libra -= 24;
        }
        if (libra >= 80 && libra <= 89) {
            libra -= 30;
        }
        if (libra >= 96 && libra <= 105) {
            libra -= 36;
        }
        if (libra >= 112 && libra <= 121) {
            libra -= 42;
        }
        if (libra >= -128 && libra <= -119) {
            libra += 208;
        }
        if (libra >= -112 && libra <= -103) {
            libra += 202;
        }

        return libra;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}
