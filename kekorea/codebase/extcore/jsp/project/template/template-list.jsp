<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
boolean isSupervisor = (boolean) request.getAttribute("isSupervisor");
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
<body style="overflow: hidden;">
	<form>
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
		<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
		<input type="hidden" name="sessionid" id="sessionid">
		<input type="hidden" name="lastNum" id="lastNum">
		<input type="hidden" name="curPage" id="curPage">
		<table class="search-table">
			<colgroup>
				<col width="130">
				<col width="*">
				<col width="130">
				<col width="*">
				<col width="130">
				<col width="*">
			</colgroup>
			<tr>
				<th>템플릿 명</th>
				<td colspan="7" class="indent5">
					<input type="text" name="name" id="name" class="width-600">
				</td>
			</tr>
			<tr>
				<th>작성자</th>
				<td class="indent5">
					<input type="text" name="creator" id="creator" data-multi="false">
					<input type="hidden" name="creatorOid" id="creatorOid">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('creator');">
				</td>
				<th>작성일</th>
				<td class="indent5">
					<input type="text" name="createdFrom" id="createdFrom" class="width-100">
					~
					<input type="text" name="createdTo" id="createdTo" class="width-100">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('createdFrom', 'createdTo');">
				</td>
				<th>수정자</th>
				<td class="indent5">
					<input type="text" name="modifier" id="modifier" data-multi="false">
					<input type="hidden" name="modifierOid" id="modifierOid">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('modifier');">
				</td>
				<th>수정일</th>
				<td class="indent5">
					<input type="text" name="modifiedFrom" id="modifiedFrom" class="width-100">
					~
					<input type="text" name="modifiedTo" id="modifiedTo" class="width-100">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('modifiedFrom', 'modifiedTo');">
				</td>
			</tr>
		</table>

		<table class="button-table">
			<tr>
				<td class="left">
					<img src="/Windchill/extcore/images/fileicon/file_excel.gif" title="엑셀 다운로드" onclick="exportExcel();">
					<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('template-list');">
					<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('template-list');">
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
				</td>
				<td class="right">
					<%
					if (isSupervisor) {
					%>
					<input type="button" value="등록" title="등록" class="blue" onclick="create();">
					<!--	<input type="button" value="저장" title="저장" onclick="save();">	-->
					<!--	<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">	-->
					<%
					}
					%>
					<input type="button" value="조회" title="조회" style="background-color: navy;" onclick="loadGridData();">
				</td>
			</tr>
		</table>

		<!-- 메뉴얼 비디오 구간 -->
		<%@include file="/extcore/jsp/common/video-layer.jsp"%>

		<div id="grid_wrap" style="height: 680px; border-top: 1px solid #3180c3;"></div>
		<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		<script type="text/javascript">
			let myGridID;
			function _layout() {
				return [ {
					dataField : "name",
					headerText : "템플릿 명",
					dataType : "string",
					width : 350,
					style : "aui-left",
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/template/info?oid=" + oid);
							popup(url, 1400, 630);
						}
					},
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "description",
					headerText : "내용",
					dataType : "string",
					style : "aui-left",
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "duration",
					headerText : "기간",
					dataType : "numeric",
					width : 100,
					postfix : "일",
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "enable",
					headerText : "사용여부",
					dataType : "boolean",
					width : 100,
					filter : {
						showIcon : false,
						inline : false
					},
					renderer : {
						type : "CheckBoxEditRenderer"
					}
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
					dataField : "createdDate",
					headerText : "작성일",
					dataType : "date",
					formatString : "yyyy-mm-dd",
					width : 100,
					filter : {
						showIcon : true,
						inline : true,
						displayFormatValues : true
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
					dataField : "modifiedDate",
					headerText : "수정일",
					dataType : "date",
					formatString : "yyyy-mm-dd",
					width : 100,
					filter : {
						showIcon : true,
						inline : true,
						displayFormatValues : true
					},
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
					selectionMode : "singleRow",
					enableMovingColumn : true,
					showInlineFilter : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
					pageRowCount : 20,
					showPageRowSelect : true,
					usePaging : true
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				loadGridData();
				AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
				AUIGrid.bind(myGridID, "vScrollChange", function(event) {
					hideContextMenu();
					// 					vScrollChangeHandler(event);
				});
				AUIGrid.bind(myGridID, "hScrollChange", function(event) {
					hideContextMenu();
				});
			}

			function loadGridData() {
				let params = new Object();
				const url = getCallUrl("/template/list");
				const field = [ "name", "creatorOid", "createdFrom", "createdTo", "modifierOid", "modifiedFrom", "modifiedTo" ];
				params = toField(params, field);
				AUIGrid.showAjaxLoader(myGridID);
				parent.openLayer();
				call(url, params, function(data) {
					AUIGrid.removeAjaxLoader(myGridID);
					if (data.result) {
						document.getElementById("sessionid").value = data.sessionid;
						document.getElementById("curPage").value = data.curPage;
						document.getElementById("lastNum").value = data.list.length;
						AUIGrid.setGridData(myGridID, data.list);
					} else {
						alert(data.msg);
					}
					parent.closeLayer();
				});
			}

			function create() {
				const url = getCallUrl("/template/create");
				popup(url, 1200, 300);
			}

			function deleteRow() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
				const sessionId = document.getElementById("sessionId").value;
				for (let i = checkedItems.length - 1; i >= 0; i--) {
					const item = checkedItems[i].item;
					if (!checker(sessionId, item.creatorId) || !checker(sessionId, item.modifierId)) {
						alert("데이터 작성자 또는 수정자가 아닙니다.");
						return false;
					}
					const rowIndex = checkedItems[i].rowIndex;
					AUIGrid.removeRow(myGridID, rowIndex);
				}
			}

			function save() {
				const url = getCallUrl("/template/delete");
				const params = new Object();
				const removeRows = AUIGrid.getRemovedItems(myGridID);
				if (removeRows.length === 0) {
					alert("변경된 내용이 없습니다.");
					return false;
				}

				params.removeRows = removeRows;

				if (!confirm("저장 하시겠습니까?")) {
					return false;
				}
				parent.openLayer();
				call(url, params, function(data) {
					alert(data.msg);
					parent.closeLayer();
					if (data.result) {
						loadGridData();
					}
				});
			}

			document.addEventListener("DOMContentLoaded", function() {
				document.getElementById("name").focus();
				const columns = loadColumnLayout("template-list");
				const contenxtHeader = genColumnHtml(columns);
				$("#h_item_ul").append(contenxtHeader);
				$("#headerMenu").menu({
					select : headerMenuSelectHandler
				});
				createAUIGrid(columns);
				AUIGrid.resize(myGridID);
				finderUser("creator");
				twindate("created");
				finderUser("modifier");
				twindate("modified");
			});

			function exportExcel() {
				const exceptColumnFields = [ "enable" ];
				const sessionName = document.getElementById("sessionName").value;
				exportToExcel("템플릿 리스트", "템플릿", "템플릿 리스트", exceptColumnFields, sessionName);
			}

			document.addEventListener("keydown", function(event) {
				const keyCode = event.keyCode || event.which;
				if (keyCode === 13) {
					loadGridData();
				}
			})

			document.addEventListener("click", function(event) {
				hideContextMenu();
			})

			window.addEventListener("resize", function() {
				AUIGrid.resize(myGridID);
			});
			function gridResize() {
				const ww = window.innerWidth; //1654
				const hh = window.innerHeight; //834
				console.log(ww + "==" + hh);
				//4row 15, 200
				//2row 15, 140
				AUIGrid.resize(myGridID, ww - 15, hh - 140);
			}
			

			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab11");
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