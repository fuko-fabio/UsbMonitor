package com.nps.usb.olimex;

import com.nps.usb.DeviceConfiguration;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 *
 */
public class OlimexDeviceConfiguration implements DeviceConfiguration {

	private String name = "Olimex sam3";
	private int productId = 24857;
	private int vendorId = 1003;
	private int analog= 4;
	private int digital = 32;
	private int pwm = 4;

	@Override
	public String getName() {
		return name;
	}
	
    @Override
    public int getProductId() {
        	return productId;
    }

    @Override
    public int getVendorId() {
        return vendorId;
    }

    @Override
    public int getAnalog() {
        return analog;
    }

    @Override
    public int getDigital() {
        return digital;
    }

    @Override
    public int getPwm() {
        return pwm;
    }

    //TODO Rmove set functions in final project
	public void setProductId(int productId) {
		this.productId = productId;	
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;	
	}

}
