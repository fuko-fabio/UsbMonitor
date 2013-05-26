package com.nps.usb;

import java.nio.ByteBuffer;


import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 * 
 */
public class UsbGate {

    protected UsbDevice mUsbDevice;
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;
    private boolean connected = false;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbEndpointIn;
    private UsbEndpoint mUsbEndpointOut;
    private UsbDeviceConnection mUsbConnection;
    private UsbManager mUsbManager;
    private UsbRequest mUsbRequest;
    private DeviceConfiguration mDevConfiguration;

    /**
     * @param device USB device object
     * @throws IllegalArgumentException
     * @throws UsbGateException 
     */
    public UsbGate(UsbManager usbManager, UsbDevice device, DeviceConfiguration deviceConfiguration) throws IllegalArgumentException, UsbGateException {
        if (device == null) {
            throw new IllegalArgumentException("Device not found!");
        }
        
        if (deviceConfiguration == null) {
            throw new IllegalArgumentException("Device configuration not found!");
        }
        if (usbManager == null) {
            throw new IllegalArgumentException("USB manager not found!");
        }
        mDevConfiguration = deviceConfiguration;
        mUsbManager = usbManager;
        mUsbDevice = device;
		if (device.getVendorId() == mDevConfiguration.getVendorId()
				&& device.getProductId() == mDevConfiguration.getProductId()) {
			for (int i = 0; i < device.getInterfaceCount(); i++) {
				if (device.getInterface(i).getInterfaceClass() == 
						UsbConstants.USB_CLASS_CDC_DATA) {
					mUsbInterface = device.getInterface(i);
				}
			}	
			for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
				UsbEndpoint endpoint = mUsbInterface.getEndpoint(i);
				if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
						mUsbEndpointIn = endpoint;
					} else {
						mUsbEndpointOut = endpoint;
					}
				}
			}
			if (mUsbEndpointIn == null || mUsbEndpointOut == null) {
				throw new UsbGateException("Endpoints not found!");
			}
			mUsbDevice = device;
        } else
            throw new UsbGateException("Wrong device product ID or vendor ID");
    }
    
    /**
     * Create connection with USB device
     * @throws SecurityException if connection cannot be established
     * @throws UsbGateException 
     */
    public void createConnection() throws UsbGateException {
    	mUsbConnection = mUsbManager.openDevice(mUsbDevice);
        if(mUsbConnection != null)
        	connected = true;
        else
        	throw new UsbGateException("Cannot get connection");
    }
    
    /**
     * @return true if connection with usb device is opened
     */
    public boolean isConnected(){
    	return connected;
    }

    /**
     * Send synchronous data via USB
     * 
     * @param buffer data to send
     * @param length buffer length
     * @return true for success or false for failure
     * @throws IllegalAccessException if USB interface cannot be claimed
     */
    public boolean send(byte[] buffer, int length) throws IllegalAccessException {
    	if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
    		throw new IllegalAccessException("USB interface cannot be claimed");
        return mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, length, TIMEOUT) < 0 ? false : true;
    }

    /**
     * Send synchronous data via USB with specified timeout
     * 
     * @param buffer data to send
     * @param length buffer length
     * @param timeout
     * @return true for success or false for failure
     * @throws IllegalAccessException if USB interface cannot be claimed
     */
    public boolean send(byte[] buffer, int length, int timeout) throws IllegalAccessException {
    	if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
    		throw new IllegalAccessException("USB interface cannot be claimed");
        return mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, length, timeout) < 0 ? false : true;
    }

    /**
     * Read synchronous data from USB
     * 
     * @param buffer received data
     * @param length buffer length
     * @return true for success or false for failure
     * @throws IllegalAccessException if USB interface cannot be claimed 
     */
    public boolean receive(byte[] buffer, int length) throws IllegalAccessException {
    	if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
    		throw new IllegalAccessException("USB interface cannot be claimed");
        return mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, length, TIMEOUT) < 0 ? false : true;
    }

    /**
     * Read synchronous data from USB with specified timeout
     * 
     * @param buffer received data
     * @param length buffer length
     * @param timeout
     * @return true for success or false for failure
     * @throws IllegalAccessException if USB interface cannot be claimed 
     */
    public boolean receive(byte[] buffer, int length, int timeout) throws IllegalAccessException {
    	if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
    		throw new IllegalAccessException("USB interface cannot be claimed");
        return mUsbConnection.bulkTransfer(mUsbEndpointOut, buffer, length, timeout) < 0 ? false : true;
    }

    /**
     * Send asynchronous data via USB
     * 
     * @param buffer data to send
     * @return true if the operation succeeded
     * @throws IllegalAccessException if the USB request was not opened or  if USB interface cannot be claimed
     */
    public boolean sendAsync(byte[] buffer) throws IllegalAccessException {
    	if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
    		throw new IllegalAccessException("USB interface cannot be claimed ");
        if (!mUsbRequest.initialize(mUsbConnection, mUsbEndpointOut))
            throw new IllegalAccessException("USB request cannot be opened");

        boolean sent = mUsbRequest.queue(ByteBuffer.wrap(buffer), buffer.length);

        mUsbRequest.cancel();
        mUsbRequest.close();
        return sent;
    }

    /**
     * Read asynchronous data from USB
     * 
     * @param buffer received data
     * @return true if the operation succeeded
     * @throws IllegalAccessException if the USB request was not opened or  if USB interface cannot be claimed
     */
    public boolean receiveAsync(byte[] buffer) throws IllegalAccessException {
    	if (!mUsbConnection.claimInterface(mUsbInterface, forceClaim))
    		throw new IllegalAccessException("USB interface cannot be claimed ");
        if (!mUsbRequest.initialize(mUsbConnection, mUsbEndpointIn))
            throw new IllegalAccessException("USB request cannot be opened");

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
        boolean recived = mUsbRequest.queue(byteBuffer, buffer.length);
        buffer = byteBuffer.array();

        mUsbRequest.cancel();
        mUsbRequest.close();
        return recived;
    }

    /**
     * Close USB connection and release resources
     * 
     * @return true if the operation succeeded
     * @throws IllegalArgumentException if USB connection or interface not exist
     */
    public boolean close() throws IllegalArgumentException {
        if (mUsbConnection == null)
            throw new IllegalArgumentException("USB connection not foud");
        if (mUsbInterface == null)
            throw new IllegalArgumentException("USB interface not foud");

        if (!mUsbConnection.releaseInterface(mUsbInterface))
            return false;

        mUsbConnection.close();
        return true;
    }
}
