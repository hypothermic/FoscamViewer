package nl.hypothermic.fscviewer.core;

import java.io.PrintStream;

/*******************************\
 * > XLogger.java            < *
 * FoscamViewer by hypothermic *
 * www.github.com/hypothermic/ *
 *  See LICENSE.md for legal   *
\*******************************/

// Simple logging utility
public class XLogger {
	
	private PrintStream out;
	
	public XLogger(PrintStream out) {
		this.out = out;
	}
	
	public void info(String msg) {
		out.println("[INFO] ");
	}
	
	public void warn(String msg) {
		out.println("[WARNING] ");
	}
	
	public void severe(String msg) {
		out.println("[SEVERE] ");
	}
	
	// Only HTML programmers will understand this name
	public void hr() {
		out.println("====================");
	}
}
