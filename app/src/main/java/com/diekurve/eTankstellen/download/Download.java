package com.diekurve.eTankstellen.download;

import android.util.Log;

import com.diekurve.eTankstellen.model.ChargingStationDAO;
import com.diekurve.eTankstellen.model.chargingStation;
import com.diekurve.eTankstellen.model.chargingStations;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Download implements Runnable {

    private chargingStations database;

    /**
     * Reads csv file and cleans each line and creates new chargingStation objects and
     * inserts them into the database
     */
    public void csvRead() throws InterruptedException {
        ChargingStationDAO chargingStationDAO = database.chargingStationDAO();
        List<chargingStation> allChargingStations = new ArrayList<>();

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new URL(
                    "https://data.bundesnetzagentur.de/Bundesnetzagentur/" +
                            "SharedDocs/Downloads/DE/Sachgebiete/Energie/Unternehmen_Institutionen/"
                            + "E_Mobilitaet/Ladesaeulenregister.csv").openStream()));
            CSVParser parser =
                    new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(false).build();
            CSVReader csvReader =
                    new CSVReaderBuilder(input).withSkipLines(11).withCSVParser(parser).build();

            Log.i("Info", "Begin reading csv-file");
            List<String[]> lines =  csvReader.readAll();
            for (String[] line: lines ) {
                allChargingStations.add(new chargingStation(Arrays.asList(line)));
            }
            Log.i("Info", "Charging stations: " + csvReader.getLinesRead());
            csvReader.close();
            Log.i("Info", "Finished reading csv-file");
            chargingStationDAO.insertAll(allChargingStations);

        } catch (Exception e) {
            Log.e("err", e.toString());
        }
        // try-catch
    } // csvRead

    @Override
    public void run() {
        try {
            csvRead();
        } catch (InterruptedException e) {
            Log.e("Error", e.toString());
            throw new RuntimeException(e);
        }
    }
} // Download
