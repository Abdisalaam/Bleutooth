package com.example.bluetoothtestoudversie;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
	protected static final int DISCOVERY_REQUEST = 1;
	/** Called when the activity is first created.*/
	private BluetoothAdapter btAdapter;
	public TextView statusUpdate;
	public Button connect;
	public Button disconnect;
	public ImageView logo;
	public String toastText ="";
	public String statusText = "";
	private BluetoothDevice remoteDevice;
	
	
   // Create a BroadcastReceiver to receive state changes
	BroadcastReceiver bluetoothState = new BroadcastReceiver(){
		@Override
	   public void onReceive(Context context, Intent intent){
			//String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
			String stateExtra = BluetoothAdapter.EXTRA_STATE;
			int state = intent.getIntExtra(stateExtra, -1);
		   //int previousState = intent.getIntExtra(prevStateExtra, -1);
			//String toastText = "";
			switch(state){
			case(BluetoothAdapter.STATE_TURNING_ON):
			{
				toastText = "Bluetooth turning on";
				Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
			break;
			}
			case(BluetoothAdapter.STATE_ON):
			{
				toastText = "Bluetooth on";
				Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
				setupUI();
			break;
			}
			case(BluetoothAdapter.STATE_TURNING_OFF):
			{
				toastText = "Bluetooth turning off";
				Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
			break;
			}
			case(BluetoothAdapter.STATE_OFF):
			{
				toastText = "Bluetooth off";
				Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
				setupUI();
			break;
			}
			}
	  
		}
	   
   };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupUI();
    }


    private void setupUI() {
	 // get references
    	final TextView statusUpdate = (TextView) findViewById(R.id.result);
    	final Button connect = (Button) findViewById(R.id.connectBtn);
    	final Button disconnect = (Button) findViewById(R.id.disconnectBtn);
    	final ImageView logo = (ImageView) findViewById(R.id.logo);
    	
    // set display view
    	
    	disconnect.setVisibility(View.GONE);
    	logo.setVisibility(View.GONE);
    	btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter.isEnabled()){
		   String address = btAdapter.getAddress();
		   String name = btAdapter.getName();
		   statusText = name + " : "+ address;
		   statusUpdate.setText(statusText);
		   disconnect.setVisibility(View.VISIBLE);
	    	logo.setVisibility(View.VISIBLE);
	    	connect.setVisibility(View.GONE);
		}
		else{
			connect.setVisibility(View.VISIBLE);
			statusUpdate.setText("Bluetooth is niet aan");
		}
    	
    	connect.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
				//String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
				//IntentFilter filter = new IntentFilter(actionStateChanged);
				//registerReceiver(bluetoothState, filter);
				//startActivityForResult(new Intent(actionRequestEnable), 0);
				
				// register for discovery event
				String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
				String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
				IntentFilter filter = new IntentFilter(scanModeChanged);
				registerReceiver(bluetoothState, filter);
				startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST);
				
				
			}
		});// end connect onClickListener
    	
    	disconnect.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
			    btAdapter.disable();
			    disconnect.setVisibility(View.GONE);
			    logo.setVisibility(View.GONE);
			    connect.setVisibility(View.VISIBLE);
			    statusUpdate.setText("Bluetooth is off");
				
			}
		});// end disconnect oNcLICKLISTENER
		
	}// End setupUI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(requestCode == DISCOVERY_REQUEST){
    		Toast.makeText(Main.this, "Discovery in progress", Toast.LENGTH_SHORT).show();
    		setupUI();
    		findDevices();
    	}
    }


	private void findDevices() {
		String lastUsedRemoteDevice = getLastUsedRemoteBTDevice();
		if(lastUsedRemoteDevice != null){
			toastText = "Checking for known paired devices, namely:"+lastUsedRemoteDevice;
			Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
			
			// see if this device is in a list of currently visible (?), paired devices
			Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
			for(BluetoothDevice paireddevice : pairedDevices){
				if(paireddevice.getAddress().equals(lastUsedRemoteDevice)){
					toastText = "Found device:" + paireddevice.getName() + "@" + lastUsedRemoteDevice;
					Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
					remoteDevice = paireddevice;
				}
			}
			
		}// end if
		if(remoteDevice == null){
			toastText = "Starting discovery for remote devices...";
			Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
			
			// start discovery
			if(btAdapter.startDiscovery()){
				toastText = "Discovery theread started...Scannig for devices";
				Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
				registerReceiver(discoveryResult,  new IntentFilter(BluetoothDevice.ACTION_FOUND));
			}
		}
		
	}// end findDevices
    
	// Create a BroadcastReceiver to receive device discovery
	
	BroadcastReceiver discoveryResult = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
			//BluetoothDevice remoteDevice;
			toastText ="Discoverd: " + remoteDeviceName;
			Toast.makeText(Main.this, toastText, Toast.LENGTH_SHORT).show();
			statusUpdate.setText(statusText);
		}
		
	};
	

	private String getLastUsedRemoteBTDevice() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String result = prefs.getString("LAST_REMOTE_DEVICE_ADDRESS", null);
		return result;
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
