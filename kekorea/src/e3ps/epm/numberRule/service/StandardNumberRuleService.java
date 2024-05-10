package e3ps.epm.numberRule.service;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import e3ps.admin.numberRuleCode.service.NumberRuleCodeHelper;
import e3ps.common.Constants;
import e3ps.common.util.CommonUtils;
import e3ps.common.util.DateUtils;
import e3ps.common.util.QuerySpecUtils;
import e3ps.common.util.StringUtils;
import e3ps.epm.numberRule.NumberRule;
import e3ps.epm.numberRule.NumberRuleMaster;
import e3ps.epm.numberRule.dto.NumberRuleDTO;
import e3ps.epm.workOrder.WorkOrder;
import e3ps.workspace.ApprovalContract;
import e3ps.workspace.ApprovalContractPersistableLink;
import e3ps.workspace.service.WorkspaceHelper;
import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.ownership.Ownership;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.services.StandardManager;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

public class StandardNumberRuleService extends StandardManager implements NumberRuleService {

	public static StandardNumberRuleService newStandardNumberRuleService() throws WTException {
		StandardNumberRuleService instance = new StandardNumberRuleService();
		instance.initialize();
		return instance;
	}

	@Override
	public void save(HashMap<String, List<NumberRuleDTO>> dataMap) throws Exception {
		List<NumberRuleDTO> addRows = dataMap.get("addRows");
		List<NumberRuleDTO> removeRows = dataMap.get("removeRows");
		List<NumberRuleDTO> editRows = dataMap.get("editRows");
		Transaction trs = new Transaction();
		try {
			trs.start();

			Ownership ownership = CommonUtils.sessionOwner();

			for (NumberRuleDTO dto : addRows) {
				int version = dto.getVersion();
				String number = dto.getNumber();
				String name = dto.getName();
				String writtenDocuments = dto.getWrittenDocuments_code();
				String drawingCompany = dto.getDrawingCompany_code();
				String businessSector = dto.getBusinessSector_code();
				String classificationWritingDepartments = dto.getClassificationWritingDepartments_code();
				String size = dto.getSize_code();
				int lotNo = dto.getLotNo();
				String unitName = dto.getUnitName();
				Map<String, Object> result = NumberRuleHelper.manager.last(number);

				String next = (String) result.get("next");
				String last = (String) result.get("last");

				String newNumber = number + next;

				dto.setNumber(newNumber);

				NumberRuleMaster master = NumberRuleMaster.newNumberRuleMaster();
				master.setCreateTime(DateUtils.today());
				master.setOwnership(ownership);
				master.setName(name);
				master.setNumber(newNumber);
				master.setLotNo(lotNo);
				master.setUnitName(unitName);
				master.setDocument(
						NumberRuleCodeHelper.manager.getNumberRuleCode("WRITTEN_DOCUMENT", writtenDocuments));
//				master.setSize(NumberRuleCodeHelper.manager.getNumberRuleCode("SIZE", size));
				master.setCompany(NumberRuleCodeHelper.manager.getNumberRuleCode("DRAWING_COMPANY", drawingCompany));
				master.setSector(NumberRuleCodeHelper.manager.getNumberRuleCode("BUSINESS_SECTOR", businessSector));
				master.setDepartment(NumberRuleCodeHelper.manager.getNumberRuleCode("CLASSIFICATION_WRITING_DEPARTMENT",
						classificationWritingDepartments));
				PersistenceHelper.manager.save(master);

				NumberRule numberRule = NumberRule.newNumberRule();
				numberRule.setLatest(true); // 최신이 필요 없을ㄷ...
				numberRule.setVersion(version);
				numberRule.setState(Constants.State.INWORK);
				numberRule.setMaster(master);
				numberRule.setOwnership(ownership);
				numberRule.setCreateTime(DateUtils.today());
				PersistenceHelper.manager.save(numberRule);
			}

			for (NumberRuleDTO dto : removeRows) {
				String oid = dto.getOid();
				NumberRule latest = (NumberRule) CommonUtils.getObject(oid);

				QuerySpec query = new QuerySpec();
				int idx = query.appendClassList(WorkOrder.class, true);
				QuerySpecUtils.toEqualsAnd(query, idx, WorkOrder.class, "numberRuleReference.key.id", latest);
				QueryResult qr = PersistenceHelper.manager.find(query);
				if (qr.hasMoreElements()) {
					throw new Exception(latest.getMaster().getNumber() + " 도번이 도면일람표에 사용 되고 있습니다.");
				}

				NumberRuleMaster master = latest.getMaster();
				boolean isLast = NumberRuleHelper.manager.isLast(master);
				if (isLast) {
					PersistenceHelper.manager.delete(latest);
					PersistenceHelper.manager.delete(master);
				} else {
					NumberRule pre = NumberRuleHelper.manager.predecessor(latest);
					pre.setLatest(true);
					PersistenceHelper.manager.modify(pre);
					PersistenceHelper.manager.delete(latest);
				}
			}

			for (NumberRuleDTO dto : editRows) {
				String number = dto.getNumber();
				String name = dto.getName();
				String writtenDocuments = dto.getWrittenDocuments_code();
				String drawingCompany = dto.getDrawingCompany_code();
				String businessSector = dto.getBusinessSector_code();
				String classificationWritingDepartments = dto.getClassificationWritingDepartments_code();
				String size = dto.getSize_code();
				String oid = dto.getOid();
				int lotNo = dto.getLotNo();
				String unitName = dto.getUnitName();

				NumberRule numberRule = (NumberRule) CommonUtils.getObject(oid);
				NumberRuleMaster master = numberRule.getMaster();
				master.setModifyTime(DateUtils.today());
				master.setName(name);
				master.setLotNo(lotNo);
				master.setUnitName(unitName);
				master.setNumber(number);
				master.setDocument(
						NumberRuleCodeHelper.manager.getNumberRuleCode("WRITTEN_DOCUMENT", writtenDocuments));
				master.setSize(NumberRuleCodeHelper.manager.getNumberRuleCode("SIZE", size));
				master.setCompany(NumberRuleCodeHelper.manager.getNumberRuleCode("DRAWING_COMPANY", drawingCompany));
				master.setSector(NumberRuleCodeHelper.manager.getNumberRuleCode("BUSINESS_SECTOR", businessSector));
				master.setDepartment(NumberRuleCodeHelper.manager.getNumberRuleCode("CLASSIFICATION_WRITING_DEPARTMENT",
						classificationWritingDepartments));
				PersistenceHelper.manager.modify(master);

				PersistenceHelper.manager.modify(numberRule);
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
	public void revise(HashMap<String, List<NumberRuleDTO>> dataMap) throws Exception {
		List<NumberRuleDTO> addRows = dataMap.get("addRows");
		Transaction trs = new Transaction();
		try {
			trs.start();

			for (NumberRuleDTO dto : addRows) {
				String oid = dto.getOid();
				int next = dto.getNext();
				String note = dto.getNote();

				NumberRule pre = (NumberRule) CommonUtils.getObject(oid);
				pre.setLatest(false);
				pre = (NumberRule) PersistenceHelper.manager.modify(pre);

				NumberRule latest = NumberRule.newNumberRule();
				latest.setCreateTime(DateUtils.today());
				latest.setModifyTime(DateUtils.today());
				latest.setLatest(true);
				latest.setVersion(next);
				latest.setMaster(pre.getMaster());
				latest.setState(Constants.State.INWORK);
				latest.setOwnership(CommonUtils.sessionOwner());
				latest.setNote(note);
				PersistenceHelper.manager.save(latest);
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
	public void loader(String path) throws Exception {
		SessionContext prev = SessionContext.newContext();
		Transaction trs = new Transaction();
		try {
			trs.start();

			SessionHelper.manager.setAdministrator();

			File file = new File(path);

			Workbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);

			int rows = sheet.getPhysicalNumberOfRows(); // 시트의 행 개수 가져오기

			// 모든 행(row)을 순회하면서 데이터 가져오기
			for (int i = 1; i < rows; i++) {
				Row row = sheet.getRow(i);

				// 도면 생성 회사부터
				String cc = row.getCell(2).getStringCellValue();

				// 사업부문 고정 K

				String number = row.getCell(1).getStringCellValue(); // 도면 번호

				String name = null;
				if (row.getCell(4) != null) {
					if (row.getCell(4).getCellType() == CellType.NUMERIC) {
						name = String.valueOf(row.getCell(4).getNumericCellValue());
					} else if (row.getCell(4).getCellType() == CellType.STRING) {
						name = row.getCell(4).getStringCellValue(); // 도면명
					}
				} else {
					name = number;
				}
				String company = row.getCell(4).getStringCellValue();

				String size = null;
				if (row.getCell(5) != null) {
					size = row.getCell(5).getStringCellValue();
				}

				String state = null;
				if (row.getCell(8) != null) {
					state = row.getCell(8).getStringCellValue();
				}

				String creator = null;
				if (row.getCell(9) != null) {
					creator = row.getCell(9).getStringCellValue();// 작성자
				} else {
					creator = "관리자";
				}

				if (DateUtil.isCellDateFormatted(row.getCell(10))) {
					Date date = row.getCell(10).getDateCellValue();
					String createdDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
					System.out.println("createdDate=" + createdDate);
				}

				double version = row.getCell(11).getNumericCellValue();

				NumberRule numberRule = NumberRule.newNumberRule();

				System.out.println("creator=" + creator);

				System.out.println("version=" + (int) version);
				System.out.println("i=" + i);

				NumberRuleMaster master = NumberRuleMaster.newNumberRuleMaster();
				master.setSector(NumberRuleCodeHelper.manager.getNumberRuleCode("BUSINESS_SECTOR", "K"));
				if (!StringUtils.isNull(size)) {
					master.setSector(NumberRuleCodeHelper.manager.getNumberRuleCode("SIZE", size));
				}

				master.setCompany(NumberRuleCodeHelper.manager.getNumberRuleCode("DRAWING_COMPANY", company));

//				master.setDocument(
//						NumberRuleCodeHelper.manager.getNumberRuleCode("WRITTEN_DOCUMENT", writtenDocuments));

//				master.setDepartment(NumberRuleCodeHelper.manager.getNumberRuleCode("CLASSIFICATION_WRITING_DEPARTMENT",
//						classificationWritingDepartments));

			}

			workbook.close();

			trs.commit();
			trs = null;
		} catch (Exception e) {
			e.printStackTrace();
			trs.rollback();
			throw e;
		} finally {
			if (trs != null)
				trs.rollback();
			SessionContext.setContext(prev);
		}
	}

	@Override
	public void register(Map<String, Object> params) throws Exception {
		String name = (String) params.get("name"); // 제목
		String description = (String) params.get("description");
		ArrayList<Map<String, String>> data = (ArrayList<Map<String, String>>) params.get("data"); // 결재문서
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
			contract.setContractType("NUMBERRULE");
			contract.setOwnership(CommonUtils.sessionOwner());
			contract = (ApprovalContract) PersistenceHelper.manager.save(contract);

			for (Map<String, String> dd : data) {
				String oid = dd.get("oid");
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
}
