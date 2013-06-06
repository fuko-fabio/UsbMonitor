package com.nps.usbmonitor;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.nps.usb.DeviceConfiguration;
import com.nps.usb.UsbGate;
import com.nps.usb.UsbGateException;
import com.nps.usb.packet.PacketTransfer;
import com.nps.usb.packet.PacketTransferException;

public class NpsUsbService extends Service {

	private NotificationManager notificationManager;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.local_service_started;
	private static final String TAG = "NpsUsbService";
	
	private static boolean isRunning = false;

	private UsbManager usbManager;
	private UsbDevice device;
	private DeviceConfiguration deviceConfiguration;
	private UsbGate usbGate;
	private PacketTransfer packetTransfer;
	
	// Keeps track of all current registered clients.
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_INT_VALUE = 3;
	static final int MSG_SET_STRING_VALUE = 4;
	
	static final int MSG_ERROR_CREATE_USB_GATE = 5;
	static final int MSG_ERROR_OPEN_USB_GATE = 6;
	static final int MSG_ERROR_SWITCH_TO_STREAM = 7;
	
	// Target we publish for clients to send messages to IncomingHandler.
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public NpsUsbService getService() {
			return NpsUsbService.this;
		}

		public IBinder getMessenger(){
		    return mMessenger.getBinder();
		}
	}

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		// Display a notification about us starting. We put an icon in the status bar.
		showNotification();
		isRunning = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("LocalService", "Received start id " + startId + ": " + intent);
		device = intent.getParcelableExtra("device");
		deviceConfiguration = intent.getParcelableExtra("deviceConfiguration");
		try {
			usbGate = new UsbGate(usbManager, device, deviceConfiguration);
			Log.d(TAG, "USB gate created succesfully.");
			openCommunication();
		} catch (Exception e) {
			Log.d(TAG, "Cannot initialize USB gate: " + e.getMessage());
			sendMessageToUI(MSG_ERROR_CREATE_USB_GATE, e.getMessage());
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		notificationManager.cancel(NOTIFICATION);
		usbGate.close();

		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT)
				.show();
		isRunning = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Show a notification while this service is running.
	 */
	@SuppressWarnings("deprecation")
    private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.local_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this,
				getText(R.string.local_service_label), text, contentIntent);

		// Send the notification.
		notificationManager.notify(NOTIFICATION, notification);
	}

	public void openCommunication() {
		try {
			usbGate.createConnection();
			Log.d(TAG, "USB connection oppened succesfully.");
			packetTransfer = new PacketTransfer(usbGate);
			packetTransfer.setToStreamMode();
			Log.d(TAG, "USB communication switched to stream mode.");
		} catch (UsbGateException e) {
			Log.d(TAG, "Cannot open USB connection.");
			sendMessageToUI(MSG_ERROR_OPEN_USB_GATE, e.getMessage());
		} catch (PacketTransferException e) {
			Log.d(TAG, "Cannot switch to stream mode: " + e.getMessage());
			sendMessageToUI(MSG_ERROR_SWITCH_TO_STREAM, e.getMessage());
			usbGate.close();
		}
	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler { // Handler of incoming messages from
											// clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_INT_VALUE:

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void sendMessageToUI(int what, String msg) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				// Send data as a String
				Bundle b = new Bundle();
				b.putString("msg", msg);
				Message message = Message.obtain(null, MSG_SET_STRING_VALUE);
				message.setData(b);
				mClients.get(i).send(message);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

	private void sendMessageToUI(int what, int msg) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				// Send data as an Integer
				mClients.get(i).send(
						Message.obtain(null, MSG_SET_INT_VALUE, msg, 0));
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

	public static boolean isRunning() {
		return isRunning;
	}
}
