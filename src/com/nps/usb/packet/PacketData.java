package com.nps.usb.packet;

public class PacketData {
	
	private short lenght;
	private PacketCommand command;
	private byte subcommand;
	private byte ack;
	byte[] readBuffer;
	byte[] sendBuffer;
	
	public PacketData() {
		this.lenght = 200;
		this.command = PacketCommand.UNKNOWN;
		this.subcommand = 0;
		this.ack = 0;
	}
	
	public PacketData(short lenght, PacketCommand command, byte subcommand,
			byte ack) {
		super();
		this.lenght = lenght;
		this.command = command;
		this.subcommand = subcommand;
		this.ack = ack;
	}
	
	public short getLenght() {
		return lenght;
	}
	
	public void setLenght(short lenght) {
		this.lenght = lenght;
	}
	
	public PacketCommand getCommand() {
		return command;
	}
	
	public void setCommand(PacketCommand command) {
		this.command = command;
	}
	
	public byte getSubcommand() {
		return subcommand;
	}
	
	public void setSubcommand(byte subcommand) {
		this.subcommand = subcommand;
	}
	
	public byte getAck() {
		return ack;
	}
	
	public void setAck(byte ack) {
		this.ack = ack;
	}
	
	public byte[] getReadBuffer() {
		return readBuffer;
	}

	public void setReadBuffer(byte[] readBuffer) {
		this.readBuffer = readBuffer;
	}

	public byte[] getSendBuffer() {
		return sendBuffer;
	}

	public void setSendBuffer(byte[] sendBuffer) {
		this.sendBuffer = sendBuffer;
	}


}
