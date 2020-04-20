package demo.war;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SHelloUtils {
	private static final String SESSION_PARAM_FEELING = "feeling";
	private static final String LIBERTY_LOGO_IMG_TAG = "<img src=\"images/OL_logomark_32.png\">";
	private static final String WAS_LOGO_IMG_TAG = "<img src=\"images/WAS_32.png\">";

	public static String greeting(HttpSession session) {
		String lastFeeling = getFeelingFromSession(session);
		if (session.isNew()) {
			return "This is the first time we've talked.";
		} else if (null == lastFeeling) {
			return "You didn't tell me how you were feeling last itme.";
		} else {
			return "Last time you told me you were feeling '" + lastFeeling + "'.";
		}
	}

	private static String getFeelingFromSession(HttpSession session) {
		Object fieldParam = session.getAttribute(SESSION_PARAM_FEELING);
		return fieldParam == null ? null : fieldParam.toString();
	}

	public static String getRequestHost(HttpServletRequest request) {
		return request.getServerName() + ":" + request.getServerPort();
	}

	public static String getLocalHost(HttpServletRequest request) {
		return request.getLocalName() + ":" + request.getLocalPort();
	}

	private static String tryGetLibertyServerName() {
		String serverName = null;

		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName serverInfo = new ObjectName("WebSphere:feature=kernel,name=ServerInfo");
			if (mbs.isRegistered(serverInfo)) {
				serverName = mbs.getAttribute(serverInfo, "Name").toString();
			}
		} catch (Exception e) {
			/* swallow it */ }

		return serverName;
	}

	private static String tryGetWASServerName() {
		String serverName = null;

		try {
			InitialContext ic = new InitialContext();
			serverName = ic.lookup("servername").toString();
		} catch (Exception e) {
			/* swallow it */ }

		return serverName;
	}

	public static String getServerName() {
		String serverName = "Unable to determine server name :(";

		String temp = tryGetLibertyServerName();
		if (temp != null) {
			serverName = temp + LIBERTY_LOGO_IMG_TAG;
		} else {
			temp = tryGetWASServerName();
			if (temp != null) {
				serverName = temp + WAS_LOGO_IMG_TAG;
			}
		}

		return serverName;
	}

}
