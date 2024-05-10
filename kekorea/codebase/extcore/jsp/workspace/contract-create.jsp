<%@page import="e3ps.project.output.service.OutputHelper"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.common.util.StringUtils"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%><!DOCTYPE html>

<%
String toid = (String) request.getAttribute("toid");
String poid = (String) request.getAttribute("poid");
String location = (String) request.getAttribute("location");
JSONArray list = (JSONArray) request.getAttribute("list");
String number = (String) request.getAttribute("number");
String tProg = (String) request.getAttribute("tProg");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<input type="hidden" name="toid" id="toid" value="<%=toid != null ? toid : ""%>">
<input type="hidden" name="poid" id="poid" value="<%=poid != null ? poid : ""%>">
<input type="hidden" name="location" id="location" value="<%=location != null ? location : OutputHelper.OUTPUT_NEW_ROOT%>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				도면결재 등록
			</div>
		</td>
		<td class="right">
			<input type="button" value="등록" title="등록" onclick="registerLine();" style="background-color: navy;">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>

<table class="create-table">
	<colgroup>
		<col width="150">
		<col width="*">
	</colgroup>
	<tr>
		<th class="req lb">결재 제목</th>
		<td class="indent5">
			<input type="text" name="name" id="name" class="width-700">
		</td>
	</tr>
	<tr>
		<th class="req lb">결재 의견</th>
		<td class="indent5">
			<textarea id="description" name="description" rows="5"></textarea>
		</td>
	</tr>
	<tr>
		<th class="req lb">도번</th>
		<td colspan="3">
			<div class="include">
				<div id="grid_wrap11" style="height: 200px; border-top: 1px solid #3180c3; margin: 5px;"></div>
				<script type="text/javascript">
					let myGridID11;
					const columns11 = [ {
						dataField : "number",
						headerText : "도면번호",
						dataType : "string",
						width : 100,
						editable : false,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "size_txt",
						headerText : "사이즈",
						dataType : "string",
						width : 80,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "lotNo",
						headerText : "LOT",
						dataType : "numeric",
						width : 80,
						formatString : "###0",
						filter : {
							showIcon : true,
							inline : true,
							displayFormatValues : true
						},
					}, {
						dataField : "unitName",
						headerText : "UNIT NAME",
						dataType : "string",
						width : 200,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "name",
						headerText : "도번명",
						dataType : "string",
						width : 250,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "businessSector_txt",
						headerText : "사업부문",
						dataType : "string",
						width : 200,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "classificationWritingDepartments_txt",
						headerText : "작성부서구분",
						dataType : "string",
						width : 150,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "writtenDocuments_txt",
						headerText : "작성문서구분",
						dataType : "string",
						width : 150,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "version",
						headerText : "버전",
						dataType : "string",
						width : 100,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "state",
						headerText : "상태",
						dataType : "string",
						width : 80,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "creator",
						headerText : "작성자",
						dataType : "string",
						width : 100,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "createdDate_txt",
						headerText : "작성일",
						dataType : "string",
						width : 100,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "modifier",
						headerText : "수정자",
						dataType : "string",
						width : 100,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "modifiedDate_txt",
						headerText : "수정일",
						dataType : "string",
						width : 100,
						filter : {
							showIcon : true,
							inline : true
						},
					}, {
						dataField : "oid",
						visible : false,
						dataType : "string"
					}, {
						dataField : "eoid",
						visible : false,
						dataType : "string"
					} ]

					function createAUIGrid11(columnLayout) {
						const props = {
							headerHeight : 30,
							showRowNumColumn : true,
							rowNumHeaderText : "번호",
							showAutoNoDataMessage : false,
							enableSorting : false,
						}
						myGridID11 = AUIGrid.create("#grid_wrap11", columnLayout, props);
					}
				</script>
			</div>
		</td>
	</tr>
	<tr>
		<th class="req lb">결재 도면</th>
		<td>
			<div class="include">
				<input type="button" value="도면 추가" title="도면 추가" class="" onclick="insert();">
				<input type="button" value="도면 삭제" title="도면 삭제" class="red" onclick="deleteRow();">
				<div id="grid_wrap" style="height: 200px; border-top: 1px solid #3180c3; margin: 3px 5px 3px 5px;"></div>
				<script type="text/javascript">
					let myGridID;
					const columns = [ {
						dataField : "name",
						headerText : "NAME",
						dataType : "string",
					}, {
						dataField : "dwg_no",
						headerText : "DWG NO",
						dataType : "string",
						width : 250
					}, {
						dataField : "name_of_parts",
						headerText : "NAME_OF_PARTS",
						dataType : "string",
						width : 250
					}, {
						dataField : "version",
						headerText : "버전",
						dataType : "string",
						width : 100,
					}, {
						dataField : "state",
						headerText : "상태",
						dataType : "string",
						width : 100
					}, {
						dataField : "creator",
						headerText : "작성자",
						dataType : "date",
						width : 100
					}, {
						dataField : "createdDate_txt",
						headerText : "작성일",
						dataType : "string",
						width : 100
					}, {
						dataField : "oid",
						visible : false,
						dataType : "string"
					} ]

					function createAUIGrid(columnLayout) {
						const props = {
							headerHeight : 30,
							showRowNumColumn : true,
							rowNumHeaderText : "번호",
							showStateColumn : true,
							showRowCheckColumn : true,
							enableSorting : false,
							selectionMode : "multipleCells"
						}
						myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
						AUIGrid.bind(myGridID, "beforeRemoveRow", auiBeforeRemoveRow);
					}

					function insert() {
						const url = getCallUrl("/epm/popup?method=append&multi=false");
						popup(url, 1600, 700);
					}

					function append(arr, callBack) {

						const params = new Object();
						const url = getCallUrl("/epm/append");
						params.arr = arr;
						openLayer();
						call(url, params, function(data) {
							const list1 = data.list1;
							const list2 = data.list2;
							if (data.result) {
								for (let i = 0; i < list1.length; i++) {
									const isUnique = AUIGrid.isUniqueValue(myGridID, "oid", list1[i].oid);
									if (isUnique) {
										AUIGrid.addRow(myGridID, list1[i]);
									}
								}

								for (let i = 0; i < list2.length; i++) {
									const isUnique = AUIGrid.isUniqueValue(myGridID11, "oid", list2[i].oid);
									if (isUnique) {
										AUIGrid.addRow(myGridID11, list2[i]);
									}
								}
							} else {
								alert(data.msg);
							}
							closeLayer();
						})
						callBack(true);
					}

					function auiBeforeRemoveRow(event) {
						const items = event.items;
						for (let i = 0; i < items.length; i++) {
							const item = items[i];
							const rowIndex = AUIGrid.getRowIndexesByValue(myGridID11, "eoid", item.oid);
							AUIGrid.removeRow(myGridID11, rowIndex);
						}
					}

					// 행 삭제
					function deleteRow() {
						const checked = AUIGrid.getCheckedRowItems(myGridID);
						for (let i = checked.length - 1; i >= 0; i--) {
							const rowIndex = checked[i].rowIndex;
							AUIGrid.removeRow(myGridID, rowIndex);
						}
					}
				</script>
			</div>
		</td>
	</tr>
	<tr>
		<th class="req lb">결재</th>
		<td>
			<jsp:include page="/extcore/jsp/common/approval-register.jsp">
				<jsp:param value="" name="oid" />
				<jsp:param value="create" name="mode" />
			</jsp:include>
		</td>
	</tr>
</table>
<script type="text/javascript">
function registerLine() {
	const url = getCallUrl("/epm/register");
	const params = new Object();
	const addRows = AUIGrid.getAddedRowItems(myGridID); // 문서
	const addRows11 = AUIGrid.getAddedRowItems(myGridID11); // 문서
	const addRows8 = AUIGrid.getAddedRowItems(myGridID8); // 결재
	const name = document.getElementById("name");
	const description = document.getElementById("description").value;

	if (isNull(name.value)) {
		alert("결재 제목을 입력하세요.");
		name.focus();
		return false;
	}

	if (addRows8.length === 0) {
		alert("결재선을 지정하세요.");
		_register();
		return false;
	}

	if (addRows.length === 0) {
		alert("결재할 도면을 추가하세요.");
		insert();
		return false;
	}

	if (!confirm("도면결재를 등록하시겠습니까?")) {
		return false;
	}
	params.name = name.value;
	params.description = description;
	params.addRows = addRows;
	params.addRows11 = addRows11;
	toRegister(params, addRows8);
	openLayer();
	call(url, params, function(data) {
		alert(data.msg);
		if (data.result) {
			document.location.href = getCallUrl("/workspace/approval");
			opener.loadGridData();
			self.close();
		} else {
			closeLayer();
		}
	})
}

document.addEventListener("DOMContentLoaded", function() {
	document.getElementById("name").focus();
	createAUIGrid(columns);
	createAUIGrid11(columns11);
	createAUIGrid8(columns8);
	gridResize();
	gridResize8()
	gridResize11()
});

window.addEventListener("resize", function() {
	gridResize();
	gridResize8()
	gridResize11()
});

function gridResize(){
	const ww = window.innerWidth;	//1654
	const hh = window.innerHeight;	//834
	//4row 15, 200
	//3row 15, 160
	//2row 15, 140
	//popup 15, 50
	AUIGrid.resize(myGridID, ww-150, 200);
}
function gridResize8(){
	const ww = window.innerWidth;	//1654
	const hh = window.innerHeight;	//834
	//4row 15, 200
	//3row 15, 160
	//2row 15, 140
	//popup 15, 50
	AUIGrid.resize(myGridID8, ww-150, 200);
}
function gridResize11(){
	const ww = window.innerWidth;	//1654
	const hh = window.innerHeight;	//834
	//4row 15, 200
	//3row 15, 160
	//2row 15, 140
	//popup 15, 50
	AUIGrid.resize(myGridID11, ww-150, 200);
}
</script>
