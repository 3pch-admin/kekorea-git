<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.korea.configSheet.ConfigSheet"%>
<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.korea.configSheet.beans.ConfigSheetDTO"%>
<%@page import="net.sf.json.JSONArray"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
JSONArray data = (JSONArray) request.getAttribute("data");
ConfigSheetDTO dto = (ConfigSheetDTO) request.getAttribute("dto");
String oid = (String) request.getAttribute("oid");
ConfigSheet cn = (ConfigSheet) CommonUtils.getObject(oid);
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
int latestVersion = (int) request.getAttribute("latestVersion");
String loid = (String) request.getAttribute("loid");
%>
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
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				CONFIG SHEET 정보
			</div>
		</td>
		<td class="right">
			<%
			//if ((dto.isEdit() && dto.isLatest()) || isAdmin) {
			%>
			<input type="button" value="수정" title="수정" class="blue" onclick="update('modify');">
			<%
			//}
// 			out.println(dto.isRevise());
// 			out.println(dto.isLatest());
			if ((dto.isRevise() && cn.getLatest()) || isAdmin) {
			%>
			<input type="button" value="개정" title="개정" onclick="update('revise');">
			<%
			}
			%>
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
			<a href="#tabs-2">CONFIG SHEET</a>
		</li>
		<li>
			<a href="#tabs-3">결재이력</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="view-table">
			<colgroup>
				<col width="150">
				<col width="600">
				<col width="150">
				<col width="600">
			</colgroup>
			<tr>
				<th class="lb">CONFIG SHEET 제목</th>
				<td class="indent5"><%=dto.getName()%></td>
				<th class="lb">CONFIG SHEET 번호</th>
				<td class="indent5"><%=dto.getNumber()%></td>
			</tr>
			<tr>
				<th class="lb">상태</th>
				<td class="indent5"><%=dto.getState()%></td>
				<th class="lb">버전</th>
				<td class="indent5"><%=dto.getVersion()%>
					(
					<a href="javascript:view('<%=loid%>');">
						<font color="red">
							<b><%=latestVersion%></b>
						</font>
					</a>
					)
				</td>
			</tr>
			<tr>
				<th class="lb">작성자</th>
				<td class="indent5"><%=dto.getCreator()%></td>
				<th>작성일</th>
				<td class="indent5"><%=dto.getCreatedDate_txt()%></td>
			</tr>
			<tr>
				<th class="lb">KEK 작번</th>
				<td colspan="3">
					<jsp:include page="/extcore/jsp/common/project-include.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
						<jsp:param value="view" name="mode" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">설명</th>
				<td class="indent5" colspan="3">
					<textarea name="description" id="description" rows="6" readonly="readonly"><%=dto.getContent()%></textarea>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5" colspan="3">
					<jsp:include page="/extcore/jsp/common/secondary-view.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
					</jsp:include>
				</td>
			</tr>
		</table>
	</div>
	<div id="tabs-2">
		<div id="grid_wrap" style="height: 800px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID;
			const data = <%=data%>
			console.log(data);
			const columns = [ {
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
				int index = 0;
				ArrayList<String> dd = dto.getDataFields();
				for (int i = 0; i < dd.size(); i++) {
					String dataFields = dd.get(i);
					String key = dd.get(0);
				%>
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
			<%
				index++;
				}
			%>
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

			function createAUIGrid(columnLayout) {
				const props = {
					headerHeight : 30,
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
				myGridID = AUIGrid.create("#grid_wrap", columns, props);
				AUIGrid.setGridData(myGridID, data);
			}
		</script>
	</div>
	<div id="tabs-3">
		<!-- 결재이력 -->
		<jsp:include page="/extcore/jsp/common/approval-history.jsp">
			<jsp:param value="<%=dto.getOid()%>" name="oid" />
		</jsp:include>
	</div>
</div>
<script type="text/javascript">
	function update(mode) {
		openLayer();
		const oid = document.getElementById("oid").value;
		const url = getCallUrl("/configSheet/update?oid=" + oid + "&mode=" + mode);
		document.location.href = url;
	}

	function _delete() {

		if (!confirm("삭제 하시겠습니까?")) {
			return false;
		}

		const oid = document.getElementById("oid").value;
		const url = getCallUrl("/configSheet/delete?oid=" + oid);
		openLayer();
		call(url, null, function(data) {
			alert(data.msg);
			if (data.result) {
// 				opener.loadGridData();
				self.close();
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
					const isCreated9 = AUIGrid.isCreated(myGridID9);
					if (isCreated9) {
						gridResize9();
					} else {
						createAUIGrid9(columns9);
					}
					break;
				case "tabs-2":
					const isCreated = AUIGrid.isCreated(myGridID);
					if (isCreated) {
						gridResize();
					} else {
						createAUIGrid(columns);
					}
					break;
				case "tabs-3":
					const isCreated100 = AUIGrid.isCreated(myGridID100);
					if (isCreated100) {
						gridResize100();
					} else {
						createAUIGrid100(columns100);
					}
					break;
				}
			}
		});
		createAUIGrid9(columns9);
		createAUIGrid(columns);
		createAUIGrid100(columns100);
		gridResize9();
		AUIGrid.resize(myGridID);
		gridResize100();
	});

	window.addEventListener("resize", function() {
		gridResize9();
		gridResize();
		gridResize100();
	});
	function gridResize9(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		console.log(hh);
		AUIGrid.resize(myGridID9, ww-220, 150);
	}
	function gridResize(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		console.log(hh);
		AUIGrid.resize(myGridID, ww-65, hh-140);
	}
	function gridResize100(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		console.log(hh);
		AUIGrid.resize(myGridID100, ww-15, hh-140);
	}
</script>