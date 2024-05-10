<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.epm.numberRule.dto.NumberRuleDTO"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="wt.org.WTUser"%>
<%@page import="e3ps.common.util.ContentUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
NumberRuleDTO dto = (NumberRuleDTO) request.getAttribute("dto");
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
boolean isSupervisor = (boolean) request.getAttribute("isSupervisor");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
Timestamp time = (Timestamp) request.getAttribute("time");
JSONArray sizes = (JSONArray) request.getAttribute("sizes");
JSONArray drawingCompanys = (JSONArray) request.getAttribute("drawingCompanys");
JSONArray writtenDocuments = (JSONArray) request.getAttribute("writtenDocuments");
JSONArray businessSectors = (JSONArray) request.getAttribute("businessSectors");
JSONArray classificationWritingDepartments = (JSONArray) request.getAttribute("classificationWritingDepartments");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%> 
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<style type="text/css">
.preView {
	background-color: #caf4fd;
	cursor: pointer;
}
</style>   
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				KEK 도번 정보
			</div>
		</td>
		<td class="right">
			<input type="button" value="수정" title="수정" class="blue" onclick="modify();">	
			<input type="button" value="개정" title="개정" onclick="update('revise');">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>	

<div id="tabs">
	<ul>
		<li>
			<a href="#tabs-1">기본정보</a>
		</li>
		<li>
			<a href="#tabs-2">버전정보</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="view-table">
			<colgroup>
				<col width="10%"> <!-- 왼쪽 칼럼 -->
				<col width="400"> <!-- 왼쪽 설명 -->
				<col width="130"> <!-- 오른쪽 칼럼 -->
				<col width="400"> <!-- 오른쪽 설명 -->
				<col width="0"> <!-- 오른쪽 여백 -->
			</colgroup>
			 <tr>
				<th class="lb">도면번호</th>
				<td class="indent5"><%=dto.getNumber()%></td>
				<th>사이즈</th>
				<td class="indent5"><%=dto.getSize_code()%></td>				
			</tr>
			<tr>
				<th class="lb">LOT</th>
				<td class="ident5"><%=dto.getLotNo()%></td>
				<th>UNIT NAME</th>
				<td class="ident5"><%=dto.getUnitName()%></td>
			</tr>
			<tr>
				<th class="lb">도번명</th>
				<td class="ident5"><%=dto.getName()%></td>
				<th>사업부문</th>
				<td class="ident5"><%=dto.getBusinessSector_code()%></td>
			</tr>
			<tr>
				<th class="lb">도면생성회사</th>
				<td class="ident5"><%=dto.getDrawingCompany_code()%></td>
				<th>작성부서구분</th>
				<td class="ident5"><%=dto.getClassificationWritingDepartments_code()%></td>
			</tr>
			<tr>
				<th class="lb">작성문서구분</th>
				<td class="ident5"><%=dto.getWrittenDocuments_code()%></td>
				<th>버전</th>
				<td class="ident5"><%=dto.getVersion()%></td>
			</tr>
			<tr>
				<th class="lb">상태</th>
				<td class="ident5"><%=dto.getState()%></td>
				<th>작성자</th>
				<td class="ident5"><%=dto.getCreator()%></td>
			</tr>	
			<tr>
				<th class="lb">작성일</th>
				<td class="ident5"><%=dto.getCreatedDate_txt()%></td>
				<th>수정자</th>
				<td class="ident5"><%=dto.getModifier()%></td>
			</tr>		
			<tr>
				<th class="lb">수정일</th>
				<td class="ident5"><%=dto.getModifiedDate_txt()%>
			</tr>
			
		</table>
	</div>
	</div>
<div id="tabs-2">
<div id="grid_wrap" style="height: 1250px; border-top: 1px solid #3180c3;"></div>
<script type="text/javascript">

</script>
</div>
<script type="text/javascript">
	function preView() {
		const oid = document.getElementById("oid").value;
		const url = getCallUrl("/aui/thumbnail?oid=" + oid);
		popup(url);
	}
	
	function view() {
		const loid = document.getElementById("loid").value;
		const url = getCallUrl("/numberRule/view?oid=" + loid);
		popup(url, 1400, 700);
	}
	document.addEventListener("DOMContentLoaded", function() {
		$("#tabs").tabs({
			active : 0,
			activate : function(event, ui) {
				var tabId = ui.newPanel.prop("id");
				switch (tabId) {
				case "tabs-1":
					const isCreated50 = AUIGrid.isCreated(myGridID50);
					if (isCreated50) {
						AUIGrid.resize(myGridID50);
					} else {
						createAUIGrid50(columns50);
					}
					break;
				case "tabs-2":
					const isCreated = AUIGrid.isCreated(myGridID);
					if (isCreated) {
						AUIGrid.resize(myGridID);
					} else {
						createAUIGrid50(columns);
					}
					break;

				}
			}
		});

		createAUIGrid(columns);
		createAUIGrid50(columns50);
// 		createAUIGrid100(columns100);
		AUIGrid.resize(myGridID);
		AUIGrid.resize(myGridID50);
// 		AUIGrid.resize(myGridID100);
	})

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID);
		AUIGrid.resize(myGridID50);
// 		AUIGrid.resize(myGridID100);
	});
</script>























