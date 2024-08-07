<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="wt.org.WTUser"%>
<%@page import="org.json.JSONArray"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
ArrayList<Map<String, String>> customers = (ArrayList<Map<String, String>>) request.getAttribute("customers");
ArrayList<Map<String, String>> maks = (ArrayList<Map<String, String>>) request.getAttribute("maks");
ArrayList<Map<String, String>> projectTypes = (ArrayList<Map<String, String>>) request.getAttribute("projectTypes");
ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) request.getAttribute("list");
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
String poid = (String) request.getAttribute("poid");
String toid = (String) request.getAttribute("toid");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
<input type="hidden" name="sessionid" id="sessionid"><input type="hidden" name="lastNum" id="lastNum">
<input type="hidden" name="curPage" id="curPage">
<input type="hidden" name="toid" id="toid" value="<%=toid%>">
<input type="hidden" name="poid" id="poid" value="<%=poid%>">
<table class="search-table">
	<colgroup>
		<col width="100">
		<col width="500">
		<col width="100">
		<col width="500">
		<col width="100">
		<col width="500">
		<col width="100">
		<col width="500">
	</colgroup>
	<tr>
		<th>회의록 제목</th>
		<td class="indent5">
			<input type="text" name="name" id="name" class="width-200">
		</td>
		<th>KEK 작번</th>
		<td class="indent5">
			<input type="text" name="kekNumber" id="kekNumber" class="width-200">
		</td>
		<th>KE 작번</th>
		<td class="indent5">
			<input type="text" name="keNumber" id="keNumber" class="width-200">
		</td>
		<th>발행일</th>
		<td class="indent5">
			<input type="text" name="pdateFrom" id="pdateFrom" class="width-100">
			~
			<input type="text" name="pdateTo" id="pdateTo" class="width-100">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('pdateFrom', 'pdateTo')">
		</td>
	</tr>
	<tr>
		<th>작번 상태</th>
		<td class="indent5">
			<select name="kekState" id="kekState" class="width-200">
				<option value="">선택</option>
				<option value="준비">준비</option>
				<option value="설계중">설계중</option>
				<option value="설계완료">설계완료</option>
				<option value="작업완료">작업완료</option>
				<option value="중단됨">중단됨</option>
				<option value="취소">취소</option>
			</select>
		</td>
		<th>모델</th>
		<td class="indent5">
			<input type="text" name="model" id="model">
		</td>
		<th>거래처</th>
		<td class="indent5">
			<select name="customer_name" id="customer_name" class="width-200">
				<option value="">선택</option>
				<%
				for (Map customer : customers) {
				%>
				<option value="<%=customer.get("key")%>"><%=customer.get("value")%></option>
				<%
				}
				%>
			</select>
		</td>
		<th>설치장소</th>
		<td class="indent5">
			<select name="install_name" id="install_name" class="width-200">
				<option value="">선택</option>
			</select>
		</td>
	</tr>
	<tr>
		<th>작번 유형</th>
		<td class="indent5">
			<select name="projectType" id="projectType" class="width-200">
				<option value="">선택</option>
				<%
				for (Map projectType : projectTypes) {
				%>
				<option value="<%=projectType.get("key")%>"><%=projectType.get("value")%></option>
				<%
				}
				%>
			</select>
		</td>
		<th>기계 담당자</th>
		<td class="indent5">
			<input type="text" name="machine" id="machine" data-multi="false">
			<input type="hidden" name="machineOid" id="machineOid">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('machine')">
		</td>
		<th>전기 담당자</th>
		<td class="indent5">
			<input type="text" name="elec" id="elec" data-multi="false">
			<input type="hidden" name="elecOid" id="elecOid">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('elec')">
		</td>
		<th>SW 담당자</th>
		<td class="indent5">
			<input type="text" name="soft" id="soft" data-multi="false">
			<input type="hidden" name="softOid" id="softOid">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('soft')">
		</td>
	</tr>
	<tr>
		<th>막종</th>
		<td class="indent5">
			<select name="mak_name" id="mak_name" class="width-200">
				<option value="">선택</option>
				<%
				for (Map<String, String> map : maks) {
					String oid = map.get("key");
					String name = map.get("value");
				%>
				<option value="<%=oid%>"><%=name%></option>
				<%
				}
				%>
			</select>
		</td>
		<th>막종상세</th>
		<td class="indent5">
			<select name="detail_name" id="detail_name" class="width-200">
				<option value="">선택</option>
			</select>
		</td>
		<th>USER ID</th>
		<td class="indent5">
			<input type="text" name="userId" id="userId">
		</td>
		<th>작업 내용</th>
		<td colspan="3" class="indent5">
			<input type="text" name="description" id="description" class="width-200">
		</td>
	</tr>
</table>


<table class="button-table">
	<tr>
		<td class="right">
			<input type="button" value="추가" title="추가" class="blue" onclick="connect();">
			<input type="button" value="조회" title="조회" onclick="loadGridData();">
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>

<div id="grid_wrap" style="height: 480px; border-top: 1px solid #3180c3;"></div>
<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
<script type="text/javascript">
	let myGridID;
	function _layout() {
		return [ {
			dataField : "name",
			headerText : "회의록 제목",
			dataType : "string",
			width : 350,
			filter : {
				showIcon : true,
				inline : true
			},
			cellMerge : true,
		}, {
			dataField : "projectType_name",
			headerText : "작번유형",
			dataType : "string",
			width : 80,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "customer_name",
			headerText : "거래처",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "install_name",
			headerText : "설치장소",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "mak_name",
			headerText : "막종",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "detail_name",
			headerText : "막종상세",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "kekNumber",
			headerText : "KEK 작번",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "keNumber",
			headerText : "KE 작번",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "userId",
			headerText : "USER ID",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "description",
			headerText : "작업 내용",
			dataType : "string",
			width : 450,
			style : "aui-left",
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "model",
			headerText : "모델",
			dataType : "string",
			width : 130,
			filter : {
				showIcon : true,
				inline : true
			},
		}, {
			dataField : "pdate_txt",
			headerText : "발행일",
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
			width : 80,
			filter : {
				showIcon : true,
				inline : true
			},
			cellMerge : true,
			mergeRef : "name",
			mergePolicy : "restrict"
		}, {
			dataField : "creator",
			headerText : "작성자",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true
			},
			cellMerge : true,
			mergeRef : "name",
			mergePolicy : "restrict"
		}, {
			dataField : "createdDate_txt",
			headerText : "작성일",
			dataType : "string",
			width : 100,
			filter : {
				showIcon : true,
				inline : true,
			},
			cellMerge : true,
			mergeRef : "name",
			mergePolicy : "restrict"
		} ]
	}

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
			fixedColumnCount : 1,
			enableCellMerge : true,
			forceTreeView : true,
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

	function connect() {
		const toid = document.getElementById("toid").value;
		const poid = document.getElementById("poid").value;
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		if (checkedItems.length == 0) {
			alert("추가할 회의록을 선택하세요.");
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
		const params = new Object();
		const url = getCallUrl("/meeting/list");
		const kekNumber = document.getElementById("kekNumber").value;
		const keNumber = document.getElementById("keNumber").value;
		const pdateFrom = document.getElementById("pdateFrom").value;
		const pdateTo = document.getElementById("pdateTo").value;
		const userId = document.getElementById("userId").value;
		const kekState = document.getElementById("kekState").value;
		const model = document.getElementById("model").value;
		const customer_name = document.getElementById("customer_name").value;
		const install_name = document.getElementById("install_name").value;
		const projectType = document.getElementById("projectType").value;
		const machineOid = document.getElementById("machineOid").value;
		const elecOid = document.getElementById("elecOid").value;
		const softOid = document.getElementById("softOid").value;
		const mak_name = document.getElementById("mak_name").value;
		const detail_name = document.getElementById("detail_name").value;
		const description = document.getElementById("description").value;
		const name = document.getElementById("name").value;
		params.name = name;
		params.kekNumber = kekNumber;
		params.keNumber = keNumber;
		params.pdateFrom = pdateFrom;
		params.pdateTo = pdateTo;
		params.userId = userId;
		params.kekState = kekState;
		params.model = model;
		params.customer_name = customer_name;
		params.install_name = install_name;
		params.projectType = projectType;
		params.machineOid = machineOid;
		params.elecOid = elecOid;
		params.softOid = softOid;
		params.mak_name = mak_name;
		params.detail_name = detail_name;
		params.description = description;
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
		const columns = loadColumnLayout("meeting-list");
		const contenxtHeader = genColumnHtml(columns);
		$("#h_item_ul").append(contenxtHeader);
		$("#headerMenu").menu({
			select : headerMenuSelectHandler
		});
		createAUIGrid(columns);
		AUIGrid.resize(myGridID);
		twindate("pdate");
		selectbox("kekState");
		$("#customer_name").bindSelect({
			onchange : function() {
				const oid = this.optionValue;
				$("#install_name").bindSelect({
					ajaxUrl : getCallUrl("/commonCode/getChildrens?parentOid=" + oid),
					reserveKeys : {
						options : "list",
						optionValue : "value",
						optionText : "name"
					},
					setValue : this.optionValue,
					alwaysOnChange : true,
				})
			}
		})
		selectbox("install_name");
		selectbox("projectType");
		finderUser("machine");
		finderUser("elec");
		finderUser("soft");
		$("#mak_name").bindSelect({
			onchange : function() {
				const oid = this.optionValue;
				$("#detail_name").bindSelect({
					ajaxUrl : getCallUrl("/commonCode/getChildrens?parentOid=" + oid),
					reserveKeys : {
						options : "list",
						optionValue : "value",
						optionText : "name"
					},
					setValue : this.optionValue,
					alwaysOnChange : true,
				})
			}
		})
		selectbox("detail_name");
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
		AUIGrid.resize(myGridID);
	});
	
	
</script>