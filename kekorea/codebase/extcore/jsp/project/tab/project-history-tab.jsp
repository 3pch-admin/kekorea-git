<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.korea.configSheet.beans.ConfigSheetDTO"%>
<%@page import="net.sf.json.JSONArray"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	JSONArray data = (JSONArray) request.getAttribute("data");
	boolean isAdmin = (boolean) request.getAttribute("isAdmin");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<body>
	<form>
		<table class="button-table">
			<tr>
				<td class="right">
					<input type="button" value="저장" title="저장" class="blue" onclick="save();">
				</td>
			</tr>
		</table>

		<div id="grid_wrap" style="height: 670px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID;
			const columns = [ {
				dataField : "key",
				headerText : "이력관리컬럼",
				dataType : "string",
				style : "aui-left",
				editable : false,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "value",
				headerText : "데이터",
				dataType : "string",
				style : "aui-left",
				filter : {
					showIcon : true,
					inline : true
				},
			}, ]

			function createAUIGrid(columnLayout) {
				const props = {
					editable : true,
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					selectionMode : "singleRow",
					enableFilter : true,
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
				AUIGrid.setGridData(myGridID,
		<%=data%>
			);
			}

			function save() {
				const url = getCallUrl("/history/dataSave");
				const editRows = AUIGrid.getEditedRowItems(myGridID);
				const params = new Object();

				if (editRows.length === 0) {
					alert("변경된 내용이 없습니다.");
					return false;
				}

				if (!confirm("저장 하시겠습니까?")) {
					return false;
				}

				params.editRows = editRows;
				parent.parent.openLayer();
				call(url, params, function(data) {
					alert(data.msg);
					if (data.result) {
						document.location.reload();
					}
					parent.parent.closeLayer();
				})
			}
			document.addEventListener("DOMContentLoaded", function() {
				createAUIGrid(columns);
				AUIGrid.resize(myGridID);
			})

			window.addEventListener("resize", function() {
				AUIGrid.resize(myGridID);
			});
		</script>
	</form>
</body>

</form>
</body>
</head>
</html>