<%@page import="java.sql.Timestamp"%>
<%@page import="wt.org.WTUser"%>
<%@page import="e3ps.epm.numberRule.dto.NumberRuleDTO"%>
<%@page import="e3ps.epm.dto.KeDrawingDTO"%>
<%@page import="java.util.Map"%>
<%@page import="e3ps.common.util.StringUtils"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.common.util.ContentUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
NumberRuleDTO dto = (NumberRuleDTO) request.getAttribute("dto");
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
boolean isSupervisor = (boolean) request.getAttribute("isSupervisor");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
Timestamp time = (Timestamp) request.getAttribute("time");
JSONArray sizes = (JSONArray) request.getAttribute("sizes");
JSONArray drawingCompanys = (JSONArray) request.getAttribute("drawingCompanys");
JSONArray writtenDocuments = (JSONArray) request.getAttribute("writtenDocuments");
JSONArray businessSectors = (JSONArray) request.getAttribute("businessSectors");
JSONArray classificationWritingDepartments = (JSONArray) request.getAttribute("classificationWritingDepartments");
JSONArray history = (JSONArray) request.getAttribute("history");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%> 
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<style type="text/css">
.preView {
	background-color: #caf4fd;
	cursor: pointer;
}
</style>
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				KEK 도번 정보
			</div>
		</td>
		<td class="right">
			<!-- <input type="button" value="수정" title="수정" class="blue" onclick="modify();">	 -->	
			<!-- <input type="button" value="개정" title="개정" onclick="update('revise');"> -->
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
			<a href="#tabs-2">버전정보</a>
		</li>
<!-- 		<li> -->
<!-- 			<a href="#tabs-3">결재이력</a> -->
<!-- 		</li> -->
	</ul>
	<div id="tabs-1">
		<table class="view-table">
			<colgroup>
				<col width="10%">
				<col width="300">
				<col width="130">
				<col width="300">
				
			</colgroup>
			<tr>
				<th class="lb">도면번호</th>
				<td class="indent5" colspan="3">
				<input type="hidden" name="number" id="number" value="<%=dto.getNumber()%>">
				<%=dto.getNumber()%></td>
			</tr>
			<tr>
				<th class="lb">사이즈</th>
				<td class="indent5"><%=dto.getSize_code()%></td>	
				<th class="lb">LOT</th>
				<td class="ident5">&nbsp;<%=dto.getLotNo()%></td>				
			</tr>
			<tr>
				<th class="lb">UNIT NAME</th>
				<td class="ident5">&nbsp;<%=dto.getUnitName()%></td>
				<th class="lb">도번명</th>
				<td class="ident5">&nbsp;<%=dto.getName()%></td>
			</tr>
			<tr>
				<th class="lb">사업부문</th>
				<td class="ident5">&nbsp;<%=dto.getBusinessSector_txt()%></td>
				<th class="lb">도면생성회사</th>
				<td class="ident5">&nbsp;<%=dto.getDrawingCompany_txt()%></td>
			</tr>
			<tr>
				<th class="lb">작성부서구분</th>
				<td class="ident5">&nbsp;<%=dto.getClassificationWritingDepartments_txt()%></td>
				<th class="lb">작성문서구분</th>
				<td class="ident5">&nbsp;<%=dto.getWrittenDocuments_txt()%></td>
			</tr>
			<tr>
				<th class="lb">버전</th>
				<td class="ident5">&nbsp;<%=dto.getVersion()%></td>
				<th class="lb">상태</th>
				<td class="ident5">&nbsp;<%=dto.getState()%></td>
			</tr>	
			<tr>
				<th class="lb">작성자</th>
				<td class="ident5">&nbsp;<%=dto.getCreator()%></td>
				<th class="lb">작성일</th>
				<td class="ident5">&nbsp;<%=dto.getCreatedDate_txt()%></td>
			</tr>		
			<tr>
				<th class="lb">수정자</th>
				<td class="ident5">&nbsp;<%=dto.getModifier()%></td>
				<th class="lb">수정일</th>
				<td class="ident5">&nbsp;<%=dto.getModifiedDate_txt()%></td>
			</tr>
		</table>
	</div>
	<div id="tabs-2">
		<div id="grid_wrap" style="height: 470px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
		const businessSector = <%=businessSectors%>
		const drawingCompany = <%=drawingCompanys%>
		const size = <%=sizes%>
		const writtenDocuments = <%=writtenDocuments%>
		const classificationWritingDepartments = <%=classificationWritingDepartments%>
		let myGridID;
		
		const columns = [ {
				dataField : "number",
				headerText : "도면번호",
				dataType : "string",
				width : 100,
				editable : false,
				style : "underline",
				renderer : {
					type : "LinkRenderer",
					baseUrl : "javascript",
					jsCallback : function(rowIndex, columnIndex, value, item) {
						const oid = item.oid;
						const url = getCallUrl("/numberRule/view?oid=" + oid);
						popup(url, 1000, 600);
					}
				},
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "size_code",
				headerText : "사이즈",
				dataType : "string",
				width : 80,
			}, {
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
				width : 150,
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
				},
			}, {
				dataField : "version",
				headerText : "버전",
				dataType : "string",
				width : 100,
				editable : false,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "state",
				headerText : "상태",
				dataType : "string",
				width : 80,
				editable : false,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "creator",
				headerText : "작성자",
				dataType : "string",
				width : 100,
				editable : false,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "createdDate_txt",
				headerText : "작성일",
				dataType : "string",
				width : 100,
				editable : false,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "modifier",
				headerText : "수정자",
				dataType : "string",
				width : 100,
				editable : false,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "modifiedDate_txt",
				headerText : "수정일",
				dataType : "string",
				width : 100,
				editable : false,
				filter : {
					showIcon : true,
					inline : true
				},
			} ]
		

		function createAUIGrid(columnLayout) {
			const props = {
				headerHeight : 30,
				showRowNumColumn : true,
				showRowCheckColumn : true,
				showStateColumn : true,
				rowNumHeaderText : "번호",
				showAutoNoDataMessage : false,
				enableFilter : true,
				enableMovingColumn : true,
				showInlineFilter : true,
				useContextMenu : true,
				enableRightDownFocus : true,
				filterLayerWidth : 320,
				filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
				editable : false,
				fixedColumnCount : 1,
				pageRowCount: 20,
				showPageRowSelect: true,
				usePaging : true
			};
			myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
			loadGridData();
			AUIGrid.bind(myGridID, "vScrollChange", function(event) {
// 				vScrollChangeHandler(event);
			});
			AUIGrid.bind(myGridID, "hScrollChange", function(event) {
				hideContextMenu();
			});
			//AUIGrid.bind(myGridID, "cellEditBegin", auiCellEditBeginHandler);
			//AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
			//AUIGrid.bind(myGridID, "keyDown", auiKeyDownHandler);
		}

		function auiCellDoubleClick(event) {
			const dataField = event.dataField;
			const oid = event.item.oid;
			if (dataField === "number") {
				const url = getCallUrl("/numberRule/view?oid=" + oid);
				popup(url, 1000, 400);
			}
		}
		// enter 키 행 추가
		/*
		function auiKeyDownHandler(event) {
			if (event.keyCode == 13) { // 엔터 키
				var selectedItems = AUIGrid.getSelectedItems(event.pid);
				var rowIndex = selectedItems[0].rowIndex;
				if (rowIndex === AUIGrid.getRowCount(event.pid) - 1) { // 마지막 행인지 여부
					const item = {
						version : 1,
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
		*/
		function create() {
			const url = getCallUrl("/numberRule/create");
			popup(url, 1475, 450);
		}

		// 			function register() {
		// 				const url = getCallUrl("/numberRule/register");
		// 				popup(url);
		// 			}



		function loadGridData() {
			let params = new Object();
			console.log("############################");
			const url = getCallUrl("/numberRule/list");
			const field = [ "number" ];
			const latest = false;
			params = toField(params, field);
			params.latest = latest;
			AUIGrid.showAjaxLoader(myGridID);
			parent.openLayer();
			call(url, params, function(data) {
				AUIGrid.removeAjaxLoader(myGridID);
				$("input[name=sessionid]").val(data.sessionid);
				$("input[name=curPage]").val(data.curPage);
				console.log("############################=="+data.list);
				AUIGrid.setGridData(myGridID, data.list);
				parent.closeLayer();
			})
		}

		function deleteRow() {
			const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
			const sessionId = document.getElementById("sessionId").value;
			for (let i = checkedItems.length - 1; i >= 0; i--) {
				const item = checkedItems[i].item;
				if (!isNull(item.creatorId) && !checker(sessionId, item.creatorId)) {
					alert("데이터 작성자가 아닙니다.");
					return false;
				}
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
			AUIGrid.addRow(myGridID, item, "first");
		}

		function exportExcel() {
			const exceptColumnFields = [];
			const sessionName = document.getElementById("sessionName").value;
			exportToExcel("KEK 도번 리스트", "KEK 도번", "KEK 도번 리스트", exceptColumnFields, sessionName);
		}

			
			document.addEventListener("DOMContentLoaded", function() {
				$("#tabs").tabs({
					active : 0,
					activate : function(event, ui) {
						var tabId = ui.newPanel.prop("id");
						switch (tabId) {
						case "tabs-1":
							/*
							const isCreated50 = AUIGrid.isCreated(myGridID50);
							if (isCreated50) {
								AUIGrid.resize(myGridID50);
							} else {
								createAUIGrid50(columns50);
							}
							*/
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
			
			
			
		});



		window.addEventListener("resize", function() {
			gridResize();
		});
		
		function gridResize(){
			const ww = window.innerWidth;	//1654
			const hh = window.innerHeight;	//834
			console.log(ww+"=="+hh);
			//4row 15, 200
			//3row 15, 160
			//2row 15, 140
			AUIGrid.resize(myGridID, ww-15, hh-160);
		}
</script>