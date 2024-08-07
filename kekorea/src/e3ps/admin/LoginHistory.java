package e3ps.admin;

import com.ptc.windchill.annotations.metadata.GenAsPersistable;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;

import wt.fc.WTObject;
import wt.util.WTException;

@GenAsPersistable(superClass = WTObject.class,

		properties = {

				@GeneratedProperty(name = "ip", type = String.class, javaDoc = "접속 아이피"),

				@GeneratedProperty(name = "id", type = String.class, javaDoc = "접속 아이디")

		}

)
public class LoginHistory extends _LoginHistory {

	static final long serialVersionUID = 1;

	public static LoginHistory newLoginHistory() throws WTException {
		LoginHistory instance = new LoginHistory();
		instance.initialize();
		return instance;
	}
}
