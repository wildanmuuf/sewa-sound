package com.unpam.sewasound;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class ImageSoundAdapter extends RecyclerView.Adapter<ImageSoundAdapter.SimpleViewHolder> {

    private Context ctx;
    private int pos;
    private LayoutInflater inflater;
    AlertDialog alert;
    ArrayList<Uri> mArrayUri;
    ArrayList<Bitmap> bitmaps;
    int countLoadImage;
    public ImageSoundAdapter(Context ctx, ArrayList<Bitmap> bitmaps) {
        this.ctx = ctx;
        this.bitmaps = bitmaps;
    }

    public ImageSoundAdapter(Context ctx, ArrayList<Bitmap> bitmaps, ArrayList<Uri> mArrayUri, int countLoadImage) {
        this.mArrayUri = mArrayUri;
        this.ctx = ctx;
        this.bitmaps = bitmaps;
        this.countLoadImage = countLoadImage;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivGallery;
        private Button btnDelete;
        public SimpleViewHolder(View view) {
            super(view);
            ivGallery = (ImageView) view.findViewById(R.id.ivGallery);
            btnDelete = (Button) view.findViewById(R.id.btn_delete_image);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!v.isSelected()){
                        v.setSelected(true);
                        btnDelete.setVisibility(View.VISIBLE);
                        v.setBackgroundResource(R.drawable.rect_sel);
                    }else{
                        v.setSelected(false);
                        btnDelete.setVisibility(View.GONE);
                        v.setBackgroundResource(0);
                    }
                }
            });
        }
    }
    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(this.ctx).inflate(R.layout.gv_sound_item, parent, false);

        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {

        holder.ivGallery.setImageBitmap(bitmaps.get(position));
        for(int i = 0; i < bitmaps.size(); i++){
            holder.btnDelete.setId(i);
            if(holder.btnDelete.getId() == i){
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx).
                                setTitle("Apakah anda yakin ingin menghapus ini ?").
                                setMessage("Hapus sound ini akan menyebabkan data sound anda akan hilang.").setCancelable(false).
                                setPositiveButton("HAPUS", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        bitmaps.remove(position);

                                        notifyDataSetChanged();
                                    }
                                }).setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alert = alertBuilder.create();
                        alert.show();
                    }
                });
            }
        }

        if(mArrayUri != null ){
            if(mArrayUri.size() >0){
                for(int i = bitmaps.size(); i > mArrayUri.size(); i++) {
                    holder.btnDelete.setId(i);
                    if (holder.btnDelete.getId() == i) {
                        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx).
                                        setTitle("Apakah anda yakin ingin menghapus ini ?").
                                        setMessage("Hapus sound ini akan menyebabkan data sound anda akan hilang.").setCancelable(false).
                                        setPositiveButton("HAPUS", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(countLoadImage>0){
                                                    mArrayUri.remove(bitmaps.size() - position);
                                                }else{
                                                    mArrayUri.remove(position);
                                                }
                                                notifyDataSetChanged();
                                            }
                                        }).setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                alert = alertBuilder.create();
                                alert.show();
                            }
                        });
                    }
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return this.bitmaps.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
