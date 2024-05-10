<%@page import="e3ps.workspace.dto.ApprovalContractDTO"%>
<%@page import="wt.fc.Persistable"%>
<%@page import="e3ps.project.output.OutputDocumentMasterLink"%>
<%@page import="e3ps.doc.E3PSDocument"%>
<%@page import="e3ps.project.output.OutputDocumentLink"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="e3ps.doc.PRJDocument"%>
<%@page import="wt.doc.WTDocument"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.project.output.Output"%>
<%@page import="wt.query.ClassAttribute"%>
<%@page import="wt.util.WTAttributeNameIfc"%>
<%@page import="wt.enterprise.RevisionControlled"%>
<%@page import="wt.vc.ControlBranch"%>
<%@page import="wt.vc.VersionControlHelper"%>
<%@page import="wt.query.SearchCondition"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="e3ps.project.output.dto.OutputDTO"%>
<%@page import="wt.session.SessionHelper"%>
<%@page import="wt.org.WTUser"%>
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
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) SessionHelper.manager.getPrincipal();
ApprovalContractDTO dto = (ApprovalContractDTO) request.getAttribute("dto");
JSONArray contractEPMList = (JSONArray)request.getAttribute("contractEPMList");
JSONArray contractNumberRuleList = (JSONArray)request.getAttribute("contractNumberRuleList");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				도면 결재 정보
			</div>
		</td>
		<td class="right">
			<input type="button" value="수정" title="수정" class="blue" onclick="update();">
			<%
			if (isAdmin) {
			%>
			<input type="button" value="삭제" title="삭제" class="red" onclick="_delete();">
			<%
			}
			%>
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
			<a href="#tabs-3">결재이력</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="view-table">
			<colgroup>
				<col width="200">
				<col width="*">
			</colgroup>
			<tr>
				<th class="lb">도면 결재 제목</th>
				<td class="indent5"><%=dto.getName()%></td>
			</tr>
			<tr>
				<th class="lb">결재 의견</th>
				<td class="indent5">
					<textarea id="description" name="description" rows="5"><%=dto.getDescription()%></textarea>
					
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
						AUIGrid.setGridData(myGridID11,<%=contractNumberRuleList%> );
					}
				</script>
			</div>
		</td>
	</tr>
	<tr>
		<th class="req lb">결재 도면</th>
		<td>
			<div class="include">
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
						AUIGrid.setGridData(myGridID,<%=contractEPMList%> );
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
		</table>
	</div>
	<div id="tabs-3">
		<!-- 결재이력 -->
		<jsp:include page="/extcore/jsp/common/approval-history.jsp">
			<jsp:param value="<%=dto.getOid()%>" name="oid" />
		</jsp:include>
	</div>
</div>
<script type="text/javascript">
	function update() {
		const oid = document.getElementById("oid").value;
		const url = getCallUrl("/workspace/update?oid=" + oid);
		openLayer();
		document.location.href = url;
	}
	
	
	function _delete() {
		
		if(!confirm("삭제하시겠습니까?")) {
			return false;
		}
		
		const oid = document.getElementById("oid").value;
		const url = getCallUrl("/workspace/deleteContract?oid=" + oid);
		openLayer();
		call(url, null, function(data) {
			alert(data.msg);
			if (data.result) {
 				opener.loadGridData();
				self.close();
			} else {
				closeLayer();
			}
		}, "GET");
	}

	document.addEventListener("DOMContentLoaded", function() {
		$("#tabs").tabs({
			active : 0,
			activate : function(event, ui) {
				var tabId = ui.newPanel.prop("id");
				switch (tabId) {
				case "tabs-1":
					const isCreated11 = AUIGrid.isCreated(myGridID11);
					if (isCreated11) {
						gridResize11();
					} else {
						createAUIGrid9(columns11);
					}
					const isCreated = AUIGrid.isCreated(myGridID);
					if (isCreated) {
						gridResize();
					} else {
						createAUIGrid9(columns);
					}
					
					break;
				case "tabs-3":
					const isCreated100 = AUIGrid.isCreated(myGridID100);
					if (isCreated100) {
						AUIGrid.resize(myGridID100);
					} else {
						createAUIGrid100(columns100);
					}
					break;
				}
			}
		});
		createAUIGrid11(columns11);
		createAUIGrid(columns);
		createAUIGrid100(columns100);
		gridResize11();
		AUIGrid.resize(myGridID);
		AUIGrid.resize(myGridID100);
		
	})

	window.addEventListener("resize", function() {
		gridResize11();
		gridResize();
		AUIGrid.resize(myGridID100);
	});
	
	function gridResize11(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID11, ww-150, 200);
	}
	function gridResize(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID, ww-150, 200);
	}
</script>