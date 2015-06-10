package com.andressantibanez.spotifystreamer.artistsearch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andressantibanez.spotifystreamer.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistSearchResultsAdapter extends BaseAdapter {

    static final String TAG = ArtistSearchResultsAdapter.class.getSimpleName();

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Artist> mArtistsList;

    public ArtistSearchResultsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mArtistsList = new ArrayList<>();

    }

    public void setArtistsList(List<Artist> artistsList) {
        mArtistsList = artistsList != null ? artistsList : new ArrayList<Artist>() ;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mArtistsList.size();
    }

    @Override
    public Artist getItem(int i) {
        if(mArtistsList.size() == 0)
            return null;

        return mArtistsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mLayoutInflater.inflate(R.layout.list_item_artist, viewGroup, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        Artist artist = mArtistsList.get(position);

        holder.name.setText(artist.name);

        List<Image> images = artist.images;
        if(images.size() > 0)
            Picasso.with(mContext).load(images.get(0).url).into(holder.thumbnail);
        else
            holder.thumbnail.setImageBitmap(null);

        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.name) TextView name;
        @InjectView(R.id.thumbnail) ImageView thumbnail;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


}
