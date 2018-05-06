package nl.hypothermic.fscviewer.cv;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nl.hypothermic.fscviewer.core.TransmissionProtocol;
import nl.hypothermic.fscviewer.core.VideoCodec;

/*******************************\
 * > VideoProcessor.java     < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic  *
 *  See LICENSE.md for legal   *
\*******************************/

public class VideoProcessor {
	
	private final String MRL;
	private final ImageView view;
	private FFmpegFrameGrabber grabber;
	private Thread playThread;
	private final TransmissionProtocol prot;
	private final VideoCodec codec;
	private FloatControl volume; // TODO: user volume control (volume.setValue(negatieve float decibel))
	
	/** If VideoView is connected to camera and processing video **/
	private boolean isProcessing;
	
	private String status;
	
	public VideoProcessor(String MRL, TransmissionProtocol prot, VideoCodec codec, ImageView view) {
		this.MRL = MRL;
		this.prot = prot;
		this.codec = codec;
		this.view = view;
		this.grabber = new FFmpegFrameGrabber(MRL);
		
		/**
		 * Source: https://github.com/bytedeco/javacv/blob/master/samples/JavaFxPlayVideoAndAudio.java
		 * @author: Dmitriy Gerashenko <d.a.gerashenko@gmail.com> 
		 */
		playThread = new Thread(() -> {
            try {
                grabber = new FFmpegFrameGrabber(MRL);
                if (prot == TransmissionProtocol.UDP) {
                	grabber.setOption("rtsp_transport", "udp");
                } else if (prot == TransmissionProtocol.TCP) {
                	grabber.setOption("rtsp_transport", "tcp");
                }
                if (codec == VideoCodec.H264) {
                	grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                } else if (codec == VideoCodec.MPEG4) {
                	grabber.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
                }
                grabber.start();
                AudioFormat audioFormat = new AudioFormat(grabber.getSampleRate(), 16, grabber.getAudioChannels(), true, true);

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info);
                soundLine.open(audioFormat);
                volume = (FloatControl) soundLine.getControl(FloatControl.Type.MASTER_GAIN);
                soundLine.start();

                Java2DFrameConverter converter = new Java2DFrameConverter();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                
                isProcessing = true;
                while (!Thread.interrupted()) {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        break;
                    }
                    if (frame.image != null) {
                        Image image = SwingFXUtils.toFXImage(converter.convert(frame), null);
                        Platform.runLater(() -> {
                            view.setImage(image);
                        });
                    } else if (frame.samples != null) {
                        ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                        channelSamplesShortBuffer.rewind();

                        ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);

                        for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                            short val = channelSamplesShortBuffer.get(i);
                            outBuffer.putShort(val);
                        }

                        /**
                         * We need this because soundLine.write ignores
                         * interruptions during writing.
                         */
                        try {
                            executor.submit(() -> {
                                soundLine.write(outBuffer.array(), 0, outBuffer.capacity());
                                outBuffer.clear();
                            }).get();
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                isProcessing = false;
                executor.shutdownNow();
                executor.awaitTermination(10, TimeUnit.SECONDS);
                soundLine.stop();
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        });
	    playThread.start();
	}
	
	public void stop() {
		playThread.interrupt();
		try {
			grabber.close();
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			;
		}
	}
	
	public void setHeight(int height) {
		grabber.setImageHeight(height);
	}
	
	public void setWidth(int width) {
		grabber.setImageWidth(width);
	}
	
	public boolean isProcessing() {
		return isProcessing;
	}
	
	public String getStatus() {
		return this.status;
	}
}
