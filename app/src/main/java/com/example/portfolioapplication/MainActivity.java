package com.example.portfolioapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextNom, editTextPrenom, editTextClasse, editTextRemarques;
    private Button buttonEnregistrer, buttonAppel, buttonAjouterNote;
    private String phoneNumber = ""; // pour stocker le numéro de téléphone récupéré
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int BACKGROUND_LOCATION_REQUEST_CODE = 101;
    private NetworkReceiver networkReceiver;

    // Pour stocker la liste des notes (mise à jour depuis le webservice et lors de l'ajout d'une nouvelle note)
    private ArrayList<Note> notesList = new ArrayList<>();

    private static final int REQUEST_CODE_ADD_NOTE = 1;
    // URL de l'API
    private static final String API_URL = "https://webhook.site/db5f594c-b37a-4746-aa83-da975dd39ceb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Le layout sera choisi selon l'orientation

        // Liaison des vues
        editTextNom = findViewById(R.id.editTextNom);
        editTextPrenom = findViewById(R.id.editTextPrenom);
        editTextClasse = findViewById(R.id.editTextClasse);
        editTextRemarques = findViewById(R.id.editTextRemarques);
        buttonEnregistrer = findViewById(R.id.buttonEnregistrer);
        buttonAppel = findViewById(R.id.buttonAppel);
        buttonAjouterNote = findViewById(R.id.buttonAjouterNote);

        // Au démarrage, consommer le webservice pour récupérer les données du profil
        if (isNetworkAvailable()) {
            new FetchStudentDataTask().execute(API_URL);
        } else {
            Toast.makeText(this, "Pas de connexion internet", Toast.LENGTH_LONG).show();
        }

        // Envoi des données lors du clic sur le bouton "ENREGISTRER"
        buttonEnregistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "Les données sont ajoutées", Toast.LENGTH_SHORT).show();

                String nom = editTextNom.getText().toString();
                String prenom = editTextPrenom.getText().toString();
                String classe = editTextClasse.getText().toString();
                String remarque = editTextRemarques.getText().toString();

                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("nom", nom);
                    jsonData.put("prenom", prenom);
                    jsonData.put("classe", classe);
                    jsonData.put("remarque", remarque);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isNetworkAvailable()) {
                    new PostStudentDataTask().execute(jsonData.toString());
                } else {
                    Toast.makeText(MainActivity.this, "Pas de connexion internet", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Bouton Appel
        buttonAppel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Numéro de téléphone non disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bouton "Ajouter Note" : ouvre NoteActivity pour ajouter une nouvelle note.
        buttonAjouterNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // On transmet le nom et prénom pour afficher le titre dans NoteActivity
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("nom", editTextNom.getText().toString());
                intent.putExtra("prenom", editTextPrenom.getText().toString());
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });
        startLocationServiceIfPermitted();
    }

    /**
     * Vérifie l'état de la connectivité réseau.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Tâche asynchrone pour récupérer les données du profil via GET.
     */
    private class FetchStudentDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder sb = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        sb.append(inputLine);
                    }
                    in.close();
                    response = sb.toString();
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                editTextNom.setText(jsonObject.optString("nom"));
                editTextPrenom.setText(jsonObject.optString("prenom"));
                editTextClasse.setText(jsonObject.optString("classe"));
                editTextRemarques.setText(jsonObject.optString("remarque"));

                // Récupération du numéro de téléphone depuis le JSON (clé "telephone")
                phoneNumber = jsonObject.optString("telephone");

                // Récupération et stockage des notes
                JSONArray notesArray = jsonObject.optJSONArray("notes");
                if (notesArray != null) {
                    for (int i = 0; i < notesArray.length(); i++) {
                        JSONObject noteObj = notesArray.getJSONObject(i);
                        String matiere = noteObj.optString("matiere");
                        int score = noteObj.optInt("note");
                        notesList.add(new Note(matiere, score));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Erreur lors de la récupération des données", Toast.LENGTH_LONG).show();
            }

            // Si le conteneur pour le fragment existe (mode paysage), charger le fragment des notes.
            if (findViewById(R.id.fragment_container) != null) {
                NotesFragment notesFragment = NotesFragment.newInstance(notesList);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, notesFragment)
                        .commit();
            }
        }
    }

    /**
     * Tâche asynchrone pour envoyer les données du profil via POST.
     */
    private class PostStudentDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String jsonData = params[0];
            String response = "";
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(jsonData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK ||
                        responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder sb = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        sb.append(inputLine);
                    }
                    in.close();
                    response = sb.toString();
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, "Données envoyées avec succès", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Récupère le résultat de NoteActivity et ajoute la nouvelle note à la liste.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK && data != null) {
            String matiere = data.getStringExtra("matiere");
            int noteValue = data.getIntExtra("note", 0);
            Note newNote = new Note(matiere, noteValue);
            notesList.add(newNote);
            Toast.makeText(this, "Nouvelle note ajoutée", Toast.LENGTH_SHORT).show();

            // Si le fragment des notes est affiché (mode paysage), mettre à jour le fragment
            if (findViewById(R.id.fragment_container) != null) {
                NotesFragment notesFragment = NotesFragment.newInstance(notesList);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, notesFragment).commit();
            }
        }
    }
    private void startLocationServiceIfPermitted() {
        if (hasLocationPermissions()) {
            startGpsService();
            Log.d("GPS", "Service started from MainActivity");
        } else {
            Log.d("GPS", "Permissions missing, requesting...");
            requestLocationPermissions();
        }
    }

    private boolean hasLocationPermissions() {
        boolean foregroundGranted =
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean backgroundGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundGranted =
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        return foregroundGranted && backgroundGranted;
    }

    private void startGpsService() {
        Intent serviceIntent = new Intent(this, GPSTrackingService.class);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } catch (SecurityException e) {
            Log.e("GPS", "Permission error while starting service", e);
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<>();

            // Check foreground permissions
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    checkSelfPermission(android.Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.FOREGROUND_SERVICE_LOCATION);
            }

            // Request foreground permissions first
            if (!permissionsNeeded.isEmpty()) {
                requestPermissions(permissionsNeeded.toArray(new String[0]), LOCATION_REQUEST_CODE);
            }
            // Then check background permission (Android 10+)
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        BACKGROUND_LOCATION_REQUEST_CODE
                );
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister receiver to avoid leaks
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Register NetworkReceiver when activity comes to foreground
        networkReceiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }
}
