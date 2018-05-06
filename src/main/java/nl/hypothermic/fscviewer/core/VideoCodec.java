package nl.hypothermic.fscviewer.core;

public enum VideoCodec {

	AUTO, H264, MPEG4;
	
	public static VideoCodec match(int value) {
		switch (value) {
		case 0:
			return AUTO;
		case 1:
			return H264;
		case 2:
			return MPEG4;
		}
		throw new IllegalArgumentException("Not found");
	}
}
