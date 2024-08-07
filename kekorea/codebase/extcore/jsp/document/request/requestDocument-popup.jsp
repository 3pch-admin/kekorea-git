<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
ArrayList<Map<String, String>> customers = (ArrayList<Map<String, String>>) request.getAttribute("customers");
ArrayList<Map<String, String>> maks = (ArrayList<Map<String, String>>) request.getAttribute("maks");
ArrayList<Map<String, String>> projectTypes = (ArrayList<Map<String, String>>) request.getAttribute("projectTypes");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<script type="text/javascript" src="/Windchill/extcore/js/auigrid.js?v=1010"></script>
</head>
<body style="overflow:hidden;">
	<form>
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
				<th>의뢰서 제목</th>
				<td class="indent5">
					<input type="text" name="name" id="name" class="width-200">
				</td>
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
					<input type="text" name="pdateFrom" id="pdateFrom" class="width-100">
					~
					<input type="text" name="pdateTo" id="pdateTo" class="width-100">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('pdateFrom', 'pdateTo')">
				</td>
			</tr>
			<tr>
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
				<th>상태</th>
				<td class="indent5">
					<select name="state" id="state" class="width-200">
						<option value="">선택</option>
						<option value="INWORK">작업 중</option>
						<option value="UNDERAPPROVAL">승인 중</option>
						<option value="APPROVED">승인됨</option>
						<option value="RETURN">반려됨</option>
					</select>
				</td>
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
				<th>작업 내용</th>
				<td class="indent5">
					<input type="text" name="description" id="description" class="width-200">
				</td>
			</tr>
		</table>

		<table class="button-table">
			<tr>
				<td class="left">
					<img src="/Windchill/extcore/images/fileicon/file_excel.gif" title="엑셀 다운로드" onclick="exportExcel();">
					<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('requestDocument-list');">
					<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('requestDocument-list');">
					
					<%
// 					if (isAdmin) {
					%>
<!-- 					<input type="button" value="저장" title="저장" onclick="save();"> -->
<!-- 					<input type="button" value="행 삭제" title="행 삭제" class="red" onclick="deleteRow();"> -->
					<%
// 					}
					%>
				</td>
				<td class="right">
					<input type="button" value="등록" title="등록" class="blue" onclick="create();">
					<!--  <input type="button" value="확장" title="확장" onclick="expand();" style="background-color: orange;"> -->
					<input type="button" value="조회" title="조회" onclick="loadGridData();" style="background-color: navy;">
				</td>
			</tr>
		</table>

		<div id="grid_wrap" style="height: 635px; border-top: 1px solid #3180c3;"></div>
		<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		<script type="text/javascript">
			let myGridID;
			function _layout() {
				return [ {
					dataField : "name",
					headerText : "의뢰서 제목",
					dataType : "string",
					width : 300,
					style : "aui-left",
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/requestDocument/view?oid=" + oid);
							popup(url, 1600, 800);
						}
					},
					filter : {
						showIcon : true,
						inline : true
					}
				}, {
					dataField : "number",
					headerText : "의뢰서 번호",
					dataType : "string",
					width : 400,
					filter : {
						showIcon : true,
						inline : true
					}
				}, {
					dataField : "kekNumber",
					headerText : "KEK 작번",
					dataType : "string",
					width : 400,
					filter : {
						showIcon : true,
						inline : true
					}
				}, {
					dataField : "keNumber",
					headerText : "KE 작번",
					dataType : "string",
					width : 400,
					filter : {
						showIcon : true,
						inline : true
					}
				}, {
					dataField : "version",
					headerText : "버전",
					dataType : "string",
					width : 80,
					filter : {
						showIcon : false,
						inline : false
					}
				}, {
					dataField : "state",
					headerText : "상태",
					dataType : "string",
					width : 80,
					filter : {
						showIcon : true,
						inline : true
					}
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
						displayFormatValues : true
					}
				}, {
					dataField : "modifier",
					headerText : "수정자",
					dataType : "string",
					width : 100,
					filter : {
						showIcon : true,
						inline : true
					}
				}, {
					dataField : "modifiedDate",
					headerText : "수정일",
					dataType : "string",
					width : 100,
					filter : {
						showIcon : true,
						inline : true,
						displayFormatValues : true
					}
				} ]
			}

			function createAUIGrid(columnLayout) {
				const props = {
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					selectionMode : "singleRow",
					enableFilter : true,
					enableMovingColumn : true,
					showInlineFilter : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
					pageRowCount: 20,
					showPageRowSelect: true,
					usePaging : true
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				loadGridData();
				AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);
				AUIGrid.bind(myGridID, "vScrollChange", function(event) {
					hideContextMenu();
// 					vScrollChangeHandler(event);
				});
				AUIGrid.bind(myGridID, "hScrollChange", function(event) {
					hideContextMenu();
				});
			}

			function auiCellDoubleClick(event) {
				const dataField = event.dataField;
				const oid = event.item.oid;
				console.log(oid);
				if (dataField === "name") {
					const url = getCallUrl("/requestDocument/view?oid=" + oid);
					popup(url, 1400, 700);
				}
			}

			function save() {
				if (!confirm("저장 하시겠습니까?")) {
					return false;
				}
				const url = getCallUrl("/requestDocument/save");
				const params = new Object();
				const removeRows = AUIGrid.getRemovedItems(myGridID);
				params.removeRows = removeRows;
				call(url, params, function(data) {
					alert(data.msg);
					if (data.result) {
						loadGridData();
					}
				})
			}

			function create() {
				const url = getCallUrl("/requestDocument/create");
				popup(url, 1800, 750);
// 				popup(url);
			}

			function loadGridData() {
				let params = new Object();
				const url = getCallUrl("/requestDocument/list");
				const field = [ "name", "kekNumber", "keNumber", "pdateFrom", "pdateTo", "customer_name", "install_name", "projectType", "machineOid", "elecOid", "softOid", "mak_name", "detail_name", "description", "state", "creatorOid", "createdFrom", "createdTo" ];
				params = toField(params, field);
				AUIGrid.showAjaxLoader(myGridID);
				parent.openLayer();
				call(url, params, function(data) {
					console.log(data);
					document.getElementById("curPage").value = data.curPage;document.getElementById("lastNum").value = data.list.length;
					document.getElementById("sessionid").value = data.sessionid;
					AUIGrid.setGridData(myGridID, data.list);
					AUIGrid.removeAjaxLoader(myGridID);
					parent.closeLayer();
				});
			}

			function deleteRow() {
				const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
				for (let i = checkedItems.length - 1; i >= 0; i--) {
					const rowIndex = checkedItems[i].rowIndex;
					AUIGrid.removeRow(myGridID, rowIndex);
				}
			}

			document.addEventListener("DOMContentLoaded", function() {
				toFocus("name");
				const columns = loadColumnLayout("request-list");
				const contenxtHeader = genColumnHtml(columns);
				$("#h_item_ul").append(contenxtHeader);
				$("#headerMenu").menu({
					select : headerMenuSelectHandler
				});
				createAUIGrid(columns);
				gridResize();
				twindate("pdate");
				selectbox("state");
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
				finderUser("creator");
				twindate("created");
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

			function exportExcel() {
				const exceptColumnFields = [];
				const sessionName = document.getElementById("sessionName").value;
				exportToExcel("의뢰서 리스트", "의뢰서", "의뢰서 리스트", exceptColumnFields, sessionName);
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
				AUIGrid.resize(myGridID, ww-15, hh-200);
			}
		</script>
	</form>
</body>
</html>