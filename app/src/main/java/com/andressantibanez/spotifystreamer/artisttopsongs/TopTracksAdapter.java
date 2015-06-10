package com.andressantibanez.spotifystreamer.artisttopsongs;

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
import kaaes.spotify.webapi.android.models.Track;

public class TopTracksAdapter extends BaseAdapter{

    public static final String TAG = TopTracksAdapter.class.getSimpleName();

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Track> mTopTracksList;

    public TopTracksAdapter(Context context) {
        super();

        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mTopTracksList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mTopTracksList.size();
    }

    @Override
    public Track getItem(int i) {
        if(mTopTracksList.size() == 0)
            return null;

        return mTopTracksList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        if(convertView != null){
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mLayoutInflater.inflate(R.layout.list_item_top_track, viewGroup, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        Track track = getItem(position);

        //Get thumbnailUrl
        String thumbnailUrl = null;
        if(track.album.images.size() > 0) {
            //Try getting 200px first
            thumbnailUrl = Utils.getThumbnailUrl(track.album.images, 200);
            //If none found, try anyone
            if(thumbnailUrl == null)
                thumbnailUrl = Utils.getThumbnailUrl(track.album.images, 0);
        }

        //Apply data to layuot
        if(thumbnailUrl != null)
            Picasso.with(mContext).load(thumbnailUrl).into(holder.thumbnail);
        else
            holder.thumbnail.setImageBitmap(null);

        holder.trackName.setText(track.name);
        holder.albumName.setText(track.album.name);

        return convertView;
    }

    public void setTopTracksList(List<Track> topTracksList) {
        this.mTopTracksList = topTracksList;
    }

    static class ViewHolder {
        @InjectView(R.id.thumbnail) ImageView thumbnail;
        @InjectView(R.id.track_name) TextView trackName;
        @InjectView(R.id.album_name) TextView albumName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
