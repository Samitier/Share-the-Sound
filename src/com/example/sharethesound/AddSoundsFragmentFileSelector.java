package com.example.sharethesound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AddSoundsFragmentFileSelector extends Fragment{
	
	
	private enum DISPLAYMODE{ ABSOLUTE, RELATIVE; }
	private final DISPLAYMODE displayMode = DISPLAYMODE.ABSOLUTE;
	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = new File("/");
	private ListView fileList;
	
	public AddSoundsFragmentFileSelector() {	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.add_sounds, container, false);
		
		fileList = (ListView) rootView.findViewById(R.id.fileList);
		fileList.setClickable(true);
	    fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
	    	
	    	@Override
	    	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	    		int selectionRowID = position;
	    		String selectedFileString = directoryEntries.get(selectionRowID);
	    		if (selectedFileString.equals(".")) {
	    			// Refresh
	    			browseTo(currentDirectory);
	    		} else if(selectedFileString.equals("..")){
	    			upOneLevel();
	    		} else {
	    			File clickedFile = null;
	    			switch(displayMode){
	    				case RELATIVE:
	    					clickedFile = new File(currentDirectory.getAbsolutePath() 
	    												+ directoryEntries.get(selectionRowID));
	    					break;
	    				case ABSOLUTE:
	    					clickedFile = new File(directoryEntries.get(selectionRowID));
	    					break;
	    			}
	    			if(clickedFile != null)
	    				browseTo(clickedFile);
	    		}
	    	}
	    });
	    browseToRoot();
	    return rootView;
	}

	/**
	 * This function browses to the 
	 * root-directory of the file-system.
	 */
	private void browseToRoot() {
		browseTo(new File("/"));
    }
	
	/**
	 * This function browses up one level 
	 * according to the field: currentDirectory
	 */
	private void upOneLevel(){
		if(this.currentDirectory.getParent() != null)
			this.browseTo(this.currentDirectory.getParentFile());
	}
	
	private void browseTo(final File aDirectory){
		if (aDirectory.isDirectory()){
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
		}else{			
			// Hem clicat un arxiu
		}
	}

	private void fill(File[] files) {
		this.directoryEntries.clear();
		
		// Add the "." and the ".." == 'Up one level'
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.directoryEntries.add(".");
		
		if(this.currentDirectory.getParent() != null)
			this.directoryEntries.add("..");
		if(files != null) {
			switch(this.displayMode){
				case ABSOLUTE:
					for (File file : files){
						this.directoryEntries.add(file.getPath());
					}
					break;
				case RELATIVE: // On relative Mode, we have to add the current-path to the beginning
					int currentPathStringLenght = this.currentDirectory.getAbsolutePath().length();
					for (File file : files){
						this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLenght));
					}
					break;
			}
		}
		
		ArrayAdapter<String> directoryList = new ArrayAdapter<String>(getActivity().getApplicationContext(),
					R.layout.add_sounds_row, this.directoryEntries);
		fileList.setAdapter(directoryList);
	}
	
}
