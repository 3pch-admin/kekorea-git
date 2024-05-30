<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
ArrayList<Map<String, String>> customers = (ArrayList<Map<String, String>>) request.getAttribute("customers");
ArrayList<Map<String, String>> maks = (ArrayList<Map<String, String>>) request.getAttribute("maks");
ArrayList<Map<String, String>> projectTypes = (ArrayList<Map<String, String>>) request.getAttribute("projectTypes");
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
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
</head>
<body style="overflow:hidden;">
	<form>
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
		<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
		<input type="hidden" name="sessionid" id="sessionid"><input type="hidden" name="lastNum" id="lastNum">
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
				<th>제목</th>
				<td class="indent5">
					<input type="text" name="name" id="name" class="width-200">
				</td>
				<th>작성자</th>
				<td class="indent5">
					<input type="text" name="creator" id="creator" data-multi="false">
					<input type="hidden" name="creatorOid" id="creatorOid">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('creator')">
				</td>
				<th>작성일</th>
				<td class="indent5">
					<input type="text" name="createdFrom" id="createdFrom" class="width-100">
					~
					<input type="text" name="createdTo" id="createdTo" class="width-100">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('createdFrom', 'createdTo')">
				</td>
			</tr>
		</table>

		<table class="button-table">
			<tr>
				<td class="left">
					<img src="/Windchill/extcore/images/fileicon/file_excel.gif" title="엑셀 다운로드" onclick="exportExcel();">
					<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('contract-list');">
					<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('contract-list');">
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
					<!-- <img src="/Windchill/extcore/images/help.gif" title="메뉴얼 재생" onclick="play('test1.mp4');"> -->
				</td>
				<td class="right">
					<input type="button" value="등록" title="등록" class="blue" onclick="create();">
					<!-- <input type="button" value="확장" title="확장" onclick="expand();" style="background-color: orange;"> -->
					<%
					if (isAdmin) {
					%>
					<!--  <input type="button" value="저장" title="저장" onclick="save();"> -->
					<!-- <input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();"> -->
					<%
					}
					%>
					<input type="button" value="조회" title="조회" onclick="loadGridData();" style="background-color: navy;">
				</td>
			</tr>
		</table>
		
		<!-- 메뉴얼 비디오 구간 -->
		<%@include file="/extcore/jsp/common/video-layer.jsp"%>

		<div id="grid_wrap" style="height: 635px; border-top: 1px solid #3180c3;"></div>
		<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		<script type="text/javascript">
			let myGridID;
			function _layout() {
				return [ {
					dataField : "name",
					headerText : "결재 제목",
					dataType : "string",
					width : 850,
					style : "aui-left",
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/workspace/view?oid=" + oid);
							popup(url, 1600, 1000);
						}
					},
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
						inline : true,
					},
				} ]
			}

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
					pageRowCount: 20,
					showPageRowSelect: true,
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
				AUIGrid.bind(myGridID, "cellDoubleClick", auiCellDoubleClickHandler);
			}

			function auiCellDoubleClickHandler(event) {
				const dataField = event.dataField;
				const oid = event.item.oid;
				if (dataField === "name") {
					const url = getCallUrl("/workspace/view?oid=" + oid);
					popup(url);
				}
			}

			function loadGridData() {
				let params = new Object();
				const url = getCallUrl("/workspace/list");
				const field = [ "name", "creatorOid", "createdFrom", "createdTo" ];
				params = toField(params, field);
				AUIGrid.showAjaxLoader(myGridID);
				parent.openLayer();
				call(url, params, function(data) {
					AUIGrid.removeAjaxLoader(myGridID);
					document.getElementById("sessionid").value = data.sessionid;
					document.getElementById("curPage").value = data.curPage;document.getElementById("lastNum").value = data.list.length;
					AUIGrid.setGridData(myGridID, data.list);
					parent.closeLayer();
				});
			}

			function create() {
				const url = getCallUrl("/workspace/create");
				popup(url,1300, 900);
			}
			document.addEventListener("DOMContentLoaded", function() {
				toFocus("name");
				const columns = loadColumnLayout("contract-list");
				const contenxtHeader = genColumnHtml(columns);
				$("#h_item_ul").append(contenxtHeader);
				$("#headerMenu").menu({
					select : headerMenuSelectHandler
				});
				createAUIGrid(columns);
				gridResize();
				finderUser("creator");
				twindate("created");
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
				gridResize();
			});
			function gridResize(){
				//1636-629
				const ww = window.innerWidth;	//1654
				const hh = window.innerHeight;	//834
				AUIGrid.resize(myGridID, ww-15, hh-100);
			}
			
			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab17");
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