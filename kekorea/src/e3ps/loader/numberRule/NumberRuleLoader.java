package e3ps.loader.numberRule;

import e3ps.epm.numberRule.service.NumberRuleHelper;
import e3ps.epm.numberRule.service.NumberRuleService;
import wt.method.RemoteMethodServer;

public class NumberRuleLoader {

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			System.out.println("엑셀 파일을 입력하세요.");
			System.exit(0);
		}
		
//		RemoteMethodServer.getDefault().setUserName("wcadmin");
//		RemoteMethodServer.getDefault().setPassword("design12");

		NumberRuleLoader loader = new NumberRuleLoader();
		loader.loadNumberRule(args[0]);

		System.exit(0);
	}

	private void loadNumberRule(String path) throws Exception {
		NumberRuleHelper.service.loader(path);
	}
}
