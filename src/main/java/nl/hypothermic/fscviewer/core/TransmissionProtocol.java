package nl.hypothermic.fscviewer.core;

public enum TransmissionProtocol {
	
	// Not all Foscam cameras support UDP
	AUTO, UDP, TCP;
	
	public static TransmissionProtocol match(int value) {
		switch (value) {
		case 0:
			return AUTO;
		case 1:
			return UDP;
		case 2:
			return TCP;
		}
		throw new IllegalArgumentException("Not found");
	}
}
