package com.example.portfolioapplication;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;

public class NotesFragment extends Fragment {

    private ArrayList<Note> notesList;

    public NotesFragment() {
        // Constructeur par défaut
    }

    public static NotesFragment newInstance(ArrayList<Note> notes) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putSerializable("notes", notes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notesList = (ArrayList<Note>) getArguments().getSerializable("notes");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate le layout pour ce fragment
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        ListView listView = view.findViewById(R.id.listViewNotes);

        // Définir l’adapter pour la ListView
        NoteAdapter adapter = new NoteAdapter(getActivity(), notesList);
        listView.setAdapter(adapter);

        return view;
    }
}
