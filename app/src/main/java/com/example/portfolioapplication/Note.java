package com.example.portfolioapplication;

import java.io.Serializable;

public class Note implements Serializable {
    private String matiere;
    private int note;

    public Note(String matiere, int note) {
        this.matiere = matiere;
        this.note = note;
    }

    public String getMatiere() {
        return matiere;
    }

    public int getNote() {
        return note;
    }
}
