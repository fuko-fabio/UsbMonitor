/**
 * 
 */
package com.nps.usb.packet;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 *
 */
public class PacketTransferException extends Exception {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -8485014422033067650L;
	
	public PacketTransferException() {
		super("PacketTransferException: An unknown error occurred!");
	}

	public PacketTransferException(String error) {
		super(error);
	}
}
