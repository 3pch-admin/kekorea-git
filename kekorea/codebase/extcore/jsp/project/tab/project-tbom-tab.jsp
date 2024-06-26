<%@page import="e3ps.bom.tbom.service.TBOMHelper"%>
<%@page import="java.util.Map"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.korea.cip.dto.CipDTO"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String oid = request.getParameter("oid");
JSONArray data = (JSONArray) request.getAttribute("data");
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
		<div id="grid_wrap6" style="height: 780px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID6;
			const data = <%=data%>
			const columns6 = [ {
				dataField : "lotNo",
				headerText : "LOT",
				dataType : "numeric",
				width : 100,
				formatString : "###0",
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "code",
				headerText : "중간코드",
				dataType : "string",
				width : 130,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "keNumber",
				headerText : "부품번호",
				dataType : "string",
				width : 150,
				renderer : {
					type : "LinkRenderer",
					baseUrl : "javascript",
					jsCallback : function(rowIndex, columnIndex, value, item) {
						const oid = item.oid;
						const url = getCallUrl("/kePart/view?oid=" + oid);
						popup(url, 1400, 700);
					}
				},
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "name",
				headerText : "부품명",
				dataType : "string",
				renderer : {
					type : "LinkRenderer",
					baseUrl : "javascript",
					jsCallback : function(rowIndex, columnIndex, value, item) {
						const oid = item.oid;
						const url = getCallUrl("/kePart/view?oid=" + oid);
						popup(url, 1400, 700);
					}
				},
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "model",
				headerText : "규격",
				dataType : "string",
				width : 200,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "qty",
				headerText : "QTY",
				dataType : "numeric",
				width : 100,
				formatString : "###0",
				filter : {
					showIcon : true,
					inline : false,
					type : "numeric"
				},
			}, {
				dataField : "unit",
				headerText : "UNIT",
				dataType : "string",
				width : 130,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "provide",
				headerText : "PROVIDE",
				dataType : "string",
				width : 130,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "discontinue",
				headerText : "DISCONTINUE",
				dataType : "string",
				width : 200,
				filter : {
					showIcon : true,
					inline : true
				},
			} ]

			function createAUIGrid6(columnLayout) {
				const props = {
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					selectionMode : "multipleCells",
					showAutoNoDataMessage : false,
					enableFilter : true,
					showInlineFilter : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
				};
				myGridID6 = AUIGrid.create("#grid_wrap6", columnLayout, props);
				AUIGrid.setGridData(myGridID6, data);
			}

			document.addEventListener("DOMContentLoaded", function() {
				// 화면 활성화시 불러오게 설정한다 속도 생각 
				createAUIGrid6(columns6);
				AUIGrid.resize(myGridID6);
			})

			window.addEventListener("resize", function() {
				AUIGrid.resize(myGridID6);
			});
		</script>
	</form>
</body>
</html>