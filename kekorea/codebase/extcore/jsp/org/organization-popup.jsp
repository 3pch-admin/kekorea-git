<%@page import="net.sf.json.JSONArray"%>
<%@page import="wt.org.WTUser"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="e3ps.org.service.OrgHelper"%>
<%@page import="e3ps.common.util.CommonUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean multi = (boolean) request.getAttribute("multi");
String openerId = (String) request.getAttribute("openerId");
ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) request.getAttribute("list");
JSONArray maks = (JSONArray) request.getAttribute("maks");
JSONArray departments = JSONArray.fromObject(list);
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js"></script>
<input type="hidden" name="sessionid" id="sessionid">
<input type="hidden" name="lastNum" id="lastNum">
<input type="hidden" name="curPage" id="curPage">
<input type="hidden" name="lastNum" id="lastNum">
<input type="hidden" name="oid" id="oid">
<table class="search-table">
	<colgroup>
		<col width="130">
		<col width="600">
		<col width="130">
		<col width="600">
		<col width="130">
		<col width="600">
	</colgroup>
	<tr>
		<th>부서</th>
		<td class="indent5" colspan="5">
			<span id="departmentText"><%=OrgHelper.DEPARTMENT_ROOT%></span>
		</td>
	</tr>
	<tr>
		<th>이름</th>
		<td class="indent5">
			<input type="text" name="userName" id="userName">
		</td>
		<th>아이디</th>
		<td class="indent5">
			<input type="text" name="userId" id="userId">
		</td>
		<th>퇴사여부</th>
		<td>
			&nbsp;
			<div class="pretty p-switch">
				<input type="radio" name="resign" value="" checked="checked">
				<div class="state p-success">
					<label>
						<b>재직</b>
					</label>
				</div>
			</div>
			&nbsp;
			<div class="pretty p-switch">
				<input type="radio" name="resign" value="true">
				<div class="state p-success">
					<label>
						<b>퇴사</b>
					</label>
				</div>
			</div>
		</td>
	</tr>
</table>


<table class="button-table">
	<tr>
		<td class="left">
			
		</td>
		<td class="right">
			<input type="button" value="추가" title="추가" class="blue" onclick="selectedUser()">
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
			<jsp:include page="/extcore/jsp/common/department-include.jsp">
				<jsp:param value="list" name="mode" />
				<jsp:param value="465" name="height" />
			</jsp:include>
		</td>
		<td>&nbsp;</td>
		<td valign="top">
			<div id="grid_wrap" style="height: 465px; border-top: 1px solid #3180c3;"></div>
		</td>
	</tr>
</table>
<script type="text/javascript">
	let myGridID;
	const maks = <%=maks%>
	const departments = <%=departments%>
	const dutys = [ "사장", "부사장", "PL", "TL" ];
	const columns = [ {
		dataField : "name",
		headerText : "이름",
		dataType : "string",
		width : 100,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "id",
		headerText : "아이디",
		dataType : "string",
		width : 100,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "duty",
		headerText : "직급",
		dataType : "string",
		width : 130,
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "department_oid",
		headerText : "부서",
		dataType : "string",
		width : 150,
		labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
			let retStr = "";
			for (let i = 0, len = departments.length; i < len; i++) {
				if (departments[i]["oid"] == value) {
					retStr = departments[i]["name"];
					break;
				}
			}
			return retStr == "" ? value : retStr;
		},
	}, {
		dataField : "mak",
		headerText : "막종",
		dataType : "string",
		style : "aui-left",
		width : 300,
		labelFunction : function(rowIndex, columnIndex, value, headerText, item) {
			let retStr = "";
			for (let i = 0, len = maks.length; i < len; i++) {
				if (maks[i]["key"] == value) {
					retStr = maks[i]["value"];
					break;
				}
			}
			return retStr == "" ? value : retStr;
		},
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "email",
		headerText : "이메일",
		dataType : "string",
		style : "aui-left",
		filter : {
			showIcon : true,
			inline : true
		},
	}, {
		dataField : "resign",
		headerText : "퇴사여부",
		dataType : "boolean",
		width : 80,
		renderer : {
			type : "CheckBoxEditRenderer",
			editable : false
		},
		filter : {
			showIcon : false,
			inline : false
		},
	}, {
		dataField : "createdDate",
		headerText : "등록일",
		dataType : "date",
		formatString : "yyyy-mm-dd",
		width : 100,
		filter : {
			showIcon : true,
			inline : true,
			displayFormatValues : true
		},
	} ]

	function createAUIGrid(columnLayout) {
		const props = {
			headerHeight : 30,
			showRowNumColumn : true,
			showRowCheckColumn : true,
			rowNumHeaderText : "번호",
			showAutoNoDataMessage : false,
			enableFilter : true,
			selectionMode : "multipleCells",
			showInlineFilter : true,
			filterLayerWidth : 320,
			filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
			pageRowCount: 20,
			showPageRowSelect: true,
			usePaging : true,
			<%if (!multi) {%>
			rowCheckToRadio : true
			<%}%>
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		loadGridData();
		AUIGrid.bind(myGridID, "vScrollChange", function(event) {
// 			vScrollChangeHandler(event);
		});
	}

	function loadGridData() {
		let params = new Object();
		const url = getCallUrl("/org/list");
		const field = ["userName","userId","oid"];
		const resign = !!document.querySelector("input[name=resign]:checked").value;
		params = toField(params, field);
		params.resign = resign;
		AUIGrid.showAjaxLoader(myGridID);
		openLayer();
		call(url, params, function(data) {
			AUIGrid.removeAjaxLoader(myGridID);
			if (data.result) {
				document.getElementById("sessionid").value = data.sessionid;
				document.getElementById("curPage").value = data.curPage;
				document.getElementById("lastNum").value = data.list.length;
				AUIGrid.setGridData(myGridID, data.list);
			} else {
				alert(data.msg);
			}
			closeLayer();
		});
	}

	function selectedUser() {
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		if(checkedItems.length ==0) {
			alert("추가할 사용자를 선택하세요.");
			return;
		}
		inputUser("<%=openerId%>", checkedItems[0]);
		self.close();
	}
	
	document.addEventListener("DOMContentLoaded", function() {
		document.getElementById("userName").focus();
		createAUIGrid(columns);
		_createAUIGrid(_columns); // 트리
		AUIGrid.resize(myGridID);
		AUIGrid.resize(_myGridID); // 트리
// 		selectbox("_psize");
	});
	
	document.addEventListener("keydown", function(event) {
		const keyCode = event.keyCode || event.which;
		if (keyCode === 13) {
			loadGridData();
		}
	})

	window.addEventListener("resize", function() {
		
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//465
		console.log(ww+"=="+hh);
		AUIGrid.resize(_myGridID, 228, hh-150); // 트리
		AUIGrid.resize(myGridID, ww-260, hh-150);
		
	});
</script>