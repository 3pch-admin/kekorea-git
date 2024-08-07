package e3ps.admin.commonCode;

import com.ptc.windchill.annotations.metadata.GenAsEnumeratedType;

@GenAsEnumeratedType
public class CommonCodeType extends _CommonCodeType {

	public static final CommonCodeType CUSTOMER = toCommonCodeType("CUSTOMER");
	public static final CommonCodeType INSTALL = toCommonCodeType("INSTALL");
	public static final CommonCodeType PROJECT_TYPE = toCommonCodeType("PROJECT_TYPE");
	public static final CommonCodeType MAK = toCommonCodeType("MAK");
	public static final CommonCodeType TASK_TYPE = toCommonCodeType("TASK_TYPE");
	public static final CommonCodeType MAK_DETAIL = toCommonCodeType("MAK_DETAIL");
	public static final CommonCodeType USER_TYPE = toCommonCodeType("USER_TYPE");
}
