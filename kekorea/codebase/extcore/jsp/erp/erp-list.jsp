<%@page import="net.sf.json.JSONArray"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="wt.org.WTUser"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
boolean isSupervisor = (boolean) request.getAttribute("isSupervisor");
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
		<input type="hidden" name="sessionid" id="sessionid">
		<input type="hidden" name="lastNum" id="lastNum">
		<input type="hidden" name="curPage" id="curPage">

		<table class="button-table">
			<tr>
				<td class="left">
					<img src="/Windchill/extcore/images/fileicon/file_excel.gif" title="엑셀 다운로드" onclick="exportExcel();">
					<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('erp-list');">
					<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('cip-list');">
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
					<!--  <img src="/Windchill/extcore/images/help.gif" title="메뉴얼 재생" onclick="play('test.mp4');"> -->
				</td>
				<td class="right">
<!-- 					<input type="button" value="조회" title="조회" onclick="loadGridData();" style="background-color: navy;"> -->
				</td>
			</tr>
		</table>

		<!-- 메뉴얼 비디오 구간 -->
<%-- 		<%@include file="/extcore/jsp/common/video-layer.jsp"%> --%>

		<div id="grid_wrap" style="height: 740px; border-top: 1px solid #3180c3;"></div>
		<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		<script type="text/javascript">
			let myGridID;
			function _layout() {
				return [ {
					dataField : "sendResult",
					headerText : "성공여부",
					dataType : "boolean",
					width : 60,
					filter : {
						showIcon : false,
						inline : false
					},
					renderer : {
						type : "CheckBoxEditRenderer",
					}
				}, {
					dataField : "name",
					headerText : "ERP 로그 제목",
					dataType : "string",
					style : "aui-left",
					width : 350,
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "sendType",
					headerText : "ERP 로그 타입",
					dataType : "string",
					width : 150,
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "resultMsg",
					headerText : "ERP 로그 메세지",
					dataType : "string",
					style : "aui-left",
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "sendQuery",
					headerText : "ERP 로그 쿼리",
					dataType : "string",
					style : "aui-left",
					width : 350,
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
					width : 150,
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
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					enableMovingColumn : true,
					selectionMode : "multipleCells",
					showInlineFilter : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
					pageRowCount: 20,
					showPageRowSelect: true,
					usePaging : true
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				loadGridData();
				AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
			}

			function loadGridData() {
				let params = new Object();
				const url = getCallUrl("/erp/list");
// 				params = toField(params, field);
				AUIGrid.showAjaxLoader(myGridID);
				parent.openLayer();
				call(url, params, function(data) {
					if (data.result) {
						AUIGrid.removeAjaxLoader(myGridID);
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

			function exportExcel() {
				const exceptColumnFields = [ "preView", "preViewBtn", "icons", "iconsBtn" ];
				const sessionName = document.getElementById("sessionName").value;
				exportToExcel("CIP 리스트", "CIP", "CIP 리스트", exceptColumnFields, sessionName);
			}

			document.addEventListener("DOMContentLoaded", function() {
				const columns = loadColumnLayout("erp-ist");
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
			
			function gridResize(){
				//1636-629
				const ww = window.innerWidth;	//1654
				const hh = window.innerHeight;	//834
				AUIGrid.resize(myGridID, ww-15, hh-50);
			}
			
			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab38");
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