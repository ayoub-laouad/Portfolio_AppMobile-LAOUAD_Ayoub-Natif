package com.example.portfolioapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NoteAdapter extends ArrayAdapter<Note> {

    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note noteItem = getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_item, parent, false);
            holder = new ViewHolder();
            holder.tvMatiere = convertView.findViewById(R.id.tv_matiere);
            holder.imgStatus = convertView.findViewById(R.id.img_status);
            holder.tvBadge = convertView.findViewById(R.id.tv_badge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Affichage de la matière
        holder.tvMatiere.setText(noteItem.getMatiere());

        // Mise à jour du badge avec la note
        holder.tvBadge.setText(String.valueOf(noteItem.getNote()));

        // Définir l'icône et la couleur du badge selon la note
        if (noteItem.getNote() >= 10) {
            holder.imgStatus.setImageResource(R.drawable.ic_like);
            holder.tvBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Vert
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_dislike);
            holder.tvBadge.setBackgroundColor(Color.parseColor("#F44336")); // Rouge
        }

        return convertView;
    }

    // ViewHolder pour optimiser le recyclage des vues
    static class ViewHolder {
        TextView tvMatiere;
        ImageView imgStatus;
        TextView tvBadge;
    }
}
