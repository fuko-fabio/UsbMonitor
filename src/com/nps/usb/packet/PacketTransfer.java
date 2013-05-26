package com.nps.usb.packet;

import java.nio.ByteBuffer;

import com.nps.usb.UsbGate;

public class PacketTransfer {

	private static final int DAFAULT_BUFFER_SIZE = 8192;
	private CommunicationMode communicationMode = CommunicationMode.COMMAND;
	private TransferParameters transferParameters = new TransferParameters();
	private UsbGate usbGate;

	public PacketTransfer(UsbGate usbGate) {
		this.usbGate = usbGate;
	}

	/**
	 * Switch communication to command mode
	 * 
	 * @throws PacketTransferException
	 */
	public void setToCommandMode() throws PacketTransferException {
		PacketData packetData = new PacketData();
		packetData.setCommand(PacketCommand.RESET_PACKETS);
		packetData.setSubcommand((byte) 76);
		packetData.setAck((byte) 87);
		
		try {
			exchangeData((byte) 0, preparePacket(packetData));
			communicationMode = CommunicationMode.COMMAND;
		} catch (Exception e) {
			throw new PacketTransferException("Cannot switch to command mode");
		}
	}
	
	/**
	 * Switch communication to stream mode
	 * 
	 * @throws PacketTransferException
	 */
	public void setToStreamMode() throws PacketTransferException {
		
		byte[] readBuffer = new byte[DAFAULT_BUFFER_SIZE];
		PacketData packetData = new PacketData();
		packetData.setCommand(PacketCommand.SWITCH_TO_STREAM);
		packetData.setSubcommand((byte) 2);
		packetData.setAck((byte) 0);

		try {
			if (this.usbGate.send(preparePacket(packetData), this.transferParameters.getStreamOutSize())) {

				if (this.usbGate.receive(readBuffer, this.transferParameters.getStreamInSize())) {					
					PacketData packedData = readPacket((byte) 0, readBuffer);
					if (packedData.getCommand() != PacketCommand.SWITCH_TO_STREAM) {
						throw new PacketTransferException(
								"Received unknown command. Buffer: " + readBuffer);
					} else {
						this.communicationMode = CommunicationMode.STREAM;
					}
				} else {
					throw new PacketTransferException("Cannot read data from device");
				}
			} else {
				throw new PacketTransferException("Cannot send data to device");
			}
		} catch (Exception e) {
			throw new PacketTransferException("Cannot switch to stream mode: " + e.getMessage());
		}
	}

	/**
	 * Exchange data between devices
	 * 
	 * @param stream stream
	 * @param sendBuffer buffer to send
	 * @return received buffer data
	 * @throws PacketTransferException
	 */
	public PacketData exchangeData(byte stream, byte[] sendBuffer) throws PacketTransferException {

		// this.nanoTime = System.nanoTime();
		byte[] readBuffer = new byte[DAFAULT_BUFFER_SIZE];
		try {
			if (usbGate.send(sendBuffer, transferParameters.getStreamOutSize())) {
				if (usbGate.receive(readBuffer, transferParameters.getStreamInSize())) {
					return readPacket(stream, readBuffer);
				} else {
					throw new PacketTransferException("Cannot read data from device");
				}
			} else {
				throw new PacketTransferException("Cannot send data to device");
			}
		} catch (IllegalAccessException e) {
			throw new PacketTransferException(
					"Cannot exchange data between devices: " + e.getMessage());
		}
	}

	/**
	 * Prepare packed data as byte array from given object
	 * @param packetData packet specific data
	 * @return byte array prepared packet buffer
	 */
	public byte[] preparePacket(PacketData packetData) {
		ByteBuffer packetOutBuffer = ByteBuffer.wrap(new byte[DAFAULT_BUFFER_SIZE]);
		packetOutBuffer.putInt(packetData.getCommand().value());
		packetOutBuffer.putShort(packetData.getLenght());
		packetOutBuffer.put(packetData.getSubcommand());
		packetOutBuffer.put(packetData.getAck());
		packetOutBuffer.putInt(transferParameters.getDefaultStreamOutSize());
		packetOutBuffer.putInt(transferParameters.getDefaultStreamOutSize());
		
		return packetOutBuffer.array();
	}

	/**
	 *  Read data from packet
	 *  
	 * @param stream stream
	 * @param readBuffer buffer to read
	 * @return decoded packet data as object
	 */
	public PacketData readPacket(byte stream, byte[] readBuffer) {

		ByteBuffer packetInBuffer = ByteBuffer.wrap(readBuffer);
		PacketData packetData = new PacketData();
		packetData.setReadBuffer(readBuffer);
		packetData.setCommand(PacketCommand.getValue(packetInBuffer.getInt()));
		packetData.setLenght(packetInBuffer.getShort());
		packetData.setSubcommand(packetInBuffer.get());
		packetData.setAck(packetInBuffer.get());

		if (stream == 1) {

		} else if (stream == 2) {

		} else {
			transferParameters.setStreamOutSize(packetInBuffer.getInt());
			transferParameters.setStreamInSize(packetInBuffer.getInt());
		}

		return packetData;
	}
	
	/**
	 * Current communication mode
	 * 
	 * @return communication mode
	 */
	public CommunicationMode getCommunicationMode() {
		return communicationMode;
	}
}
