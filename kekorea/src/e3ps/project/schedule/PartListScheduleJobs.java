/**
 * 
 */
package e3ps.project.schedule;

import e3ps.bom.partlist.PartListMaster;
import e3ps.bom.partlist.PartListMasterProjectLink;
import e3ps.common.util.CommonUtils;
import e3ps.project.Project;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.query.QuerySpec;

/**
 * @클래스명 : PartListScheduleJobs.java
 * @최초 작성자 :
 * @최초 작성일 : 2024. 07. 11
 * @설명 :
 */
public class PartListScheduleJobs {
	public static void startBatch() {
		try {
			System.out.println("START!! KEK PARTLIST SCHEDULE");
			startCalc();
			System.out.println("END!! KEK PARTLIST SCHEDULE");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @메소드명 :
	 * @최초 작성자 :
	 * @최초 작성일 : 2024. 07. 11
	 * @설명 :
	 */
	private static void startCalc() throws Exception {

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(Project.class, true);
		QueryResult rs = PersistenceHelper.manager.find(query);
		while (rs.hasMoreElements()) {
			Object[] obj = (Object[]) rs.nextElement();
			Project pjt = (Project) obj[0];

			System.out.println("계산 되는 작번 번호 = " + pjt.getKekNumber());

			QueryResult qr = PersistenceHelper.manager.navigate(pjt, "master", PartListMasterProjectLink.class, false);
			double mT = 0D;
			double eT = 0D;
			while (qr.hasMoreElements()) {
				PartListMaster p = (PartListMaster) qr.nextElement();
				String engType = p.getEngType();
				if ("기계".equals(engType)) {
					mT = p.getTotalPrice();
				} else if ("전기".equals(engType)) {
					eT = p.getTotalPrice();
				}
			}
			pjt.setOutputMachinePrice(mT);
			pjt.setOutputElecPrice(eT);
			PersistenceHelper.manager.modify(pjt);
			System.out.println("계산 되는 작번 번호 종료 = " + pjt.getKekNumber());
		}
	}
}
