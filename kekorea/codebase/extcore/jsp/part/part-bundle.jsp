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
<style type="text/css">
.aui-warn {
	font-weight: bold;
	color: red;
}
</style>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
</head>
<body>
	<form>
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
		<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
		<input type="hidden" name="sessionid" id="sessionid">
		<input type="hidden" name="lastNum" id="lastNum">
		<input type="hidden" name="curPage" id="curPage">

		<table class="button-table">
			<tr>

				<td class="right">
					<!--  	<input type="button" value="행 추가(이전)" title="행 추가(이전)" class="" onclick="addBeforeRow();"> -->
					<!--	<input type="button" value="행 추가(이후)" title="행 추가(이후)" onclick="addAfterRow();" style="background-color: orange;">-->
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
					<input type="button" value="행 추가" title="행 추가" class="" onclick="addLastRow();">
					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">
					<!-- 					<input type="button" value="저장" title="저장" class="blue" onclick="save('')"> -->
					<input type="button" value="저장(ERP)" title="저장(ERP)" onclick="save()" style="background-color: navy;">
				</td>
			</tr>
		</table>

		<table class="create-table">
			<colgroup>
				<col width="150">
				<col width="*">
			</colgroup>
			<tr>
				<td colspan="2">
					<div id="grid_wrap" style="height: 400px; border-top: 1px solid #3180c3; margin: 5px 5px 5px 5px;"></div>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5">
					<div class="AXUpload5" id="secondary_layer"></div>
					<div class="AXUpload5QueueBox_list" id="uploadQueueBox" style="height: 300px;"></div>
				</td>
		</table>
		<script type="text/javascript">
			let myGridID;
			const list = [ "KRW", "JPY" ];
			const columns = [ {
				dataField : "dwg_check",
				headerText : "체크(DWG_NO)",
				dataType : "string",
				width : 120,
				editable : false,
// 			}, {
// 				dataField : "ycode_check",
// 				headerText : "체크(YCODE)",
// 				dataType : "string",
// 				width : 120,
// 				editable : false,
			}, {
				dataField : "number",
				headerText : "품번",
				dataType : "string",
				width : 150,
				editable : false
			}, {
				dataField : "name",
				headerText : "품명",
				dataType : "string",
				width : 250,
			}, {
				dataField : "spec",
				headerText : "규격",
				headerStyle : "aui-warn",
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

			let secondary = new AXUpload5();
			function load() {
				secondary.setConfig({
					isSingleUpload : false,
					targetID : "secondary_layer",
					uploadFileName : "secondary",
					buttonTxt : "파일 선택",
					uploadMaxFileSize : (1024 * 1024 * 1024),
					uploadUrl : getCallUrl("/content/upload"),
					dropBoxID : "uploadQueueBox",
					queueBoxID : "uploadQueueBox",
					uploadPars : {
						roleType : "secondary"
					},
					uploadMaxFileCount : 100,
					deleteUrl : getCallUrl("/content/delete"),
					fileKeys : {},
					onComplete : function() {
						
					},
					onUpload : function() {
						let form = document.querySelector("form");
						let secondaryTag = document.createElement("input");
						secondaryTag.type = "hidden";
						secondaryTag.name = "secondarys";
						secondaryTag.value = this.cacheId;
						secondaryTag.id = this.tagId;
						form.appendChild(secondaryTag);
					}
				})
			}

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

			function save() {
				const addRows = AUIGrid.getAddedRowItems(myGridID);
				const secondarys = toArray("secondarys");
// 				if (addRows.length !== secondarys.length) {
// 					alert("등록하려는 데이터와 첨부파일의 개수가 일치하지 않습니다.\n데이터 개수 : " + addRows.length + ", 첨부파일 개수 : " + secondarys.length);
// 					return false;
// 				}
				for (let i = 0; i < addRows.length; i++) {
					const item = addRows[i];
					const rowIndex = AUIGrid.rowIdToIndex(myGridID, item._$uid);

					if (isNull(item.name)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 2, "품명 값은 공백을 입력 할 수 없습니다.");
						return false;
					}

					if (isNull(item.spec)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 4, "규격 값은 공백을 입력 할 수 없습니다.");
						return false;
					}
					item.rowIndex = rowIndex;
				}

	

				addRows.sort(function(a, b) {
					return a.rowIndex - b.rowIndex;
				});

				if (!confirm("등록 하시겠습니까?")) {
					return false;
				}
				const params = new Object();
				const url = getCallUrl("/part/bundle");
				params.addRows = addRows;
				params.secondarys = secondarys;
				parent.openLayer();
				call(url, params, function(data) {
					console.log(data);
					alert(data.msg);
					if (data.result) {
						const list = data.list;
						for(let i=0; i<list.length; i++) {
							const ycode = list[i];
							let item = new Object();
							item.number = ycode;
							AUIGrid.updateRow(myGridID, item, i);
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

				// 				if (dataField === "number" && !isNull(number)) {
				// 					const url = getCallUrl("/part/bundleValidatorNumber?number=" + number);
				// 					call(url, null, function(data) {
				// 						const ycode_check = data.ycode_check;
				// 						item.ycode_check = ycode_check;
				// 						item.ycode = data.ycode;
				// 						AUIGrid.updateRow(myGridID, item, rowIndex);
				// 					}, "GET");
				// 				}

				// PDM 에 등록 안된 품목이다..
				if (dataField === "spec" && !isNull(spec)) {
					// 					alert("C");
// 					if (item.ycode !== undefined && item.ycode === false) {
						const url = getCallUrl("/erp/getErpItemBySpec?spec=" + item.spec + "&callLoc=부품일괄등록");
						call(url, null, function(data) {
							console.log(data);
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
// 					}
				}

				// PDM 에 데이터가 있다.
				// 				if (dataField === "number" && !isNull(number)) {
				// // 					if (item.dwg !== undefined && item.dwg === false) {
				// 						const url = getCallUrl("/erp/getErpItemByPartNo?partNo=" + number +"&callLoc=부품일괄등록");
				// 						call(url, null, function(data) {
				// 							if (data.result) {
				// 								const newItem = {
				// 									name : data.itemName,
				// 									number : data.itemNo,
				// 									maker : data.maker,
				// 									customer : data.customer,
				// 									price : data.price,
				// 									currency : data.currency,
				// 									unit : data.unit
				// 								};
				// 								AUIGrid.updateRow(myGridID, newItem, rowIndex);
				// 							}
				// 						}, "GET");
				// // 					}
				// 				}
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
			function deleteRow() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
				const sessionId = document.getElementById("sessionId").value;
				for (let i = checkedItems.length - 1; i >= 0; i--) {
					const item = checkedItems[i].item;
					const rowIndex = checkedItems[i].rowIndex;
					if ((!isNull(item.creatorId) && !checker(sessionId, item.creatorId))) {
						// 					if ((!isNull(item.creatorId) && !checker(sessionId, item.creatorId)) || (!isNull(item.modifierId) && !checker(sessionId, item.modifierId))) {
						alert(rowIndex + "행 데이터의 작성자 혹은 수정자가 아닙니다.");
						return false;
					}
					AUIGrid.removeRow(myGridID, rowIndex);
				}
			}

			function addLastRow() {
				const item = new Object();

				AUIGrid.addRow(myGridID, item, "last");
			}

			document.addEventListener("DOMContentLoaded", function() {
				createAUIGrid(columns);
				AUIGrid.resize(myGridID);
				load();
			});

			window.addEventListener("resize", function() {
				AUIGrid.resize(myGridID);
			});

			function deleteAllFiles() {
				const secondarys = document.getElementsByName("secondarys");
				for (let i = secondarys.length - 1; i >= 0; i--) {
					const tag = secondarys[i];
					tag.parentNode.removeChild(tag);
				}

				var l = $("form:eq(0)").find("div.readyselect");
				$.each(l, function(idx) {
					var fid = l.eq(idx).attr("id");
					secondary.removeUploadedList(fid);
					l.eq(idx).hide();
				})
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
			
			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab21");
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