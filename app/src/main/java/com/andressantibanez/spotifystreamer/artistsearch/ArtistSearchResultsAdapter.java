package com.andressantibanez.spotifystreamer.artistsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andressantibanez.spotifystreamer.R;
import com.andressantibanez.spotifystreamer.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;

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
        mArtistsList = artistsList;
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

        //Get artist
        Artist artist = mArtistsList.get(position);

        //Get thumbnailUrl
        String thumbnailUrl = null;
        if(artist.images.size() > 0) {
            //Try 200px image
            thumbnailUrl = Utils.getThumbnailUrl(artist.images, 200);
            //Get first available
            if(thumbnailUrl == null) {
                thumbnailUrl = Utils.getThumbnailUrl(artist.images, 0);
            }
        }

        //Apply data to layout
        if(thumbnailUrl != null)
            Picasso.with(mContext).load(thumbnailUrl).into(holder.thumbnail);
        else
            holder.thumbnail.setImageBitmap(null);

        holder.name.setText(artist.name);


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
