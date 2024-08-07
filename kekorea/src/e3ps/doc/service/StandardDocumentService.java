package e3ps.doc.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import e3ps.common.content.service.CommonContentHelper;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.IBAUtils;
import e3ps.doc.WTDocumentWTPartLink;
import e3ps.doc.dto.DocumentDTO;
import e3ps.epm.numberRule.NumberRule;
import e3ps.epm.numberRule.NumberRulePersistableLink;
import e3ps.project.output.Output;
import e3ps.project.output.OutputDocumentLink;
import e3ps.workspace.ApprovalContract;
import e3ps.workspace.ApprovalContractPersistableLink;
import e3ps.workspace.ApprovalLine;
import e3ps.workspace.ApprovalMaster;
import e3ps.workspace.service.WorkspaceHelper;
import wt.clients.folder.FolderTaskLogic;
import wt.clients.vc.CheckInOutTaskLogic;
import wt.content.ApplicationData;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.services.StandardManager;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;

public class StandardDocumentService extends StandardManager implements DocumentService {

	public static StandardDocumentService newStandardDocumentService() throws WTException {
		StandardDocumentService instance = new StandardDocumentService();
		instance.initialize();
		return instance;
	}

	@Override
	public void register(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name"); // 제목
		String description = (String) params.get("description"); // 의견
		ArrayList<Map<String, String>> addRows = (ArrayList<Map<String, String>>) params.get("addRows"); // 결재문서
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
			contract.setContractType("DOCUMENT");
			contract = (ApprovalContract) PersistenceHelper.manager.save(contract);

			for (Map<String, String> addRow : addRows) {
				String oid = addRow.get("oid"); // document oid
				WTDocument document = (WTDocument) CommonUtils.getObject(oid);
				ApprovalContractPersistableLink aLink = ApprovalContractPersistableLink
						.newApprovalContractPersistableLink(contract, document);
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
	public void create(DocumentDTO dto) throws Exception {
		String number = dto.getNumber();
		String name = dto.getName();
		String description = dto.getDescription();
		String location = dto.getLocation();
		boolean isSelf = dto.isSelf();
		ArrayList<String> primarys = dto.getPrimarys();
		ArrayList<Map<String, String>> addRows7 = dto.getAddRows7();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		ArrayList<Map<String, Object>> addRows11 = dto.getAddRows11();
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTDocument document = WTDocument.newWTDocument();
			document.setName(name);
			document.setNumber(number);
			document.setDescription(description);

			Folder folder = FolderTaskLogic.getFolder(location, CommonUtils.getPDMLinkProductContainer());
			FolderHelper.assignLocation((FolderEntry) document, folder);

			document = (WTDocument) PersistenceHelper.manager.save(document);

			// 도번 추가
			for (Map<String, Object> addRow11 : addRows11) {
				String oid = (String) addRow11.get("oid");
				NumberRule numberRule = (NumberRule) CommonUtils.getObject(oid);
				numberRule.setPersist(document.getMaster());
				PersistenceHelper.manager.modify(numberRule);
				IBAUtils.createIBA(document, "s", "NUMBER_RULE", numberRule.getMaster().getNumber());
				IBAUtils.createIBA(document, "s", "NUMBER_RULE_VERSION", String.valueOf(numberRule.getVersion()));
			}

			for (int i = 0; i < primarys.size(); i++) {
				String cacheId = (String) primarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(document);
				if (i == 0) {
					applicationData.setRole(ContentRoleType.PRIMARY);
				} else {
					applicationData.setRole(ContentRoleType.SECONDARY);
				}
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(document, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow7 : addRows7) {
				String oid = addRow7.get("oid");
				WTPart part = (WTPart) CommonUtils.getObject(oid);
				WTDocumentWTPartLink link = WTDocumentWTPartLink.newWTDocumentWTPartLink(document, part);
				PersistenceHelper.manager.save(link);
			}

			if (isSelf) {
				WorkspaceHelper.service.self(document.getPersistInfo().getObjectIdentifier().getStringValue());
			} else {
				// 결재시작
				if (approvalRows.size() > 0) {
					WorkspaceHelper.service.register(document, agreeRows, approvalRows, receiveRows);
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
	public void delete(String oid) throws Exception {
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTDocument document = (WTDocument) CommonUtils.getObject(oid);

			QueryResult qr = PersistenceHelper.manager.navigate(document, "part", WTDocumentWTPartLink.class, false);
			while (qr.hasMoreElements()) {
				WTDocumentWTPartLink link = (WTDocumentWTPartLink) qr.nextElement();
				PersistenceHelper.manager.delete(link);
			}

			qr.reset();
			qr = PersistenceHelper.manager.navigate(document, "output", OutputDocumentLink.class);
			while (qr.hasMoreElements()) {
				Output output = (Output) qr.nextElement();
				PersistenceHelper.manager.delete(output);
			}

			QueryResult result = PersistenceHelper.manager.navigate(document.getMaster(), "numberRule",
					NumberRulePersistableLink.class);
			while (result.hasMoreElements()) {
				NumberRule numberRule = (NumberRule) result.nextElement();
				numberRule.setPersist(null);
				PersistenceHelper.manager.modify(numberRule);
			}

			WorkspaceHelper.manager.deleteAllLines(document);

			document = (WTDocument) PersistenceHelper.manager.refresh(document);

			PersistenceHelper.manager.delete(document);

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
	public void modify(DocumentDTO dto) throws Exception {
		String oid = dto.getOid();
		String name = dto.getName();
		String description = dto.getDescription();
		String location = dto.getLocation();
		boolean isSelf = dto.isSelf();
		ArrayList<String> primarys = dto.getPrimarys();
		ArrayList<Map<String, String>> addRows7 = dto.getAddRows7();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		ArrayList<Map<String, Object>> addRows11 = dto.getAddRows11();
		boolean isApproval = approvalRows.size() > 0;
		ApprovalMaster mm = null;
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTDocument document = (WTDocument) CommonUtils.getObject(oid);

			if (isApproval) {
				WorkspaceHelper.manager.deleteAllLines(document);
			} else {
				mm = WorkspaceHelper.manager.getMaster(document);
			}

			document = (WTDocument) PersistenceHelper.manager.refresh(document);

			Folder cFolder = CheckInOutTaskLogic.getCheckoutFolder();
			CheckoutLink clink = WorkInProgressHelper.service.checkout(document, cFolder, "문서 수정 체크 아웃");
			WTDocument workCopy = (WTDocument) clink.getWorkingCopy();
			workCopy.setDescription(description);

			WTDocumentMaster master = (WTDocumentMaster) workCopy.getMaster();
			WTDocumentMasterIdentity identity = (WTDocumentMasterIdentity) master.getIdentificationObject();
			identity.setName(name);
			master = (WTDocumentMaster) IdentityHelper.service.changeIdentity(master, identity);

			WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
			String msg = user.getFullName() + " 사용자가 문서를 수정 하였습니다.";
			// 필요하면 수정 사유로 대체
			workCopy = (WTDocument) WorkInProgressHelper.service.checkin(workCopy, msg);

			Folder folder = FolderTaskLogic.getFolder(location, CommonUtils.getPDMLinkProductContainer());
			FolderHelper.service.changeFolder((FolderEntry) workCopy, folder);

			QueryResult result = PersistenceHelper.manager.navigate(master, "numberRule",
					NumberRulePersistableLink.class);
			while (result.hasMoreElements()) {
				NumberRule numberRule = (NumberRule) result.nextElement();
				numberRule.setPersist(null);
				PersistenceHelper.manager.modify(numberRule);
			}

			// 도번 추가
			for (Map<String, Object> addRow11 : addRows11) {
				NumberRule numberRule = (NumberRule) CommonUtils.getObject((String) addRow11.get("oid"));
				numberRule.setPersist(workCopy.getMaster());
				PersistenceHelper.manager.modify(numberRule);
				IBAUtils.createIBA(workCopy, "s", "NUMBER_RULE", numberRule.getMaster().getNumber());
				IBAUtils.createIBA(workCopy, "s", "NUMBER_RULE_VERSION", String.valueOf(numberRule.getVersion()));
			}

			// ???
			CommonContentHelper.manager.clear(workCopy);

			for (int i = 0; i < primarys.size(); i++) {
				String cacheId = (String) primarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(workCopy);
				if (i == 0) {
					applicationData.setRole(ContentRoleType.PRIMARY);
				} else {
					applicationData.setRole(ContentRoleType.SECONDARY);
				}
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(workCopy, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow7 : addRows7) {
				WTPart part = (WTPart) CommonUtils.getObject(addRow7.get("oid"));
				WTDocumentWTPartLink link = WTDocumentWTPartLink.newWTDocumentWTPartLink(workCopy, part);
				PersistenceHelper.manager.save(link);
			}

			workCopy = (WTDocument) PersistenceHelper.manager.refresh(workCopy);
			if (!isApproval) {
				if (mm != null) {
					String n = WorkspaceHelper.manager.getName(workCopy);
					mm.setPersist(workCopy);
					mm.setName(n);

					ArrayList<ApprovalLine> all = WorkspaceHelper.manager.getAllLines(mm);
					for (ApprovalLine line : all) {
						line.setName(n);
						PersistenceHelper.manager.modify(line);
					}
					PersistenceHelper.manager.modify(mm);
				}
			}

			if (isSelf) {
				WorkspaceHelper.service.self(workCopy.getPersistInfo().getObjectIdentifier().getStringValue());
			} else {
				// 결재시작
				if (approvalRows.size() > 0) {
					WorkspaceHelper.service.register(workCopy, agreeRows, approvalRows, receiveRows);
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
	public void revise(DocumentDTO dto) throws Exception {
		String oid = dto.getOid();
		String name = dto.getName();
		String description = dto.getDescription();
		String location = dto.getLocation();
		boolean isSelf = dto.isSelf();
		ArrayList<String> primarys = dto.getPrimarys();
		ArrayList<Map<String, String>> addRows7 = dto.getAddRows7();
		ArrayList<Map<String, String>> agreeRows = dto.getAgreeRows();
		ArrayList<Map<String, String>> approvalRows = dto.getApprovalRows();
		ArrayList<Map<String, String>> receiveRows = dto.getReceiveRows();
		ArrayList<Map<String, Object>> addRows11 = dto.getAddRows11();
		Transaction trs = new Transaction();
		try {
			trs.start();

			WTDocument document = (WTDocument) CommonUtils.getObject(oid);

			WTDocument newDoc = (WTDocument) VersionControlHelper.service.newVersion(document);
			WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
			String msg = user.getFullName() + " 사용자가 문서를 개정 하였습니다.";
			VersionControlHelper.setNote(newDoc, msg);
			newDoc.setDescription(description);
			WTDocumentMaster master = (WTDocumentMaster) newDoc.getMaster();
			WTDocumentMasterIdentity identity = (WTDocumentMasterIdentity) master.getIdentificationObject();
			identity.setName(name);
			master = (WTDocumentMaster) IdentityHelper.service.changeIdentity(master, identity);

			// 필요하면 수정 사유로 대체
//			newDoc = (WTDocument) WorkInProgressHelper.service.checkin(newDoc, msg);
			PersistenceHelper.manager.save(newDoc);

			Folder folder = FolderTaskLogic.getFolder(location, CommonUtils.getPDMLinkProductContainer());
			FolderHelper.service.changeFolder((FolderEntry) newDoc, folder);

			QueryResult result = PersistenceHelper.manager.navigate(master, "numberRule",
					NumberRulePersistableLink.class);
			while (result.hasMoreElements()) {
				NumberRule numberRule = (NumberRule) result.nextElement();
				numberRule.setPersist(null);
				PersistenceHelper.manager.modify(numberRule);
			}

			// 도번 추가
			for (Map<String, Object> addRow11 : addRows11) {
				NumberRule numberRule = (NumberRule) CommonUtils.getObject((String) addRow11.get("oid"));
				numberRule.setPersist(newDoc.getMaster());
				PersistenceHelper.manager.modify(numberRule);
				IBAUtils.createIBA(newDoc, "s", "NUMBER_RULE", numberRule.getMaster().getNumber());
				IBAUtils.createIBA(newDoc, "s", "NUMBER_RULE_VERSION", String.valueOf(numberRule.getVersion()));
			}

			CommonContentHelper.manager.clear(newDoc);

			for (int i = 0; i < primarys.size(); i++) {
				String cacheId = (String) primarys.get(i);
				File vault = CommonContentHelper.manager.getFileFromCacheId(cacheId);
				ApplicationData applicationData = ApplicationData.newApplicationData(newDoc);
				if (i == 0) {
					applicationData.setRole(ContentRoleType.PRIMARY);
				} else {
					applicationData.setRole(ContentRoleType.SECONDARY);
				}
				PersistenceHelper.manager.save(applicationData);
				ContentServerHelper.service.updateContent(newDoc, applicationData, vault.getPath());
			}

			for (Map<String, String> addRow7 : addRows7) {
				WTPart part = (WTPart) CommonUtils.getObject(addRow7.get("oid"));
				WTDocumentWTPartLink link = WTDocumentWTPartLink.newWTDocumentWTPartLink(newDoc, part);
				PersistenceHelper.manager.save(link);
			}

			if (isSelf) {
				WorkspaceHelper.service.self(newDoc.getPersistInfo().getObjectIdentifier().getStringValue());
			} else {
				// 결재시작
				if (approvalRows.size() > 0) {
					WorkspaceHelper.service.register(newDoc, agreeRows, approvalRows, receiveRows);
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
}
