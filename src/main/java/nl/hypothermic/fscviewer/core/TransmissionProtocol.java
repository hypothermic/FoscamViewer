package nl.hypothermic.fscviewer.core;

/*******************************\
 * > TransmissionProtocol.java *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public enum TransmissionProtocol {

	/**
	 * Automatically decide whether to use UDP or TCP
	 */
    AUTO,
    /**
     * Force communication over UDP
     */
    UDP,
    
    /**
     * Force communication over TCP
     */
    TCP;

	/**
	 * Get TransmissionProtocol instance from integer
	 * 
	 * @param value 0, 1, 2
	 * @return TransmissionProtocol instance
	 * @throws IllegalArgumentException
	 */
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
