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
ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) request.getAttribute("list");
JSONArray elecs = (JSONArray) request.getAttribute("elecs");
JSONArray softs = (JSONArray) request.getAttribute("softs");
JSONArray machines = (JSONArray) request.getAttribute("machines");
JSONArray maks = (JSONArray) request.getAttribute("maks");
JSONArray installs = (JSONArray) request.getAttribute("installs");
JSONArray customers = (JSONArray) request.getAttribute("customers");
JSONArray details = (JSONArray) request.getAttribute("details");
JSONArray projectTypes = (JSONArray) request.getAttribute("projectTypes");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>    
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				의뢰서 등록
			</div>
		</td>
		<td class="right">
			<input type="button" value="등록" title="등록" class="blue" onclick="create();">
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
			<a href="#tabs-2">신규작번추가</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="create-table">
			<colgroup>
				<col width="150">
<!-- 				<col width="500"> -->
			</colgroup>
			<tr>
				<th class="req lb">의뢰서 제목</th>
				<td class="indent5">
					<input type="text" name="name" id="name" class="width-800">
				</td>
			</tr>
			<tr>
				<th class="lb">내용</th>
				<td class="indent5" colspan="3">
					<textarea name="description" id="description" rows="6"></textarea>
				</td>
			</tr>
			<tr>
				<th class="lb">관련 작번</th>
				<td colspan="5">
					<jsp:include page="/extcore/jsp/common/project-include.jsp">
						<jsp:param value="" name="oid" />
						<jsp:param value="true" name="multi"/>
						<jsp:param value="create" name="mode" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5" colspan="3">
					<jsp:include page="/extcore/jsp/common/attach-primary.jsp">
						<jsp:param value="" name="oid" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="req lb">결재</th>
				<td colspan="3">
					<jsp:include page="/extcore/jsp/common/approval-register.jsp">
						<jsp:param value="" name="oid" />
						<jsp:param value="create" name="mode" />
					</jsp:include>
				</td>
			</tr>
		</table>
	</div>
	<div id="tabs-2">
		<table class="create-table">
			<tr>
				<th class="left">작번 템플릿</th>
				<td class="indent5" style="border-right: hidden;">
					<select name="template" id="template" class="width-500">
						<option value="">선택</option>
						<%
						for (Map<String, String> map : list) {
							String oid = map.get("key");
							String name = map.get("value");
						%>
						<option value="<%=oid%>"><%=name%></option>
						<%
						}
						%>
					</select>
				</td>
				<td class="right">
					<input type="button" value="행 추가" title="행 추가" class="" onclick="addRow();" style="font-size: 12pt;">
					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();" style="font-size: 12pt;">
				</td>
			</tr>
		</table>
		<div id="grid_wrap" style="height: 610px; border-top: 1px solid #3180c3; margin-top: 5px;"></div>
	</div>
	<script type="text/javascript">
		let myGridID;
		const maks = <%=maks%>
		const installs = <%=installs%>
		const details = <%=details%>
		const customers = <%=customers%>
		const elecs = <%=elecs%>
		const machines = <%=machines%>
		const softs = <%=softs%>
		const projectTypes = <%=projectTypes%>
		let detailMap = {};
		let installMap = {};
		const columns = [ {
			dataField : "projectType_code",
			headerText : "작번유형",
			dataType : "string",
			width : 80,
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
				list : projectTypes,
				keyField : "key",
				valueField : "value",
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					let isValid = false;
					for (let i = 0, len = projectTypes.length; i < len; i++) {
						if (projectTypes[i]["value"] == newValue) {
							isValid = true;
							break;
						}
					}
					
					if (fromClipboard) {
						for (let i = 0, len = projectTypes.length; i < len; i++) {
							if (projectTypes[i]["key"] == newValue) {
								isValid = true;
								break;
							}
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
				for (let i = 0, len = projectTypes.length; i < len; i++) {
					if (projectTypes[i]["key"] == value) {
						retStr = projectTypes[i]["value"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		}, {
			dataField : "customer_code",
			headerText : "거래처",
			width : 150,
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
				list : customers,
				keyField : "key",
				valueField : "value",
				descendants : [ "install_code" ],
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					let isValid = false;
					for (let i = 0, len = customers.length; i < len; i++) {
						if (customers[i]["value"] == newValue) {
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
				for (let i = 0, len = customers.length; i < len; i++) {
					if (customers[i]["key"] == value) {
						retStr = customers[i]["value"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		}, {
			dataField : "install_code",
			headerText : "설치장소",
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
				list : installs,
				keyField : "key",
				valueField : "value",
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					const param = item.customer_code;
					let dd = installMap[param];
					if(dd === undefined) {
						dd = installs;
					}
					let isValid = false;
					for (let i = 0, len = dd.length; i < len; i++) {
						if (dd[i]["value"] == newValue) {
							isValid = true;
							break;
						}
					}
					return {
						"validate" : isValid,
						"message" : "리스트에 있는 값만 선택(입력) 가능합니다."
					};
				},
				listFunction : function(rowIndex, columnIndex, item, dataField) {
					const param = item.customer_code;
					const dd = installMap[param];
					if (dd === undefined) {
						return installs;
					}
					return dd;
				},
			},
			labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
				let retStr = "";
				const param = item.customer_code;
				let dd = installMap[param];
				if (dd === undefined) {
					dd = installs;
				}
// 					return value;
				for (let i = 0, len = dd.length; i < len; i++) {
					if (dd[i]["key"] == value) {
						retStr = dd[i]["value"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		}, {
			dataField : "mak_code",
			headerText : "막종",
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
				list : maks,
				keyField : "key",
				valueField : "value",
				descendants : [ "detail_code" ],
				descendantDefaultValues : [ "" ],
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					let isValid = false;
					for (let i = 0, len = maks.length; i < len; i++) {
						if (maks[i]["value"] == newValue) {
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
				for (let i = 0, len = maks.length; i < len; i++) {
					if (maks[i]["key"] == value) {
						retStr = maks[i]["value"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		}, {
			dataField : "detail_code",
			headerText : "막종상세",
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
				list : details,
				keyField : "key",
				valueField : "value",
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					const param = item.mak_code;
					let dd = detailMap[param];
					if(dd === undefined) {
						dd = details;
					}
					let isValid = false;
						for (let i = 0, len = dd.length; i < len; i++) {
						if (dd[i]["value"] == newValue) {
							isValid = true;
							break;
						}
				}
					return {
						"validate" : isValid,
						"message" : "리스트에 있는 값만 선택(입력) 가능합니다."
					};
				},
				listFunction : function(rowIndex, columnIndex, item, dataField) {
					var param = item.mak_code;
					var dd = detailMap[param];
					if (dd === undefined) {
						return details;
					}
					return dd;
				},
			},
			labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
				let retStr = "";
				const param = item.mak_code;
				let dd = detailMap[param];
				if (dd === undefined) {
					dd = details;
				}
				for (let i = 0, len = dd.length; i < len; i++) {
					if (dd[i]["key"] == value) {
						retStr = dd[i]["value"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		}, {
			dataField : "kekNumber",
			headerText : "KEK 작번",
			dataType : "string",
			width : 100,
			editRenderer : {
				type : "InputEditRenderer",
				regExp : "^[a-zA-Z0-9]+$",
				autoUpperCase : true
			},
		}, {
			dataField : "keNumber",
			headerText : "KE 작번",
			dataType : "string",
			width : 100,
			editRenderer : {
				type : "InputEditRenderer",
// 				regExp : "^[a-zA-Z0-9-]+$",
				autoUpperCase : true
			},
		}, {
			dataField : "userId",
			headerText : "USER ID",
			dataType : "string",
			width : 100,
			editRenderer : {
				type : "InputEditRenderer",
				autoUpperCase : true
			},
		}, {
			dataField : "customDate",
			headerText : "요구 납기일",
			dataType : "date",
			dateInputFormat : "yyyy-mm-dd",
			formatString : "yyyy년 mm월 dd일",
			width : 150,
			renderer : {
				type : "IconRenderer",
				iconWidth : 16,
				iconHeight : 16,
				iconPosition : "aisleRight",
				iconTableRef : {
					"default" : "/Windchill/extcore/component/AUIGrid/images/calendar-icon.png"
				},
				onClick : function(event) {
					AUIGrid.openInputer(event.pid);
				}
			},
			editRenderer : {
				type : "CalendarRenderer",
				defaultFormat : "yyyy-mm-dd",
				showEditorBtnOver : false,
				onlyCalendar : true,
				showExtraDays : true,
				showTodayBtn : true,
				showUncheckDateBtn : true,
				todayText : "오늘 선택",
				uncheckDateText : "날짜 선택 해제",
				uncheckDateValue : "",
			}
		}, {
			dataField : "description",
			headerText : "작업 내용",
			dataType : "string",
			width : 250,
			style : "aui-left",
		}, {
			dataField : "model",
			headerText : "모델",
			dataType : "string",
			width : 130,
			editRenderer : {
				type : "InputEditRenderer",
				autoUpperCase : true
			},
		}, {
			dataField : "pdate",
			headerText : "발행일",
			dataType : "date",
			dateInputFormat : "yyyy-mm-dd",
			formatString : "yyyy년 mm월 dd일",
			width : 150,
			renderer : {
				type : "IconRenderer",
				iconWidth : 16,
				iconHeight : 16,
				iconPosition : "aisleRight",
				iconTableRef : {
					"default" : "/Windchill/extcore/component/AUIGrid/images/calendar-icon.png"
				},
				onClick : function(event) {
					AUIGrid.openInputer(event.pid);
				}
			},
			editRenderer : {
				type : "CalendarRenderer",
				defaultFormat : "yyyy-mm-dd",
				showEditorBtnOver : false,
				onlyCalendar : true,
				showExtraDays : true,
				showTodayBtn : true,
				showUncheckDateBtn : true,
				todayText : "오늘 선택",
				uncheckDateText : "날짜 선택 해제",
				uncheckDateValue : "",
			}
		}, {
			dataField : "machine",
			headerText : "기계 담당자",
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
				list : machines,
				keyField : "oid",
				valueField : "name",
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					let isValid = false;
					for (let i = 0, len = machines.length; i < len; i++) {
						if (machines[i] == newValue) {
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
				for (let i = 0, len = machines.length; i < len; i++) {
					if (machines[i]["oid"] == value) {
						retStr = machines[i]["name"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		}, {
			dataField : "elec",
			headerText : "전기 담당자",
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
				list : elecs,
				keyField : "oid",
				valueField : "name",
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					let isValid = false;
					for (let i = 0, len = elecs.length; i < len; i++) {
						if (elecs[i] == newValue) {
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
				for (let i = 0, len = elecs.length; i < len; i++) {
					if (elecs[i]["oid"] == value) {
						retStr = elecs[i]["name"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		}, {
			dataField : "soft",
			headerText : "SW 담당자",
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
				list : softs,
				keyField : "oid",
				valueField : "name",
				validator : function(oldValue, newValue, item, dataField, fromClipboard, which) {
					let isValid = false;
					for (let i = 0, len = softs.length; i < len; i++) {
						if (softs[i] == newValue) {
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
				for (let i = 0, len = softs.length; i < len; i++) {
					if (softs[i]["oid"] == value) {
						retStr = softs[i]["name"];
						break;
					}
				}
				return retStr == "" ? value : retStr;
			},
		} ]

		function createAUIGrid(columnLayout) {
			const props = {
				headerHeight : 30,
				showRowNumColumn : true,
				showRowCheckColumn : true,
				rowNumHeaderText : "번호",
				noDataMessage : "작성된 작번내용이 없습니다.",
				selectionMode : "multipleCells",
				editable : true,
				enableSorting : false,
				useContextMenu : true,
				enableRightDownFocus : true,
				$compaEventOnPaste : true,
				fillColumnSizeMode : true,
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
			//readyHandler();
			AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
			AUIGrid.bind(myGridID, "keyDown", auiKeyDownHandler);
		}
		
		//행 삭제
		function deleteRow() {
			const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
			for (let i = checkedItems.length-1; i >= 0; i--) {
				const item = checkedItems[i].item;
				const rowIndex = checkedItems[i].rowIndex;
				AUIGrid.removeRow(myGridID, rowIndex);
			}
		}
		
		//행 추가
		function addRow() {
			const item = new Object();
			item.latest = true;
			AUIGrid.addRow(myGridID, item, "last");
		}
		
		// enter 키 행 추가
		function auiKeyDownHandler(event) {
			if (event.keyCode == 13) { // 엔터 키
				var selectedItems = AUIGrid.getSelectedItems(event.pid);
				var rowIndex = selectedItems[0].rowIndex;
				if (rowIndex === AUIGrid.getRowCount(event.pid) - 1) { // 마지막 행인지 여부 
					AUIGrid.addRow(event.pid, {}); // 행 추가
					return false; // 엔터 키의 기본 행위 안함.
				}
			}
			return true; // 기본 행위 유지
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

		function auiCellEditEndHandler(event) {
			const dataField = event.dataField;
			const item = event.item;
			const rowIndex = event.rowIndex;
			if (dataField === "mak_code") {
				const mak = item.mak_code;
				const url = getCallUrl("/commonCode/getChildrens?parentCode=" + mak + "&codeType=MAK");
				call(url, null, function(data) {
					detailMap[mak] = data.list;
				}, "GET");
			}

			if (dataField === "customer_code") {
				const customer = item.customer_code;
				const url = getCallUrl("/commonCode/getChildrens?parentCode=" + customer + "&codeType=CUSTOMER");
				call(url, null, function(data) {
					installMap[customer] = data.list;
				}, "GET");
			}

			if (dataField === "kekNumber" || dataField === "projectType_code") {
				if (!isNull(item.kekNumber) && !isNull(item.projectType_code)) {
					const params = new Object();
					const url = getCallUrl("/requestDocument/validate");
					params.kekNumber = item.kekNumber;
					params.projectType_code = item.projectType_code;
					openLayer();
					call(url, params, function(data) {
						if (data.validate) {
							alert(rowIndex + "행에 입력한 작번은 이미 등록되어있습니다.");
							item.kekNumber = "";
							item.projectType_code = "";
							AUIGrid.updateRow(myGridID, item, rowIndex);
							closeLayer();
							return false;
						}
						closeLayer();
					})
				}
			}
		}

		function readyHandler() {
			const item = new Object();
			AUIGrid.addRow(myGridID, item, "first");
		}

		function createTest() {
			console.log('createTest()');
			const params = new Object();
			console.log('params ', params);
			//const url = getCallUrl("/requestDocument/create");
			const name = document.getElementById("name");
			console.log('name ', name);
			const template = document.getElementById("template");
			console.log('template ', template);
			const description = document.getElementById("description");
			console.log('description ', description);
			const addRows = AUIGrid.getAddedRowItems(myGridID);
			console.log('addRows ', addRows);
			const addRows8 = AUIGrid.getAddedRowItems(myGridID8);
			console.log('addRows8 ', addRows8);
			const addRows9 = AUIGrid.getAddedRowItems(myGridID9);
			console.log('myGridID9', myGridID9);
			console.log('addRows9 ', addRows9);
			
		}
		
		function create() {

			const params = new Object();
			const url = getCallUrl("/requestDocument/create");
			const name = document.getElementById("name");
			const template = document.getElementById("template");
			const description = document.getElementById("description");
			const addRows = AUIGrid.getAddedRowItems(myGridID);
			const addRows8 = AUIGrid.getAddedRowItems(myGridID8);
			const addRows9 = AUIGrid.getAddedRowItems(myGridID9);

			if (isNull(name.value)) {
				alert("의뢰서 제목을 입력하세요.");
				name.focus();
				return false;
			}
			
			if (addRows9.length === 0 && addRows.length === 0) {
				alert("최소 하나이상의 관련 작번 또는 신규작번을 추가하세요.");
				//insert9();
				return false;
			}else{
				for(let i=0; addRows.length>i; i++){
					if (isNull(addRows[i].projectType_code) || isNull(addRows[i].projectType_code)) {
	 					alert("신규작번의 작번유형을 선택해주세요.");
	 					return false;
	 				}
					if (isNull(addRows[i].customer_code) || isNull(addRows[i].customer_code)) {
	 					alert("신규작번의 거래처를 선택해주세요.");
	 					return false;
	 				}
					if (isNull(addRows[i].install_code) || isNull(addRows[i].install_code)) {
	 					alert("신규작번의 설치장소를 선택해주세요.");
	 					return false;
	 				}
					if (isNull(addRows[i].mak_code) || isNull(addRows[i].mak_code)) {
	 					alert("신규작번의 막종을 선택해주세요.");
	 					return false;
	 				}
					if (isNull(addRows[i].detail_code) || isNull(addRows[i].detail_code)) {
	 					alert("신규작번의 막종상세를 선택해주세요.");
	 					return false;
	 				}
	 				if (isNull(addRows[i].kekNumber) || isNull(addRows[i].kekNumber)) {
	 					alert("신규작번의 KEK 작번을 입력해주세요.");
	 					return false;
	 				}
	 				if (isNull(addRows[i].keNumber) || isNull(addRows[i].keNumber)) {
	 					alert("신규작번의 KE 작번을 입력해주세요.");
	 					return false;
	 				}
	 				if (isNull(addRows[i].userId) || isNull(addRows[i].userId)) {
	 					alert("신규작번의 USER ID를 입력해주세요.");
	 					return false;
	 				}
	 				if (isNull(addRows[i].customDate) || isNull(addRows[i].customDate)) {
	 					alert("신규작번의 요구납기일을 선택해주세요.");
	 					return false;
	 				}
	 				if (isNull(addRows[i].model) || isNull(addRows[i].model)) {
	 					alert("신규작번의 모델을 입력해주세요.");
	 					return false;
	 				}
	 				if (isNull(addRows[i].pdate) || isNull(addRows[i].pdate)) {
	 					alert("신규작번의 발행일을 선택해주세요.");
	 					return false;
	 				}
	 			}
			}
				
			
			

// 			if (isNull(template.value)) {
// 				alert("작번 템플릿을 선택하세요.");
// 				return false;
// 			}

			if (addRows8.length === 0) {
				alert("결재선을 지정하세요.");
				_register();
				return false;
			}
			/*
			if (addRows9.length === 0 && addRows.length === 0) {
				alert("최소 하나이상의 작번을 추가하세요.");
				insert9();
				return false;
			}
			*/
// 			for(let i=0; addRows.length>i; i++){
// 				if (isNull(addRows[i].kekNumber) || isNull(addRows[i].keNumber)) {
// 					alert("관련 작번을 추가하세요.");
// 					return false;
// 				}
// 			}

			if (!confirm("등록 하시겠습니까?")) {
				return false;
			}

			params.name = name.value;
			params.description = description.value;
			params.addRows = addRows;
			params.addRows9 = addRows9;
			params.primarys = toArray("primarys");
			params.template = template.value;
			toRegister(params, addRows8);
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
	</script>
</div>
<script type="text/javascript">
	document.addEventListener("DOMContentLoaded", function() {
		$("#tabs").tabs({
			active : 0,
			activate : function(event, ui) {
				var tabId = ui.newPanel.prop("id");
				switch (tabId) {
				case "tabs-1":
					const isCreated9 = AUIGrid.isCreated(myGridID9);//작번
					if (isCreated9) {
						AUIGrid.resize(myGridID9);
					} else {
						createAUIGrid9(columns9);
					}
					const isCreated8 = AUIGrid.isCreated(myGridID8); // 결재
					if (isCreated8) {
						AUIGrid.resize(myGridID8);
					} else {
						createAUIGrid8(columns8);
					}
					selectbox("template");
					break;
				case "tabs-2":
					const isCreated = AUIGrid.isCreated(myGridID);
					if (isCreated) {
						AUIGrid.resize(myGridID);
					} else {
						createAUIGrid(columns);
					}
					selectbox("template");
					break;
				}
			}
		});
		createAUIGrid9(columns9);
		createAUIGrid8(columns8);
		createAUIGrid(columns);
		AUIGrid.resize(myGridID9);
		AUIGrid.resize(myGridID8);
		AUIGrid.resize(myGridID);
		document.getElementById("name").focus();
		selectbox("template");
	})

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID9);
		AUIGrid.resize(myGridID8);
		AUIGrid.resize(myGridID);
	});
</script>