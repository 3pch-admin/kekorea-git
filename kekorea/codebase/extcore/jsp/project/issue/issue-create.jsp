<%@page import="net.sf.json.JSONArray"%>
<%@page import="java.util.Map"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
JSONArray list = (JSONArray) request.getAttribute("list");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				특이사항 등록
			</div>
		</td>
		<td class="right">
			<input type="button" value="등록" title="등록" class="blue" onclick="create();">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>

<table class="create-table">
	<colgroup>
		<col width="130">
		<col width="*">
	</colgroup>
	<tr>
		<th class="req lb">특이사항 제목</th>
		<td class="indent5">
			<input type="text" name="name" id="name" class="width-500">
		</td>
	</tr>
	<tr>
		<th class="req lb">KEK 작번</th>
		<td>
			<jsp:include page="/extcore/jsp/common/project-include.jsp">
				<jsp:param value="" name="oid" />
				<jsp:param value="create" name="mode" />
			</jsp:include>
		</td>
	</tr>
	<tr>
		<th class="req lb">내용</th>
		<td class="indent5">
			<textarea name="description" id="description" rows="10"></textarea>
		</td>
	</tr>
	<tr>
		<th class="lb">첨부파일</th>
		<td class="indent5">
			<jsp:include page="/extcore/jsp/common/attach-secondary.jsp">
				<jsp:param value="" name="oid" />
			</jsp:include>
		</td>
	</tr>
</table>
<script type="text/javascript">
	function create() {
		const params = new Object();
		const url = getCallUrl("/issue/create");
		const name = document.getElementById("name");
		const description = document.getElementById("description");
		const addRows9 = AUIGrid.getAddedRowItems(myGridID9);

		params.name = name.value;
		params.description = description.value;
		params.addRows9 = addRows9;
		params.secondarys = toArray("secondarys");
		if (isNull(params.name)) {
			alert("특이사항 제목 값은 공백을 입력 할 수 없습니다.");
			name.focus();
			return false;
		}
		if (isNull(params.description)) {
			alert("내용 값은 공백을 입력 할 수 없습니다.");
			description.focus();
			return false;
		}
		if (!confirm("등록 하시겠습니까?")) {
			return false;
		}
		openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
				opener.document.location.reload();
				self.close();
			} else {
				closeLayer();
			}
		})
	}

	function auiBeforeRemoveRow(event) {
		const item = event.items[0];
		const oid = document.getElementById("oid").value;
		if (item.oid === oid) {
			alert("선택된 작번은 제거 할 수 없습니다.");
			return false;
		}
		return true;
	}

	document.addEventListener("DOMContentLoaded", function() {
		createAUIGrid9(columns9);
		AUIGrid.resize(myGridID9);
		AUIGrid.addRow(myGridID9, <%=list%>);
		AUIGrid.bind(myGridID9, "beforeRemoveRow", auiBeforeRemoveRow);
		document.getElementById("name").focus();
	})

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID9);
	});
</script>