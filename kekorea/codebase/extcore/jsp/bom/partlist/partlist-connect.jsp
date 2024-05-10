<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
String toid = (String) request.getAttribute("toid");
String poid = (String) request.getAttribute("poid");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
<input type="hidden" name="sessionid" id="sessionid"><input type="hidden" name="lastNum" id="lastNum">
<input type="hidden" name="toid" id="toid" value="<%=toid%>">
<input type="hidden" name="poid" id="poid" value="<%=poid%>">
<input type="hidden" name="curPage" id="curPage">

<table class="search-table">
	<colgroup>
		<col width="130">
		<col width="*">
		<col width="130">
		<col width="*">
		<col width="130">
		<col width="*">
	</colgroup>
	<tr>
		<th>수배표 제목</th>
		<td class="indent5">
			<input type="text" name="fileName" class="width-200">
		</td>
		<th>상태</th>
		<td class="indent5">
			<select name="state" id="state" class="width-200">
				<option value="">선택</option>
			</select>
		</td>
		<th>KEK 작번</th>
		<td class="indent5">
			<input type="text" name="partName" class="width-200">
		</td>
		<th>KE 작번</th>
		<td class="indent5">
			<input type="text" name="number" class="width-200">
		</td>
	</tr>
	<tr>
		<th>설명</th>
		<td class="indent5">
			<input type="text" name="number" class="width-200">
		</td>
		<th>설계 구분</th>
		<td class="indent5">
			<select name="projectType_name" id="projectType_name" class="width-100">
				<option value="">선택</option>
			</select>
		</td>
		<th>막종</th>
		<td class="indent5">
			<input type="text" name="number" class="width-200">
		</td>
		<th>작업 내용</th>
		<td class="indent5">
			<input type="text" name="number" class="width-200">
		</td>
	</tr>
	<tr>
		<th>작성자</th>
		<td class="indent5">
			<input type="text" name="creator" id="creator" data-multi="false" class="width-200">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('creator');">
		</td>
		<th>작성일</th>
		<td class="indent5">
			<input type="text" name="createdFrom" id="createdFrom" class="width-100">
			~
			<input type="text" name="createdTo" id="createdTo" class="width-100">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('createdFrom', 'createdTo');">
		</td>
		<th>수정자</th>
		<td class="indent5">
			<input type="text" name="modifier" id="modifier" data-multi="false" class="width-200">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('modifier');">
		</td>
		<th>수정일</th>
		<td class="indent5">
			<input type="text" name="modifiedFrom" id="modifiedFrom" class="width-100">
			~
			<input type="text" name="modifiedTo" id="modifiedTo" class="width-100">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('modifiedFrom', 'modifiedTo');">
		</td>
	</tr>
</table>

<table class="button-table">
	<tr>
		<td class="left">
			
			
		</td>
		<td class="right">
			<input type="button" value="추가" title="추가" class="blue" onclick="connect();">
			<input type="button" value="조회" title="조회" onclick="loadGridData();">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>

<div id="grid_wrap" style="height: 670px; border-top: 1px solid #3180c3;"></div>
<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
<script type="text/javascript">
	let myGridID;
	function _layout() {
		return [ {
			dataField : "name",
			headerText : "수배표 제목",
			dataType : "string",
			width : 300,
			style : "aui-left",
			renderer : {
				type : "LinkRenderer",
				baseUrl : "javascript",
				jsCallback : function(rowIndex, columnIndex, value, item) {
					const oid = item.oid;
					const url = getCallUrl("/partlist/view?oid=" + oid);
					popup(url, 1600, 800);
				}
			},
			filter : {
				showIcon : true,
				inline : true
			}
		}, {
			dataField : "projectType_name",
			headerText : "설계구분",
			dataType : "string",
			width : 80,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "kekNumber",
			headerText : "KEK 작번",
			dataType : "string",
			width : 400,
			style : "aui-left",
//			renderer : {
//				type : "LinkRenderer",
//				baseUrl : "javascript",
//				jsCallback : function(rowIndex, columnIndex, value, item) {
//						alert("( " + rowIndex + ", " + columnIndex + " ) " + item.color + "  Link 클릭\r\n자바스크립트 함수 호출하고자 하는 경우로 사용하세요!");
//					const poid = item.poid;
//					const url = getCallUrl("/project/info?oid=" + poid);
//					popup(url);
//				}
//			},
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "keNumber",
			headerText : "KE 작번",
			dataType : "string",
			style : "underline", //링크를 안 건다면 언더라인 효과 빼는 게 맞을 것 같음. 확인 필요
			width : 400,
			style : "aui-left",
//				renderer : { //팝업 필요성 확인 필요
//					type : "LinkRenderer",
//					baseUrl : "javascript",
//					jsCallback : function(rowIndex, columnIndex, value, item) {
//						const poid = item.poid;
//						const url = getCallUrl("/project/info?oid=" + poid);
//						popup(url);
//					}
//				},
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
			}
		}, {
			dataField : "createdDate_txt",
			headerText : "작성일",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true,
			}
		}, {
			dataField : "modifiedDate_txt",
			headerText : "수정일",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true,
			}
		}, {
			dataField : "state",
			headerText : "상태",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			}
		} ]
	};

	function createAUIGrid(columnLayout) {
		const props = {
			headerHeight : 30,
			showRowNumColumn : true,
			rowNumHeaderText : "번호",
			showAutoNoDataMessage : false,
			enableFilter : true,
			selectionMode : "multipleCells",
			showInlineFilter : true,
			filterLayerWidth : 320,
			filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
			fixedColumnCount : 1,
			enableCellMerge : true,
			showRowCheckColumn : true,
			pageRowCount: 20,
			showPageRowSelect: true,
			usePaging : true
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		loadGridData();
		AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
		AUIGrid.bind(myGridID, "vScrollChange", function(event) {
			hideContextMenu();
// 			vScrollChangeHandler(event);
		});
		AUIGrid.bind(myGridID, "hScrollChange", function(event) {
			hideContextMenu();
		});
	}

	function loadGridData() {
		const params = new Object();
		const url = getCallUrl("/partlist/list");
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


	document.addEventListener("DOMContentLoaded", function() {
		const columns = loadColumnLayout("partlist-list");
		const contenxtHeader = genColumnHtml(columns);
		$("#h_item_ul").append(contenxtHeader);
		$("#headerMenu").menu({
			select : headerMenuSelectHandler
		});
		createAUIGrid(columns);
		gridResize();
		selectbox("state");
		selectbox("projectType_name");
		finderUser("creator");
		finderUser("modifier");
		twindate("created");
		twindate("modified");
	});

	function connect() {
		const toid = document.getElementById("toid").value;
		const poid = document.getElementById("poid").value;
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		if (checkedItems.length == 0) {
			alert("추가할 수배표를 선택하세요.");
			return false;
		}
		openLayer();
		opener._connect(checkedItems, toid, poid, function(res) {
			alert(res.msg);
			if (res.result) {
				closeLayer();
				opener._reload();
				self.close();
			} else {
				closeLayer();
			}
		});
	}
	
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
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		//4row 15, 200
		//3row 15, 160
		//2row 15, 140
		//popup 15, 50
		AUIGrid.resize(myGridID, ww-30, hh-200);
	}
</script>