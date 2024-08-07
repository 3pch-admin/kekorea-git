<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.korea.configSheet.beans.ConfigSheetDTO"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="e3ps.common.util.StringUtils"%>
<%@page import="e3ps.common.util.ContentUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
JSONArray categorys = (JSONArray) request.getAttribute("categorys");
JSONArray baseData = (JSONArray) request.getAttribute("data");
String oid = (String) request.getAttribute("oid");
String mode = (String) request.getAttribute("mode");
ConfigSheetDTO dto = (ConfigSheetDTO) request.getAttribute("dto");
String title = "";
if ("modify".equals(mode)) {
	title = "수정";
} else if ("revise".equals(mode)) {
	title = "개정";
}
%>
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<style type="text/css">
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

#textAreaWrap {
	font-size: 12px;
	position: absolute;
	height: 100px;
	min-width: 100px;
	background: #fff;
	border: 1px solid #555;
	display: none;
	padding: 4px;
	text-align: right;
	z-index: 9999;
}

#textAreaWrap textarea {
	font-size: 12px;
	width: calc(100% - 6px);
}

.editor_btn {
	background: #ccc;
	border: 1px solid #555;
	cursor: pointer;
	margin: 2px;
	padding: 2px;
}

.nav_u {
	display: inline-block;
}

ul, ol {
	list-style: none;
	padding: 0;
	margin: 0;
}

.nav_u li {
	display: inline;
	white-space: nowrap;
	text-align: right;
}
</style>
<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				CONFIG SHEET
				<%=title%>
			</div>
		</td>
		<td class="right">
			<input type="button" value="<%=title%>" title="<%=title%>" onclick="<%=mode%>();">
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
			<a href="#tabs-2">CONFIG SHEET</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="create-table">
			<colgroup>
				<col width="200">
				<col width="*">
			</colgroup>
			<tr>
				<th class="req lb">CONFIG SHEET 제목</th>
				<td class="indent5">
					<input type="text" name="name" id="name" class="width-700" value="<%=dto.getName()%>">
				</td>
			</tr>
			<tr>
				<th class="req lb">KEK 작번</th>
				<td>
					<jsp:include page="/extcore/jsp/common/project-include.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
						<jsp:param value="update" name="mode" />
						<jsp:param value="true" name="multi" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">설명</th>
				<td class="indent5">
					<textarea name="description" id="description" rows="6"><%=dto.getContent() != null ? dto.getContent() : ""%></textarea>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5">
					<jsp:include page="/extcore/jsp/common/attach-secondary.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">결재</th>
				<td>
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
					<input type="button" value="불러오기" title="불러오기" class="blue" onclick="load();">
					<input type="button" value="열 추가" title="열 추가" onclick="toAppend();">
					<input type="button" value="열 삭제" title="열 삭제" class="red" onclick="removeColumn();">
					<input type="button" value="행 추가(이후)" title="행 추가(이후)" class="orange" onclick="addAfterRow();">
					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();">
				</td>
			</tr>
		</table>
		<div id="grid_wrap" style="height: 800px; border-top: 1px solid #3180c3;"></div>
	</div>
</div>
<div id="textAreaWrap">
	<textarea id="myTextArea" class="aui-grid-custom-renderer-ext" style="height: 90px;"></textarea>
	<ul class="nav_u">
		<li>
			<button class="editor_btn" id="editEnd">확인</button>
		</li>
		<li>
			<button class="editor_btn" id="cancel">취소</button>
		</li>
	</ul>
</div>
<script type="text/javascript">
	let myGridID;
	const categorys =
<%=categorys%>
	let itemListMap = {};
	let specListMap = {};
	const columns = [ {
		dataField : "category_Name",
		headerText : "CATEGORY",
		dataType : "string",
		style : "aui-left",
		width : 250,
		cellMerge : true,
		labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
			return item.category_name;
		},
	}, {
		dataField : "item_name",
		headerText : "ITEM",
		dataType : "string",
		width : 350,
		cellMerge : true,
		style : "aui-left",
		mergeRef : "category_Name",
		mergePolicy : "restrict",
		editable : true,
		labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
			return item.item_name;
		}
	},
	<%int index = 0;
	ArrayList<String> dd = dto.getDataFields();
	for (int i = 0; i < dd.size(); i++) {
		String dataFields = dd.get(i);%>
				{
					dataField : "<%=dataFields%>",
					headerText : "사양<%=index%>",
					dataType : "string",
					width : 250,
					renderer : {
						type : "Templaterenderer"
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
		width : 350,
		renderer : {
			type : "Templaterenderer"
		}
	}, {
		dataField : "apply",
		headerText : "APPLY",
		dataType : "string",
		width : 350,
		renderer : {
			type : "Templaterenderer"
		}
	},{
		dataField : "category_code",
		dataType : "string",
		visible : false
	}
	 ];

	function createAUIGrid(columnLayout) {
		const props = {
			headerHeight : 30,
			showRowNumColumn : true,
			showRowCheckColumn : true,
			rowNumHeaderText : "번호",
			selectionMode : "multipleCells",
			enableSorting : false,
			enableCellMerge : true,
			editable : true,
			enableRowCheckShiftKey : true,
			wordWrap : true,
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
				return "";
			}
		};
		myGridID = AUIGrid.create("#grid_wrap", columns, props);
		AUIGrid.bind(myGridID, "cellEditEnd", auiCellEditEndHandler);
		AUIGrid.bind(myGridID, "cellEditBegin", auiCellEditBeginHandler);
		readyHandler();
		auiReadyHandler();
		AUIGrid.bind(myGridID, "pasteBegin", auiPasteBeginHandler);
	}
	
	function auiPasteBeginHandler(event) {
		const data = event.clipboardData;
		let arr;
		let i, j, len, len2, str;
		// 엑셀 개행 문자가 없는 경우
		if (data.indexOf("\n") === -1) {
			return data;
		}

		arr = CSVToArray(data, "\t"); // tab 문자 구성 String 을 배열로 반환
		if (arr && arr.length) {
			if (String(arr[arr.length - 1]).trim() == "") { // 마지막 빈 값이 삽입되는 경우가 존재함.
				arr.pop();
			}
			for (i = 0, len = arr.length; i < len; i++) {
				arr2 = arr[i];
				if (arr2 && arr2.length) {
					for (j = 0, len2 = arr2.length; j < len2; j++) {
						str = arr2[j];
						arr[i][j] = str.replace(/\n/g, "<br/>"); // 엑셀 개행 문자를 br 태그로 변환
					}
				}
			}
		}
		return arr;
	}

	function CSVToArray(strData, strDelimiter) {
		strDelimiter = (strDelimiter || ",");
		const objPattern = new RegExp(("(\\" + strDelimiter + "|\\r?\\n|\\r|^)" + "(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" + "([^\"\\" + strDelimiter + "\\r\\n]*))"), "gi");
		let arrData = [ [] ];
		let arrMatches = null;
		while (arrMatches = objPattern.exec(strData)) {
			const strMatchedDelimiter = arrMatches[1];
			if (strMatchedDelimiter.length && strMatchedDelimiter !== strDelimiter) {
				arrData.push([]);
			}
			let strMatchedValue;
			if (arrMatches[2]) {
				strMatchedValue = arrMatches[2].replace(new RegExp("\"\"", "g"), "\"");
			} else {
				strMatchedValue = arrMatches[3];
			}
			arrData[arrData.length - 1].push(strMatchedValue);
		}
		return (arrData);
	};

	function load() {
		const url = getCallUrl("/configSheet/copy?method=copy&multi=false");
		popup(url, 1500, 700);
	}

	function copy(data, callBack) {
		const oid = data.item.oid;
		const params = new Object();
		const url = getCallUrl("/configSheet/copy");
		params.oid = oid;
		openLayer();
		call(url, params, function(data) {
			if (data.result && data.list.length > 0) {
				AUIGrid.clearGridData(myGridID);
				AUIGrid.addRow(myGridID, data.list);
				callBack(true, "");
			} else {
				callBack(true, data.msg);
			}
			closeLayer();
		})
	}

	function auiCellEditBeginHandler(event) {
		const dataField = event.dataField;
		if (dataField === "category_code") {
			return false;
		}

		if (event.isClipboard) {
			return true;
		}
		if (event.dataField.indexOf("spec") > -1 || event.dataField.indexOf("note") > -1 || event.dataField.indexOf("apply") > -1) {
			openTextarea(event);
		} else {
			return true;
		}

		return true;
	}

	function auiReadyHandler(event) {
		const item = AUIGrid.getGridData(myGridID);
		for (let i = 0; i < item.length; i++) {
			if (itemListMap.length === undefined) {
				const categoryCode = item[i].category_code;
				const url = getCallUrl("/configSheetCode/getChildrens?parentCode=" + categoryCode + "&codeType=CATEGORY");
				call(url, null, function(data) {
					itemListMap[categoryCode] = data.list;
				}, "GET");
			}
		}
	}

	function readyHandler() {
		const data = <%=baseData%>
		AUIGrid.addRow(myGridID, data);
	}

	function auiCellEditEndHandler(event) {
		const dataField = event.dataField;
		const item = event.item;
		const rowIndex = event.rowIndex;
		if (dataField === "category_code") {
			const categoryCode = item.category_code;
			const url = getCallUrl("/commonCode/getChildrens?parentCode=" + categoryCode + "&codeType=CATEGORY");
			call(url, null, function(data) {
				itemListMap[categoryCode] = data.list;
			}, "GET");
		}
	}

	
	let toIndex = <%=(index+1)%>;
	function toAppend() {
		const dataField = "spec" + toIndex;
		var columnObj = {
			headerText : "사양" + toIndex,
			dataField : dataField, // dataField 는 중복되지 않게 설정
			defaultValue : "", // 칼럼 추가 할 때 해당 칼럼의 기본값 지정, 만약 지정하지 않으면 초기화 하지 않음
			width : 250,
			renderer : {
				type : "Templaterenderer"
			}
		};
		const aa = AUIGrid.getColumnInfoList(myGridID, "note");
		AUIGrid.addColumn(myGridID, columnObj, aa.length-3);
		toIndex++;
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
		const rowIndex = checkedItems[0].rowIndex;
		const category_name = checkedItems[0].item.category_name;
		const category_code = checkedItems[0].item.category_code;
		const item = {
			category_name : category_name,
			category_code : category_code
		}
		AUIGrid.addRow(myGridID, item, rowIndex + 1);
	}
	
	function removeColumn() {
		
		const aa = AUIGrid.getSelectedIndex(myGridID);
		
		//console.log(aa[0]);
		//console.log(aa[1]);
		
		if( aa[1] == 0 || aa[1] == 1 || aa[1] == 2 || aa[1] == -1){
			
		}else{
			const bb = AUIGrid.getColumnInfoList(myGridID, "note");
			console.log(aa[1]);
			console.log(bb.length);
			console.log("=========================");
			if( bb.length-1 == aa[1] || bb.length-2 == aa[1] || bb.length-3 == aa[1] ){
				console.log("############# no del");
			}else{
				console.log("############# del");
				AUIGrid.removeColumn(myGridID, aa[1]);
			}		
		}
		
		
		//AUIGrid.removeColumn(myGridID, "selectedIndex");
	};
	
	function openTextarea(event) {
		const dataField = event.dataField;
		const obj = document.getElementById("textAreaWrap");
		const textArea = document.getElementById("myTextArea");
		obj.style.left = event.position.x + "px";
		obj.style.top = event.position.y + "px";
		obj.style.width = (event.size.width - 8) + "px";
		obj.style.height = "125px";
		obj.style.display = "block";
		textArea.value = String(event.value).replace(/[<]br[/][>]/gi, "\r\n");
		obj.setAttribute("data-field", dataField);
		// 행인덱스 보관
		obj.setAttribute("data-row-index", event.rowIndex);

		// 포커싱
		setTimeout(function() {
			textArea.focus();
			textArea.select();
		}, 16);
	}

	function forceEditngTextArea(value, event) {
		const dataField = document.getElementById("textAreaWrap").getAttribute("data-field"); // 보관한 dataField 얻기
		const rowIndex = Number(document.getElementById("textAreaWrap").getAttribute("data-row-index")); // 보관한 rowIndex 얻기
		value = value.replace(/\r|\n|\r\n/g, "<br/>");

		const item = {};
		item[dataField] = value;

		AUIGrid.updateRow(myGridID, item, rowIndex);
		document.getElementById("textAreaWrap").style.display = "none";
		event.preventDefault();
	};
	
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
		let checkedRowId =[];
		for (let i=0, len = checked.length ;i < len; i++) {
			checkedRowId.push(checked[i].rowIndex);
		}
		AUIGrid.removeRow(myGridID, checkedRowId);
		
		
	};

	function <%=mode%>() {
		
		const url = getCallUrl("/configSheet/<%=mode%>");
		const params = new Object();
		const oid = document.getElementById("oid").value;
		const addRows = AUIGrid.getGridData(myGridID);
		const addRows9 = AUIGrid.getGridData(myGridID9);
		const addRows8 = AUIGrid.getGridData(myGridID8);
		const name = document.getElementById("name");
		
		if (isNull(name.value)) {
			alert("CONFIG SHEET 제목을 입력하세요.");
			name.focus();
			return false;
		}
		
		if (addRows9.length === 0) {
			alert("최소 하나이상의 작번을 추가하세요.");
			insert9();
			return false;
		}

		/*
		if (addRows8.length === 0) {
			alert("결재선을 지정하세요.");
			_register();
			return false;
		}
		*/
		addRows.sort(function(a, b) {
			return a.sort - b.sort;
		});

		
		if (!confirm("<%=title%> 하시겠습니까?")) {
			return false;
		}
		
		params.name = name.value;
		params.description = document.getElementById("description").value;
		params.addRows = addRows;
		params.addRows9 = addRows9;
		params.oid = oid;
		params.secondarys = toArray("secondarys");
		toRegister(params, addRows8);
		openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
// 				opener.loadGridData();
				self.close();
			} else {
				closeLayer();
			}
		})
	}
	
	
	document.getElementById("myTextArea").addEventListener("blur", function(event) {
		const relatedTarget = event.relatedTarget || document.activeElement;

		// 확인 버튼 클릭한 경우
		if (relatedTarget.getAttribute("id") === "editEnd") {
			return;
		} else if (relatedTarget.getAttribute("id") === "cancel") { // 취소 버튼
			return;
		}
		forceEditngTextArea(this.value, event);
	});
	

	document.getElementById("cancel").addEventListener("click", function(event) {
		document.getElementById("textAreaWrap").style.display = "none";
		event.preventDefault();
	});

	document.getElementById("editEnd").addEventListener("click", function(event) {
		const value = document.getElementById("myTextArea").value;
		forceEditngTextArea(value, event);
	});

	document.addEventListener("DOMContentLoaded", function() {
		document.getElementById("name").focus();
		$("#tabs").tabs({
			active : 0,
			activate : function(event, ui) {
				const tabId = ui.newPanel.prop("id");
				switch (tabId) {
				case "tabs-1":
					const isCreated9 = AUIGrid.isCreated(myGridID9);
					if (isCreated9) {
						gridResize9();
					} else {
						createAUIGrid9(columns9);
					}
					const isCreated8 = AUIGrid.isCreated(myGridID8);
					if (isCreated8) {
						gridResize8();
					} else {
						createAUIGrid8(columns8);
					}
					break;
				case "tabs-2":
					const isCreated = AUIGrid.isCreated(myGridID);
					if (isCreated) {
						gridResize();
					} else {
						createAUIGrid(columns);
						openLayer();
						setTimeout(function() {
							AUIGrid.refresh(myGridID);
							closeLayer();
						}, 100);
					}
					break;
				}
			}
		});
		createAUIGrid9(columns9);
		createAUIGrid8(columns8);
		createAUIGrid(columns);
		gridResize9();
		gridResize8();
		gridResize();
	});

	window.addEventListener("resize", function() {
		gridResize9();
		gridResize8();
		gridResize();
	});
	
	function gridResize9(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID9, ww-220, 150);
	}
	
	function gridResize8(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID8, ww-220, 200);
	}
	
	function gridResize(){
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID, ww-75, hh-150);
	}
	
</script>