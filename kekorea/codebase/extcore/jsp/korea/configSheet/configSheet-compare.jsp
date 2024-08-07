<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.project.Project"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.bom.partlist.dto.PartListDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
JSONArray data = (JSONArray) request.getAttribute("data");
Project p1 = (Project) request.getAttribute("p1");
ArrayList<Project> destList = (ArrayList<Project>) request.getAttribute("destList");
String oid = (String) request.getAttribute("oid");
String compareArr = (String) request.getAttribute("compareArr");
ArrayList<Map<String, String>> fixedList = (ArrayList<Map<String, String>>) request.getAttribute("fixedList");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=11210"></script>
<style type="text/css">
.compare {
	background: #FFFF00 !important;
	color: #FF0000;
	font-weight: bold;
}

.row1 {background-color: #99CCFF;}
.row2 {background-color: #FFCCFF;}
.row3 {background-color: #CCFFCC;}
.row4 {background-color: #FFFFCC;}
.row5 {background-color: #FFCC99;}
.row6 {background-color: #CCCCFF;}
.row7 {background-color: #99FF66;}
.row8 {background-color: #CC99FF;}
.row9 {background-color: #66CCFF;}
.row10{background-color: #CCFFCC;}
.row11{background-color: #FFCCFF;}
.row12{	background-color: #FFFFCC;}
.row1-1 {
	background-color: #fed7be;
	font-weight: bold;
}
.row2-1 {
	background-color: #FFCCFF;
	font-weight: bold;
}

.row3-1 {
	background-color: #CCFFCC;
	font-weight: bold;
}

.row4-1 {
	background-color: #FFFFCC;
	font-weight: bold;
}

.none {
	color: black;
	font-weight: bold;
	cursor: pointer;
	text-align: center !important;
}

.link {
	color: blue;
	font-weight: bold;
	cursor: pointer;
	text-align: center !important;
}

.link:hover {
	color: blue !important;
	text-decoration: underline;
}
</style>
<input type="hidden" name="oid" id="oid" value="<%=oid%>">
<input type="hidden" name="compareArr" id="compareArr" value="<%=compareArr%>">
<table class="button-table">
	<tr>
		<td class="right">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>


<div id="grid_wrap" style="border-top: 1px solid #3180c3;"></div>
<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
<script type="text/javascript">
	let myGridID;
	const data = <%=data%>
	console.log(data);
	function _layout() {
		return [ {
			dataField : "category_name",
			headerText : "",
			dataType : "string",
			width : 150,
			style : "aui-left",
			cellMerge : true,
			styleFunction : function(rowIndex, columnIndex, value, headerText, item, dataField) {
				const id = item.id;
				if(!isNull(id)) {
					return "none";
				}
				return "";
			},
			filter : {
				showIcon : true,
				inline : true
			},
			cellColMerge: true, // 셀 가로 병합 실행
			cellColSpan: 2, // 셀 가로 병합 대상은 6개로 설정
		}, {
			dataField : "item_name",
			headerText : "",
			dataType : "string",
			width : 250,
			cellMerge : true,
			style : "aui-left",
			mergeRef : "category_code",
			mergePolicy : "restrict",
			filter : {
				showIcon : true,
				inline : true
			},
		},
		{
			headerText : "<%=p1.getKekNumber()%>",
			dataField : "P0",
			dataType : "string",
			width : 200,
			styleFunction : function(rowIndex, columnIndex, value, headerText, item, dataField) {
				const id = item.id;
				if(id !== undefined) {
					return "link";
				}
				return "";
			},
			renderer : {
				type : "Templaterenderer"
			},
			filter : {
				showIcon : true,
				inline : true
			},
		}, 
		<%int key = 1;
for (Project project : destList) {
	String dataField = "P" + key;
	String pOidKey = "P" + key+"oid";
	%>
		{
			headerText : "<%=project.getKekNumber()%>",
			dataField : "<%=dataField%>",
			dataType : "string",
			width : 250,
			styleFunction : function(rowIndex, columnIndex, value, headerText, item, dataField) {
				const id = item.id;
				if(id !== undefined) {
					return "link";
				}
				const P0 = item.P0;
				if(!isNull(value)) {
					if(P0 !== value) {
						return "compare";
					}
				}
				return "";
			},
			renderer : {
				type : "Templaterenderer"
			},
			filter : {
				showIcon : true,
				inline : true
			},
		}, 
		<%key++;
}%>
		 ]
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
			enableCellMerge : true,
			fixedColumnCount : 3,
			wordWrap : true,
			fixedRowCount : 4,
			rowStyleFunction : function(rowIndex, item) {
				const value = item.category_code;
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
				const name = item.category_name;
				if(name === "막종 / 막종상세") {
					return "row1";
				} else if(name === "고객사 / 설치장소") {
					return "row2";
				} else if(name === "KE 작번") {
					return "row3";
				} else if(name === "발행일") {
					return "row4";
				}
				return "";
			}
		}
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		console.log(data);
		AUIGrid.setGridData(myGridID, data);
		AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
		AUIGrid.bind(myGridID, "vScrollChange", function(event) {
			hideContextMenu();
		});
		AUIGrid.bind(myGridID, "hScrollChange", function(event) {
			hideContextMenu();
		});
		//AUIGrid.bind(myGridID, "cellClick", auiCellClickHandler);
	}

	function auiCellClickHandler(event) {
		console.log("event");
		console.log(event);
		console.log(event.columnIndex);
		const aa = AUIGrid.getSelectedIndex(myGridID);
		console.log("aa");
		console.log(aa);
		
		const dataField = event.dataField;

		//let result = dataField+num;
		const key = dataField+"oid";
		const id = event.item.id;
		const bb = event.columnIndex;
		const oid = event.item.oidList.bb;
		console.log("oid");
		const celName = "P"+event.columnIndex+"oid";
		//console.log(event.item.[celName]);
		
		//for( val of )
		
		if(dataField.indexOf("P") > -1 && !isNull(oid)) {
			const url = getCallUrl("/project/info?oid="+oid);
			popup(url);
		}
	}
	
	document.addEventListener("DOMContentLoaded", function() {
		const columns = loadColumnLayout("tbom-compare");
		const contenxtHeader = genColumnHtml(columns);
		$("#h_item_ul").append(contenxtHeader);
		$("#headerMenu").menu({
			select : headerMenuSelectHandler
		});
		createAUIGrid(columns);
		gridResize();
	})

	document.addEventListener("click", function(event) {
		hideContextMenu();
	})

	window.addEventListener("resize", function() {
		gridResize();
	});
	function gridResize(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID, ww-15, hh-60);
	}
</script>
