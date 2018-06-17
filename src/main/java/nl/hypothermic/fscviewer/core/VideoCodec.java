package nl.hypothermic.fscviewer.core;

/*******************************\
 * > VideoCodec.java         < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public enum VideoCodec {

	/**
	 * Automatically decide which video codec to use
	 * NOTE: might be unstable if stream is MPEG4 (see mail list vol. 3, issue 1)
	 */
    AUTO,
    
    /**
     * Force parsing of stream as H264
     */
    H264,
    
    /**
     * Force parsing of stream as MPEG4
     */
    MPEG4;

	/**
	 * Get VideoCodec instance from integer
	 * 
	 * @param value 0, 1, 2
	 * @return VideoCodec instance
	 * @throws IllegalArgumentException
	 */
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
