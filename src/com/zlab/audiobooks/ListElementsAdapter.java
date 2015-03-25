package com.zlab.audiobooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ListElementsAdapter extends ArrayAdapter<ListElements> implements OnItemClickListener {

    static Context context; 
    int layoutResourceId;    
    ListElements data[] = null;
    
    public ListElementsAdapter(Context context, int layoutResourceId, ListElements[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ListElementsHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ListElementsHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtAuthor = (TextView)row.findViewById(R.id.txtAuthor);
            holder.txtDiscription = (TextView)row.findViewById(R.id.txtDiscription);
            holder.txtSize = (TextView)row.findViewById(R.id.txtSize);
            row.setTag(holder);
        }
        else
        {
            holder = (ListElementsHolder)row.getTag();
        }
        
        ListElements books = data[position];
        holder.txtTitle.setText(books.title);
        holder.txtAuthor.setText(books.author);
        holder.txtDiscription.setText(books.discription);
        holder.txtSize.setText(books.size);
        holder.imgIcon.setImageResource(books.icon);

/*
        // Îןנוהוכÿול רנטפעû
        Typeface ptcaption=Typeface.createFromAsset(context.getAssets(), "fonts/ptcaption.ttf");
        Typeface ptcaptionnormal=Typeface.createFromAsset(context.getAssets(),"fonts/ptcaptionnormal.ttf");

        holder.txtTitle.setTypeface(ptcaptionnormal);
        holder.txtAuthor.setTypeface(ptcaption);
        holder.txtDiscription.setTypeface(ptcaptionnormal);
        holder.txtSize.setTypeface(ptcaptionnormal);
*/
        return row;
    }

    static class ListElementsHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtAuthor;
        TextView txtDiscription;
        TextView txtSize;
    }

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		/*
		Toast toast = Toast.makeText(com.zlab.audiobooks.AudiobooksMain.mainContext, "", Toast.LENGTH_SHORT);
        toast.show();
        */
	}
}