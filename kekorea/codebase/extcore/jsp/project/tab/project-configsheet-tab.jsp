<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.korea.configSheet.beans.ConfigSheetDTO"%>
<%@page import="net.sf.json.JSONArray"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	JSONArray data = (JSONArray) request.getAttribute("data");
	ConfigSheetDTO dto = (ConfigSheetDTO) request.getAttribute("dto");
	String oid = (String) request.getAttribute("oid");
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
<style type="text/css">
.comp {
	background: yellow;
	color: red;
	font-weight: bold;
}

.row_1 {
	background-color: #fed7be;
	font-weight: bold;
}

.row_2 {
	background-color: #FFCCFF;
	font-weight: bold;
}

.row_3 {
	background-color: #CCFFCC;
	font-weight: bold;
}

.row_4 {
	background-color: #FFFFCC;
	font-weight: bold;
}

.row1 {
	background-color: #99CCFF;
}

.row2 {
	background-color: #FFCCFF;
}

.row3 {
	background-color: #CCFFCC;
}

.row4 {
	background-color: #FFFFCC;
}

.row5 {
	background-color: #FFCC99;
}

.row6 {
	background-color: #CCCCFF;
}

.row7 {
	background-color: #99FF66;
}

.row8 {
	background-color: #CC99FF;
}

.row9 {
	background-color: #66CCFF;
}

.row10 {
	background-color: #CCFFCC;
}

.row11 {
	background-color: #FFCCFF;
}

.row12 {
	background-color: #FFFFCC;
}
</style>
</head>
<body>
	<form>
<%-- 		<input type="hidden" name="oid" id="oid" value="<%=dto !=null ? dto.getOid() : ""%>"> --%>
		<div id="grid_wrap1000" style="height: 780px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID1000;
			const data = <%=data%>
			const columns1000 = [ {
				dataField : "category_name",
				headerText : "CATEGORY",
				dataType : "string",
				width : 250,
				cellMerge : true,
				cellColMerge: true, // 셀 가로 병합 실행
				cellColSpan: 2, // 셀 가로 병합 대상은 6개로 설정
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "item_name",
				headerText : "ITEM",
				dataType : "string",
				width : 200,
				cellMerge : true,
				mergeRef : "category_name",
				mergePolicy : "restrict",
				filter : {
					showIcon : true,
					inline : true
				},
			}, 
			<%
			if(dto != null) {
			int index = 0;
			ArrayList<String> dd = dto.getDataFields();
			for (int i = 0; i < dd.size(); i++) {
				String dataFields = dd.get(i);
				String key = dd.get(0);%>
			{
				dataField : "<%=dataFields%>",
				headerText : "사양<%=index%>",
				dataType : "string",
				width : 250,
				styleFunction : function(rowIndex, columnIndex, value, headerText, item, dataField) {
					const vv = item.<%=key%>;
					if(vv !== value) {
						return "comp";
					}
				},				
				renderer : {
					type : "Templaterenderer"
				},
				filter : {
					showIcon : true,
					inline : true
				},
			}, 
			<%index++;
			}
			}%>
			{
				dataField : "note",
				headerText : "NOTE",
				dataType : "string",
				width : 250,
				styleFunction : function(rowIndex, columnIndex, value, headerText, item, dataField) {
					const vv = item.note;
					if(vv !== value) {
						return "comp";
					}
				},				
				renderer : {
					type : "Templaterenderer"
				},
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "apply",
				headerText : "APPLY",
				dataType : "string",
				width : 350,
				styleFunction : function(rowIndex, columnIndex, value, headerText, item, dataField) {
					const vv = item.apply;
					if(vv !== value) {
						return "comp";
					}
				},				
				renderer : {
					type : "Templaterenderer"
				},
				filter : {
					showIcon : true,
					inline : true
				},
			} ]

			function createAUIGrid1000(columnLayout) {
				const props = {
					headerHeight : 30,
					showAutoNoDataMessage : false,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					selectionMode : "multipleCells",
					enableCellMerge : true,
					enableSorting : false,
					wordWrap : true,
					enableFilter : true,
					showInlineFilter : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",					
					//fixedRowCount : 4,
					rowStyleFunction : function(rowIndex, item) {
						const value = item.category_code;
						const name = item.category_name;
						if (value === "CATEGORY_2") {
							return "row1";
						} else if (value === "CATEGORY_3") {
							return "row2";
						} else if (value === "CATEGORY_4") {
							return "row3";
						} else if (value === "CATEGORY_5") {
							return "row4";
						} else if (value === "CATEGORY_6") {
							return "row5";
						} else if (value === "CATEGORY_7") {
							return "row6";
						} else if (value === "CATEGORY_8" || value === "CATEGORY_9") {
							return "row7";
						} else if (value === "CATEGORY_10") {
							return "row8";
						} else if (value === "CATEGORY_11") {
							return "row9";
						} else if (value === "CATEGORY_12") {
							return "row4";
						} else if (value === "CATEGORY_13") {
							return "row10";
						} else if (value === "CATEGORY_14") {
							return "row11";
						} else if (value === "CATEGORY_15") {
							return "row12";
						} 
						
						if(name === "막종 / 막종상세") {
							return "row_1";
						} else if(name === "고객사 / 설치장소") {
							return "row_2";
						} else if(name === "KE 작번") {
							return "row_3";
						} else if(name === "발행일") {
							return "row_4";
						}
						return "";
					}
				};
				myGridID1000 = AUIGrid.create("#grid_wrap1000", columns1000, props);
				AUIGrid.setGridData(myGridID1000, data);
			}
			
			document.addEventListener("DOMContentLoaded", function() {
				// 화면 활성화시 불러오게 설정한다 속도 생각 
				createAUIGrid1000(columns1000);
				AUIGrid.resize(myGridID1000);
			})

			window.addEventListener("resize", function() {
				AUIGrid.resize(myGridID1000);
			});
		</script>
	</form>
</body>
</html>