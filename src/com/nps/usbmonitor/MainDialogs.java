package com.nps.usbmonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

public class MainDialogs {
	
	private MainActivity activity;

	public MainDialogs(MainActivity activity) {
		this.activity = activity;
	}

	public Dialog getUsbDeviceNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_not_found)
        	   .setMessage(R.string.usb_not_found_info)
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   activity.finish();
                   }
               });
        return builder.create();
	}
	
	public Dialog getCannotCreateUsbGateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_not_opened)
        	   .setMessage(R.string.usb_not_opened_info)
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   activity.finish();
                   }
               });
        return builder.create();
	}
	
	public Dialog getCannotOpenUsbConnectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.usb_not_connected)
        	   .setMessage(R.string.usb_not_connected_info)
               .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   activity.openCommunication();
                   }
               })
               .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   activity.finish();
                   }
               });
        return builder.create();
	}
	
}
