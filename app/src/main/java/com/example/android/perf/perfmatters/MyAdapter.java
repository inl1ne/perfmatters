package com.example.android.perf.perfmatters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Created by ilewis on 11/11/2014.
 */
public class MyAdapter extends BaseAdapter {
    private class Item {
        public int image;
        public int tint;
        public String title;
        public String speaker;

        private Item(int image, int tint, String title, String speaker) {
            this.image = image;
            this.tint = tint;
            this.title = title;
            this.speaker = speaker;
        }
    }

    private Item[] items = new Item[]{
            new Item(R.drawable.colt, R.color.tint1, "Perf Like a Pirate", "Cap'n Colt 'Mad Dog' McAnlis"),
            new Item(R.drawable.dan, R.color.tint2, "I Don't Always Perf, But When I Do...", "The Most Interesting Dan In the World"),
            new Item(R.drawable.ian, R.color.tint3, "I Liked Perf Before It Was Cool", "Ian (Just Ian)"),
            new Item(R.drawable.reto, R.color.tint4, "He's the One They Call Dr. PerfGood", "Reto Meier (not a real doctor)"),
            new Item(R.drawable.alex, R.color.tint1, "Perf Like This Guy!", "This Guy"),
    };

    private Context mContext;

    public MyAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = items[position];
        View result;
        if (convertView != null) {
            result = convertView;
        } else {
            result = LayoutInflater.from(mContext).inflate(R.layout.item, null);
        }

        ImageView image = (ImageView) result.findViewById(R.id.image);
        TextView title = (TextView) result.findViewById(R.id.title);
        TextView speaker = (TextView) result.findViewById(R.id.speaker);
        View background = result.findViewById(R.id.background);

        background.setBackgroundColor(item.tint);
        image.setImageResource(item.image);
        title.setText(item.title);
        speaker.setText(item.speaker);

        return  result;
    }
}
