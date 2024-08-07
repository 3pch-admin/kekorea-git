<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) request.getAttribute("list");
%>
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				템플릿 등록
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
		<th class="req lb">템플릿 명</th>
		<td class="indent5">
			<input type="text" name="name" id="name" class="width-500" tabindex="1">
		</td>
	</tr>
	<tr>
		<th class="lb">참조 템플릿</th>
		<td class="indent5">
			<select name="reference" id="reference" class="width-400"  tabindex="2">
				<option value="">선택</option>
				<%
				for (HashMap<String, String> map : list) {
				%>
				<option value="<%=map.get("key")%>"><%=map.get("value")%></option>
				<%
				}
				%>
			</select>
		</td>
	</tr>
	<tr>
		<th class="lb">설명</th>
		<td class="indent5">
			<textarea name="description" id="description" rows="8" tabindex="3"></textarea>
		</td>
	</tr>
</table>
<script type="text/javascript">
	function create() {

		const params = new Object();
		const description = document.getElementById("description").value;
		const name = document.getElementById("name");
		const reference = document.getElementById("reference").value;
		const url = getCallUrl("/template/create");

		if (isNull(name.value)) {
			alert("템플릿 명을 입력하세요.");
			name.focus();
			return false;
		}

		if (!confirm("등록 하시겠습니까?")) {
			return false;
		}

		params.name = name.value;
		params.description = description;
		params.reference = reference;
		openLayer();
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

	document.addEventListener("DOMContentLoaded", function() {
		toFocus("name");
		selectbox("reference");
	})
</script>