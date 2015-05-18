package com.example.sharethesound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlaylistFragment extends Fragment implements View.OnClickListener, OnCompletionListener, OnSeekBarChangeListener {
	
	ArrayList<String> songInfo;
	ArrayList<String> mAudioPath;
	ListView mListView;
	int positionPlaying = -1;
	Button bPlay, bNextSong, bPrevSong;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private SeekBar songProgressBar;

	Utils utils;
	boolean isPlaying = false;
	boolean isPaused = false;
    private Handler mHandler = new Handler();
	
	MediaPlayer mediaPlayer;
	
	public PlaylistFragment() {
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		utils = new Utils();
		if(savedInstanceState == null) {
			songInfo = new ArrayList<String>();
			mAudioPath=new ArrayList<String>();
		}
		else {
			songInfo = savedInstanceState.getStringArrayList("SongInfo");
			mAudioPath = savedInstanceState.getStringArrayList("AudioPath");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.playlist, container, false);
		mListView = (ListView) rootView.findViewById(R.id.playlistListView);
		
		bPlay = (Button) rootView.findViewById(R.id.bplay);
		bNextSong = (Button) rootView.findViewById(R.id.nextSong);
		bPrevSong = (Button) rootView.findViewById(R.id.previousSong);
		
	    songCurrentDurationLabel= (TextView) rootView.findViewById(R.id.currentTime);
	    songTotalDurationLabel=(TextView) rootView.findViewById(R.id.totalTime);
	    songProgressBar= (SeekBar) rootView.findViewById(R.id.seekBar1);

		mediaPlayer = new MediaPlayer();
	    
	    songProgressBar.setOnSeekBarChangeListener(this); 
	    mediaPlayer.setOnCompletionListener(this);
	    
		bPlay.setOnClickListener(this);
		bNextSong.setOnClickListener(this);
		bPrevSong.setOnClickListener(this);
		
		
		ArrayAdapter<String> songsAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
					R.layout.add_sounds_row, songInfo);
		mListView.setAdapter(songsAdapter);
		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
		    	@Override
		    	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					if(position != positionPlaying && positionPlaying != -1) {
						mListView.invalidateViews();
					}
		    		if(position != positionPlaying){
		    			positionPlaying = position;
		    			TextView row = (TextView) arg1.findViewById(R.id.row);
		    			row.setText(" > " + row.getText());
		    			bPlay.setText("||");
		    			playSong(mAudioPath.get(position));
		    		}
		    		isPlaying = true;
		    }
		  });
		
	    return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList("SongInfo", songInfo);
		outState.putStringArrayList("AudioPath", mAudioPath);		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.bplay:
				if(isPaused) {
					mediaPlayer.start();
					Button bt = (Button) v;
					bt.setText("||");
					isPaused= false;
				}
				else if(isPlaying) {
					mediaPlayer.pause();
					Button bt = (Button) v;
					bt.setText(">");
					isPaused = true;
				}
				else if(songInfo.size()>0){
					positionPlaying =0;
					playSong(mAudioPath.get(positionPlaying));
	    			bPlay.setText("||");
		    		isPlaying = true;
				}
				break;
			case R.id.previousSong:
				if(positionPlaying>0) {
					positionPlaying -=1;
					playSong(mAudioPath.get(positionPlaying));
				}
				break;
			case R.id.nextSong:
				if(positionPlaying < mAudioPath.size()-1) {
					positionPlaying +=1;
					playSong(mAudioPath.get(positionPlaying));
				}
		}
	}

	private void playSong(String path) {
          try {
    		mediaPlayer.reset();
			mediaPlayer.setDataSource(path);
	        mediaPlayer.prepare();
	        mediaPlayer.start();
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            updateProgressBar();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void addSong(String info, String path) {
		songInfo.add(info);
		mAudioPath.add(path);
		mListView.invalidateViews();
	}

	/**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }   
 
    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
           public void run() {
               long totalDuration = mediaPlayer.getDuration();
               long currentDuration = mediaPlayer.getCurrentPosition();
 
               // Displaying Total Duration time
               songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
               // Displaying time completed playing
               songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
 
               // Updating progress bar
               int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
               //Log.d("Progress", ""+progress);
               songProgressBar.setProgress(progress);
 
               // Running this thread after 100 milliseconds
               mHandler.postDelayed(this, 100);
           }
        };
 
    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
 
    }
 
    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
 
    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
 
        // forward or backward to certain seconds
        mediaPlayer.seekTo(currentPosition);
 
        // update timer progress again
        updateProgressBar();
    }

	@Override
	public void onCompletion(MediaPlayer arg0) {
		if(positionPlaying < mAudioPath.size()-1) {
			++positionPlaying;
			playSong(mAudioPath.get(positionPlaying));
		}
		else {
			positionPlaying = 0;
			playSong(mAudioPath.get(0));
		}
	}
	
    @Override
    public void onDestroy(){
    super.onDestroy();
       mediaPlayer.release();
    }
	
}


