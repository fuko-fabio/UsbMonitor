package com.nps.usbmonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nps.usb.DeviceConfiguration;
import com.nps.usb.UsbGate;
import com.nps.usb.UsbGateException;
import com.nps.usb.olimex.OlimexDeviceConfiguration;
import com.nps.usb.packet.PacketTransfer;
import com.nps.usb.packet.PacketTransferException;

public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	ScrollView console;
	MainDialogs dialogs;
	
	private static final String ACTION_USB_PERMISSION = "com.nps.usbmonitor.USB_PERMISSION";
	private static final String TAG = "MainActivity";

	private UsbDevice device;
	private UsbManager usbManager;
	private DeviceConfiguration deviceConfiguration = new OlimexDeviceConfiguration();
	private UsbGate usbGate;
	private PacketTransfer packetTransfer;
	
	private NpsUsbService mBoundService;
	private boolean mIsBound;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		console = (ScrollView) findViewById(R.id.console);
		dialogs = new MainDialogs(this);
		
		startService();
		doBindService();
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		findUsbDevice();
	}
	
	@Override
	protected void onStop() {
		if (usbBroadcastReceiver != null) {
			try {
				unregisterReceiver(usbBroadcastReceiver);
			} catch (Exception e) {
				Log.d(TAG, "Cannot unregister receiver: " + e.getMessage());
			}
		}	
		doUnbindService();
		super.onStop();
	}

	private void findUsbDevice() {
		
		device = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
		if (device == null) {
			HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			while(deviceIterator.hasNext()){
				UsbDevice dev = deviceIterator.next();
				if (dev.getProductId() == deviceConfiguration.getProductId()
						&& dev.getVendorId() == deviceConfiguration.getVendorId()) {
					device = dev;
					break;
				}
			}
			if (device != null) {
				if (!usbManager.hasPermission(device)) {
					PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
					IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
					filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
					registerReceiver(usbBroadcastReceiver, filter);
					usbManager.requestPermission(device, permissionIntent);
				} else {
					initializeUsbGate();
				}
			} else {
				Log.d(TAG, "Cannot find target device");
				dialogs.getUsbDeviceNotFoundDialog().show();
			}
		}
		else {
			initializeUsbGate();
		}
	}

	private void initializeUsbGate() {
    	Log.d(TAG, "Setup USB device.");
    	try {
			usbGate = new UsbGate(usbManager, device, deviceConfiguration);
			Log.d(TAG, "USB gate created succesfully.");
			openCommunication();
		} catch (Exception e) {
	    	Log.d(TAG, "Cannot initialize USB gate: " + e.getMessage());
	    	dialogs.getCannotCreateUsbGateDialog().show();
	    }
	}

	public void openCommunication() {
		try {
			usbGate.createConnection();
			Log.d(TAG, "USB connection oppened succesfully.");
			return;
			//packetTransfer = new PacketTransfer(usbGate);
			//packetTransfer.setToStreamMode();
			//Log.d(TAG, "USB communication switched to stream mode.");
		} catch (UsbGateException e) {
			Log.d(TAG, "Cannot open USB connection.");
			dialogs.getCannotOpenUsbConnectionDialog();
		//} catch (PacketTransferException e) {
		//	Log.d(TAG, "Cannot switch to stream mode: " + e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	
	private final BroadcastReceiver usbBroadcastReceiver = new BroadcastReceiver() {

		private static final String TAG = "UsbBroadcastReceiver";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (device != null) {
					Log.d(TAG, "USB device disconnected");
					// call your method that cleans up and closes communication
					// with the device
				}
			} else if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				Log.d(TAG, "USB device connected");			
			} else if (ACTION_USB_PERMISSION.equals(action)) {
	            synchronized (this) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							initializeUsbGate();
						}
					} else {
						Log.d(TAG, "permission denied for device " + device);
					}
	            }
	        }

		}
	};
	
	//-------------------------- Service --------------------------------------------

	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        mBoundService = ((NpsUsbService.LocalBinder)service).getService();

	        // Tell the user about this for our demo.
	        Toast.makeText(MainActivity.this, R.string.local_service_connected,
	                Toast.LENGTH_SHORT).show();
	    }
		
		@Override
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        mBoundService = null;
	        Toast.makeText(MainActivity.this, R.string.local_service_disconnected,
	                Toast.LENGTH_SHORT).show();
	    }
	};


	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(this, 
	    		NpsUsbService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}
	
	void startService() {
		ComponentName name = startService(new Intent(this, NpsUsbService.class));
		Log.d(TAG, name.toString());
	}

}
