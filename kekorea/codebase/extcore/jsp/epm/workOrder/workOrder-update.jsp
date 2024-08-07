<%@page import="e3ps.common.util.StringUtils"%>
<%@page import="e3ps.epm.workOrder.dto.WorkOrderDTO"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
WorkOrderDTO dto = (WorkOrderDTO) request.getAttribute("dto");
String toid = dto.getToid();
JSONArray list = (JSONArray) request.getAttribute("list");
String workOrderType = dto.getWorkOrderType();
String mode = (String)request.getAttribute("mode");
String title = "";
if ("modify".equals(mode)) {
	title = "수정";
} else if ("revise".equals(mode)) {
	title = "개정";
}
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>    
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid() %>">
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				도면일람표 <%=title %>
			</div>
		</td>
		<td class="right">
			<input type="button" value="<%=title %>" title="<%=title %>" onclick="<%=mode%>();">
			<input type="button" value="뒤로" title="뒤로" class="blue" onclick="history.go(-1);">
		</td>
	</tr>
</table>

<div id="tabs">
	<ul>
		<li>
			<a href="#tabs-1">기본정보</a>
		</li>
		<li>
			<a href="#tabs-2">도면 일람표</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="create-table">
			<colgroup>
				<col width="150">
				<col width="500">
				<col width="150">
				<col width="500">
			</colgroup>
			<tr>
				<th class="req lb">도면 일람표 명</th>
				<td class="indent5">
					<input type="text" name="name" id="name" class="width-400" value="<%=dto.getName()%>">
				</td>
				<th>설계구분</th>
				<td class="indent5">
					<select name="workOrderType" id="workOrderType" class="width-100">
						<option value="">선택</option>
						<option value="기계">기계</option>
						<option value="전기">전기</option>
					</select>
				</td>
			</tr>
			<tr>
				<th class="lb">내용</th>
				<td class="indent5" colspan="3">
					<textarea name="description" id="description" rows="5"><%=dto.getContent() != null ? dto.getContent() : ""%></textarea>
				</td>
			</tr>
			<%
				if("revise".equals(mode)) {
			%>
			<tr>
				<th class="lb">개정사유</th>
				<td class="indent5" colspan="5">
					<textarea name="note" id="note" rows="5"></textarea>
				</td>
			</tr>
			<%
				}
			%>			
			<tr>
				<th class="req lb">KEK 작번</th>
				<td colspan="3">
					<jsp:include page="/extcore/jsp/common/project-include.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
						<jsp:param value="true" name="multi"/>
						<jsp:param value="update" name="mode" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5" colspan="5">
					<jsp:include page="/extcore/jsp/common/attach-secondary.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">결재</th>
				<td colspan="3">
					<jsp:include page="/extcore/jsp/common/approval-register.jsp">
						<jsp:param value="" name="oid" />
						<jsp:param value="update" name="mode" />
					</jsp:include>
				</td>
			</tr>
		</table>
	</div>
	<div id="tabs-2">
		<table class="button-table">
			<tr>
				<td class="left">
					<input type="button" value="행 추가(이전)" title="행 추가(이전)" class="blue" onclick="addBeforeRow();">
					<input type="button" value="행 추가(이후)" title="행 추가(이후)" class="orange" onclick="addAfterRow();">
					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">
				</td>
			</tr>
		</table>
		<div id="grid_wrap" style="height: 670px; border-top: 1px solid #3180c3;"></div>
	</div>
</div>

<script type="text/javascript">
	let myGridID;
	const columns = [ {
		dataField : "ok",
		headerText : "검증",
		dataType : "boolean",
		width : 80,
		renderer : {
			type : "CheckBoxEditRenderer",
		},
	}, {
		dataField : "preView",
		headerText : "미리보기",
		width : 80,
		editable : false,
		renderer : {
			type : "ImageRenderer",
			altField : null,
			imgHeight : 34,
		},
	}, {
		dataField : "name",
		headerText : "DRAWING TITLE",
		dataType : "string",
		editable : false,
		style : "aui-left"
	}, {
		dataField : "number",
		headerText : "DWG. NO",
		dataType : "string",
		width : 200,
		editRenderer : {
			type : "InputEditRenderer",
			maxlength : 10,
			regExp : "^[a-zA-Z0-9]+$",
			autoUpperCase : true,
		},
	}, {
		dataField : "current",
		headerText : "CURRENT VER",
		dataType : "numeric",
		width : 120,
		editable : false
	}, {
		dataField : "rev",
		headerText : "REV",
		dataType : "numeric",
		width : 80,
		editable : false
	}, {
		dataField : "lotNo",
		headerText : "LOT",
		dataType : "numeric",
		width : 80,
		formatString : "###0",
		editRenderer : {
			type : "InputEditRenderer",
			onlyNumeric : true,
			maxlength : 3,
		},
	}, {
		dataField : "note",
		headerText : "NOTE",
		dataType : "string",
		width : 350,
	} ]

	function createAUIGrid(columnLayout) {
		const props = {
			headerHeight : 30,
			showRowNumColumn : true,
			showRowCheckColumn : true,
			showStateColumn : true,
			rowNumHeaderText : "번호",
			showDragKnobColumn : true,
			enableDrag : true,
			selectionMode : "multipleCells",
			enableMultipleDrag : true,
			enableDrop : true,
			$compaEventOnPaste : true,
			editable : true,
			softRemoveRowMode : false,
			enableRowCheckShiftKey : true,
			useContextMenu : true,
			enableRightDownFocus : true,
			enableSorting : false,
			contextMenuItems : [ {
				label : "선택된 행 이전 추가",
				callback : contextItemHandler
			}, {
				label : "선택된 행 이후 추가",
				callback : contextItemHandler
			}, {
				label : "_$line"
			}, {
				label : "선택된 행 삭제",
				callback : contextItemHandler
			} ],
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		readyHandler();
		AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
		AUIGrid.bind(myGridID, "cellClick", auiCellClickHandler);
		AUIGrid.bind(myGridID, "keyDown", auiKeyDownHandler);
		AUIGrid.setGridData(myGridID, <%=list%>);
	}
	
	// enter 키 행 추가
	function auiKeyDownHandler(event) {
		if (event.keyCode == 13) { // 엔터 키
			var selectedItems = AUIGrid.getSelectedItems(event.pid);
			var rowIndex = selectedItems[0].rowIndex;
			if (rowIndex === AUIGrid.getRowCount(event.pid) - 1) { // 마지막 행인지 여부 
				AUIGrid.addRow(event.pid, {}); // 행 추가
				return false; // 엔터 키의 기본 행위 안함.
			}
		}
		return true; // 기본 행위 유지
	}

	function auiCellClickHandler(event) {
		const dataField = event.dataField;
		const oid = event.item.oid;
		const preView = event.item.preView;
		if (dataField === "preView") {
			if (oid !== undefined && preView !== undefined) {
				const url = getCallUrl("/aui/thumbnail?oid=" + oid);
				popup(url);
			}
		}
	}

	function contextItemHandler(event) {
		const item = new Object();
		switch (event.contextIndex) {
		case 0:
			item.createdDate = new Date();
			AUIGrid.addRow(myGridID, item, "selectionUp");
			break;
		case 1:
			item.createdDate = new Date();
			AUIGrid.addRow(myGridID, item, "selectionDown");
			break;
		case 3:
			const selectedItems = AUIGrid.getSelectedItems(myGridID);
			const rows = AUIGrid.getRowCount(myGridID);
			if (rows === 1) {
				alert("최 소 하나의 행이 존재해야합니다.");
				return false;
			}
			for (let i = selectedItems.length - 1; i >= 0; i--) {
				const rowIndex = selectedItems[i].rowIndex;
				AUIGrid.removeRow(myGridID, rowIndex);
			}
			break;
		}
	}

	function deleteRow() {
		const checked = AUIGrid.getCheckedRowItems(myGridID);
		const rows = AUIGrid.getRowCount(myGridID);
		if (rows === 1) {
			alert("최 소 하나의 행이 존재해야합니다.");
			return false;
		}
		if (checked.length === 0) {
			alert("삭제할 행을 선택하세요.");
			return false;
		}
		for (let i = checked.length - 1; i >= 0; i--) {
			const rowIndex = checked[i].rowIndex;
			AUIGrid.removeRow(myGridID, rowIndex);
		}
	};

	function readyHandler() {
		const item = new Object();
		item.createdDate = new Date();
		AUIGrid.addRow(myGridID, item, "last");
	}

	function auiCellEditEndHandler(event) {
		const dataField = event.dataField;
		const number = event.item.number;
		if (dataField === "number" && !isNull(number)) {
			const url = getCallUrl("/workOrder/getData?number=" + number);
			call(url, null, function(data) {
				if (data.ok) {
					const item = {
						ok : data.ok,
						name : data.name,
						number : data.number,
						rev : data.rev,
						current : data.current,
						lotNo : data.lotNo,
						doid : data.doid,
						sort : event.rowIndex,
						createdDate : new Date(),
						preView : data.preView
					}
					AUIGrid.updateRow(myGridID, item, event.rowIndex);
				} else {
					const item = {
						ok : data.ok,
						number : data.number,
					}
					AUIGrid.updateRow(myGridID, item, event.rowIndex);
				}
			}, "GET");
		}
	}

	function addBeforeRow() {
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		if (checkedItems.length === 0) {
			alert("추가하려는 행의 기준이 되는 행을 선택하세요.");
			return false;
		}
		if (checkedItems.length > 1) {
			alert("하나의 행만 선택하세요.");
			return false;
		}
		const item = new Object();
		const rowIndex = checkedItems[0].rowIndex;
		item.createdDate = new Date();
		AUIGrid.addRow(myGridID, item, rowIndex);
	}

	function addAfterRow() {
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		if (checkedItems.length === 0) {
			alert("추가하려는 행의 기준이 되는 행을 선택하세요.");
			return false;
		}
		if (checkedItems.length > 1) {
			alert("하나의 행만 선택하세요.");
			return false;
		}
		const item = new Object();
		const rowIndex = checkedItems[0].rowIndex;
		item.createdDate = new Date();
		AUIGrid.addRow(myGridID, item, rowIndex + 1);
	}

	function <%=mode%>() {

		const params = new Object();
		const oid = document.getElementById("oid").value;
		const name = document.getElementById("name");
		const description = document.getElementById("description").value;
		const workOrderType = document.getElementById("workOrderType").value;
// 		const progress = document.getElementById("progress").value;
		<%
			if("revise".equals(mode)) {
		%>
		const note = document.getElementById("note").value;
		<%
			}
		%>
		const addRows = AUIGrid.getGridData(myGridID);
		const addRows9 = AUIGrid.getGridData(myGridID9);
		const addRows8 = AUIGrid.getGridData(myGridID8);
		const secondarys = toArray("secondarys");
		const url = getCallUrl("/workOrder/<%=mode%>");

		addRows.sort(function(a, b) {
			return a.sort - b.sort;
		});

		if (isNull(name.value)) {
			alert("도면일람표 제목을 입력하세요.");
			name.focus();
			return false;
		}
		
		if (secondarys.length === 0) {
			alert("첨부파일을 선택하세요.");
			return false;
		}

		if (addRows9.length === 0) {
			alert("최소 하나이상의 작번을 추가하세요.");
			insert9();
			return false;
		}

// 		if (addRows8.length === 0) {
// 			alert("결재선을 지정하세요.");
// 			_register();
// 			return false;
// 		}

		if (addRows.length === 0) {
			alert("도면일람표의 데이터는 최소 하나 이상이어야 합니다.");
			return false;
		}

		if (!confirm("<%=title%> 하시겠습니까?")) {
			return false;
		}

		params.oid = oid;
		params.name = name.value;
		params.description = description;
		params.workOrderType = workOrderType;
// 		params.progress = Number(progress);
		params.addRows = addRows;
		params.addRows9 = addRows9;
		<%
			if("revise".equals(mode)) {
		%>
		params.note = note;
		<%
			}
		%>
		params.secondarys = secondarys; 
		toRegister(params, addRows8);
		openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
				<%if (!StringUtils.isNull(toid)) {%>
				opener._reload();
				<%} else {%>
				
				if (window.opener) {
				    // 부모 창의 loadGridData 함수가 존재하는지 확인합니다.
				    if (typeof window.opener.loadGridData === 'function') {
				    	opener.loadGridData();
				    } else {
				    	opener.document.location.reload();
				    }
				}
				<%}%>
				self.close();
			} else {
				closeLayer();
			}
		})
	}

	document.addEventListener("DOMContentLoaded", function() {
		document.getElementById("name").focus();
		$("#tabs").tabs({
			active : 0,
			activate : function(event, ui) {
				var tabId = ui.newPanel.prop("id");
				switch (tabId) {
				case "tabs-1":
					const isCreated9 = AUIGrid.isCreated(myGridID9);
					if (isCreated9) {
						AUIGrid.resize(myGridID9);
					} else {
						createAUIGrid9(columns9);
					}
					const isCreated8 = AUIGrid.isCreated(myGridID8);
					if (isCreated8) {
						AUIGrid.resize(myGridID8);
					} else {
						createAUIGrid8(columns8);
					}
					selectbox("workOrderType");
					$("#workOrderType").bindSelectDisabled(true);
					break;
				case "tabs-2":
					const isCreated = AUIGrid.isCreated(myGridID);
					if (isCreated) {
						AUIGrid.resize(myGridID);
					} else {
						createAUIGrid(columns);
					}
					break;
				}
			}
		});
		selectbox("workOrderType");
		$("#workOrderType").bindSelectSetValue("<%=workOrderType%>");
		$("#workOrderType").bindSelectDisabled(true);
		createAUIGrid(columns);
		createAUIGrid9(columns9);
		createAUIGrid8(columns8);
		AUIGrid.resize(myGridID9);
		AUIGrid.resize(myGridID8);
	})

	window.addEventListener("resize", function() {
		AUIGrid.resize(myGridID);
		AUIGrid.resize(myGridID9);
		AUIGrid.resize(myGridID8);
	});
</script>