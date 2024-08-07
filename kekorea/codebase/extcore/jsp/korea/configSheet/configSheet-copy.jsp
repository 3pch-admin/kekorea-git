<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String method = (String) request.getAttribute("method");
boolean multi = (boolean) request.getAttribute("multi");
ArrayList<Map<String, String>> customers = (ArrayList<Map<String, String>>) request.getAttribute("customers");
ArrayList<Map<String, String>> maks = (ArrayList<Map<String, String>>) request.getAttribute("maks");
ArrayList<Map<String, String>> projectTypes = (ArrayList<Map<String, String>>) request.getAttribute("projectTypes");
ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) request.getAttribute("list");
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
String before = (String) request.getAttribute("before");
String end = (String) request.getAttribute("end");
%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js"></script>
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
<input type="hidden" name="sessionid" id="sessionid"><input type="hidden" name="lastNum" id="lastNum">
<input type="hidden" name="curPage" id="curPage">
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
		<th>KEK 작번</th>
		<td class="indent5">
			<input type="text" name="kekNumber" id="kekNumber">
		</td>
		<th>KE 작번</th>
		<td class="indent5">
			<input type="text" name="keNumber" id="keNumber">
		</td>
		<th>발행일</th>
		<td class="indent5">
			<input type="text" name="pdateFrom" id="pdateFrom" class="width-100" value="<%=before%>">
			~
			<input type="text" name="pdateTo" id="pdateTo" class="width-100" value="<%=end%>">
			<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFronTo('pdateFrom', 'pdateTo')">
		</td>
		<th>USER ID</th>
		<td class="indent5">
			<input type="text" name="userId" id="userId">
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
		<th>템플릿</th>
		<td class="indent5">
			<select name="template" id="template" class="width-200">
				<option value="">선택</option>
				<%
				for (Map<String, String> map : list) {
					String oid = map.get("key");
					String name = map.get("value");
				%>
				<option value="<%=oid%>"><%=name%></option>
				<%
				}
				%>
			</select>
		</td>
		<th>작업 내용</th>
		<td colspan="3" class="indent5">
			<input type="text" name="description" id="description" class="width-200">
		</td>
	</tr>
</table>

<table class="button-table">
	<tr>
		<td class="left">
			<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('project-popup');">
			<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('project-popup');">
			
		</td>
		<td class="right">
			<input type="button" value="추가" title="추가" class="blue" onclick="<%=method%>();">
			<input type="button" value="조회" title="조회" onclick="loadGridData();">
			<input type="button" value="닫기" title="닫기" class="red" onclick="self.close();">
		</td>
	</tr>
</table>

<div id="grid_wrap" style="height: 480px; border-top: 1px solid #3180c3;"></div>
<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
<script type="text/javascript">
	let myGridID;
	function _layout() {
		 return [ {
				dataField : "state",
				headerText : "진행상태",
				dataType : "string",
				width : 80,
				renderer : {
					type : "TemplateRenderer",
				},
				filter : {
					showIcon : false,
					inline : false
				},
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
				width : 130,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "keNumber",
				headerText : "KE 작번",
				dataType : "string",
				width : 130,
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
				dataField : "pdate",
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
				dataField : "completeDate",
				headerText : "설계 완료일",
				dataType : "date",
				formatString : "yyyy-mm-dd",
				width : 100,
			}, {
				dataField : "customDate",
				headerText : "요구 납기일",
				dataType : "date",
				formatString : "yyyy-mm-dd",
				width : 100,
				filter : {
					showIcon : true,
					inline : true,
					displayFormatValues : true
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
				dataField : "machine",
				headerText : "기계 담당자",
				dataType : "string",
				width : 100,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "elec",
				headerText : "전기 담당자",
				dataType : "string",
				width : 100,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "soft",
				headerText : "SW 담당자",
				dataType : "string",
				width : 100,
				filter : {
					showIcon : true,
					inline : true
				},
			}, {
				dataField : "kekProgress",
				headerText : "진행율",
				postfix : "%",
				width : 80,
				renderer : {
					type : "BarRenderer",
					min : 0,
					max : 100
				},
				filter : {
					showIcon : false,
					inline : false
				},
			}, {
				dataField : "kekState",
				headerText : "작번상태",
				dataType : "string",
				width : 100,
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
			showRowCheckColumn : true,
			rowCheckToRadio : true,
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
			pageRowCount: 20,
			showPageRowSelect: true,
			usePaging : true
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		loadGridData();

	}
	
	function loadGridData() {
		let params = new Object();
		const url = getCallUrl("/project/list");
		const field = [ "kekNumber", "keNumber", "pdateFrom", "pdateTo", "userId", "kekState", "model", "customer_name", "install_name", "projectType", "machineOid", "elecOid", "softOid", "mak_name", "detail_name", "template", "description" ];
		params = toField(params, field);
		AUIGrid.showAjaxLoader(myGridID);
		openLayer();
		call(url, params, function(data) {
			AUIGrid.removeAjaxLoader(myGridID);
			document.getElementById("sessionid").value = data.sessionid;
			document.getElementById("curPage").value = data.curPage;document.getElementById("lastNum").value = data.list.length;
			AUIGrid.setGridData(myGridID, data.list);
			closeLayer();
		});
	}
	
	function <%=method%>() {
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		if (checkedItems.length == 0) {
			alert("CONFIG SHEET 복사할 작번을 선택하세요.");
			return false;
		}
		openLayer();
		opener.<%=method%>(checkedItems[0], function(result, msg) {
			if(result) {
				setTimeout(function() {
					if(msg !== "") {
						alert(msg);
					}
					closeLayer();
				}, 500);
			}
		});
	}
	
	
	document.addEventListener("DOMContentLoaded", function() {
		document.getElementById("kekNumber").focus();
		const columns = loadColumnLayout("project-popup");
		const contenxtHeader = genColumnHtml(columns); 
		$("#h_item_ul").append(contenxtHeader);
		$("#headerMenu").menu({
			select : headerMenuSelectHandler
		});
		createAUIGrid(columns);
// 		AUIGrid.resize(myGridID);
		gridResize();
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
		selectbox("template");
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
		gridResize();
	});
	
	function gridResize(){
		//1636-629
		const ww = window.innerWidth;	//1654
		const hh = window.innerHeight;	//834
		AUIGrid.resize(myGridID, ww-15, hh-200);
	}
</script>