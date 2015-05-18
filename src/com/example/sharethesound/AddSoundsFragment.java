package com.example.sharethesound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AddSoundsFragment extends Fragment { //S'hauria de fer amb listFragment!!	
	OnSongSelectedListener mListener;
	
	String[] songInfo;
	String[] mAudioPath;
	ArrayList<Integer> itemsSelected;
	ListView mListView;
	
	public AddSoundsFragment() {	
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSongSelectedListener");
        }
    }
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		itemsSelected = new ArrayList<Integer>();
		if(savedInstanceState == null) getMusic();
		else{
			songInfo = savedInstanceState.getStringArray("SongInfo");
			mAudioPath = savedInstanceState.getStringArray("AudioPath");
			itemsSelected = savedInstanceState.getIntegerArrayList("ItemsSelected");
		}
	}
	
	/** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	  View rootView = inflater.inflate(R.layout.add_sounds, container, false);
	  mListView = (ListView) rootView.findViewById(R.id.fileList);
	 
	  ArrayAdapter<String> songsAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
				R.layout.add_sounds_row, songInfo);
	  mListView.setAdapter(songsAdapter);

	  mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
	    	@Override
	    	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	    		if(!itemsSelected.contains(position)){
	    			TextView row = (TextView) arg1.findViewById(R.id.row);
	    			row.setTextColor(Color.GRAY);
	    			mListener.onSongSelected(songInfo[position], mAudioPath[position]);
	    			itemsSelected.add(position);
	    		}
	    }
	  });
	  
	  return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntegerArrayList("ItemsSelected", itemsSelected);
		outState.putStringArray("SongInfo", songInfo);
		outState.putStringArray("AudioPath", mAudioPath);
	}
	
	private void getMusic() {
		final Cursor mCursor = this.getActivity().getContentResolver().query(
	            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	            new String[] { MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION },
	            null,
	            null,
	            "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

	    int count = mCursor.getCount();

	    songInfo = new String[count];
	    mAudioPath = new String[count];
	    int i = 0;
	    if (mCursor.moveToFirst()) {
	        do {
	        	String duration = calculateDuration(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
	            songInfo[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) + " - " +
	            		   mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) + " (" +
	            		   duration +")";
	            mAudioPath[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
	            i++;
	        } while (mCursor.moveToNext());
	    }   

	    mCursor.close();
	}

	private String calculateDuration(String string) {
		Long duration = Long.valueOf(string);
		duration /=1000;
		String seconds;
		if((duration%60)<10) seconds = "0" + String.valueOf(duration%60);
		else seconds =  String.valueOf(duration%60);
	    return String.valueOf(duration/60) + ":" + seconds; 
	}
	
	// Container Activity must implement this interface
    public interface OnSongSelectedListener {
        public void onSongSelected(String info, String path);
    }
}
