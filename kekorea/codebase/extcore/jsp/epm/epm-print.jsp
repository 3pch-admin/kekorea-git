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
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<style type="text/css">
a#link {
	display: none;
}
</style>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
</head>
<body>
	<form>
		<input type="hidden" name="items" id="items">
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
		<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
		<input type="hidden" name="sessionid" id="sessionid">
		<input type="hidden" name="lastNum" id="lastNum">
		<input type="hidden" name="curPage" id="curPage">

		<table class="button-table">
			<tr>
				<td class="left">
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
					<input type="button" value="도면 추가" class="blue" title="도면 추가" onclick="attach();">
					<input type="button" value="도면 삭제" title="도면 삭제" onclick="deleteRow();">
				</td>
				<td class="right">
					<input type="button" value="DWG ERP I/F" class="blue" title="DWG ERP I/F" onclick="sendErp();">
					<input type="button" value="DWG" class="" title="DWG" onclick="data('dwg');">
					<input type="button" value="PDF" class="orange" title="PDF" onclick="data('pdf');">
					<input type="button" value="프린트" class="red" title="프린트" onclick="print();">
				</td>
			</tr>
		</table>



		<div id="grid_wrap" style="height: 740px; border-top: 1px solid #3180c3;"></div>
		<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		<script type="text/javascript">
			let myGridID;
			function _layout() {
				return [ {
					dataField : "dwg",
					headerText : "DWG",
					dataType : "string",
					width : 150
				}, {
					dataField : "pdf",
					headerText : "PDF",
					dataType : "string",
					width : 150
				}, {
					dataField : "name",
					headerText : "파일이름",
					dataType : "string",
					style : "aui-left",
				// 					width : 150
				}, {
					dataField : "name_of_parts",
					headerText : "품명",
					dataType : "string",
					width : 300
				}, {
					dataField : "location",
					headerText : "FOLDER",
					dataType : "string",
					width : 250
				}, {
					dataField : "state",
					headerText : "상태",
					dataType : "string",
					width : 100
				}, {
					dataField : "version",
					headerText : "버전",
					dataType : "string",
					width : 100
				} ]
			}

			function createAUIGrid(columnLayout) {
				const props = {
					softRemoveRowMode : false,
					headerHeight : 30,
					showRowCheckColumn : true,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					selectionMode : "multipleCells",
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);

			}

			document.addEventListener("DOMContentLoaded", function() {
				const columns = loadColumnLayout("system-list");
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

			function gridResize() {
				//1636-629
				const ww = window.innerWidth; //1654
				const hh = window.innerHeight; //834
				AUIGrid.resize(myGridID, ww - 15, hh - 50);
			}

			function attach() {
				const url = getCallUrl("/epm/attach");
				popup(url, 1600, 800);
			}

			function data(type) {
				let url;
				if (type === "pdf") {
					url = getCallUrl("/epm/pdf");
				} else if (type === "dwg") {
					url = getCallUrl("/epm/dwg");
				}

				const checked = AUIGrid.getCheckedRowItems(myGridID);
				if (checked.length === 0) {
					alert("다운로드 할 도면을 선택하세요.");
					return false;
				}

				const arr = new Array();
				for (let i = 0; i < checked.length; i++) {
					arr.push(checked[i].item.oid);
				}

				let params = new Object();
				params.arr = arr;
				parent.openLayer();
				call(callUrl, params, function(data) {
					if (data.result) {
						const loc = data.url;
						$("a#link").attr("href", loc);
						document.getElementById("link").click();
					} else {
						alert(data.msg);
					}
					parent.closeLayer();
				})
			}

			function sendErp() {
				const checked = AUIGrid.getCheckedRowItems(myGridID);
				if (checked.length === 0) {
					alert("ERP 전송 할 도면을 선택하세요.");
					return false;
				}

				const arr = new Array();
				for (let i = 0; i < checked.length; i++) {
					arr.push(checked[i].item.oid);
				}

				const url = getCallUrl("/epm/sendErp");
				let params = new Object();
				params.arr = arr;
				parent.openLayer();
				call(callUrl, params, function(data) {
					alert(data.msg);
					if (data.result) {
						AUIGrid.clearGridData(myGridID1);
					}
					parent.closeLayer();
				})
			}

			function data(arr, callBack) {
				AUIGrid.setGridData(myGridID, arr);
				callBack(true);
			}

			function deleteRow() {
				const checked = AUIGrid.getCheckedRowItems(myGridID);
				if (checked.length === 0) {
					alert("삭제할 행을 선택하세요.");
					return false;
				}

				for (let i = checked.length - 1; i >= 0; i--) {
					const rowIndex = checked[i].rowIndex;
					AUIGrid.removeRow(myGridID, rowIndex);
				}
			};

			function print() {
				const checked = AUIGrid.getCheckedRowItems(myGridID);
				if (checked.length === 0) {
					alert("프린트 할 도면을 선택하세요.");
					return false;
				}

				let items;
				for (let i = 0; i < checked.length; i++) {
					const oid = checked[i].item.oid;
					items += oid + ",";
				}

				$("#items").val(items);
				var url = "/Windchill/extcore/jsp/epm/printClipboard.jsp";
				var title = "batchPrint";
				var opts = "toolbar=0,location=0,directory=0,status=1,menubar=0,scrollbars=1,resizable=1,";
				leftpos = (screen.width - 1000) / 2;
				toppos = (screen.height - 600) / 2;
				rest = "width=1000,height=600,left=" + leftpos + ',top=' + toppos;
				var newwin = window.open("", title, opts + rest);
				$("form").attr("target", title); // form.target
				$("form").attr("action", url); // form.action 이
				$("form").attr("method", "post");
				$("form").submit();
				newwin.focus();
			}

			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab18");
					if (tab != null) {
						const iframe = tab.querySelector('iframe');
						iframe.src = iframe.src;
					}
				}
			});
		</script>
		<a id="link"></a>
	</form>
</body>
</html>