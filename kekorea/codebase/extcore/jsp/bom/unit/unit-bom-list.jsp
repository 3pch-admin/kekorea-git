<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
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
				<col width="100">
				<col width="500">
				<col width="100">
				<col width="500">
				<col width="100">
				<col width="500">
			</colgroup>
			<tr>
				<th>UCODE</th>
				<td class="indent5">
					<input type="text" name="uCode" id="uCode" class="width-200">
				</td>
				<th>YCODE</th>
				<td class="indent5">
					<input type="text" name="yCode" id="yCode">
				</td>
				<th>UCODE SPEC</th>
				<td class="indent5">
					<input type="text" name="uSpec" id="uSpec">
				</td>
			</tr>
			<tr>
				<th>YCODE SPEC</th>
				<td class="indent5">
					<input type="text" name="ySpec" id="ySpec" class="width-200">
				</td>
				<th>UCODE 품명</th>
				<td class="indent5">
					<input type="text" name="uPartName" id="uPartName">
				</td>
				<th>YCODE 품명</th>
				<td class="indent5">
					<input type="text" name="yPartName" id="yPartName">
				</td>
			</tr>
		</table>

		<table class="button-table">
			<tr>
				<td class="left">
					<img src="/Windchill/extcore/images/fileicon/file_excel.gif" title="엑셀 다운로드" onclick="exportExcel();">
					<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('unit-list');">
					<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('unit-list');">
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
					<!-- <img src="/Windchill/extcore/images/help.gif" title="메뉴얼 재생" onclick="play('test.mp4');">  -->
				</td>
				<td class="right">
					<input type="button" value="등록" title="등록" class="blue" onclick="create();">
					<!--	<input type="button" value="확장" title="확장" onclick="expand();" style="background-color: orange;"> -->
					<input type="button" value="조회" title="조회" onclick="loadGridData();" style="background-color: navy;">
				</td>
			</tr>
		</table>


		<div id="grid_wrap" style="height: 680px; border-top: 1px solid #3180c3;"></div>
		<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		<script type="text/javascript">
			let myGridID;
			function _layout() {
				return [ {
					dataField : "ucode",
					headerText : "UCODE",
					dataType : "string",
					width : 150,
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/unit/view?oid=" + oid);
							popup(url, 1500, 800);
						}
					},
					filter : {
						showIcon : true,
						inline : true,
					},
				}, {
					dataField : "partName",
					headerText : "품명",
					dataType : "string",
					width : 200,
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/unit/view?oid=" + oid);
							popup(url, 1500, 500);
						}
					},
					filter : {
						showIcon : true,
						inline : true,
					},
				}, {
					dataField : "spec",
					headerText : "규격",
					dataType : "string",
					filter : {
						showIcon : true,
						inline : true,
					},
				}, {
					dataField : "unit",
					headerText : "기준단위",
					dataType : "string",
					width : 150,
					filter : {
						showIcon : true,
						inline : true,
					},
				}, {
					dataField : "maker",
					headerText : "메이커",
					dataType : "string",
					width : 100,
					filter : {
						showIcon : true,
						inline : true,
					},
				}, {
					dataField : "customer",
					headerText : "기본구매처",
					dataType : "string",
					width : 100,
					filter : {
						showIcon : true,
						inline : true,
					},
				}, {
					dataField : "price",
					headerText : "단가",
					dataType : "numeric",
					width : 100,
					formatString : "#,##0",
					postfix : "원",
					filter : {
						showIcon : true,
						inline : true,
					},
				}, {
					dataField : "currency",
					headerText : "통화",
					dataType : "numeric",
					width : 100,
					formatString : "#,##0",
					filter : {
						showIcon : true,
						inline : true,
					},
				} ]
			};

			function createAUIGrid(columnLayout) {
				const props = {
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					selectionMode : "multipleCells",
					enableMovingColumn : true,
					showInlineFilter : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
					editable : false,
					pageRowCount : 20,
					showPageRowSelect : true,
					usePaging : true
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				loadGridData();
				AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
				AUIGrid.bind(myGridID, "vScrollChange", function(event) {
					hideContextMenu();
				});
				AUIGrid.bind(myGridID, "hScrollChange", function(event) {
					hideContextMenu();
				});
			}

			function loadGridData() {
				let params = new Object();
				const url = getCallUrl("/unit/list");
				const field = [ "uCode", "yCode", "uSpec", "ySpec", "uPartName", "yPartName" ];
				params = toField(params, field);
				AUIGrid.showAjaxLoader(myGridID);
				parent.openLayer();
				call(url, params, function(data) {
					console.log(data);
					AUIGrid.removeAjaxLoader(myGridID);
					document.getElementById("sessionid").value = data.sessionid;
					document.getElementById("curPage").value = data.curPage;
					AUIGrid.setGridData(myGridID, data.list);
					parent.closeLayer();
				});
			}

			function create() {
				const url = getCallUrl("/unit/create");
				popup(url, 1500, 800);
			}

			function exportExcel() {
// 				const exceptColumnFields = [ "latest" ];
// 				const sessionName = document.getElementById("sessionName").value;
// 				exportToExcel("TBOM 리스트", "TBOM", "TBOM 리스트", exceptColumnFields, sessionName);
			}

			document.addEventListener("DOMContentLoaded", function() {
				toFocus("uCode");
				const columns = loadColumnLayout("unit-list");
				const contenxtHeader = genColumnHtml(columns);
				$("#h_item_ul").append(contenxtHeader);
				$("#headerMenu").menu({
					select : headerMenuSelectHandler
				});
				createAUIGrid(columns);
				AUIGrid.resize(myGridID);
			});

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
			
			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab33");
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