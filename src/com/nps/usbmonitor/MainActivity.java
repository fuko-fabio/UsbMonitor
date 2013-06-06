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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best to
     * switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    MainDialogs dialogs;

    private static final String ACTION_USB_PERMISSION = "com.nps.usbmonitor.USB_PERMISSION";
    private static final String TAG = "MainActivity";

    private UsbDevice device;
    private UsbManager usbManager;
    private DeviceConfiguration deviceConfiguration = new OlimexDeviceConfiguration();

    private NpsUsbService mBoundService;
    private boolean mIsBound;
    private Intent serviceIntent;
    private final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger messengerService;
  
    //-------------------------------------------- UI ---------------------------------------------
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        dialogs = new MainDialogs(this);
    }
    
    @Override
    protected void onStart() {
        checkIfServiceIsRunning();
        super.onStart();
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
    
    @Override
    protected void onDestroy() {
        if (NpsUsbService.isRunning() && serviceIntent != null) {
            stopService(serviceIntent);
        }
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
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
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    //-------------------------------------------- USB --------------------------------------------

    private void findUsbDevice() {

        device = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null) {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice dev = deviceIterator.next();
                if (dev.getProductId() == deviceConfiguration.getProductId()
                        && dev.getVendorId() == deviceConfiguration.getVendorId()) {
                    device = dev;
                    break;
                }
            }
            if (device != null) {
                if (!usbManager.hasPermission(device)) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                    registerReceiver(usbBroadcastReceiver, filter);
                    usbManager.requestPermission(device, permissionIntent);
                } else {
                    initializeUsbService();
                }
            } else {
                Log.d(TAG, "Cannot find target device");
                dialogs.getUsbDeviceNotFoundDialog().show();
            }
        } else {
            initializeUsbService();
        }
    }

    private void initializeUsbService() {
        Log.d(TAG, "Setup USB service.");
        serviceIntent = new Intent(this, NpsUsbService.class);
        serviceIntent.putExtra("device", device);
        serviceIntent.putExtra("deviceConfiguration", deviceConfiguration);
        Log.d(TAG, "Starting nps usb service...");
        startService(serviceIntent);
        doBindService();
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
                    stopService(serviceIntent);
                    dialogs.getDeviceDisconnectedDialog();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d(TAG, "USB device connected");
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            initializeUsbService();
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }

        }
    };

    //------------------------------------------ Service ------------------------------------------
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((NpsUsbService.LocalBinder) service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(MainActivity.this, R.string.local_service_connected, Toast.LENGTH_SHORT)
                    .show();
            messengerService = new Messenger(((NpsUsbService.LocalBinder) service).getMessenger());
            try {
                Message msg = Message.obtain(null, NpsUsbService.MSG_REGISTER_CLIENT);
                msg.replyTo = messengerService;
                messengerService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do
                // anything with it
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            messengerService = null;
            mBoundService = null;
            Toast.makeText(MainActivity.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        Log.d(TAG, "Binding nps usb service...");
        bindService(new Intent(this, NpsUsbService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        Log.d(TAG, "Unbinding nps usb service...");
        if (mIsBound) {
            // If we have received the service, and hence registered with it,
            // then now is the time to unregister.
            if (messengerService != null) {
                try {
                    Message msg = Message.obtain(null, NpsUsbService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = messengerService;
                    messengerService.send(msg);
                } catch (RemoteException e) {
                    Log.d(TAG, "Cannot unregister client from service: " + e.getMessage());
                    // There is nothing special we need to do if the service has
                    // crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private void checkIfServiceIsRunning() {
        // If the service is running when the activity starts, we want to
        // automatically bind to it.
        if (NpsUsbService.isRunning()) {
            doBindService();
        } else {
            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            findUsbDevice();
        }
    }
    
    //------------------------------------- Service messenger--------------------------------------
    
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String message;
            Log.d(TAG, "Message from service: " + msg.what);
            switch (msg.what) {
                case NpsUsbService.MSG_SET_INT_VALUE:
                    Log.d(TAG, "Int Message: " + msg.arg1);
                    break;
                case NpsUsbService.MSG_ERROR_CREATE_USB_GATE:
                    message = msg.getData().getString("msg");
                    dialogs.getCannotCreateUsbGateDialog(message).show();
                    break;
                case NpsUsbService.MSG_ERROR_OPEN_USB_GATE:
                    message = msg.getData().getString("msg");
                    dialogs.getCannotOpenUsbConnectionDialog(message).show();
                    break;
                case NpsUsbService.MSG_ERROR_SWITCH_TO_STREAM:
                    message = msg.getData().getString("msg");
                    dialogs.getCannotSwitchToStreamDialog(message).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (messengerService != null) {
                try {
                    Message msg = Message.obtain(null, NpsUsbService.MSG_SET_INT_VALUE,
                            intvaluetosend, 0);
                    msg.replyTo = messenger;
                    messengerService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
}
