package com.nps.usb;
/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 *
 */
public interface DeviceConfiguration {
	
	/**
	 * @return device name
	 */
	String getName();
	
	/**
	 * @return product ID
	 */
	int getProductId();
	
	/**
	 * @return product vendor ID
	 */
	int getVendorId();
	
	/**
	 * @return number of analog inputs/outputs
	 */
	int getAnalog();
	
	/**
	 * @return number of digital inputs/outputs
	 */
	int getDigital();
	
	/**
	 * @return number of pwm outputs
	 */
	int getPwm();
	
}
