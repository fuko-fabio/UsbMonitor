package com.nps.usb.packet;

public enum PacketCommand {
	
	UNKNOWN(0),
	SEND_STREAM_PACKET(1),
	RESET_PACKETS(2),
	GET_STREAM_PARAMETERS(3),
	SWITCH_TO_STREAM(4),
	SET_STREAM_PARAMETERS(5);
	
	private int value = 0;

	PacketCommand(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

	public static PacketCommand getValue(int id) {
		PacketCommand[] PacketCommands = PacketCommand.values();
		for (int i = 0; i < PacketCommands.length; i++) {
			if (PacketCommands[i].value() == id)
				return PacketCommands[i];
		}
		return PacketCommand.UNKNOWN;
	}

}
