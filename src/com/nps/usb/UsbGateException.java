package com.nps.usb;

public class UsbGateException extends Exception {

	private static final long serialVersionUID = -5453349699470206549L;

	public UsbGateException() {
		super("UsbGateException: An unknown error occurred!");
	}

	public UsbGateException(String error) {
		super(error);
	}

}
