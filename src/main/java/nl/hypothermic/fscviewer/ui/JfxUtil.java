package nl.hypothermic.fscviewer.ui;

public final class JfxUtil {
	
	// source: https://stackoverflow.com/questions/32094296/javafx-textfield-ip-address
	public static final String makePartialIPRegex() {
        String partialBlock = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))" ;
        String subsequentPartialBlock = "(\\."+partialBlock+")" ;
        String ipAddress = partialBlock+"?"+subsequentPartialBlock+"{0,3}";
        return "^"+ipAddress ;
    }
}
