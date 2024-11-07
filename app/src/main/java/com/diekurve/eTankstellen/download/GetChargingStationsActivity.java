package com.diekurve.eTankstellen.download;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.diekurve.eTankstellen.R;
import com.diekurve.eTankstellen.model.ChargingStationDAO;
import com.diekurve.eTankstellen.model.chargingStation;
import com.diekurve.eTankstellen.model.chargingStations;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetChargingStationsActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_STORAGE_INTERNET = 1;
    boolean mBound = false;
    private chargingStations database;
    private Intent returnIntent;

    /**
     * Reads downloaded csv file and cleans each line and creates new chargingStation objects and
     * inserts them into the database
     */
    public void csvRead() throws InterruptedException {
        ChargingStationDAO chargingStationDAO = database.chargingStationDAO();
        List<chargingStation> allChargingStations = new ArrayList<>();
        int rows = 0;
        try {
            String[] csvline;
            URL url = new URL("https://data.bundesnetzagentur.de/Bundesnetzagentur/" +
                    "SharedDocs/Downloads/DE/Sachgebiete/Energie/Unternehmen_Institutionen/" +
                    "E_Mobilitaet/Ladesaeulenregister.csv");

            BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
            CSVParser parser =
                    new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(false).build();
            CSVReader csvReader =
                    new CSVReaderBuilder(input).withSkipLines(11).withCSVParser(parser).build();

            System.out.println("CSV-Datei einlesen startet ");

            while ((csvline = csvReader.readNext()) != null) { // Dateiende?
                allChargingStations.add(new chargingStation(Arrays.asList(csvline)));
                rows++;
            }
            csvReader.close();

            System.out.println("Chargingstations: " + rows);
            Thread databaseInsertThread =
                    new Thread(() -> chargingStationDAO.insertAll(allChargingStations));
            databaseInsertThread.start();
            databaseInsertThread.join();
        } catch (CsvValidationException | IOException csvValidationException) {
            Log.e("err", csvValidationException.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Log.e("err", e.toString());
        } finally {
            finish();
        }
        // try-catch
    } // csvRead

//    /**
//     * Sets Handler:
//     * Callbacks associated for Service Binding, forward to bindService() for result handling
//     *
//     * @return - Handler
//     */
//    private Handler getHandler() {
//        return new Handler(Looper.getMainLooper()) {
//            public void handleMessage(Message msg) {
//                Bundle bundle = msg.getData();
//                csvFile = (String) bundle.get(RequestService.FILEPATH);
//                // Datei einlesen
//
//                String uniqueId = (String) bundle.get(RequestService.UNIQUEID);
//                String note = (String) bundle.get(RequestService.NOTIFICATION);
//                Toast.makeText(GetChargingStationsActivity.this, uniqueId + " file: " +
//                        csvFile + " Bytes: " + note, Toast.LENGTH_LONG).show();
//                try {
//                    csvRead();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }// handleMessage
//        };
//    }    // Serviceverbindung einrichten

    /**
     * @param savedInstanceState -
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Ladestationen");
        setContentView(R.layout.activity_download);
        database = chargingStations.getDatabase(getApplicationContext());

        // Permission grant/gewähren
        String[] permissions = {Manifest.permission.INTERNET};
        ActivityCompat.requestPermissions(this, permissions,
                MY_PERMISSIONS_STORAGE_INTERNET);

        // Bind to RequestService
//        Intent myIntent = new Intent(this, RequestService.class);
//        bindService(myIntent, mConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * @param requestCode  RequestCode of Permission
     * @param permissions  String array of permissions
     * @param grantResults Granted permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_STORAGE_INTERNET) {
            // wenn die Anfrage gecancelled wird, sind die Ergebnisfelder leer.
            if (grantResults.length > 0) {
                for (int grant : grantResults) {
                    if (grant == PackageManager.PERMISSION_GRANTED)
                        // Permissions wurden gewährt
                        System.out.println(permissions[grant] + " vorhanden");
                        // Berechtigungen stehen zu Verfügung, Zugriffe ausführen ..
                    else
                        System.out.println(permissions[grant] + "  n i c h t  vorhanden");
                    // Permissions werden abgelehnt, spezifische Zugriffe werden nicht ausgeführt
                }
            }
        }
    }
//    /**
//     * Creates a new ServiceConnection
//    */
//    private final ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // an den RequestService binden,
//            // Service-Objekt casting auf IBinder und LocalService instance erhalten
//            binder = (RequestService.RequestServiceBinder) service;
//            System.out.println(binder);
//            mService = binder.getService();
//            // callback setzen
//            mService.setCallback(getHandler());
//            System.out.println("DOWNLOAD starten");
//
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };

    /**
     * If activity finished unbind service
     */
    @Override
    public void finish() {
        //unbindService(mConnection);
        super.finish();
    }
} // GetChargingStationActivity
