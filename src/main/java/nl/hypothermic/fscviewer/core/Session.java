package nl.hypothermic.fscviewer.core;

import javafx.scene.image.ImageView;
import nl.hypothermic.foscamlib.Foscam;
import nl.hypothermic.foscamlib.exception.ConnectException;
import nl.hypothermic.fscviewer.cv.VideoManager;

/*******************************\
 * > Session.java            < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

public class Session {

    // The idea is to keep the credentials within the Session object once they're set
    private final String host, user, pwd;
    private final int port;
    private final TransmissionProtocol prot;
    private final VideoCodec codec;
    public static Foscam f;
    private ImageView videoView;

    public final View viewcl = new View();
    public final Controls ctrlcl = new Controls();

    public Session(final String host, final int port, final String user, final String pwd, final ImageView videoView, final TransmissionProtocol prot, final VideoCodec codec) {
    	this.host = host;
    	this.port = port;
    	this.user = user;
    	this.pwd = pwd;
    	this.videoView = videoView;
    	this.prot = prot;
    	this.codec = codec;
    }

    public class View {

    	public View() {
    		;
    	}

    	private VideoManager vp;

    	public void connect() {
    		vp = new VideoManager("rtsp://" + user + ":" + pwd + "@" + host + ":" + port + "/videoMain", prot, codec, videoView);
    	}
    	
    	public void connect(String relativeUrl) {
    		vp = new VideoManager("rtsp://" + user + ":" + pwd + "@" + host + ":" + port + relativeUrl, prot, codec, videoView);
    	}

    	public void disconnect() {
    		if (vp != null) {
    			vp.stop();
    		}
		}
    }

    public class Controls {

    	public Controls() {
    		;
    	}

    	public void connect() throws ConnectException {
    		f = new Foscam(host, port, user, pwd);
    	}

    	public String getHost() {
    		return (host == null) ? "<error>" : host;
    	}
    }
}
