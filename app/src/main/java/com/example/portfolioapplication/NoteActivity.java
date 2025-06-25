package com.example.portfolioapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class NoteActivity extends AppCompatActivity {

    private EditText editTextMatiere, editTextNote;
    private TextView textViewTitle;
    private Button buttonEnregistrerNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        editTextMatiere = findViewById(R.id.editTextMatiere);
        editTextNote = findViewById(R.id.editTextNote);
        buttonEnregistrerNote = findViewById(R.id.buttonEnregistrerNote);

        // Mettre à jour le titre avec le nom et prénom transmis depuis MainActivity
        textViewTitle = findViewById(R.id.textViewTitle);
        String nom = getIntent().getStringExtra("nom");
        String prenom = getIntent().getStringExtra("prenom");
        textViewTitle.setText(nom + " " + prenom);


        buttonEnregistrerNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String matiere = editTextMatiere.getText().toString();
                String noteStr = editTextNote.getText().toString();

                if (matiere.isEmpty() || noteStr.isEmpty()) {
                    Toast.makeText(NoteActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {
                    int noteValue;
                    try {
                        noteValue = Integer.parseInt(noteStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(NoteActivity.this, "La note doit être un nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Envoyer les données en POST vers l'API
                    new SendNoteToApiTask().execute(matiere, String.valueOf(noteValue));

                    // Préparer l'Intent résultat avec les données de la nouvelle note
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("matiere", matiere);
                    resultIntent.putExtra("note", noteValue);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }
    private class SendNoteToApiTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String matiere = params[0];
            String note = params[1];

            try {
                URL url = new URL("https://webhook.site/db5f594c-b37a-4746-aa83-da975dd39ceb");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Créer l'objet JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("matiere", matiere);
                jsonParam.put("note", note);

                // Envoyer les données
                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("utf-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(NoteActivity.this, "Note envoyée avec succès !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NoteActivity.this, "Échec de l'envoi", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
