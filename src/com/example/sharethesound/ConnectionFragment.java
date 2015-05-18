package com.example.sharethesound;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/* This fragment will only work if there is a bluetooth Adapter in the device. If not, there will be null pointer exception*/

public class ConnectionFragment extends Fragment{
	
	protected static final int REQUEST_ENABLE_BT = 1;
	BluetoothAdapter bluetoothAdapter;
	ToggleButton bBluetoothActivation;
	
	ListView bondedDevicesView;
	ListView newDevicesView;
	
	ArrayList<String> bondedDevices;
	ArrayList<String> bondedDevicesMAC;
	
	ArrayList<String> newDevices;
	ArrayList<String> newDevicesMAC;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            //if i don't want to repeat bonded devices here: if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
	            // Add the name and address to an array adapter to show in a ListView
	            newDevices.add(device.getName());
	            newDevicesMAC.add(device.getAddress());
				newDevicesView.invalidateViews();
	        }
	        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(newDevices.size()==0) newDevices.add("No near devices found");
				newDevicesView.invalidateViews();
            }
	        else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
				switch (state) {
					case BluetoothAdapter.STATE_OFF:
						bBluetoothActivation.setChecked(false);
						bondedDevices.clear();
						bondedDevicesMAC.clear();
						newDevices.clear();
						newDevicesMAC.clear();
						bondedDevicesView.invalidateViews();
						newDevicesView.invalidateViews();
					break;

					case BluetoothAdapter.STATE_ON:
						bBluetoothActivation.setChecked(true);
						fillListviews();
					break;
				}
	        }
	    }
	};
	
	public ConnectionFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.connection, container, false);
		bBluetoothActivation = (ToggleButton) rootView.findViewById(R.id.bBluetoothActivation);
		bluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
		
	    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	    getActivity().registerReceiver(mReceiver, filter);
	    IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    getActivity().registerReceiver(mReceiver, filter2);
	    IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	    getActivity().registerReceiver(mReceiver, filter3);
		
	    
		bondedDevicesView = (ListView) rootView.findViewById(R.id.bondedDevices);
		newDevicesView = (ListView) rootView.findViewById(R.id.nearDevices);
		
		newDevices = new ArrayList<String>();
		bondedDevices = new ArrayList <String>();
		newDevicesMAC = new ArrayList<String>();
		bondedDevicesMAC = new ArrayList <String>();
		
		ArrayAdapter<String> bondedAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
				R.layout.add_sounds_row, bondedDevices);
		bondedDevicesView.setAdapter(bondedAdapter);
	    ArrayAdapter<String> nearAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
				R.layout.add_sounds_row, newDevices);
	    newDevicesView.setAdapter(nearAdapter);
	    
	    bondedDevicesView.setOnItemClickListener(onDeviceClickListener);
	    newDevicesView.setOnItemClickListener(onDeviceClickListener);

		if(bluetoothAdapter.isEnabled()) {
			bBluetoothActivation.setChecked(true);
			fillListviews();
		}

		bBluetoothActivation.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isOn) {
				if(isOn) {
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				    enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
				else  {
					bluetoothAdapter.disable();
				}
				bondedDevicesView.invalidateViews();
				newDevicesView.invalidateViews();
			}
			
		});
			
		return rootView;
	}
	
	
	/*Fills the bonded devices list and begin finding near devices*/
	private void fillListviews(){
		
		Set<BluetoothDevice> pdevices = bluetoothAdapter.getBondedDevices();
		
		for(BluetoothDevice bd: pdevices) {
			bondedDevices.add(bd.getName());
			bondedDevicesMAC.add(bd.getAddress());
		}
		bondedDevicesView.invalidateViews();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
		bluetoothAdapter.startDiscovery();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/* Results from the start bluetooth activity for result. If user presses confirms the bluetooth, then the lists are 
		 * filled. 
		 * If not, the button remains off.
		 */
		if(requestCode == REQUEST_ENABLE_BT) {
			if(resultCode == getActivity().RESULT_CANCELED) {
				bBluetoothActivation.setChecked(false);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	    bluetoothAdapter.cancelDiscovery();
	    getActivity().unregisterReceiver(mReceiver);
	}
	
	private OnItemClickListener onDeviceClickListener = new OnItemClickListener() {
	@Override
	public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
	        // Cancel discovery because it's costly and we're about to connect
	    bluetoothAdapter.cancelDiscovery();
	    /*
	    // Create the result Intent and include the MAC address
	    Intent intent = new Intent();
	    intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
	
	    // Set result and finish this Activity
	        setResult(Activity.RESULT_OK, intent);
	        finish();*/
	    }
	};
	
}
