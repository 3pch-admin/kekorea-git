<%@page import="e3ps.part.service.PartHelper"%>
<%@page import="e3ps.part.dto.PartDTO"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<% 
PartDTO dto = (PartDTO) request.getAttribute("dto");
ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) request.getAttribute("list");
%>    
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<input type="hidden" name="location" id="location" value="<%=dto.getLocation() %>">
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid() %>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				부품 수정
			</div>
		</td>
		<td class="right">
			<input type="button" value="수정" title="수정" onclick="modify();">
			<input type="button" value="닫기" title="닫기" style="background-color: navy;" onclick="self.close();">
		</td>
	</tr>
</table>
<table class="create-table">
	<colgroup>
		<col width="150">
		<col width="*">
<!-- 		<col width="150"> -->
<!-- 		<col width="600"> -->
	</colgroup>	
<!-- 	<tr> -->
<!-- 		<th class="req lb">저장위치</th> -->
<!-- 		<td class="indent5" colspan="3"> -->
<%-- 			<span id="loc"><%=dto.getLocation() %></span> --%>
<!-- 			<input type="button" value="폴더선택" title="폴더선택" class="blue" onclick="folder();"> -->
<!-- 		</td> -->
<!-- 	</tr> -->
<!-- 	<tr> -->
<!-- 		<th class="req lb">파일 이름</th> -->
<!-- 		<td class="indent5"> -->
<%-- 			<input type="text" name="name" id="name" class="width-400" value="<%=dto.getName() %>"> --%>
<!-- 		</td> -->
<!-- 		<th class="req">상태</th> -->
<!-- 		<td class="indent5"> -->
<!-- 			<select name="state" id="state" class="width-200"> -->
<!-- 				<option value="">선택</option> -->
<!-- 				<option value="INWORK">작업 중</option> -->
<!-- 				<option value="UNDERAPPROVAL">승인 중</option> -->
<!-- 				<option value="APPROVED">승인됨</option> -->
<!-- 				<option value="RETURN">반려됨</option> -->
<!-- 			</select> -->
<!-- 		</td> -->
<!-- 	</tr> -->
	<tr>
		<th class="req lb">첨부파일</th>
		<td class="indent5">
			<jsp:include page="/extcore/jsp/common/attach-primary.jsp">
				<jsp:param value="<%=dto.getOid() %>" name="oid" />
			</jsp:include>
		</td>
	</tr>
	
</table>
<script type="text/javascript">
	function folder() {
		const location = decodeURIComponent("/Default/도면");
		const url = getCallUrl("/folder?location=" + location + "&container=product&method=setNumber&multi=false");
		popup(url, 500, 600);
	}
	
	function setNumber(item) {
		const url = getCallUrl("/doc/setNumber");
		const params = new Object();
		params.loc = item.location;
		call(url, params, function(data) {
			document.getElementById("loc").innerHTML = item.location;
			document.getElementById("location").value = item.location;
			document.getElementById("number").value = data.number;
		})
	}
	
	function modify(){
		const params = new Object();
		const url = getCallUrl("/part/modify");
// 		const name = document.getElementById("name");
// 		const state = document.getElementById("state");
		const primarys = toArray("primarys");
		const oid = document.getElementById("oid").value;
// 		if(location === "/Default/도면") {
// 			alert("문서 저장위치를 선택하세요.");
// 			folder();
// 			return false;
// 		}
		
// 		if (isNull(name.value)) {
// 			alert("문서제목을 입력하세요.");
// 			name.focus();
// 			return false;
// 		}
// 		if(primarys.length === 0) {
// 			alert("첨부파일을 선택하세요.");
// 			return false;
// 		}

		if (!confirm("수정 하시겠습니까?")) {
			return false;
		}
		
// 		params.name = name.value;
// 		params.state = state.value;
		params.primarys = toArray("primarys");
		params.oid = oid;
		openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
 				opener.loadGridData();
				self.close();
			} else {
				closeLayer();
			}
		});
	};
	
	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID8);
		AUIGrid.resize(myGridID);
	});
	
	document.addEventListener("DOMContentLoaded", function() {
// 		selectbox("state");
	});
</script>
