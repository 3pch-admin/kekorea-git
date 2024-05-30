package e3ps.epm.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import e3ps.common.util.CommonUtils;
import e3ps.epm.numberRule.NumberRule;
import e3ps.workspace.ApprovalContract;
import e3ps.workspace.ApprovalContractPersistableLink;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.service.WorkspaceHelper;
import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.pom.Transaction;
import wt.services.StandardManager;
import wt.util.WTException;

public class StandardEpmService extends StandardManager implements EpmService {

	private static final long serialVersionUID = 8782888052535449244L;

	public static StandardEpmService newStandardEpmService() throws WTException {
		StandardEpmService instance = new StandardEpmService();
		instance.initialize();
		return instance;
	}

	@Override
	public void register(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name"); // 제목
		String description = (String) params.get("description");
		ArrayList<Map<String, String>> addRows = (ArrayList<Map<String, String>>) params.get("addRows"); // 결재문서
		ArrayList<Map<String, String>> addRows11 = (ArrayList<Map<String, String>>) params.get("addRows11"); // 결재문서
		ArrayList<Map<String, String>> agreeRows = (ArrayList<Map<String, String>>) params.get("agreeRows"); // 검토
		ArrayList<Map<String, String>> approvalRows = (ArrayList<Map<String, String>>) params.get("approvalRows"); // 결재
		ArrayList<Map<String, String>> receiveRows = (ArrayList<Map<String, String>>) params.get("receiveRows"); // 수신
		Transaction trs = new Transaction();
		try {
			trs.start();

			ApprovalContract contract = ApprovalContract.newApprovalContract();
			contract.setName(name);
			contract.setDescription(description);
			contract.setStartTime(new Timestamp(new Date().getTime()));
			contract.setState(WorkspaceHelper.STATE_APPROVAL_APPROVING);
			contract.setContractType("EPMDOCUMENT");
			contract.setOwnership(CommonUtils.sessionOwner());
			contract = (ApprovalContract) PersistenceHelper.manager.save(contract);

			for (Map<String, String> addRow : addRows) {
				String oid = addRow.get("oid");
				EPMDocument epm = (EPMDocument) CommonUtils.getObject(oid);
				ApprovalContractPersistableLink aLink = ApprovalContractPersistableLink
						.newApprovalContractPersistableLink(contract, epm);
				PersistenceHelper.manager.save(aLink);
			}

			for (Map<String, String> addRow11 : addRows11) {
				String oid = addRow11.get("oid");
				NumberRule numberRule = (NumberRule) CommonUtils.getObject(oid);
				ApprovalContractPersistableLink aLink = ApprovalContractPersistableLink
						.newApprovalContractPersistableLink(contract, numberRule);
				PersistenceHelper.manager.save(aLink);
			}

			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(contract, agreeRows, approvalRows, receiveRows);
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
	}
	
	@Override
	public void update(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name"); // 제목
		String description = (String) params.get("description");
		ArrayList<Map<String, String>> addRows = (ArrayList<Map<String, String>>) params.get("addRows"); // 결재문서
		ArrayList<Map<String, String>> addRows11 = (ArrayList<Map<String, String>>) params.get("addRows11"); // 결재문서
		ArrayList<Map<String, String>> agreeRows = (ArrayList<Map<String, String>>) params.get("agreeRows"); // 검토
		ArrayList<Map<String, String>> approvalRows = (ArrayList<Map<String, String>>) params.get("approvalRows"); // 결재
		ArrayList<Map<String, String>> receiveRows = (ArrayList<Map<String, String>>) params.get("receiveRows"); // 수신
		String oid = (String)params.get("oid");
		
		boolean isApproval = approvalRows.size() > 0;
		ApprovalMaster mm = null;
		
		Transaction trs = new Transaction();
		try {
			trs.start();
			ApprovalContract contract = (ApprovalContract)CommonUtils.getObject(oid);
			
			if (isApproval) {
				WorkspaceHelper.manager.deleteAllLines(contract);
			} else {
				mm = WorkspaceHelper.manager.getMaster(contract);
			}
			
			QueryResult qr = PersistenceHelper.manager.navigate(contract, "persist", ApprovalContractPersistableLink.class, false);
			while (qr.hasMoreElements()) {
				ApprovalContractPersistableLink link = (ApprovalContractPersistableLink) qr.nextElement();
				PersistenceHelper.manager.delete(link);
			}
			
			
			//ApprovalContract contract = ApprovalContract.newApprovalContract();
			contract.setName(name);
			contract.setDescription(description);
			contract.setStartTime(new Timestamp(new Date().getTime()));
			contract.setState(WorkspaceHelper.STATE_APPROVAL_APPROVING);
			contract.setContractType("EPMDOCUMENT");
			contract = (ApprovalContract) PersistenceHelper.manager.save(contract);

			for (Map<String, String> addRow : addRows) {
				String oid0 = addRow.get("oid");
				EPMDocument epm = (EPMDocument) CommonUtils.getObject(oid0);
				ApprovalContractPersistableLink aLink = ApprovalContractPersistableLink
						.newApprovalContractPersistableLink(contract, epm);
				PersistenceHelper.manager.save(aLink);
			}

			for (Map<String, String> addRow11 : addRows11) {
				String oid11 = addRow11.get("oid");
				NumberRule numberRule = (NumberRule) CommonUtils.getObject(oid11);
				ApprovalContractPersistableLink aLink = ApprovalContractPersistableLink
						.newApprovalContractPersistableLink(contract, numberRule);
				PersistenceHelper.manager.save(aLink);
			}

			contract = (ApprovalContract) PersistenceHelper.manager.refresh(contract);
			
			if (!isApproval) {
				if (mm != null) {
					String n = WorkspaceHelper.manager.getName(contract);
					mm.setPersist(contract);
					mm.setName(n);

					ArrayList<ApprovalLine> all = WorkspaceHelper.manager.getAllLines(mm);
					for (ApprovalLine line : all) {
						line.setName(n);
						PersistenceHelper.manager.modify(line);
					}
					PersistenceHelper.manager.modify(mm);
				}
			}
			
			
			if (approvalRows.size() > 0) {
				WorkspaceHelper.service.register(contract, agreeRows, approvalRows, receiveRows);
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
	}
	
	@Override
	public void delete(String oid) throws Exception {
		
		Transaction trs = new Transaction();
		try {
			trs.start();
			ApprovalContract contract = (ApprovalContract)CommonUtils.getObject(oid);
			
			WorkspaceHelper.manager.deleteAllLines(contract);
			
			QueryResult qr = PersistenceHelper.manager.navigate(contract, "persist", ApprovalContractPersistableLink.class, false);
			while (qr.hasMoreElements()) {
				ApprovalContractPersistableLink link = (ApprovalContractPersistableLink) qr.nextElement();
				PersistenceHelper.manager.delete(link);
			}
			
			contract = (ApprovalContract) PersistenceHelper.manager.delete(contract);

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
	}
}
