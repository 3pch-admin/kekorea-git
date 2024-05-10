<%@page import="e3ps.doc.request.dto.RequestDocumentDTO"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.common.util.StringUtils"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%
RequestDocumentDTO dto = (RequestDocumentDTO) request.getAttribute("dto");
String template = (String)request.getAttribute("template");
ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) request.getAttribute("list");
JSONArray elecs = (JSONArray) request.getAttribute("elecs");
JSONArray softs = (JSONArray) request.getAttribute("softs");
JSONArray machines = (JSONArray) request.getAttribute("machines");
JSONArray maks = (JSONArray) request.getAttribute("maks");
JSONArray installs = (JSONArray) request.getAttribute("installs");
JSONArray customers = (JSONArray) request.getAttribute("customers");
JSONArray details = (JSONArray) request.getAttribute("details");
JSONArray projectTypes = (JSONArray) request.getAttribute("projectTypes");
JSONArray projects = (JSONArray) request.getAttribute("projects");
System.out.println(projects);
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>  
<%-- <input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>"> --%>
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				의뢰서 수정
			</div>
		</td>
		<td class="right">
			<input type="button" value="수정" title="수정" onclick="modify();">
			<input type="button" value="닫기" title="닫기" class="blue" onclick="self.close();">
		</td>
	</tr>
</table>
<table class="create-table">
	<colgroup>
		<col width="150">
		<col width="*">
	</colgroup>
	<tr>
		<th class="req lb">의뢰서 제목</th>
		<td class="indent5">
			<input type="text" name="name" id="name" class="width-300" value="<%=dto.getName()%>">
		</td>
	</tr>
	<tr>
		<th class="lb">내용</th>
		<td class="indent5">
			<textarea name="description" id="description" rows="6"><%=dto.getReqDescription() != null ? dto.getReqDescription() : ""%></textarea>
		</td>
	</tr>
	<tr>
		<th class="req lb">KEK 작번</th>
		<td >
			<jsp:include page="/extcore/jsp/common/project-include.jsp">
				<jsp:param value="<%=dto.getOid()%>" name="oid" />
				<jsp:param value="true" name="multi" />
				<jsp:param value="update" name="mode" />
			</jsp:include>
			
		</td>
	</tr>
	<tr>
		<th class="lb">첨부파일</th>
		<td class="indent5">
			<jsp:include page="/extcore/jsp/common/attach-primary.jsp">
				<jsp:param value="<%=dto.getOid() %>" name="oid" />
				<jsp:param value="update" name="mode" />
			</jsp:include></td>
	</tr>
	<tr>
		<th class="req lb">결재</th>
		<td >
			<jsp:include page="/extcore/jsp/common/approval-register.jsp">
				<jsp:param value="<%=dto.getOid() %>" name="oid" />
		<jsp:param value="update" name="mode" />
			</jsp:include>
		</td>
	</tr>
</table>
		
<script type="text/javascript">

	function modify() {
		const params = new Object();
		const url = getCallUrl("/requestDocument/modify");
		
		const oid = document.getElementById("oid").value;
		const name = document.getElementById("name");
		const description = document.getElementById("description").value;
		const template = document.getElementById("template");
		const addRows9 = AUIGrid.getGridData(myGridID9);
		const addRows8 = AUIGrid.getGridData(myGridID8);
		const primarys = toArray("primarys");
		
		if (isNull(name.value)) {
			alert("의뢰서 제목을 입력하세요.");
			name.focus();
			return false;
		}

		if (addRows9.length === 0) {
			alert("최소 하나이상의 작번을 추가하세요.");
			return false;
		}

		if (addRows8.length === 0) {
			alert("결재선을 지정하세요.");
			_register();
			return false;
		}
		

		if (!confirm("수정 하시겠습니까?")) {
			return false;
		}
		
		
		params.description = description;
		params.oid = oid;
		params.name = name.value;
		params.addRows9 = addRows9;
		params.primarys = primarys;
		toRegister(params, addRows8);
		
		openLayer();
		console.log(url);
		console.log(params);
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
				opener.loadGridData();
				self.close();
			} else {
				closeLayer();
			}
		})
	}
</script>
<script type="text/javascript">
	document.addEventListener("DOMContentLoaded", function() {
		createAUIGrid8(columns8);
		AUIGrid.resize(myGridID8);
		createAUIGrid9(columns9);
		AUIGrid.resize(myGridID9);
		document.getElementById("name").focus();
	})

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID8);
		AUIGrid.resize(myGridID9);
	});
</script>