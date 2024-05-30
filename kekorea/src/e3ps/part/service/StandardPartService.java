package e3ps.part.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import e3ps.bom.partlist.PartListData;
import e3ps.common.content.service.CommonContentHelper;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.IBAUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.doc.WTDocumentWTPartLink;
import e3ps.erp.service.ErpHelper;
import e3ps.workspace.ApprovalContract;
import e3ps.workspace.ApprovalContractPersistableLink;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.service.WorkspaceHelper;
import wt.clients.folder.FolderTaskLogic;
import wt.content.ApplicationData;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.org.WTUser;
import wt.ownership.Ownership;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.services.StandardManager;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

public class StandardPartService extends StandardManager implements PartService {

	public static StandardPartService newStandardPartService() throws WTException {
		StandardPartService instance = new StandardPartService();
		instance.initialize();
		return instance;
	}

	@Override
	public Map<String, Object> bundle(Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<>();
		ArrayList<Map<String, Object>> addRows = (ArrayList<Map<String, Object>>) params.get("addRows");
		ArrayList<String> secondarys = (ArrayList<String>) params.get("secondarys");
		ArrayList<String> list = new ArrayList<>();
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTPart part = null;
			for (int i = 0; i < addRows.size(); i++) {
				Map<String, Object> addRow = addRows.get(i);

				String number = (String) addRow.get("number");
				String name = (String) addRow.get("name");
				String spec = (String) addRow.get("spec");
				String maker = (String) addRow.get("maker");
				String customer = (String) addRow.get("customer");
				String unit = (String) addRow.get("unit");
				int price = (int) addRow.get("price");
				String currency = (String) addRow.get("currency");

				// 임시 테스트용
//				QuerySpec qs = new QuerySpec();
//				int ii = qs.appendClassList(WTPart.class, true);
//				QuerySpecUtils.toEquals(qs, ii, WTPart.class, WTPart.NUMBER, spec);
//				QueryResult rs = PersistenceHelper.manager.find(qs);
//
//				if (rs.size() == 0) {
				part = WTPart.newWTPart();
				part.setNumber(spec);
				part.setName(name);

				View view = ViewHelper.service.getView("Engineering");
				ViewHelper.assignToView(part, view);

				Folder folder = FolderTaskLogic.getFolder(PartHelper.COMMON_DEFAULT_ROOT,
						CommonUtils.getPDMLinkProductContainer());
				FolderHelper.assignLocation((FolderEntry) part, folder);

				part = (WTPart) PersistenceHelper.manager.save(part);

				IBAUtils.createIBA(part, "s", "NAME_OF_PARTS", name);
				IBAUtils.createIBA(part, "s", "MAKER", maker);
				IBAUtils.createIBA(part, "s", "DWG_NO", spec);
				IBAUtils.createIBA(part, "s", "PART_CODE", number);
				IBAUtils.createIBA(part, "s", "STD_UNIT", unit);
				IBAUtils.createIBA(part, "i", "PRICE", price);
				IBAUtils.createIBA(part, "s", "CURRNAME", currency);
				IBAUtils.createIBA(part, "s", "CUSTNAME", customer);

				if (secondarys.size() > 0) {
//				if (secondarys.get(i) != null) {
					String cacheId = secondarys.get(i);

					ApplicationData data = ApplicationData.newApplicationData(part);
					File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
					data.setRole(ContentRoleType.PRIMARY);
					data = (ApplicationData) ContentServerHelper.service.updateContent(part, data, vault.getPath());
				}

				String code = ErpHelper.manager.sendToErp(part);
				list.add(code);

				// part <-> partlist data.. connect
//				QuerySpec query = new QuerySpec();
//				int idx = query.appendClassList(PartListData.class, true);
//				QuerySpecUtils.toEqualsAnd(query, idx, PartListData.class, PartListData.PART_NO, number);
//				QueryResult qr = PersistenceHelper.manager.find(query);
//				while (qr.hasMoreElements()) {
//					Object[] obj = (Object[]) qr.nextElement();
//					PartListData dd = (PartListData) obj[0];
//					dd.setWtPart(part);
//					PersistenceHelper.manager.modify(dd);
//				}
			}

			result.put("list", list);
			trs.commit();
			trs = null;
		} catch (

		Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			CommonContentHelper.manager.clean();
			if (trs != null)
				trs.rollback();
		}
		return result;
	}

	@Override
	public Map<String, Object> spec(Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<>();
		ArrayList<Map<String, Object>> addRows = (ArrayList<Map<String, Object>>) params.get("addRows");
		ArrayList<Map<String, Object>> addRows2 = (ArrayList<Map<String, Object>>) params.get("addRows2");
		ArrayList<String> list = new ArrayList<>();
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTPart part = null;
			for (int i = 0; i < addRows.size(); i++) {
				Map<String, Object> addRow = addRows.get(i);
				String number = (String) addRow.get("number");
				String name = (String) addRow.get("spec");
				String spec = (String) addRow.get("spec");
				String maker = (String) addRow.get("maker");
				String customer = (String) addRow.get("customer");
				String unit = (String) addRow.get("unit");
				int price = (int) addRow.get("price");
				String currency = (String) addRow.get("currency");

				part = WTPart.newWTPart();
				part.setNumber(spec);
				part.setName(name);

				View view = ViewHelper.service.getView("Engineering");
				ViewHelper.assignToView(part, view);

				Folder folder = FolderTaskLogic.getFolder(PartHelper.SPEC_DEFAULT_ROOT,
						CommonUtils.getPDMLinkProductContainer());
				FolderHelper.assignLocation((FolderEntry) part, folder);

				part = (WTPart) PersistenceHelper.manager.save(part);

				IBAUtils.createIBA(part, "s", "NAME_OF_PARTS", name);
				IBAUtils.createIBA(part, "s", "MAKER", maker);
				IBAUtils.createIBA(part, "s", "DWG_NO", spec);
				IBAUtils.createIBA(part, "s", "PART_CODE", number);
				IBAUtils.createIBA(part, "s", "STD_UNIT", unit);
				IBAUtils.createIBA(part, "i", "PRICE", price);
				IBAUtils.createIBA(part, "s", "CURRNAME", currency);
				IBAUtils.createIBA(part, "s", "CUSTNAME", customer);

				Map<String, Object> addRow2 = addRows2.get(i);
				String oid = (String) addRow2.get("oid");
				WTDocument document = (WTDocument) CommonUtils.getObject(oid);
				WTDocumentWTPartLink link = WTDocumentWTPartLink.newWTDocumentWTPartLink(document, part);
				PersistenceHelper.manager.save(link);

				part = (WTPart) PersistenceHelper.manager.refresh(part);
				String code = "";
				if (ErpHelper.isErpSend) {
					code = ErpHelper.manager.sendToErpItem(part, document);
				}
				list.add(code);
			}

			result.put("list", list);
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
		return result;
	}

	@Override
	public void modify(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name");
		String state = (String) params.get("state");
//		String oid = (String) params.get("oid");
		System.out.println(name + "======================" + state);
		Transaction trs = new Transaction();
		try {
			trs.start();
			WTPart part = WTPart.newWTPart();
			part.setName(name);
			PersistenceHelper.manager.save(part);

			QueryResult result = PersistenceHelper.manager.navigate(part, "WTPart", WTDocumentWTPartLink.class, false);
			while (result.hasMoreElements()) {
				WTDocumentWTPartLink link = (WTDocumentWTPartLink) result.nextElement();
				PersistenceHelper.manager.delete(link);
			}

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
	public void batch(Map<String, Object> params) throws Exception {
//		Map<String, Object> result = new HashMap<>();
		ArrayList<Map<String, Object>> addRows = (ArrayList<Map<String, Object>>) params.get("addRows");
		ArrayList<String> secondarys = (ArrayList<String>) params.get("secondarys");
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTPart part = null;
			for (int i = 0; i < addRows.size(); i++) {
				Map<String, Object> addRow = addRows.get(i);
				String cacheId = secondarys.get(i);
				String oid = (String) addRow.get("oid");
				String number = (String) addRow.get("number");

				part = (WTPart) CommonUtils.getObject(oid);

				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(part);
				applicationData.setRole(ContentRoleType.PRIMARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(part, applicationData, vault.getPath());

				// part <-> partlist data.. connect
				QuerySpec query = new QuerySpec();
				int idx = query.appendClassList(PartListData.class, true);
				QuerySpecUtils.toEqualsAnd(query, idx, PartListData.class, PartListData.PART_NO, number);
				QueryResult qr = PersistenceHelper.manager.find(query);
				while (qr.hasMoreElements()) {
					Object[] obj = (Object[]) qr.nextElement();
					PartListData dd = (PartListData) obj[0];
					dd.setWtPart(part);
					PersistenceHelper.manager.modify(dd);
				}
			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			CommonContentHelper.manager.clean();
			if (trs != null)
				trs.rollback();
		}
	}

	@Override
	public void code(Map<String, Object> params) throws Exception {
		Transaction trs = new Transaction();
		try {
			trs.start();

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.getSuppressed();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
		}
	}

	@Override
	public void plm(Map<String, Object> params) throws Exception {
//		Map<String, Object> result = new HashMap<>();
		ArrayList<Map<String, Object>> addRows = (ArrayList<Map<String, Object>>) params.get("addRows");
		ArrayList<String> secondarys = (ArrayList<String>) params.get("secondarys");
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTPart part = null;
			for (int i = 0; i < addRows.size(); i++) {
				String cacheId = secondarys.get(i);
				Map<String, Object> addRow = addRows.get(i);
				String number = (String) addRow.get("number");
				String name = (String) addRow.get("spec");
				String spec = (String) addRow.get("spec");
				String maker = (String) addRow.get("maker");
				String customer = (String) addRow.get("customer");
				String unit = (String) addRow.get("unit");
				int price = (int) addRow.get("price");
				String currency = (String) addRow.get("currency");

				part = WTPart.newWTPart();
				part.setNumber(spec);
				part.setName(name);

				View view = ViewHelper.service.getView("Engineering");
				ViewHelper.assignToView(part, view);

				Folder folder = FolderTaskLogic.getFolder(PartHelper.SPEC_DEFAULT_ROOT,
						CommonUtils.getPDMLinkProductContainer());
				FolderHelper.assignLocation((FolderEntry) part, folder);

				part = (WTPart) PersistenceHelper.manager.save(part);

				IBAUtils.createIBA(part, "s", "NAME_OF_PARTS", name);
				IBAUtils.createIBA(part, "s", "MAKER", maker);
				IBAUtils.createIBA(part, "s", "DWG_NO", spec);
				IBAUtils.createIBA(part, "s", "PART_CODE", number);
				IBAUtils.createIBA(part, "s", "STD_UNIT", unit);
				IBAUtils.createIBA(part, "i", "PRICE", price);
				IBAUtils.createIBA(part, "s", "CURRNAME", currency);
				IBAUtils.createIBA(part, "s", "CUSTNAME", customer);
				part = (WTPart) PersistenceHelper.manager.refresh(part);

				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(part);
				applicationData.setRole(ContentRoleType.PRIMARY);
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(part, applicationData, vault.getPath());

			}

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			CommonContentHelper.manager.clean();
			if (trs != null)
				trs.rollback();
		}
	}

	@Override
	public void create(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name");
		ArrayList<Map<String, Object>> arr1 = (ArrayList<Map<String, Object>>) params.get("arr1");
		ArrayList<Map<String, Object>> arr2 = (ArrayList<Map<String, Object>>) params.get("arr2"); // 라이브러리

		Transaction trs = new Transaction();
		try {
			trs.start();

			ApprovalContract contract = ApprovalContract.newApprovalContract();
			contract.setName(name);
			contract.setStartTime(new Timestamp(new Date().getTime()));
			contract.setState(WorkspaceHelper.STATE_APPROVAL_COMPLETE);
			contract.setContractType("PART_CODE");
			contract = (ApprovalContract) PersistenceHelper.manager.save(contract);

			for (Map<String, Object> dd : arr1) {
				String oid = (String) dd.get("oid");
				WTPart part = (WTPart) CommonUtils.getObject(oid);
				ApprovalContractPersistableLink aLink = ApprovalContractPersistableLink
						.newApprovalContractPersistableLink(contract, part);
				PersistenceHelper.manager.save(aLink);

				if (ErpHelper.isOperation) {
					ErpHelper.manager.sendToErp(part);
				}
			}

			for (Map<String, Object> dd : arr2) {
				String oid = (String) dd.get("oid"); // 라이브러리 파트 OID
				WTPart part = (WTPart) CommonUtils.getObject(oid);
				ApprovalContractPersistableLink aLink = ApprovalContractPersistableLink
						.newApprovalContractPersistableLink(contract, part);
				PersistenceHelper.manager.save(aLink);
			}

			Timestamp startTime = new Timestamp(new Date().getTime());

			ApprovalMaster master = null;
			name = WorkspaceHelper.manager.getName(contract);

			Ownership ownership = Ownership.newOwnership(SessionHelper.manager.getPrincipal());

			WTUser completeUser = (WTUser) SessionHelper.manager.getPrincipal();

			// 기안자생성
			master = ApprovalMaster.newApprovalMaster();
			master.setName(name);
			master.setCompleteTime(startTime);
			master.setOwnership(ownership);
			master.setPersist(contract);
			master.setStartTime(startTime);
			master.setState(WorkspaceHelper.STATE_MASTER_APPROVAL_COMPELTE);
			master.setCompleteUserID(completeUser.getName());
			master = (ApprovalMaster) PersistenceHelper.manager.save(master);

			// 검토가 있을 경우..
			ApprovalLine startLine = ApprovalLine.newApprovalLine();
			startLine.setName(master.getName());
			startLine.setOwnership(ownership);
			startLine.setMaster(master);
			startLine.setReads(true);
			startLine.setSort(-50);
			startLine.setStartTime(startTime);
			startLine.setType(WorkspaceHelper.SUBMIT_LINE);
			// 기안자
			startLine.setRole(WorkspaceHelper.WORKING_SUBMIT);
			startLine.setDescription(ownership.getOwner().getFullName() + " 사용자가 결재를 제출 하였습니다.");
			startLine.setCompleteUserID(completeUser.getName());
			startLine.setState(WorkspaceHelper.STATE_SUBMIT_COMPLETE);
			startLine.setCompleteTime(startTime);

			startLine = (ApprovalLine) PersistenceHelper.manager.save(startLine);

			ApprovalLine appLine = ApprovalLine.newApprovalLine();
			appLine.setName(master.getName());
			appLine.setOwnership(ownership);
			appLine.setCompleteTime(startTime);
			appLine.setDescription("자가 결재");
			appLine.setMaster(master);
			appLine.setType(WorkspaceHelper.APPROVAL_LINE);
			appLine.setReads(true);
			appLine.setRole(WorkspaceHelper.WORKING_APPROVAL);
			appLine.setSort(0);
			appLine.setStartTime(startTime);
			appLine.setState(WorkspaceHelper.STATE_APPROVAL_COMPLETE);
			appLine = (ApprovalLine) PersistenceHelper.manager.save(appLine);

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
