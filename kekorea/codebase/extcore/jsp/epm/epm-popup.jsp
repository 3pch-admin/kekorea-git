<%@page import="e3ps.org.service.OrgHelper"%>
<%@page import="wt.epm.EPMDocumentType"%>
<%@page import="e3ps.epm.service.EpmHelper"%>
<%@page import="e3ps.part.service.PartHelper"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="wt.doc.WTDocument"%>
<%@page import="e3ps.common.util.StringUtils"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="wt.fc.PagingQueryResult"%>
<%@page import="e3ps.common.util.PageQueryUtils"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
boolean multi = (boolean) request.getAttribute("multi");
String method = (String) request.getAttribute("method");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
<input type="hidden" name="sessionid" id="sessionid"><input type="hidden" name="lastNum" id="lastNum">
<input type="hidden" name="curPage" id="curPage">
<input type="hidden" name="oid" id="oid">
<!-- 폴더 OID -->

<table class="search-table" id="product-table">
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
		<th>도면분류</th>
		<td colspan="7" class="indent5">
			<input type="hidden" name="location" id="location" value="<%=EpmHelper.DEFAULT_ROOT%>">
			<span id="locationText"><%=EpmHelper.DEFAULT_ROOT%></span>
		</td>
	</tr>
	<tr>
		<th>파일이름</th>
		<td class="indent5">
			<input type="text" name="fileName" id="fileName">
		</td>
		<th>품번</th>
		<td class="indent5">
			<input type="text" name="partCode" id="partCode">
		</td>
		<th>품명</th>
		<td class="indent5">
			<input type="text" name="partName" id="partName">
		</td>
		<th>규격</th>
		<td class="indent5">
			<input type="text" name="number" id="number">
		</td>
	</tr>
	<tr>
		<th>캐드타입</th>
		<td class="indent5">
			<select name="cadType" id="cadType" class="width-200">
				<option value="">선택</option>
				<option value="CADASSEMBLY">어셈블리 (ASSEMBLY)</option>
				<option value="CADCOMPONENT">파트 (PART)</option>
				<option value="CADDRAWING">도면 (DRAWING)</option>
			</select>
		</td>
		<th>MATERIAL</th>
		<td class="indent5">
			<input type="text" name="material" id="material">
		</td>
		<th>REMARK</th>
		<td class="indent5">
			<input type="text" name="remark" id="remark">
		</td>
		<th>REFERENCE 도면´</th>
		<td class="indent5">
			<input type="text" name="reference" id="reference">
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
		<th>수정자</th>
		<td class="indent5">
			<input type="text" name="modifier" id="modifier" data-multi="false">
			<input type="hidden" name="modifierOid" id="modifierOid">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('modifier')">
		</td>
		<th>수정일</th>
		<td class="indent5">
			<input type="text" name="modifiedFrom" id="modifiedFrom" class="width-100">
			~
			<input type="text" name="modifiedTo" id="modifiedTo" class="width-100">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('modifiedFrom', 'modifiedTo')">
		</td>
	</tr>
	<tr>
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
		<th>버전</th>
		<td colspan="5">
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
						<b>모든버전</b>
					</label>
				</div>
			</div>
		</td>
	</tr>
</table>

<table class="button-table">
	<tr>
		<td class="left">
			<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('epm-popup');">
			<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('epm-popup');">
			
<!-- 			 <input type="button" value="라이브러리" title="라이브러리" class="blue" onclick="toggle('library');"> -->
		</td>
		<td class="right">
			<input type="button" value="추가" title="추가" class="blue" onclick="<%=method%>();">
			<input type="button" value="조회" title="조회" onclick="loadGridData();">
<!-- 			<input type="button" value="라이브러리" title="라이브러리" class="blue" onclick="toggle('library');" style="background-color: orange;"> -->
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
				<jsp:param value="<%=EpmHelper.DEFAULT_ROOT%>" name="location" />
				<jsp:param value="product" name="container" />
				<jsp:param value="list" name="mode" />
				<jsp:param value="455" name="height" />
			</jsp:include>
		</td>
		<td>&nbsp;</td>
		<td>
			<div id="grid_wrap" style="height: 455px; border-top: 1px solid #3180c3;"></div>
			<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		</td>
	</tr>
</table>
<script type="text/javascript">
let myGridID;
function _layout() {
	return [ {
		dataField : "thumnail",
		headerText : "",
		dataType : "string",
		width : 60,
		renderer : {
			type : "ImageRenderer",
			altField : null,
			onClick : function(event) {
			}
		},
		filter : {
			showIcon : false,
			inline : false
		},
	}, {
		dataField : "name",
		headerText : "파일이름",
		dataType : "string",
		width : 350,
		style : "aui-left",
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "part_code",
		headerText : "품번",
		dataType : "string",
		width : 130,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "name_of_parts",
		headerText : "품명",
		dataType : "string",
		width : 350,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "dwg_no",
		headerText : "규격",
		dataType : "string",
		width : 130,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "material",
		headerText : "MATERIAL",
		dataType : "string",
		width : 130,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "remarks",
		headerText : "REMARK",
		dataType : "string",
		width : 150,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "reference",
		headerText : "REFERENCE 도면",
		dataType : "string",
		width : 150,
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
			showIcon : false,
			inline : false
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
		formatString : "yyyy-mm-dd",
		width : 100,
		filter : {
			showIcon : true,
			inline : true,
			displayFormatValues : true
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
		formatString : "yyyy-mm-dd",
		width : 100,
		filter : {
			showIcon : true,
			inline : true,
			displayFormatValues : true
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
		dataField : "location",
		headerText : "FOLDER",
		dataType : "string",
		width : 100,
		filter : {
			showIcon : false,
			inline : false
		},
	} ]
}

function createAUIGrid(columns) {
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
		enableRowCheckShiftKey : true,
		enableRightDownFocus : true,
		filterLayerWidth : 320,
		filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
		showRowCheckColumn : true,
		pageRowCount: 20,
		showPageRowSelect: true,
		usePaging : true
	};
	myGridID = AUIGrid.create("#grid_wrap", columns, props);
	// 				loadGridData();
	AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
	AUIGrid.bind(myGridID, "vScrollChange", function(event) {
		hideContextMenu();
// 		vScrollChangeHandler(event);
	});
	AUIGrid.bind(myGridID, "hScrollChange", function(event) {
		hideContextMenu();
	});
}

function <%=method%>() {
const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
if (checkedItems.length == 0) {
	alert("추가할 도면을 선택하세요.");
	return false;
}

const arr = new Array();
for(let i=0; i<checkedItems.length; i++) {
	const item = checkedItems[i].item;
	arr.push(item);
}

openLayer();
opener.<%=method%>(arr, function(result) {
		if(result) {
			setTimeout(function() {
				closeLayer();
			}, 500);
		}
	});
}

function loadGridData() {
	let params = new Object();
	const url = getCallUrl("/epm/list");
	const latest = !!document.querySelector("input[name=latest]:checked").value;
	const oid = document.getElementById("oid").vaue;
	const field = [ "fileName", "number", "oid", "state", "cadType", "createdFrom", "createdTo", "creatorOid", "modifierOid", "modifiedFrom", "modifiedTo", "material", "reference", "remark", "partCode", "partName" ];
	params = toField(params, field);
	params.container = "PRODUCT";
	params.latest = latest;
	AUIGrid.showAjaxLoader(myGridID);
	parent.openLayer();
	console.log(params);
	call(url, params, function(data) {
		AUIGrid.removeAjaxLoader(myGridID);
		document.getElementById("sessionid").value = data.sessionid;
		document.getElementById("curPage").value = data.curPage;document.getElementById("lastNum").value = data.list.length;
		AUIGrid.setGridData(myGridID, data.list);
		parent.closeLayer();
	});
}

document.addEventListener("DOMContentLoaded", function() {
	const columns = loadColumnLayout("epm-popup");
	const contenxtHeader = genColumnHtml(columns);
	$("#h_item_ul").append(contenxtHeader);
	$("#headerMenu").menu({
		select : headerMenuSelectHandler
	});
	createAUIGrid(columns);
	_createAUIGrid(_columns);
	gridResizeTree();
	gridResizeMain();
	selectbox("state");
	selectbox("cadType");
	finderUser("creator");
	finderUser("modifier");
	twindate("created");
	twindate("modified");
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
	gridResizeTree();
	gridResizeMain();
});
function gridResizeTree(){
	const ww = window.innerWidth;	//1654
	const hh = window.innerHeight;	//834
	//4row 15, 200
	//2row 15, 140
	AUIGrid.resize(_myGridID, 228, hh-250);
}
function gridResizeMain(){
	const ww = window.innerWidth;	//1654
	const hh = window.innerHeight;	//834
	//4row 15, 200
	//2row 15, 140
	AUIGrid.resize(myGridID, ww-260, hh-250);
}
</script>