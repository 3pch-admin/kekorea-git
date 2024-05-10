<%@page import="e3ps.common.util.DateUtils"%>
<%@page import="wt.ownership.Ownership"%>
<%@page import="e3ps.common.util.StringUtils"%>
<%@page import="e3ps.org.People"%>
<%@page import="e3ps.admin.numberRuleCode.service.NumberRuleCodeHelper"%>
<%@page import="org.apache.poi.ss.usermodel.CellType"%>
<%@page import="org.apache.poi.ss.usermodel.Cell"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.apache.poi.ss.util.NumberToTextConverter"%>
<%@page import="wt.query.ClassAttribute"%>
<%@page import="e3ps.epm.numberRule.NumberRule"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="wt.query.SearchCondition"%>
<%@page import="e3ps.epm.numberRule.NumberRuleMaster"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="org.apache.poi.ss.usermodel.Row"%>
<%@page import="org.apache.poi.xssf.usermodel.XSSFWorkbook"%>
<%@page import="org.apache.poi.ss.usermodel.Sheet"%>
<%@page import="org.apache.poi.ss.usermodel.Workbook"%>
<%@page import="java.io.File"%>
<%@page import="wt.vc.VersionControlHelper"%>
<%@page import="wt.org.OrganizationServicesHelper"%>
<%@page import="wt.org.WTUser"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="wt.doc.WTDocument"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	File f = new File("D:" + File.separator + "1.xlsx");

	Workbook workbook = new XSSFWorkbook(f);
	Sheet sheet = workbook.getSheetAt(0);

	int rows = sheet.getPhysicalNumberOfRows(); // 시트의 행 개수 가져오기

	// 모든 행(row)을 순회하면서 데이터 가져오기
	for (int i = 1; i < rows; i++) {
		Row row = sheet.getRow(i);
		String number = getExcelValue(row.getCell(0));
		System.out.println("number=" + number);
		String company = getExcelValue(row.getCell(1));
		String name = getExcelValue(row.getCell(3));
		String size = getExcelValue(row.getCell(4));
		String year = getExcelValue(row.getCell(5));
		String state = getExcelValue(row.getCell(7));
		String creator = getExcelValue(row.getCell(8));
		String date = getExcelValue(row.getCell(9));
		String version = getExcelValue(row.getCell(10));
		String doc = getExcelValue(row.getCell(11));

		QuerySpec query = new QuerySpec();
		int idx = query.appendClassList(NumberRule.class, true);
		int idx_m = query.appendClassList(NumberRuleMaster.class, true);
		SearchCondition sc = new SearchCondition(NumberRuleMaster.class, NumberRuleMaster.NUMBER, "=", number);
		query.appendWhere(sc, new int[] { idx_m });
		query.appendAnd();
		sc = new SearchCondition(NumberRule.class, NumberRule.VERSION, "=", Integer.parseInt(version));
		query.appendWhere(sc, new int[] { idx });

		QueryResult qr = PersistenceHelper.manager.find(query);
		boolean exist = false;
		if (qr.hasMoreElements()) {
			Object[] obj = (Object[]) qr.nextElement();
			NumberRuleMaster m = (NumberRuleMaster) obj[1];
			out.println("EXIST = " + m.getNumber() + "<br>");
			exist = true;
		}

		if (!exist) {

			QuerySpec qs = new QuerySpec();
			int ii = qs.appendClassList(People.class, true);
			SearchCondition ss = new SearchCondition(People.class, People.NAME, "=", creator.trim());
			qs.appendWhere(ss, new int[] { ii });
			QueryResult result = PersistenceHelper.manager.find(qs);
			String id = "";
			if (result.hasMoreElements()) {
				Object[] obj = (Object[]) result.nextElement();
				People p = (People) obj[0];
				id = p.getId();
			}

			Ownership ownership = null;
			if (!StringUtils.isNull(id)) {
				WTUser u = OrganizationServicesHelper.manager.getUser(id);
				ownership = Ownership.newOwnership(u);
			} else {
				ownership = CommonUtils.sessionOwner();
			}

			if ("FAMS".equals(company.trim())) {
				company = "S";
			} else if ("KEK(설계)".equals(company.trim())) {
				company = "K";
			}

			String dept = "";
			if ("기타,제작사양서".equals(doc.trim())) {
				doc = "C";
			} else if ("전기,SOFT".equals(doc.trim())) {
				// 				dept = "";
			} else if ("기계도면".equals(doc.trim())) {

			} else if ("작업지시서".equals(doc.trim())) {
				doc = "A";
			}

			NumberRuleMaster master = NumberRuleMaster.newNumberRuleMaster();

			master.setOwnership(ownership);
			if (StringUtils.isNull(name)) {
				master.setName(number);
			} else {
				master.setName(name);
			}

			master.setCreateTime(DateUtils.convertDate(date));
			master.setModifyTime(DateUtils.convertDate(date));
			master.setNumber(number);
			master.setLotNo(0);
			master.setUnitName(null);
			master.setDocument(NumberRuleCodeHelper.manager.getNumberRuleCode("WRITTEN_DOCUMENT", doc));
			master.setSize(NumberRuleCodeHelper.manager.getNumberRuleCode("SIZE", size));
			master.setCompany(NumberRuleCodeHelper.manager.getNumberRuleCode("DRAWING_COMPANY", company));
			master.setSector(NumberRuleCodeHelper.manager.getNumberRuleCode("BUSINESS_SECTOR", "K"));
			master.setDepartment(
					NumberRuleCodeHelper.manager.getNumberRuleCode("CLASSIFICATION_WRITING_DEPARTMENT", dept));
			PersistenceHelper.manager.save(master);

			NumberRule numberRule = NumberRule.newNumberRule();
			numberRule.setLatest(true); // 최신이 필요 없을ㄷ...
			numberRule.setVersion(Integer.parseInt(version));
			numberRule.setState(state);
			numberRule.setMaster(master);
			numberRule.setOwnership(ownership);
			numberRule.setCreateTime(DateUtils.convertDate(date));
			numberRule.setModifyTime(DateUtils.convertDate(date));
			PersistenceHelper.manager.save(numberRule);
		}
	}
	workbook.close();
	out.println("END!");
%>

<%!private String getExcelValue(Cell cell) {
		if (cell == null) {
			return "";
		}
		if (cell.getCellType().equals(CellType.NUMERIC)) {
			if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				return dateFormat.format(cell.getDateCellValue());
			} else {
				// return String.valueOf(cell.getNumericCellValue());
				return NumberToTextConverter.toText(cell.getNumericCellValue());
			}
		} else if (cell.getCellType().equals(CellType.STRING)) {
			return cell.getStringCellValue();
		} else if (cell.getCellType().equals(CellType.BLANK)) {
			return "";
		} else if (cell.getCellType().equals(CellType.ERROR)) {
			return String.valueOf(cell.getErrorCellValue());
		} else if (cell.getCellType().equals(CellType.BOOLEAN)) {
			return String.valueOf(cell.getBooleanCellValue());
		}
		return "";
	}%>