package de.hda.nzse22.download;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hda.nzse22.R;
import de.hda.nzse22.model.ChargingStationDAO;
import de.hda.nzse22.model.NZSEDatabase;
import de.hda.nzse22.model.chargingStation;

public class GetChargingStationsActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_STORAGE_INTENRET = 1;
    private final String filePath = "NZSE";
    private final String ladestationen = "ladestationen.txt"; // Name der lokalen Datei
    private final String url =
            "https://data.bundesnetzagentur.de/Bundesnetzagentur/SharedDocs/Downloads/DE/Sachgebiete/Energie/Unternehmen_Institutionen/E_Mobilitaet/Ladesaeulenregister.csv";
    boolean mBound = false;
    private NZSEDatabase database;
    private Intent returnIntent;
    private String csvFile = "Empty"; // liefert die Downloadfunktion
    private RequestService mService = null;
    private RequestService.RequestServiceBinder binder;

    /**
     * Reads downloaded csv file and cleans each line and creates new chargingStation objects and inserts them into the database
     */
    public void csvRead() throws InterruptedException {
        ChargingStationDAO chargingStationDAO = database.chargingStationDAO();
        List<chargingStation> allChargingStations = new ArrayList<>();
        int rows = 0;
        try {
            String[] csvline; // eingelesene csvZeile
            // lokale Datei ansprechen
            File myFile = new File(csvFile);
            FileInputStream fIn = new FileInputStream(myFile);
            InputStreamReader isr = new InputStreamReader(fIn, "ISO_8859-1");
            BufferedReader myReader = new BufferedReader(isr);

            CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(false).build();
            CSVReader csvReader = new CSVReaderBuilder(myReader).withSkipLines(11).withCSVParser(parser).build();

            System.out.println("CSV-Datei einlesen startet ");

            while ((csvline = csvReader.readNext()) != null) { // Dateiende?
                allChargingStations.add(new chargingStation(Arrays.asList(csvline)));
                rows++;
            }
            csvReader.close();
            myReader.close();

            System.out.println("Chargingstations: " + rows);
            Thread databaseInsertThread = new Thread(() -> chargingStationDAO.insertAll(allChargingStations));
            databaseInsertThread.start();
            databaseInsertThread.join();
        } catch (CsvValidationException csvValidationException) {
            csvValidationException.printStackTrace();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            finish();
        }
        // try-catch
    } // csvRead

    /**
     * Sets Handler:
     * Callbacks associated for Service Binding, forward to bindService() for result handling
     *
     * @return - Handler
     */
    private Handler getHandler() {
        return new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                csvFile = (String) bundle.get(RequestService.FILEPATH);
                // Datei einlesen

                String uniqueId = (String) bundle.get(RequestService.UNIQUEID);
                String note = (String) bundle.get(RequestService.NOTIFICATION);
                Toast.makeText(GetChargingStationsActivity.this, uniqueId + " file: " +
                        csvFile + " Bytes: " + note, Toast.LENGTH_LONG).show();
                try {
                    csvRead();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }// handleMessage
        };
    }    // Serviceverbindung einrichten

    /**
     * @param savedInstanceState -
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Ladestationen");
        setContentView(R.layout.activity_download);
        database = NZSEDatabase.getDatabase(getApplicationContext());

        // Permission grant/gewähren
        String[] permissions = {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_STORAGE_INTENRET);

        // Bind to RequestService
        Intent myIntent = new Intent(this, RequestService.class);
        bindService(myIntent, mConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * @param requestCode  RequestCode of Permission
     * @param permissions  String array of permissions
     * @param grantResults Granted permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_STORAGE_INTENRET) {// wenn die Anfrage gecancelled wird, sind die Ergebnisfelder leer.
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
    }    /**
     * Creates a new CerviceConnection
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // an den RequestService binden,
            // Service-Objekt casting auf IBinder und LocalService instance erhalten
            binder = (RequestService.RequestServiceBinder) service;
            System.out.println(binder);
            mService = binder.getService();
            // callback setzen
            mService.setCallback(getHandler());
            System.out.println("DOWNLOAD starten");
            binder.runURLDownload("download", url, filePath, ladestationen);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     * If activity finshed unbind service
     */
    @Override
    public void finish() {
        System.out.println("******  bye bye");
        unbindService(mConnection);
        super.finish();
    }



} // GetChargingStationActivity
