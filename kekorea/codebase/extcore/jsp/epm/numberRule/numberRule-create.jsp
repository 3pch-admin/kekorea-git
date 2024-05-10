<%@page import="java.sql.Timestamp"%>
<%@page import="wt.org.WTUser"%>
<%@page import="java.util.HashMap"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<%
JSONArray sizes = (JSONArray) request.getAttribute("sizes");
JSONArray classificationWritingDepartments = (JSONArray) request.getAttribute("classificationWritingDepartments");
JSONArray writtenDocuments = (JSONArray) request.getAttribute("writtenDocuments");

WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
Timestamp time = (Timestamp) request.getAttribute("time");
JSONArray drawingCompanys = (JSONArray) request.getAttribute("drawingCompanys");
JSONArray businessSectors = (JSONArray) request.getAttribute("businessSectors");

%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
<body style="overflow:hidden;">

<table class="button-table">
<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				KEK 도번 등록
			</div>
		</td>
		<td class="right">
			<input type="button" value="저장" title="저장" class="blue" onclick="save();">
			<input type="button" value="행 추가" title="행 추가" onclick="addRow();">
			<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">			
		</td>
	</tr>
</table>
<div id="grid_wrap" style="height: 590px; border-top: 1px solid #3180c3;"></div>
</body>
<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
<script type="text/javascript">
			let myGridID;
			const businessSector =
		<%=businessSectors%>
			const drawingCompany =
		<%=drawingCompanys%>
			const size =
		<%=sizes%>
			const writtenDocuments =
		<%=writtenDocuments%>
			const classificationWritingDepartments =
		<%=classificationWritingDepartments%>
			function _layout() {
				return [ {
					dataField : "number",
					headerText : "도면번호",
					dataType : "string",
					width : 100,
					editable : false,
					filter : {
						showIcon : true,
						inline : true
					},
				}, 
// 				{
// 					dataField : "size_code",
// 					headerText : "사이즈",
// 					dataType : "string",
// 					width : 80,
// 					renderer : {
// 						type : "IconRenderer",
// 						iconWidth : 16,
// 						iconHeight : 16,
// 						iconPosition : "aisleRight",
// 						iconTableRef : {
// 							"default" : "/Windchill/extcore/component/AUIGrid/images/list-icon.png"
// 						},
// 						onClick : function(event) {
// 							AUIGrid.openInputer(event.pid);
// 						}
// 					},
// 					editRenderer : {
// 						type : "ComboBoxRenderer",
// 						autoCompleteMode : true,
// 						autoEasyMode : true,
// 						matchFromFirst : false,
// 						showEditorBtnOver : false,
// 						list : size,
// 						keyField : "key",
// 						valueField : "value",
// 						validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
// 							let isValid = false;
// 							for (let i = 0, len = size.length; i < len; i++) {
// 								if (size[i]["value"] == newValue) {
// 									isValid = true;
// 									break;
// 								}
// 							}
// 							return {
// 								"validate" : isValid,
// 								"message" : "리스트에 있는 값만 선택(입력) 가능합니다."
// 							};
// 						}
// 					},
// 					labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
// 						let retStr = "";
// 						for (let i = 0, len = size.length; i < len; i++) {
// 							if (size[i]["key"] == value) {
// 								retStr = size[i]["value"];
// 								break;
// 							}
// 						}
// 						return retStr == "" ? value : retStr;
// 					},
// 					filter : {
// 						showIcon : true,
// 						inline : true
// 					},
// 				}, 
				{
					dataField : "lotNo",
					headerText : "LOT",
					dataType : "numeric",
					width : 80,
					formatString : "###0",
					editRenderer : {
						type : "InputEditRenderer",
						onlyNumeric : true,
						maxlength : 4,
					},
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
					style : "aui-left",
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "businessSector_code",
					headerText : "사업부문",
					dataType : "string",
					width : 200,
					editable : false,
					labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
						let retStr = "";
						for (let i = 0, len = businessSector.length; i < len; i++) {
							if (businessSector[i]["key"] == value) {
								retStr = businessSector[i]["value"];
								break;
							}
						}
						return retStr == "" ? value : retStr;
					},
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "drawingCompany_code",
					headerText : "도면생성회사",
					dataType : "string",
					width : 150,
					editable : false,
					labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
						let retStr = "";
						for (let i = 0, len = drawingCompany.length; i < len; i++) {
							if (drawingCompany[i]["key"] == value) {
								retStr = drawingCompany[i]["value"];
								break;
							}
						}
						return retStr == "" ? value : retStr;
					},
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "classificationWritingDepartments_code",
					headerText : "작성부서구분",
					dataType : "string",
					width : 150,
					renderer : {
						type : "IconRenderer",
						iconWidth : 16,
						iconHeight :   16,
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
						list : classificationWritingDepartments,
						keyField : "key",
						valueField : "value",
						validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
							let isValid = false;
							if (fromClipboard) {
								for (let i = 0, len = classificationWritingDepartments.length; i < len; i++) {
									if (classificationWritingDepartments[i]["key"] == newValue) {
										isValid = true;
										break;
									}
								}
							}
							
							for (let i = 0, len = classificationWritingDepartments.length; i < len; i++) {
								if (classificationWritingDepartments[i]["value"] == newValue) {
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
					labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
						let retStr = "";
						for (let i = 0, len = classificationWritingDepartments.length; i < len; i++) {
							if (classificationWritingDepartments[i]["key"] == value) {
								retStr = classificationWritingDepartments[i]["value"];
								break;
							}
						}
						return retStr == "" ? value : retStr;
					},
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "writtenDocuments_code",
					headerText : "작성문서구분",
					dataType : "string",
					width : 275,
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
						list : writtenDocuments,
						keyField : "key",
						valueField : "value",
						validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
							let isValid = false;
							
							if (fromClipboard) {
								for (let i = 0, len = writtenDocuments.length; i < len; i++) {
									if (writtenDocuments[i]["key"] == newValue) {
										isValid = true;
										break;
									}
								}
							}
							
							for (let i = 0, len = writtenDocuments.length; i < len; i++) {
								if (writtenDocuments[i]["value"] == newValue) {
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
					labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
						let retStr = "";
						for (let i = 0, len = writtenDocuments.length; i < len; i++) {
							if (writtenDocuments[i]["key"] == value) {
								retStr = writtenDocuments[i]["value"];
								break;
							}
						}
						return retStr == "" ? value : retStr;
					},
					filter : {
						showIcon : true,
						inline : true
					}
				} ]
			}

			function createAUIGrid(columnLayout) {
				const props = {
					headerHeight : 30,
					showRowNumColumn : true,
					showRowCheckColumn : true,
					showStateColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					selectionMode : "multipleCells",
					enableMovingColumn : true,
					showInlineFilter : false,
					useContextMenu : true,
					enableRightDownFocus : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
					editable : true,
					fixedColumnCount : 1,
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				
				AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
				AUIGrid.bind(myGridID, "vScrollChange", function(event) {
					hideContextMenu();
// 					vScrollChangeHandler(event);
				});
				AUIGrid.bind(myGridID, "hScrollChange", function(event) {
					hideContextMenu();
				});
				AUIGrid.bind(myGridID, "cellEditBegin", auiCellEditBeginHandler);
				AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
				AUIGrid.bind(myGridID, "keyDown", auiKeyDownHandler);
			}

			// enter 키 행 추가
			function auiKeyDownHandler(event) {
				if (event.keyCode == 13) { // 엔터 키
					var selectedItems = AUIGrid.getSelectedItems(event.pid);
					var rowIndex = selectedItems[0].rowIndex;
					if (rowIndex === AUIGrid.getRowCount(event.pid) - 1) { // 마지막 행인지 여부
						const item = {
							version : 0,
							drawingCompany_code : "K",
							businessSector_code : "K",
							number : "K"
						}
						AUIGrid.addRow(event.pid, item); // 행 추가
						return false; // 엔터 키의 기본 행위 안함.
					}
				}
				return true; // 기본 행위 유지
			}

			function create() {
				const url = getCallUrl("/numberRule/create");
				popup(url, 1600, 650);
			}

			// 			function register() {
			// 				const url = getCallUrl("/numberRule/register");
			// 				popup(url);
			// 			}

			function auiCellEditEndHandler(event) {
				const item = event.item;
				const rowIndex = event.rowIndex;
				const dataField = event.dataField;
				const value = event.value;
				if (dataField === "classificationWritingDepartments_code") {
					const newNumber = "K" + value;
					AUIGrid.setCellValue(myGridID, rowIndex, "number", newNumber);
				}

				if (dataField === "writtenDocuments_code") {
					const value1 = AUIGrid.getCellValue(myGridID, rowIndex, "classificationWritingDepartments_code");
					const newNumber = "K" + value1 + value;
					
					AUIGrid.setCellValue(myGridID, rowIndex, "number", newNumber);
					/*
					const url = getCallUrl("/numberRule/last?number=" + newNumber);
					call(url, null, function(data) {
						const next = data.next;
						AUIGrid.setCellValue(myGridID, rowIndex, "number", newNumber + next);
					}, "GET");
					*/
				}

				const lotNo = item.lotNo;
				if (dataField === "lotNo") {
					const url = getCallUrl("/erp/getUnitName?lotNo=" + lotNo + "&callLoc=KEK 도번");
					parent.openLayer();
					call(url, null, function(data) {
						if (data.result) {
							const newItem = {
								unitName : data.unitName,
							};
							AUIGrid.updateRow(myGridID, newItem, rowIndex);
						} else {
							alert(data.msg);
						}
						parent.closeLayer();
					}, "GET");
				}
			}

			function auiCellEditBeginHandler(event) {
				const dataField = event.dataField;
				const rowIndex = event.rowIndex;
				const state = event.item.state;
				if (state === "승인됨") {
					return false;
				}

				if (dataField === "writtenDocuments_code") {
					const value = AUIGrid.getCellValue(myGridID, rowIndex, "classificationWritingDepartments_code");
					if (isNull(value)) {
						alert("작성부서구분을 먼저 선택하세요.");
						return false;
					}
				}
				return true;
			}


			function save() {
				const url = getCallUrl("/numberRule/save");
				const params = new Object();
				const addRows = AUIGrid.getAddedRowItems(myGridID);
				const removeRows = AUIGrid.getRemovedItems(myGridID);
				const editRows = AUIGrid.getEditedRowItems(myGridID);

				if (addRows.length === 0 && removeRows.length === 0 && editRows.length === 0) {
					alert("변경된 내용이 없습니다.");
					return false;
				}

				for (let i = 0; i < addRows.length; i++) {
					const item = addRows[i];
					const rowIndex = AUIGrid.rowIdToIndex(myGridID, item._$uid);

					if (isNull(item.size_code)) {
// 						AUIGrid.showToastMessage(myGridID, rowIndex, 1, "사이즈를 선택하세요.");
// 						return false;
					}

					if (isNull(item.lotNo) || item.lotNo === 0) {
// 						AUIGrid.showToastMessage(myGridID, rowIndex, 2, "LOT NO의 값은 0혹은 공백을 입력 할 수 없습니다.");
// 						return false;
					}

					if (isNull(item.unitName)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 3, "UNIT NAME을 입력하세요.");
						return false;
					}

					if (isNull(item.name)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 4, "도면명을 입력하세요.");
						return false;
					}

					if (isNull(item.classificationWritingDepartments_code)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 7, "작성부서를 선택하세요.");
						return false;
					}

					if (isNull(item.writtenDocuments_code)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 8, "작성문서구분을 선택하세요.");
						return false;
					}
				}

				for (let i = 0; i < editRows.length; i++) {
					const item = editRows[i];
					const rowIndex = AUIGrid.rowIdToIndex(myGridID, item._$uid);

					if (isNull(item.size_code)) {
// 						AUIGrid.showToastMessage(myGridID, rowIndex, 1, "사이즈를 선택하세요.");
// 						return false;
					}

					if (isNull(item.lotNo) || item.lotNo === 0) {
// 						AUIGrid.showToastMessage(myGridID, rowIndex, 2, "LOT NO의 값은 0혹은 공백을 입력 할 수 없습니다.");
// 						return false;
					}

					if (isNull(item.unitName)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 3, "UNIT NAME을 입력하세요.");
						return false;
					}

					if (isNull(item.name)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 4, "도면명을 입력하세요.");
						return false;
					}

					if (isNull(item.classificationWritingDepartments_code)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 7, "작성부서를 선택하세요.");
						return false;
					}

					if (isNull(item.writtenDocuments_code)) {
						AUIGrid.showToastMessage(myGridID, rowIndex, 8, "작성문서구분을 선택하세요.");
						return false;
					}
				}

				if (!confirm("저장 하시겠습니까?")) {
					return false;
				}

				params.addRows = addRows;
				params.removeRows = removeRows;
				params.editRows = editRows;
				parent.openLayer();
				call(url, params, function(data) {
					alert(data.msg);
					parent.closeLayer();
					if (data.result) {
						opener.loadGridData();
						self.close();
					} else {
						parent.closeLayer();
					}
				});
			}


			function deleteRow() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
				for (let i = checkedItems.length - 1; i >= 0; i--) {
					const item = checkedItems[i].item;
					const rowIndex = checkedItems[i].rowIndex;
					AUIGrid.removeRow(myGridID, rowIndex);
				}
			}

			function addRow() {
				const item = new Object();
				item.version = 0;
				item.drawingCompany_code = "K";
				item.businessSector_code = "K";
				item.number = "K";
				AUIGrid.addRow(myGridID, item, "last");
			}

			function exportExcel() {
				const exceptColumnFields = [];
				const sessionName = document.getElementById("sessionName").value;
				exportToExcel("KEK 도번 리스트", "KEK 도번", "KEK 도번 리스트", exceptColumnFields, sessionName);
			}

			document.addEventListener("DOMContentLoaded", function() {
				const columns = loadColumnLayout("numberRule-list");
				const contenxtHeader = genColumnHtml(columns);
				$("#h_item_ul").append(contenxtHeader);
				$("#headerMenu").menu({
					select : headerMenuSelectHandler
				});
				createAUIGrid(columns);
				gridResize();
				selectbox("state");
				selectbox("size");
				selectbox("writtenDocuments_code");
				selectbox("classificationWritingDepartments_code");
				finderUser("creator");
				twindate("created");
			});


			document.addEventListener("click", function(event) {
				hideContextMenu();
			})

			window.addEventListener("resize", function() {
				gridResize();
			});
			function gridResize(){
				const ww = window.innerWidth;	//1654
				const hh = window.innerHeight;	//834
				//4row 15, 200
				//3row 15, 160
				//2row 15, 140
				//popup 15, 50
				AUIGrid.resize(myGridID, ww-15, hh-50);
			}
		</script>
