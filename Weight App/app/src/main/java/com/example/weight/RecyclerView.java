package com.example.weight;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class RecyclerView extends androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.MyViewHolder> {
    // define the context and activity
    private Context context;
    private Activity activity;

    // Declare ArrayLists
    private ArrayList<String> measurement_id;
    private ArrayList<String> measurement_title;
    private ArrayList<String> measurement_date;
    private ArrayList<String> measurement_weight;

    // Create a variable to increment measurement_id
    private int itemCounter = 1;

    RecyclerView(Activity activity, Context context, ArrayList<String> measurement_id,
                 ArrayList<String> measurement_title, ArrayList<String> measurement_date,
                 ArrayList<String> measurement_weight) {

        this.activity = activity;
        this.context = context;
        this.measurement_id = measurement_id;
        this.measurement_title = measurement_title;
        this.measurement_date = measurement_date;
        this.measurement_weight = measurement_weight;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_view, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M) // minimum api level is 23, see gradle settings
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
            // assign a measurement_id and set to the ui
            String id = String.valueOf(itemCounter);
            itemCounter++; // Increment the measurement_id each time

            // initialize the variables of our data at the current position
            final String title = String.valueOf(measurement_title.get(position));
            final String date = String.valueOf(measurement_date.get(position));
            final String weight = String.valueOf(measurement_weight.get(position));

            // format the edit text
            String formattedEntry = String.format("%s \n%s \n%s \n%s lbs", id, title, date, weight);

            // format the lines of text
            SpannableString span = new SpannableString(formattedEntry);
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#FFBB86FC")),
                    formattedEntry.indexOf('\n') + 1, formattedEntry.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#FF03DAC5")),
                    formattedEntry.lastIndexOf('\n') + 1, formattedEntry.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            span.setSpan(new RelativeSizeSpan(1.2f),
                    formattedEntry.lastIndexOf('\n') + 1, formattedEntry.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            holder.id_text.setText(span);

            // open the edit activity for user entries
            holder.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = holder.getBindingAdapterPosition();

                    // if the entry exists in the recycler view
                    if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {

                        // start the edit activity
                        Intent intent = new Intent(context, EditActivity.class);

                        // push the entry data with intent to the edit activity
                        intent.putExtra("id", String.valueOf(measurement_id.get(adapterPosition)));
                        intent.putExtra("title", title);
                        intent.putExtra("date", date);
                        intent.putExtra("weight", weight);

                        // start the intent
                        activity.startActivityForResult(intent, 1);
                    }
                }
            });
        }
    }

    // function to get the length of the ArrayList
    @Override
    public int getItemCount() {
        return measurement_id.size();
    }

    class MyViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        // declare the text views
        TextView id_text;
        LinearLayout mainLayout;


        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // define the ui variables
            id_text = itemView.findViewById(R.id.id_text);


            // define the layout
            mainLayout = itemView.findViewById(R.id.home_layout);


            // Set OnClickListener function for the add button
            id_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getBindingAdapterPosition();

                    // if the entry exists in the recycler view
                    if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {

                        // start the edit activity
                        Intent intent = new Intent(context, EditActivity.class);

                        // push the entry data with intent to the edit activity
                        intent.putExtra("id", String.valueOf(measurement_id.get(adapterPosition)));
                        intent.putExtra("title", String.valueOf(measurement_title.get(adapterPosition)));
                        intent.putExtra("date", String.valueOf(measurement_date.get(adapterPosition)));
                        intent.putExtra("weight", String.valueOf(measurement_weight.get(adapterPosition)));

                        // start the intent
                        activity.startActivityForResult(intent, 1);
                    }
                }
            });

        }
    }
}
