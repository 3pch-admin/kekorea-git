<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="e3ps.project.output.service.OutputHelper"%>
<%@page import="e3ps.doc.service.DocumentHelper"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
ArrayList<Map<String, String>> maks = (ArrayList<Map<String, String>>) request.getAttribute("maks");
String toid = (String) request.getAttribute("toid");
String poid = (String) request.getAttribute("poid");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
<input type="hidden" name="sessionid" id="sessionid"><input type="hidden" name="lastNum" id="lastNum">
<input type="hidden" name="curPage" id="curPage">
<input type="hidden" name="oid" id="oid">
<input type="hidden" name="toid" id="toid" value="<%=toid%>">
<input type="hidden" name="poid" id="poid" value="<%=poid%>">
<input type="hidden" name="type" id="type" value="new">
<table class="search-table">
	<colgroup>
		<col width="130">
		<col width="*">
		<col width="130">
		<col width="*">
		<col width="130">
		<col width="*">
		<col width="130">
		<col width="*">
	</colgroup>
	<tr>
		<th>문서 분류</th>
		<td colspan="7" class="indent5">
			<input type="hidden" name="location" id="location" value="<%=OutputHelper.OUTPUT_NEW_ROOT%>">
			<span id="locationText"><%=OutputHelper.OUTPUT_NEW_ROOT%></span>
		</td>
	</tr>
	<tr>
		<th>산출물 제목</th>
		<td class="indent5">
			<input type="text" name="name" id="name" class="width-200">
		</td>
		<th>산출물 번호</th>
		<td class="indent5">
			<input type="text" name="number" id="number" class="width-200">
		</td>
		<th>설명</th>
		<td class="indent5">
			<input type="text" name="content" id="content" class="width-200">
		</td>
		<th>KE 작번</th>
		<td class="indent5">
			<input type="text" name="keNumber" id="keNumber" class="width-200">
		</td>
	</tr>
	<tr>
		<th>KEK 작번</th>
		<td class="indent5">
			<input type="text" name="kekNumber" id="kekNumber" class="width-200">
		</td>
		<th>막종</th>
		<td class="indent5">
			<select name="mak" id="mak" class="width-200">
				<option value="">선택</option>
				<%
				for (Map<String, String> mak : maks) {
				%>
				<option value="<%=mak.get("key")%>"><%=mak.get("value")%></option>
				<%
				}
				%>
			</select>
		</td>
		<th>작업내용</th>
		<td class="indent5" colspan="3">
			<input type="text" name="description" id="description" class="width-400">
		</td>
	</tr>
	<tr>
		<th>작성자</th>
		<td class="indent5">
			<input type="text" name="creator" id="creator" data-multi="false">
			<input type="hidden" name="creatorOid" id="creatorOid">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('creator')">
		</td>
		<th>작성일</th>
		<td class="indent5">
			<input type="text" name="createdFrom" id="createdFrom" class="width-100">
			~
			<input type="text" name="createdTo" id="createdTo" class="width-100">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('createdFrom', 'createdTo')">
		</td>
		<th>버전</th>
		<td>
			&nbsp;
			<div class="pretty p-switch">
				<input type="radio" name="latest" value="true" checked="checked">
				<div class="state p-success">
					<label>
						<b>최신버전</b>
					</label>
				</div>
			</div>
			&nbsp;
			<div class="pretty p-switch">
				<input type="radio" name="latest" value="">
				<div class="state p-success">
					<label>
						<b>모든버전</b>
					</label>
				</div>
			</div>
		</td>
		<th>상태</th>
		<td class="indent5">
			<select name="state" id="state" class="width-200">
				<option value="">선택</option>
				<option value="INWORK">작업 중</option>
				<option value="UNDERAPPROVAL">승인 중</option>
				<option value="RELEASED">승인됨</option>
				<option value="RETURN">반려됨</option>
				<option value="WITHDRAWN">폐기</option>
			</select>
		</td>
	</tr>
</table>

<table class="button-table">
	<tr>
		<td class="left">
		</td>
		<td class="right">
			
			<input type="button" value="추가" title="추가" class="blue" onclick="connect();">
			<input type="button" value="OLD" title="OLD" onclick="toggle();" style="background-color: orange;">
			<input type="button" value="조회" title="조회" onclick="loadGridData();">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>

<table>
	<colgroup>
		<col width="230">
		<col width="10">
		<col width="*">
	</colgroup>
	<tr>
		<td valign="top">
			<jsp:include page="/extcore/jsp/common/folder-include.jsp">
				<jsp:param value="<%=OutputHelper.OUTPUT_NEW_ROOT%>" name="location" />
				<jsp:param value="product" name="container" />
				<jsp:param value="list" name="mode" />
				<jsp:param value="485" name="height" />
			</jsp:include>
		</td>
		<td valign="top">&nbsp;</td>
		<td valign="top">
			<div id="grid_wrap" style="height: 485px; border-top: 1px solid #3180c3;"></div>
			<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		</td>
	</tr>
</table>
<script type="text/javascript">
	let myGridID;
	function _layout() {
		return [ {
			dataField : "name",
			headerText : "문서제목",
			dataType : "string",
			width : 350,
			style : "aui-left",
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "number",
			headerText : "문서번호",
			dataType : "string",
			width : 120,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "description",
			headerText : "설명",
			dataType : "string",
			style : "aui-left",
			width : 350,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "location",
			headerText : "문서분류",
			dataType : "string",
			width : 250,
			style : "aui-left",
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "docType",
			headerText : "문서타입",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "state",
			headerText : "상태",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "version",
			headerText : "버전",
			dataType : "string",
			width : 80,
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
			dataField : "createdDate",
			headerText : "작성일",
			dataType : "date",
			width : 100,
			formatString : "yyyy-mm-dd",
			filter : {
				showIcon : true,
				inline : true,
				displayFormatValues : true
			},
		}, {
			dataField : "modifier",
			headerText : "수정자",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "modifiedDate",
			headerText : "수정일",
			dataType : "date",
			width : 100,
			formatString : "yyyy-mm-dd",
			filter : {
				showIcon : true,
				inline : true,
				displayFormatValues : true
			},
		}, {
			dataField : "primary",
			headerText : "첨부파일",
			width : 100,
			renderer : {
				type : "TemplateRenderer"
			},
			filter : {
				showIcon : true,
				inline : true
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
			selectionMode : "multipleCells",
			enableFilter : true,
			showInlineFilter : true,
			filterLayerWidth : 320,
			filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
			showRowCheckColumn : true,
			userPaging : true,
			pageRowCount: 20,
			showPageRowSelect: true,
			usePaging : true
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
// 		loadGridData();
		AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
		AUIGrid.bind(myGridID, "vScrollChange", function(event) {
			hideContextMenu();
// 			vScrollChangeHandler(event);
		});
		AUIGrid.bind(myGridID, "hScrollChange", function(event) {
			hideContextMenu();
		});
	}

	function connect() {
		const toid = document.getElementById("toid").value;
		const poid = document.getElementById("poid").value;
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		if (checkedItems.length == 0) {
			alert("추가할 산출물을 선택하세요.");
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

	function loadGridData() {
		const url = getCallUrl("/output/list");
		const params = new Object();
		const oid = document.getElementById("oid").value;
		const name = document.getElementById("name").value;
		const number = document.getElementById("number").value;
		const content = document.getElementById("content").value;
		const kekNumber = document.getElementById("kekNumber").value;
		const keNumber = document.getElementById("keNumber").value;
		const description = document.getElementById("description").value;
		const creatorOid = document.getElementById("creatorOid").value;
		const createdFrom = document.getElementById("createdFrom").value;
		const createdTo = document.getElementById("createdTo").value;
		const state = document.getElementById("state").value;
		const latest = !!document.querySelector("input[name=latest]:checked").value;
		const type = document.getElementById("type").value;
		params.oid = oid;
		params.name = name;
		params.number = number;
		params.content = content;
		params.kekNumber = kekNumber;
		params.keNumber = keNumber;
		params.description = description;
		params.creatorOid = creatorOid;
		params.createdFrom = createdFrom;
		params.createdTo = createdTo;
		params.state = state
		params.latest = latest;
		params.type = type;
		AUIGrid.showAjaxLoader(myGridID);
		parent.openLayer();
		call(url, params, function(data) {
			if (data.result) {
				AUIGrid.removeAjaxLoader(myGridID);
				AUIGrid.setGridData(myGridID, data.list);
				document.getElementById("sessionid").value = data.sessionid;
				document.getElementById("curPage").value = data.curPage;document.getElementById("lastNum").value = data.list.length;
			} else {
				alert(data.msg);
			}
			parent.closeLayer();
		});
	}

	document.addEventListener("DOMContentLoaded", function() {
		toFocus("name");
		const columns = loadColumnLayout("document-list");
		const contenxtHeader = genColumnHtml(columns);
		$("#h_item_ul").append(contenxtHeader);
		$("#headerMenu").menu({
			select : headerMenuSelectHandler
		});
		createAUIGrid(columns);
		_createAUIGrid(_columns);
		AUIGrid.resize(myGridID);
		AUIGrid.resize(_myGridID);
		selectbox("mak");
		selectbox("state");
		finderUser("creator");
		twindate("created");
	});

	function toggle() {
		const url = getCallUrl("/output/oconnect?poid=<%=poid%>&toid=<%=toid%>");
		document.location.href = url;
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
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//465
		console.log(ww+"=="+hh);
		AUIGrid.resize(_myGridID, 228, hh-200); // 트리
		AUIGrid.resize(myGridID, ww-260, hh-200);
		
	});
</script>