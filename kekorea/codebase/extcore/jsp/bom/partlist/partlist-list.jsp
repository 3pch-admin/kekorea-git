<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	boolean isAdmin = (boolean) request.getAttribute("isAdmin");
	boolean isSupervisor = (boolean) request.getAttribute("isSupervisor");
	WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<%@include file="/extcore/jsp/common/css.jsp"%>
<%@include file="/extcore/jsp/common/script.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
</head>
<body style="overflow: hidden;">
	<form>
		<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
		<input type="hidden" name="sessionName" id="sessionName" value="<%=sessionUser.getFullName()%>">
		<input type="hidden" name="sessionId" id="sessionId" value="<%=sessionUser.getName()%>">
		<input type="hidden" name="sessionid" id="sessionid">
		<input type="hidden" name="lastNum" id="lastNum">
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
					<input type="text" name="partlistName" id="partlistName" class="width-200">
				</td>
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
				<th>KEK 작번</th>
				<td class="indent5">
					<input type="text" name="kekNumber" id="kekNumber">
				</td>
				<th>KE 작번</th>
				<td class="indent5">
					<input type="text" name="keNumber" id="keNumber">
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
					<input type="hidden" name="modifierOId" id="modifierOid" data-multi="false">
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
		</table>

		<table class="button-table">
			<tr>
				<td class="left">
					<img src="/Windchill/extcore/images/fileicon/file_excel.gif" title="엑셀 다운로드" onclick="exportExcel();">
					<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('partlist-list');">
					<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('partlist-list');">
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
					<!--  <img src="/Windchill/extcore/images/help.gif" title="메뉴얼 재생" onclick="play('test.mp4');"> -->
				</td>
				<td class="right">
					<input type="button" value="등록" title="등록" class="blue" onclick="create();">
					<!--	<input type="button" value="확장" title="확장" onclick="expand();" style="background-color: orange;"> -->
					<input type="button" value="조회" title="조회" onclick="loadGridData();" style="background-color: navy;">
				</td>
			</tr>
		</table>

		<!-- 메뉴얼 비디오 구간 -->
		<%@include file="/extcore/jsp/common/video-layer.jsp"%>

		<div id="grid_wrap" style="height: 680px; border-top: 1px solid #3180c3;"></div>
		<%@include file="/extcore/jsp/common/aui/aui-context.jsp"%>
		<script type="text/javascript">
			let myGridID;
			function _layout() {
				return [ {
					dataField : "number",
					headerText : "수배표 번호",
					dataType : "string",
					width : 120,
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
					dataField : "name",
					headerText : "수배표 제목",
					dataType : "string",
					// 					width : 300,
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
					dataField : "eng",
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
					//					renderer : {
					//						type : "LinkRenderer",
					//						baseUrl : "javascript",
					//						jsCallback : function(rowIndex, columnIndex, value, item) {
					// 							alert("( " + rowIndex + ", " + columnIndex + " ) " + item.color + "  Link 클릭\r\n자바스크립트 함수 호출하고자 하는 경우로 사용하세요!");
					//							const poid = item.poid;
					//							const url = getCallUrl("/project/info?oid=" + poid);
					//							popup(url);
					//						}
					//					},
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
					// 					renderer : { //팝업 필요성 확인 필요
					// 						type : "LinkRenderer",
					// 						baseUrl : "javascript",
					// 						jsCallback : function(rowIndex, columnIndex, value, item) {
					// 							const poid = item.poid;
					// 							const url = getCallUrl("/project/info?oid=" + poid);
					// 							popup(url);
					// 						}
					// 					},
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
					enableMovingColumn : true,
					showInlineFilter : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
					editable : false,
					pageRowCount : 20,
					showPageRowSelect : true,
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
				// 				AUIGrid.bind(myGridID, "cellDoubleClick", auiCellDoubleClick);
			}

			// 			function auiCellDoubleClick(event) {
			// 				const dataField = event.dataField;
			// 				const item = event.item;
			// 				if (dataField === "name") {
			// 					const url = getCallUrl("/partlist/view?oid=" + item.loid);
			// 					popup(url, 1700, 800);
			// 				}
			// 			}

			function loadGridData() {
				let params = new Object();
				const url = getCallUrl("/partlist/list");
				const field = [ "partlistName", "state", "kekNumber", "keNumber",
				//"description", "engType", 
				//"pdateFrom", "pdateTo", 
				//"customer_name", "install_name",
				//"mak_name","detail_name",
				"creatorOid", "createdFrom", "createdTo", "modifier", "modifiedFrom", "modifiedTo" ];
				params = toField(params, field);
				AUIGrid.showAjaxLoader(myGridID);
				parent.openLayer();
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
					parent.closeLayer();
				});
			}

			function create() {
				const url = getCallUrl("/partlist/create");
				popup(url, 1800, 960);
			}

			document.addEventListener("DOMContentLoaded", function() {
				const columns = loadColumnLayout("partlist-list");
				const contenxtHeader = genColumnHtml(columns);
				$("#h_item_ul").append(contenxtHeader);
				$("#headerMenu").menu({
					select : headerMenuSelectHandler
				});
				createAUIGrid(columns);
				AUIGrid.resize(myGridID);
				selectbox("state");
				selectbox("projectType_name");
				finderUser("creator");
				finderUser("modifier");
				twindate("created");
				twindate("modified");

			});

			function exportExcel() {
				const exceptColumnFields = [ "primary" ];
				const sessionName = document.getElementById("sessionName").value;
				exportToExcel("수배표 리스트", "수배표", "수배표 리스트", exceptColumnFields, sessionName);
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
				AUIGrid.resize(myGridID);
			});
			function gridResize() {
				//1636-629
				const ww = window.innerWidth; //1654
				const hh = window.innerHeight; //834
				AUIGrid.resize(myGridID, ww - 15, hh - 160);
			}
			
			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab31");
					if (tab != null) {
						const iframe = tab.querySelector('iframe');
						iframe.src = iframe.src;
					}
				}
			});
		</script>
	</form>
</body>
</html>