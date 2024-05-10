<%@page import="e3ps.common.Constants"%>
<%@page import="e3ps.epm.numberRule.NumberRule"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="e3ps.admin.numberRuleCode.service.NumberRuleCodeHelper"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.epm.numberRule.NumberRuleMaster"%>
<%@page import="java.io.File"%>
<%@page import="e3ps.loader.numberRule.NumberRuleLoader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%


NumberRuleMaster master = NumberRuleMaster.newNumberRuleMaster();
master.setOwnership(CommonUtils.sessionOwner());
master.setName("K4CW00175");
master.setNumber("K4CW00175");
master.setLotNo(200);
master.setUnitName("K4CW00175");
master.setDocument(
		NumberRuleCodeHelper.manager.getNumberRuleCode("WRITTEN_DOCUMENT", "A"));
//master.setSize(NumberRuleCodeHelper.manager.getNumberRuleCode("SIZE", size));
master.setCompany(NumberRuleCodeHelper.manager.getNumberRuleCode("DRAWING_COMPANY", "K"));
master.setSector(NumberRuleCodeHelper.manager.getNumberRuleCode("BUSINESS_SECTOR", "K"));
master.setDepartment(NumberRuleCodeHelper.manager.getNumberRuleCode("CLASSIFICATION_WRITING_DEPARTMENT",
		"E"));
PersistenceHelper.manager.save(master);

NumberRule numberRule = NumberRule.newNumberRule();
numberRule.setLatest(true); // 최신이 필요 없을ㄷ...
numberRule.setVersion(1);
numberRule.setState(Constants.State.INWORK);
numberRule.setMaster(master);
numberRule.setOwnership(CommonUtils.sessionOwner());
PersistenceHelper.manager.save(numberRule);






%>