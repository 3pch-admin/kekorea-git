<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
</head>
<body>
	<form>
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
		<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
		<input type="hidden" name="sessionid" id="sessionid"><input type="hidden" name="lastNum" id="lastNum">
		<input type="hidden" name="curPage" id="curPage">

		<table class="button-table">
			<tr>
				
				<td class="right">
				<!-- 	<input type="button" value="행 추가(이전)" title="행 추가(이전)" class="" onclick="addBeforeRow();"> -->
				<!--	<input type="button" value="행 추가(이후)" title="행 추가(이후)" onclick="addAfterRow();" style="background-color: orange;"> -->
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
					<input type="button" value="행 추가" title="행 추가" class="" onclick="addLastRow();">
					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">
					<input type="button" value="저장" title="저장" onclick="save('')" style="background-color: navy;">
				</td>
			</tr>
		</table>

		<div id="grid_wrap" style="height: 350px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID;
			const list = [ "KRW", "JPY" ];
			const columns = [ {
				dataField : "dwg_check",
				headerText : "체크(DWG_NO)",
				dataType : "string",
				width : 120,
				editable : false,
			}, {
				dataField : "ycode_check",
				headerText : "체크(YCODE)",
				dataType : "string",
				width : 120,
				editable : false,
			}, {
				dataField : "number",
				headerText : "품번",
				dataType : "string",
				width : 150,
			}, {
				dataField : "name",
				headerText : "품명",
				dataType : "string",
				width : 250,
			}, {
				dataField : "spec",
				headerText : "규격",
				dataType : "string",
			}, {
				dataField : "maker",
				headerText : "메이커",
				dataType : "string",
				width : 150,
			}, {
				dataField : "customer",
				headerText : "기본구매처",
				dataType : "string",
				width : 150,
			}, {
				dataField : "unit",
				headerText : "기준단위",
				dataType : "string",
				width : 100,
			}, {
				dataField : "price",
				headerText : "단가",
				dataType : "numeric",
				width : 150,
				formatString : "#,###",
				editRenderer : {
					type : "InputEditRenderer",
					onlyNumeric : true,
				},
			}, {
				dataField : "currency",
				headerText : "통화",
				dataType : "string",
				width : 100,
				renderer : {
					type : "IconRenderer",
					iconWidth : 16,
					iconHeight : 16,
					iconPosition : "aisleRight",
					iconTableRef : {
						"default" : "/Windchill/extcore/component/AUIGrid/images/list-icon.png"
					},
					onClick : function(event) {
						AUIGrid.openInputer(event.pid);
					}
				},
				editRenderer : {
					type : "ComboBoxRenderer",
					autoCompleteMode : true,
					autoEasyMode : true,
					matchFromFirst : false,
					showEditorBtnOver : false,
					list : list,
					validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
						let isValid = false;
						for (let i = 0, len = list.length; i < len; i++) {
							if (list[i] == newValue) {
								isValid = true;
								break;
							}
						}
						return {
							"validate" : isValid,
							"message" : "리스트에 있는 값만 선택(입력) 가능합니다."
						};
					}
				},
			} ]

			function createAUIGrid(columnLayout) {
				const props = {
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showStateColumn : true,
					showRowCheckColumn : true,
					selectionMode : "multipleCells",
					editable : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					showDragKnobColumn : true,
					enableDrag : true,
					enableMultipleDrag : true,
					enableDrop : true,
					$compaEventOnPaste : true,
					enableRowCheckShiftKey : true,
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
				}

				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				readyHandler();
				AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
				AUIGrid.bind(myGridID, "keyDown", auiKeyDownHandler);
			}
			
			function deleteRow() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
				const sessionId = document.getElementById("sessionId").value;
				for (let i = checkedItems.length - 1; i >= 0; i--) {
					const item = checkedItems[i].item;
					const rowIndex = checkedItems[i].rowIndex;
					if ((!isNull(item.creatorId) && !checker(sessionId, item.creatorId))) {
						//if ((!isNull(item.creatorId) && !checker(sessionId, item.creatorId)) 
						//|| (!isNull(item.modifierId) && !checker(sessionId, item.modifierId))){
						alert(rowIndex + "행 데이터의 작성자 혹은 수정자가 아닙니다.");
						return false;
					}
					AUIGrid.removeRow(myGridID, rowIndex);
				}
			}
		</script>

		<table class="button-table">
			<tr>
				<td class="right">
					<input type="button" value="제작사양서추가(NEW)" title="제작사양서추가(NEW)" class="" onclick="only('true');">
					<input type="button" value="제작사양서추가(OLD)" title="제작사양서추가(OLD)" onclick="only('false');" style="background-color: orange;">
					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRows();">
				</td>
			</tr>
		</table>
		<div id="grid_wrap2" style="height: 350px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID2;
			const columns2 = [ {
				dataField : "number",
				headerText : "문서번호",
				dataType : "string",
				width : 140
			}, {
				dataField : "name",
				headerText : "문서제목",
				dataType : "string",
			}, {
				dataField : "version",
				headerText : "버전",
				dataType : "string",
				width : 80
			}, {
				dataField : "state",
				headerText : "상태",
				dataType : "string",
				width : 100,
			}, {
				dataField : "creator",
				headerText : "작성자",
				dataType : "string",
				width : 100
			}, {
				dataField : "createdDate",
				headerText : "작성일",
				dataType : "date",
				formatString : "yyyy-mm-dd",
				width : 100
			}, {
				dataField : "modifier",
				headerText : "수정자",
				dataType : "string",
				width : 100
			}, {
				dataField : "modifiedDate",
				headerText : "수정일",
				dataType : "date",
				formatString : "yyyy-mm-dd",
				width : 100
			}, {
				dataField : "oid",
				visible : false,
				dataType : "string"
			} ]

			function createAUIGrid2(columnLayout) {
				const props = {
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showStateColumn : true,
					showRowCheckColumn : true,
					selectionMode : "multipleCells",
				}
				myGridID2 = AUIGrid.create("#grid_wrap2", columnLayout, props);
			}
		</script>
		<script type="text/javascript">
			function save() {
				const addRows = AUIGrid.getAddedRowItems(myGridID);
				const addRows2 = AUIGrid.getAddedRowItems(myGridID2);

				for (let i = 0; i < addRows.length; i++) {
					const item = addRows[i];
					const rowIndex = AUIGrid.rowIdToIndex(myGridID, item._$uid);

					if (isNull(item.name)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 3, "품명 값은 공백을 입력 할 수 없습니다.");
						return false;
					}

					if (isNull(item.spec)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 4, "규격 값은 공백을 입력 할 수 없습니다.");
						return false;
					}
					item.rowIndex = rowIndex;
				}

				if (addRows.length !== addRows2.length) {
					alert("등록하려는 데이터와 제작사양서의 개수가 일치하지 않습니다.\n데이터 개수 : " + addRows.length + ", 제작사양서 개수 : " + addRows2.length);
					return false;
				}

				addRows.sort(function(a, b) {
					return a.rowIndex - b.rowIndex;
				});

				if (!confirm("등록 하시겠습니까?")) {
					return false;
				}
				const params = new Object();
				const url = getCallUrl("/part/spec");
				params.addRows = addRows;
				params.addRows2 = addRows2;
				parent.openLayer();
				call(url, params, function(data) {
					alert(data.msg);
					if (data.result) {
						const list = data.list;
						for (let i = 0; i < list.length; i++) {
							const code = list[i];
							AUIGrid.setCellValue(myGridID, i, 2, code);
						}
						parent.closeLayer();
					} else {
						parent.closeLayer();
					}
				})
			}

			function auiCellEditEndHandler(event) {
				const item = event.item;
				const dataField = event.dataField;
				const spec = item.spec;
				const number = item.number;
				const rowIndex = event.rowIndex;
				const check = item.ycode_check;
				if (dataField === "spec" && !isNull(spec)) {
					const url = getCallUrl("/part/bundleValidatorSpec?spec=" + spec);
					call(url, null, function(data) {
						const dwg_check = data.dwg_check;
						item.dwg_check = dwg_check;
						item.dwg = data.dwg;
						AUIGrid.updateRow(myGridID, item, rowIndex);
					}, "GET");
				}

				if (dataField === "number" && !isNull(number)) {
					const url = getCallUrl("/part/bundleValidatorNumber?number=" + number);
					call(url, null, function(data) {
						const ycode_check = data.ycode_check;
						item.ycode_check = ycode_check;
						item.ycode = data.ycode;
						AUIGrid.updateRow(myGridID, item, rowIndex);
					}, "GET");
				}

				// PDM 에 등록 안된 품목이다..
				if (dataField === "spec" && !isNull(spec)) {
					if (item.ycode !== undefined && item.ycode === false) {
						const url = getCallUrl("/erp/getErpItemBySpec?spec=" + item.spec + "&callLoc=제작사양서");
						call(url, null, function(data) {
							if (data.result) {
								const newItem = {
									name : data.itemName,
									number : data.itemNo,
									maker : data.maker,
									customer : data.customer,
									price : data.price,
									currency : data.currency,
									unit : data.unit
								};
								AUIGrid.updateRow(myGridID, newItem, rowIndex);
							}
						}, "GET");
					}
				}

				// PDM 에 데이터가 있다.
				if (dataField === "number" && !isNull(number)) {
					if (item.dwg !== undefined && item.dwg === false) {
						const url = getCallUrl("/erp/getErpItemByPartNo?partNo=" + number +"&callLoc=제작사양서");
						call(url, null, function(data) {
							if (data.result) {
								const newItem = {
									name : data.itemName,
									number : data.itemNo,
									maker : data.maker,
									customer : data.customer,
									price : data.price,
									currency : data.currency,
									unit : data.unit
								};
								AUIGrid.updateRow(myGridID, newItem, rowIndex);
							}
						}, "GET");
					}
				}
			}

			function contextItemHandler(event) {
				const item = new Object();
				switch (event.contextIndex) {
				case 0:
					AUIGrid.addRow(myGridID, item, "selectionUp");
					break;
				case 1:
					AUIGrid.addRow(myGridID, item, "selectionDown");
					break;
				case 3:
					const selectedItems = AUIGrid.getSelectedItems(myGridID);
					for (let i = selectedItems.length - 1; i >= 0; i--) {
						const rowIndex = selectedItems[i].rowIndex;
						AUIGrid.removeRow(myGridID, rowIndex);
					}
					break;
				}
			}

			function readyHandler() {
				AUIGrid.addRow(myGridID, new Object(), "first");
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
				const item = new Object();
				const rowIndex = checkedItems[0].rowIndex;
				item.createdDate = new Date();
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
				const item = new Object();
				const rowIndex = checkedItems[0].rowIndex;
				item.createdDate = new Date();
				AUIGrid.addRow(myGridID, item, rowIndex + 1);
			}
			function addLastRow() {
				const item = new Object();
				AUIGrid.addRow(myGridID, item, "last");
			}
			function deleteRows() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID2);
				const sessionId = document.getElementById("sessionId").value;
				for (let i = checkedItems.length - 1; i >= 0; i--) {
					const item = checkedItems[i].item;
					const rowIndex = checkedItems[i].rowIndex;
					if ((!isNull(item.creatorId) && !checker(sessionId, item.creatorId))) {
						//if ((!isNull(item.creatorId) && !checker(sessionId, item.creatorId)) 
						//|| (!isNull(item.modifierId) && !checker(sessionId, item.modifierId))){
						alert(rowIndex + "행 데이터의 작성자 혹은 수정자가 아닙니다.");
						return false;
					}
					AUIGrid.removeRow(myGridID2, rowIndex);
				}
			}

			function only(isNew) {
				const url = getCallUrl("/doc/only?method=append&multi=true&isNew=" + isNew);
				popup(url, 1600, 700);
			}

			function append(data, callBack) {
				for (let i = 0; i < data.length; i++) {
					const item = data[i].item;
					const isUnique = AUIGrid.isUniqueValue(myGridID2, "oid", item.oid);
					if (isUnique) {
						AUIGrid.addRow(myGridID2, item, "first");
					}
				}
				callBack(true);
			}
			
			function auiKeyDownHandler(event) {
				if (event.keyCode == 13) { // 엔터 키
					var selectedItems = AUIGrid.getSelectedItems(event.pid);
					var rowIndex = selectedItems[0].rowIndex;
					if (rowIndex === AUIGrid.getRowCount(event.pid) - 1) { // 마지막 행인지 여부 
						const item = {
								latest : true,
								state : "사용",
								version : 0
							}
						AUIGrid.addRow(myGridID, item, "last");
						
						return false; // 엔터 키의 기본 행위 안함.
					}
				}
				return true; // 기본 행위 유지
			}

			document.addEventListener("DOMContentLoaded", function() {
				createAUIGrid(columns);
				createAUIGrid2(columns2);
				AUIGrid.resize(myGridID);
				AUIGrid.resize(myGridID2);
			});

			window.addEventListener("resize", function() {
				AUIGrid.resize(myGridID);
				AUIGrid.resize(myGridID2);
			});
			
			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab24");
					if (tab != null) {
						const iframe = tab.querySelector('iframe');
						iframe.src = iframe.src;
					}
				}
			});
		</script>
	</form>
</body>
</html>