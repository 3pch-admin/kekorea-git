<%@page import="e3ps.common.util.DateUtils"%>
<%@page import="java.util.Date"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.bom.partlist.dto.PartListDTO"%>
<%@page import="java.util.Map"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
PartListDTO dto = (PartListDTO) request.getAttribute("dto");
String tt = dto.getEngType() != null ? dto.getEngType().substring(0, 2) : "";
JSONArray list = (JSONArray) request.getAttribute("list");
JSONArray data = (JSONArray) request.getAttribute("data");
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
String engType = (String)request.getAttribute("engType");
String tProg = (String) request.getAttribute("tProg");
String dd = DateUtils.getDateString(new Date(), "date");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				수배표 수정
			</div>
		</td>
		<td class="right">
			<input type="button" value="수정" title="수정" onclick="modify();">
			<input type="button" value="닫기" title="닫기" class="blue" onclick="self.close();">
		</td>
	</tr>
</table>

<div id="tabs">
	<ul>
		<li>
			<a href="#tabs-1">기본정보</a>
		</li>
		<li>
			<a href="#tabs-2">수배표</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="create-table">
			<colgroup>
				<col width="130">
				<col width="400">
				<col width="130">
				<col width="400">
				<col width="130">
				<col width="400">
			</colgroup>
			<tr>
				<th class="req lb">수배표 제목</th>
				<td class="indent5">
					<input type="text" name="name" id="name" class="width-400" value="<%=dto.getName()%>">
				</td>
				<th>설계구분</th>
				<td class="indent5">
					<select name="engType" id="engType" class="width-200">
						<option value="">선택</option>
						<option value="기계" <%if("기계".equals(tt.trim())) { %> selected="selected" <%} %>>기계</option>
						<option value="전기" <%if("전기".equals(tt.trim())) { %> selected="selected" <%} %>>전기</option>
					</select>
				</td>
				<th class="lb">진행율</th>
				<td class="indent5">
					<input type="number" name="progress" id="progress" class="width-300" value="<%=tProg %>">
				</td>
			</tr>
			<tr>
				<th class="req lb">KEK 작번</th>
				<td colspan="5">
					<jsp:include page="/extcore/jsp/common/project-include.jsp">
						<jsp:param value="<%=dto.getOid() %>" name="oid" />
						<jsp:param value="update" name="mode" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="req lb">내용</th>
				<td class="indent5" colspan="5">
					<textarea name="description" id="description" rows="8"><%=dto.getContent()%></textarea>
				</td>
			</tr>
			<tr>
				<th class="req lb">결재</th>
				<td colspan="5">
					<jsp:include page="/extcore/jsp/common/approval-register.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
						<jsp:param value="update" name="mode" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5" colspan="5">
					<jsp:include page="/extcore/jsp/common/attach-secondary.jsp">
						<jsp:param value="<%=dto.getOid() %>" name="oid" />
					</jsp:include>
				</td>
			</tr>
		</table>
	</div>
	<div id="tabs-2">
		<table class="button-table">
			<tr>
				<td class="left">
					<!--  <input type="button" value="행 추가(이전)" title="행 추가(이전)" class="blue" onclick="addBeforeRow();"> -->
					<!--  <input type="button" value="행 추가(이후)" title="행 추가(이후)" class="orange" onclick="addAfterRow();">-->
					<input type="button" value="행 추가" title="행 추가" class="orange" onclick="addLastRow();">
					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">
				</td>
			</tr>
		</table>
		<div id="grid_wrap" style="height: 780px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID;
			const columns = [ {
				dataField : "check",
				headerText : "체크",
				dataType : "string",
				width : 80,
				editable : false,
			}, {
				dataField : "lotNo",
				headerText : "LOT_NO",
				dataType : "numeric",
				width : 80,
				editRenderer : {
					type : "InputEditRenderer",
					onlyNumeric : true,
					maxlength : 3,
				},
			}, {
				dataField : "unitName",
				headerText : "UNIT NAME",
				dataType : "string",
				width : 120,
				editable : false,
			}, {
				dataField : "partNo",
				headerText : "부품번호",
				dataType : "string",
				width : 130,
			}, {
				dataField : "partName",
				headerText : "부품명",
				dataType : "string",
				width : 200,
				editable : false,
			}, {
				dataField : "standard",
				headerText : "규격",
				dataType : "string",
				width : 250,
				editable : false,
			}, {
				dataField : "maker",
				headerText : "MAKER",
				dataType : "string",
				width : 130,
			}, {
				dataField : "customer",
				headerText : "거래처",
				dataType : "string",
				width : 130,
			}, {
				dataField : "quantity",
				headerText : "수량",
				dataType : "numeric",
				width : 60,
				formatString : "###0",
				editRenderer : {
					type : "InputEditRenderer",
					onlyNumeric : true,
					allowNegative : true
				},
			}, {
				dataField : "unit",
				headerText : "단위",
				dataType : "string",
				width : 80,
				editable : false,
			}, {
				dataField : "price",
				headerText : "단가",
				dataType : "numeric",
				width : 120,
				editable : false,
			}, {
				dataField : "currency",
				headerText : "화폐",
				dataType : "string",
				width : 60,
				editable : false,
			}, {
				dataField : "won",
				headerText : "원화금액",
				dataType : "numeric",
				formatString : "#,##0",
				width : 120,
				editable : false,
			}, {
				dataField : "partListDate_txt",
				headerText : "수배일자",
				dataType : "string",
				width : 100,
				editable : false
			}, {
				dataField : "exchangeRate",
				headerText : "환율",
				dataType : "numeric",
				width : 80,
				formatString : "#,##0.0000",
				editable : false,
			}, {
				dataField : "referDrawing",
				headerText : "참고도면",
				dataType : "string",
				width : 120,
			}, {
				dataField : "classification",
				headerText : "조달구분",
				dataType : "string",
				width : 120,
			}, {
				dataField : "note",
				headerText : "비고",
				dataType : "string",
				width : 250,
			} ];

			function createAUIGrid(columnLayout) {
				const props = {
					headerHeight : 30,
					showRowNumColumn : true,
					showRowCheckColumn : true,
					showStateColumn : true,
					rowNumHeaderText : "번호",
					selectionMode : "multipleCells",
					showDragKnobColumn : true,
					enableDrag : true,
					enableMultipleDrag : true,
					enableDrop : true,
					enableSorting : false,
					$compaEventOnPaste : true,
					editable : true,
					enableRowCheckShiftKey : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					softRemoveRowMode : false,
					contextMenuItems : [ {
						label : "선택된 행 이전 추가",
						callback : contextItemHandler
					}, {
						label : "선택된 행 이후 추가",
						callback : contextItemHandler
					}, {
						label : "_$line"
					}, {
						label : "선택된 행 삭제",
						callback : contextItemHandler
					} ],
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				readyHandler();
				AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
				AUIGrid.bind(myGridID, "beforeRemoveRow", auiBeforeRemoveRow);
				AUIGrid.bind(myGridID, "keyDown", auiKeyDownHandler);
				AUIGrid.setGridData(myGridID, <%=list%>);
			}
			
			
			// enter 키 행 추가
			function auiKeyDownHandler(event) {
				if (event.keyCode == 13) { // 엔터 키
					var selectedItems = AUIGrid.getSelectedItems(event.pid);
					var rowIndex = selectedItems[0].rowIndex;
					if (rowIndex === AUIGrid.getRowCount(event.pid) - 1) { // 마지막 행인지 여부 
						const item = {
								partListDate : new Date(),
								lotNo : 0,
								quantity : 1,
								price : 0,
								exchangeRate : 0,
								won : 0
							}
							AUIGrid.addRow(myGridID, item, "last");
					
						//AUIGrid.addRow(event.pid, {}); // 행 추가
						return false; // 엔터 키의 기본 행위 안함.
					}
				}
				return true; // 기본 행위 유지
			}

			function auiCellEditEndHandler(event) {
				const rowIndex = event.rowIndex;
				const dataField = event.dataField;
				const item = event.item;
				const partNo = item.partNo;
				const lotNo = item.lotNo;
				const quantity = item.quantity;
				if (dataField === "lotNo") {
					const url = getCallUrl("/erp/getUnitName?lotNo=" + lotNo + "&callLoc=수배표");
					call(url, null, function(data) {
						if (data.result) {
							const newItem = {
								unitName : data.unitName,
								partListDate : new Date(),
								sort : rowIndex
							};
							AUIGrid.updateRow(myGridID, newItem, rowIndex);
						}
					}, "GET");
				}

				if (dataField === "partNo") {
					/*
					const url = getCallUrl("/erp/validate?partNo=" + partNo);
					call(url, null, function(data) {
						if (data.result) {
							const newItem = {
								check : data.check,
								partListDate : new Date(),
								sort : rowIndex
							};
							AUIGrid.updateRow(myGridID, newItem, rowIndex);
						}
					}, "GET");
					*/
					const url = getCallUrl("/erp/getErpItemByPartNo?partNo=" + partNo + "&quantity=1&callLoc=수배표");
					call(url, null, function(data) {
						if (data.result) {
							const newItem = {
								check : data.check,
								unit : data.unit,
								exchangeRate : data.exchangeRate,
								price : data.price,
								maker : data.maker,
								customer : data.customer,
								currency : data.currency,
								won : data.won,
								partName : data.partName,
								standard : data.standard,
								quantity : data.quantity,
								partListDate : "<%=dd%>",
								sort : rowIndex,
							};
							AUIGrid.updateRow(myGridID, newItem, rowIndex);
						}
					}, "GET");
				}


				
				if (dataField === "quantity") {
					// 값이 있을 경우만
					const url = getCallUrl("/erp/getErpItemByPartNoAndQuantity?partNo=" + partNo + "&quantity=" + quantity + "&callLoc=수배표");
					call(url, null, function(data) {
						if (data.result) {
							const newItem = {
								unit : data.unit,
								exchangeRate : data.exchangeRate,
								price : data.price,
								maker : data.maker,
								customer : data.customer,
								currency : data.currency,
								won : data.won,
								partName : data.partName,
								standard : data.standard,
								partListDate : new Date(),
								sort : rowIndex,
							};
							AUIGrid.updateRow(myGridID, newItem, rowIndex);
						}
					}, "GET");
				}
			}

			function contextItemHandler(event) {
				const item = {
					partListDate : new Date(),
					lotNo : 0,
					quantity : 0,
					price : 0,
					exchangeRate : 0,
					won : 0
				}
				switch (event.contextIndex) {
				case 0:
					AUIGrid.addRow(myGridID, item, "selectionUp");
					break;
				case 1:
					AUIGrid.addRow(myGridID, item, "selectionDown");
					break;
				case 3:
					const selectedItems = AUIGrid.getSelectedItems(myGridID);
					const rows = AUIGrid.getRowCount(myGridID);
					if (rows === 1) {
						alert("최 소 하나의 행이 존재해야합니다.");
						return false;
					}
					for (let i = selectedItems.length - 1; i >= 0; i--) {
						const rowIndex = selectedItems[i].rowIndex;
						AUIGrid.removeRow(myGridID, rowIndex);
					}
					break;
				}
			}

			function auiBeforeRemoveRow(event) {
				const rows = AUIGrid.getRowCount(myGridID);
				if (rows === 1) {
					alert("최소 하나의 행이 존재해야합니다.");
					return false;
				}
				return true;
			}

			function deleteRow() {
				const checked = AUIGrid.getCheckedRowItems(myGridID);
				const rows = AUIGrid.getRowCount(myGridID);
				if (rows === 1) {
					alert("최 소 하나의 행이 존재해야합니다.");
					return false;
				}

				if (checked.length === 0) {
					alert("삭제할 행을 선택하세요.");
					return false;
				}
				for (let i = checked.length - 1; i >= 0; i--) {
					const rowIndex = checked[i].rowIndex;
					AUIGrid.removeRow(myGridID, rowIndex);
				}
			};

			function readyHandler() {
				const item = {
					partListDate : new Date(),
					lotNo : 0,
					quantity : 0,
					price : 0,
					exchangeRate : 0,
					won : 0
				}
				AUIGrid.addRow(myGridID, item, "last");
			}

			function addBeforeRow() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
				if (checkedItems.length === 0) {
					alert("추가하려는 행의 기준이 되는 행을 선택하세요.");
					return false;
				}
				if (checkedItems.length > 1) {
					alert("하나의 행만 선택하세요.");
					return false;
				}
				const rowIndex = checkedItems[0].rowIndex;
				const item = {
					partListDate : new Date(),
					lotNo : 0,
					quantity : 0,
					price : 0,
					exchangeRate : 0,
					won : 0
				}
				AUIGrid.addRow(myGridID, item, rowIndex);
			}

			function addAfterRow() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
				if (checkedItems.length === 0) {
					alert("추가하려는 행의 기준이 되는 행을 선택하세요.");
					return false;
				}
				if (checkedItems.length > 1) {
					alert("하나의 행만 선택하세요.");
					return false;
				}
				const rowIndex = checkedItems[0].rowIndex;
				const item = {
					partListDate : new Date(),
					lotNo : 0,
					quantity : 0,
					price : 0,
					exchangeRate : 0,
					won : 0
				}
				AUIGrid.addRow(myGridID, item, rowIndex + 1);
			}
			function addLastRow() {
				const item = {
					partListDate : new Date(),
					lotNo : 0,
					quantity : 0,
					price : 0,
					exchangeRate : 0,
					won : 0
				}
				AUIGrid.addRow(myGridID, item, "last");
			}
		</script>
	</div>
</div>

<script type="text/javascript">
	function modify() {
		const params = new Object();
		const url = getCallUrl("/partlist/modify");
		const oid = document.getElementById("oid").value;
		const addRows = AUIGrid.getGridData(myGridID);
		const addRows9 = AUIGrid.getGridData(myGridID9);
		const addRows8 = AUIGrid.getGridData(myGridID8);
		const name = document.getElementById("name");
		const engType = document.getElementById("engType").value;
		const description = document.getElementById("description");
		const progress = document.getElementById("progress").value;
		if (isNull(name.value)) {
			alert("수배표 제목을 입력하세요.");
			name.focus();
			return false;
		}

		if (isNull(engType)) {
			alert("설계구분을 선택하세요.");
			return false;
		}

		if (isNull(description.value)) {
			alert("내용을 입력하세요.");
			description.focus();
			return false;
		}

		if (addRows9.length === 0) {
			alert("최소 하나 이상의 작번을 추가하세요.");
			insert9();
			return false;
		}

		for (let i = 0; i < addRows.length; i++) {
			const item = addRows[i];
			const rowIndex = AUIGrid.rowIdToIndex(myGridID, item._$uid);
			if (isNull(item.partNo)) {
				AUIGrid.showToastMessage(myGridID, rowIndex, 3, "부품번호를 입력하세요.");
				return false;
			}

			if (item.check === "NG") {
				AUIGrid.showToastMessage(myGridID, rowIndex, 0, "ERP에 등록된 부품번호가 아닙니다.");
				return false;
			}

			if (isNull(item.lotNo)) {
				AUIGrid.showToastMessage(myGridID, rowIndex, 1, "LOT NO를 입력하세요.");
				return false;
			}

			if (item.lotNo === 0) {
				AUIGrid.showToastMessage(myGridID, rowIndex, 1, "LOT NO를 입력하세요.");
				return false;
			}

			if (isNull(item.quantity)) {
				AUIGrid.showToastMessage(myGridID, rowIndex, 8, "수량을 입력하세요.");
				return false;
			}

			if (item.quantity === 0) {
				AUIGrid.showToastMessage(myGridID, rowIndex, 8, "수량은 0을 입력 할 수 없습니다.");
				return false;
			}
		}

		if (addRows8.length === 0) {
			alert("결재선을 지정하세요.");
			_register();
			return false;
		}

		addRows.sort(function(a, b) {
			return a.sort - b.sort;
		});
		
		toRegister(params, addRows8);
		const app = params.approvalRows;
		
		for(let i=0; i<app.length; i++) {
			const a = app[i];
			if(i === (app.length -1)) {
				const id = app[i].id;
				if(id !== "19940009" && id !== "20050112" && id !== "skyun" && id !== "20060140") {
// 				if(id !== "20050112" && id !== "skyun" && id !== "20060140") {
					alert("수배표의 최종결재자는 반드시 팀장님으로 지정 되어야 합니다.");
					return false;
				}
			}
		}

		if (!confirm("수정 하시겠습니까?")) {
			return false;
		}
		params.oid = oid;
		params.addRows = addRows;
		params.addRows9 = addRows9;
		params.name = name.value;
		params.engType = engType;
		params.description = description.value;
		params.progress = Number(progress);
		params.secondarys = toArray("secondarys");
// 		toRegister(params, addRows8);
		console.log(params);
		openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
// 				opener.loadGridData();
				self.close();
			}
		})
	}

	document.addEventListener("DOMContentLoaded", function() {
		document.getElementById("name").focus();
		$("#tabs").tabs({
			active : 0,
			activate : function(event, ui) {
				var tabId = ui.newPanel.prop("id");
				switch (tabId) {
				case "tabs-1":
					const isCreated9 = AUIGrid.isCreated(myGridID9);
					if (isCreated9) {
						gridResize9();
					} else {
						createAUIGrid9(columns9);
					}
					const isCreated8 = AUIGrid.isCreated(myGridID8);
					if (isCreated8) {
						gridResize8();
					} else {
						createAUIGrid8(columns8);
					}
					selectbox("engType");
					$("#engType").bindSelectSetValue("<%=tt %>");
					break;
				case "tabs-2":
					const isCreated = AUIGrid.isCreated(myGridID);
					if (isCreated) {
						gridResize();
					} else {
						createAUIGrid(columns);
					}
					break;
				}
			}
		});
		selectbox("engType");
		$("#engType").bindSelectSetValue("<%=tt%>");
		$("#engType").bindSelectDisabled(true);
		createAUIGrid9(columns9);
		createAUIGrid8(columns8);
		createAUIGrid(columns);
		gridResize9();
		gridResize8();
		gridResize();
	});

	window.addEventListener("resize", function() {
		gridResize9();
		gridResize8();
		gridResize();
	});
	function gridResize9(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID9, ww-200, 150);
	}
	function gridResize8(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID8, ww-200, 200);
	}
	function gridResize(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID, ww-70, hh-200);
	}
</script>