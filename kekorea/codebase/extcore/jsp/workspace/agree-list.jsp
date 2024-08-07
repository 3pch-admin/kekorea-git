<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
WTUser sessionUser = (WTUser) request.getAttribute("sessionUser");
String before = (String) request.getAttribute("before");
String end = (String) request.getAttribute("end");
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
<body>
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
			</colgroup>
			<tr>
				<th>결재 제목</th>
				<td class="indent5">
					<input type="text" name="approvalTitle" id="approvalTitle" class="width-300">
				</td>
				<th>검토여부</th>
				<td>
					&nbsp;
					<div class="pretty p-switch">
						<input type="radio" name="state" value="전체">
						<div class="state p-success">
							<label>
								<b>전체</b>
							</label>
						</div>
					</div>
					&nbsp;
					<div class="pretty p-switch">
						<input type="radio" name="state" value="검토중" checked="checked">
						<div class="state p-success">
							<label>
								<b>검토중</b>
							</label>
						</div>
					</div>
					&nbsp;
					<div class="pretty p-switch">
						<input type="radio" name="state" value="검토반려">
						<div class="state p-success">
							<label>
								<b>검토반려</b>
							</label>
						</div>
					</div>
					&nbsp;
					<div class="pretty p-switch">
						<input type="radio" name="state" value="검토완료">
						<div class="state p-success">
							<label>
								<b>검토완료</b>
							</label>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<th>기안자</th>
				<td class="indent5">
					<input type="text" name="submiter" id="submiter" data-multi="false">
					<input type="hidden" name="submiterOid" id="submiterOid">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearUser('submiter')">
				</td>
				<th>수신일</th>
				<td class="indent5">
					<input type="text" name="receiveFrom" id="receiveFrom" class="width-100" value="<%//=before%>">
					~
					<input type="text" name="receiveTo" id="receiveTo" class="width-100" value="<%//=end%>">
					<img src="/Windchill/extcore/images/delete.png" class="delete" title="삭제" onclick="clearFromTo('receiveFrom', 'receiveTo')">
				</td>
			</tr>
		</table>
		<table class="button-table">
			<tr>
				<td class="left">
					<img src="/Windchill/extcore/images/fileicon/file_excel.gif" title="엑셀 다운로드" onclick="exportExcel();">
					<img src="/Windchill/extcore/images/save.gif" title="테이블 저장" onclick="saveColumnLayout('agree-list');">
					<img src="/Windchill/extcore/images/redo.gif" title="테이블 초기화" onclick="resetColumnLayout('agree-list');">
					<!--  <img src="/Windchill/extcore/images/help.gif" title="메뉴얼 재생" onclick="play('test1.mp4');">-->
					<input type="button" value="새로고침" title="새로고침" style="background-color: navy;" onclick="document.location.reload();">
				</td>
				<td class="right">
					<input type="button" value="조회" title="조회" style="background-color: navy;" onclick="loadGridData();">
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
					dataField : "reads",
					headerText : "확인",
					dataType : "boolean",
					width : 60,
					filter : {
						showIcon : false,
						inline : false
					},
					renderer : {
						type : "CheckBoxEditRenderer",
					}
				}, {
					dataField : "type",
					headerText : "구분",
					dataType : "string",
					width : 80,
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/workspace/lineView?oid=" + oid + "&columnType=COLUMN_AGREE&poid=" + item.poid);
							popup(url, 1500, 800);
						}
					},
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "role",
					headerText : "역할",
					dataType : "string",
					width : 80,
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/workspace/lineView?oid=" + oid + "&columnType=COLUMN_AGREE&poid=" + item.poid);
							popup(url, 1500, 800);
						}
					},
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "name",
					headerText : "결재 제목",
					dataType : "string",
					style : "aui-left",
					renderer : {
						type : "LinkRenderer",
						baseUrl : "javascript",
						jsCallback : function(rowIndex, columnIndex, value, item) {
							const oid = item.oid;
							const url = getCallUrl("/workspace/lineView?oid=" + oid + "&columnType=COLUMN_AGREE&poid=" + item.poid);
							popup(url, 1500, 800);
						}
					},
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "point",
					headerText : "진행단계",
					dataType : "string",
					style : "right",
					renderer : {
						type : "TemplateRenderer"
					},
					filter : {
						showIcon : false,
						inline : false
					},
				}, {
					dataField : "submiter",
					headerText : "기안자",
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
					width : 80,
					filter : {
						showIcon : true,
						inline : true
					},
				}, {
					dataField : "receiveTime",
					headerText : "수신일",
					dataType : "date",
					formatString : "yyyy-mm-dd HH:MM:ss",
					width : 170,
					filter : {
						showIcon : true,
						inline : true,
						displayFormatValues : true
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
					enableMovingColumn : true,
					showInlineFilter : true,
					useContextMenu : true,
					enableRightDownFocus : true,
					filterLayerWidth : 320,
					filterItemMoreMessage : "필터링 검색이 너무 많습니다. 검색을 이용해주세요.",
					pageRowCount : 20,
					showPageRowSelect : true,
					usePaging : true
				};
				myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
				loadGridData();
				AUIGrid.bind(myGridID, "contextMenu", auiContextMenuHandler);

			}

			function loadGridData() {
				let params = new Object();
				const url = getCallUrl("/workspace/agree");
				const field = [ "approvalTitle", "submiterOid", "receiveFrom", "receiveTo" ];
				params = toField(params, field);

				const radios = document.getElementsByName("state");
				let selectedValue;

				for (let i = 0; i < radios.length; i++) {
					if (radios[i].checked) {
						selectedValue = radios[i].value;
						break;
					}
				}
				params.state = selectedValue;
				AUIGrid.showAjaxLoader(myGridID);
				parent.openLayer();
				call(url, params, function(data) {
					AUIGrid.removeAjaxLoader(myGridID);
					document.getElementById("sessionid").value = data.sessionid;
					document.getElementById("curPage").value = data.curPage;
					document.getElementById("lastNum").value = data.list.length;
					AUIGrid.setGridData(myGridID, data.list);
					parent.closeLayer();
				});
			}

			function exportExcel() {
				const exceptColumnFields = [ "reads", "point" ];
				const sessionName = document.getElementById("sessionName").value;
				exportToExcel("검토함 리스트", "검토함", "검토함 리스트", exceptColumnFields, sessionName);
			}

			document.addEventListener("DOMContentLoaded", function() {
				document.getElementById("approvalTitle").focus();
				const columns = loadColumnLayout("agree-list");
				const contenxtHeader = genColumnHtml(columns);
				$("#h_item_ul").append(contenxtHeader);
				$("#headerMenu").menu({
					select : headerMenuSelectHandler
				});
				createAUIGrid(columns);
				AUIGrid.resize(myGridID);
				// 				gridResize();
				finderUser("submiter");
				twindate("receive");
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


			window.addEventListener('keydown', function(event) {
				if (event.key === 'F5') {
					event.preventDefault();
					const tab = parent.document.getElementById("tab2");
					if (tab != null) {
						const iframe = tab.querySelector('iframe');
						iframe.src = iframe.src;
					}
				}
			});
			
			function gridResize() {
				//1636-629
				const ww = window.innerWidth; //1654
				const hh = window.innerHeight; //834
				AUIGrid.resize(myGridID, ww - 15, hh - 200);
			}
		</script>
	</form>
</body>
</html>