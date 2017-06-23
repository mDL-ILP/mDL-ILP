package com.ul.ts.products.mdlholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ul.ts.products.mdlholder.data.Category;

public class CategoryAdapter extends ArrayAdapter<Category> {

    private final Context context;
    private final Category[] objects;

    public CategoryAdapter(Context context, Category[] objects) {
        super(context, R.layout.list_category, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_category, parent, false);
        }

        ImageView categoryIcon = (ImageView) convertView.findViewById(R.id.category_icon);
        TextView categoryText = (TextView) convertView.findViewById(R.id.category_text);
        TextView categoryStartDate = (TextView) convertView.findViewById(R.id.category_startDate);
        TextView categoryEndDate = (TextView) convertView.findViewById(R.id.category_endDate);
        TextView categoryRestrictions = (TextView) convertView.findViewById(R.id.category_restrictions);

        final Category c = objects[position];

        categoryIcon.setImageBitmap(getCategoryIcon(c.getLabelAsString()));
        categoryText.setText(c.getLabelAsString());
        categoryStartDate.setText(c.getFromDate());
        categoryEndDate.setText(c.getUntillDate());
        categoryRestrictions.setText(c.getRestrictions());

        return convertView;
    }

    private Bitmap getCategoryIcon(String category) {

        switch (category) {
            case "AM":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_am);
            case "A1":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_a1);
            case "A2":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_a2);
            case "A":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_a);
            case "B1":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_b1);
            case "B":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_b);
            case "C1":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_c1);
            case "C":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_c);
            case "D1":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_d1);
            case "D":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_d);
            case "BE":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_be);
            case "C1E":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_c1e);
            case "CE":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_ce);
            case "D1E":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_d1e);
            case "DE":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_de);
            case "T":	return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_t);
            default:	return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
    }

}