package com.nps.usb.olimex;

import android.os.Parcel;
import android.os.Parcelable;

import com.nps.usb.DeviceConfiguration;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 *
 */
public class OlimexDeviceConfiguration implements DeviceConfiguration {

	private String name;
	private int productId;
	private int vendorId;
	private int analog;
	private int digital;
	private int pwm;
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeInt(productId);
		dest.writeInt(vendorId);
		dest.writeInt(analog);
		dest.writeInt(digital);
		dest.writeInt(pwm);
	}

    public static final Parcelable.Creator<OlimexDeviceConfiguration> CREATOR
		    = new Parcelable.Creator<OlimexDeviceConfiguration>() {
		public OlimexDeviceConfiguration createFromParcel(Parcel in) {
		    return new OlimexDeviceConfiguration(in);
		}
		
		public OlimexDeviceConfiguration[] newArray(int size) {
		    return new OlimexDeviceConfiguration[size];
		}
	};
	
    private OlimexDeviceConfiguration(Parcel in) {
    	name = in.readString();
    	productId = in.readInt();
    	vendorId = in.readInt();
    	analog = in.readInt();
    	digital = in.readInt();
    	pwm = in.readInt();
    }

    public OlimexDeviceConfiguration() {
    	name = "Olimex sam3";
    	productId = 24857;
    	vendorId = 1003;
    	analog= 4;
    	digital = 32;
    	pwm = 4;
    }

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
