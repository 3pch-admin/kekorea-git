<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
		<table class="button-table">
			<tr>
				<td class="left">
					<div class="header">
						<img src="/Windchill/extcore/images/header.png">
						코드 등록
					</div>
				</td>
				<td class="right">
					<input type="button" value="등록" title="등록" class="blue" onclick="create();">
				</td>
			</tr>
		</table>
		<table class="create-table">
			<colgroup>
				<col width="150">
				<col width="*">
			</colgroup>
			<tr>
				<th class="req lb">결재 제목</th>
				<td class="indent5">
					<input type="text" name="name" id="name" class="width-400">
				</td>
			</tr>
		</table>

		<br>
		<table class="button-table">
			<tr>
				<td class="left">
					<div class="header">
						<img src="/Windchill/extcore/images/header.png">
						결재 도면
					</div>
				</td>
				<td class="right">
					<input type="button" value="도면 추가" title="도면 추가" class="blue" onclick="addRow1();">
					<input type="button" value="도면 삭제" title="도면 삭제" class="red" onclick="deleteRow1();">
				</td>
			</tr>
		</table>
		<div id="grid_wrap1" style="height: 100px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID1;
			const columns1 = [ {
				dataField : "name",
				headerText : "파일명",
				dataType : "string",
				width : 350,
			}, {
				dataField : "name_of_parts",
				headerText : "품명",
				dataType : "string",
			}, {
				dataField : "dwg_no",
				headerText : "규격",
				dataType : "string",
				width : 150
			}, {
				dataField : "version",
				headerText : "버전",
				dataType : "string",
				width : 100
			}, {
				dataField : "state",
				headerText : "상태",
				dataType : "string",
				width : 120
			}, {
				dataField : "creator",
				headerText : "작성자",
				dataType : "string",
				width : 120
			}, {
				dataField : "modifier",
				headerText : "수정자",
				dataType : "string",
				width : 120
			}, {
				dataField : "oid",
				dataType : "string",
				headerText : "",
				visible : false
			} ];

			function createAUIGrid1(columnLayout) {
				const props = {
					showRowCheckColumn : true,
					autoGridHeight : true,
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					selectionMode : "multipleCells",
				};
				myGridID1 = AUIGrid.create("#grid_wrap1", columnLayout, props);
			}
		</script>

		<br>
		<table class="button-table">
			<tr>
				<td class="left">
					<div class="header">
						<img src="/Windchill/extcore/images/header.png">
						결재 라이브러리
					</div>
				</td>
				<td class="right">
					<input type="button" value="라이브러리 추가" title="라이브러리 추가" class="blue" onclick="addRow2();">
					<input type="button" value="라이브러리 삭제" title="라이브러리 삭제" class="red" onclick="deleteRow2();">
				</td>
			</tr>
		</table>
		<div id="grid_wrap2" style="height: 100px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID2;
			const columns2 = [ {
				dataField : "name",
				headerText : "파일명",
				dataType : "string",
				width : 200
			}, {
				dataField : "name_of_parts",
				headerText : "품명",
				dataType : "string",
			}, {
				dataField : "dwg_no",
				headerText : "규격",
				dataType : "string",
				width : 200
			}, {
				dataField : "version",
				headerText : "버전",
				dataType : "string",
				width : 100
			}, {
				dataField : "state",
				headerText : "상태",
				dataType : "string",
				width : 120
			}, {
				dataField : "creator",
				headerText : "작성자",
				dataType : "string",
				width : 120
			}, {
				dataField : "modifier",
				headerText : "수정자",
				dataType : "string",
				width : 120
			}, {
				dataField : "oid",
				headerText : "",
				dataType : "string",
				visible : false
			} ];

			function createAUIGrid2(columnLayout) {
				const props = {
					showRowCheckColumn : true,
					autoGridHeight : true,
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					selectionMode : "multipleCells",
				};
				myGridID2 = AUIGrid.create("#grid_wrap2", columnLayout, props);
			}
		</script>

		<br>
		<table class="button-table">
			<tr>
				<td class="left">
					<div class="header">
						<img src="/Windchill/extcore/images/header.png">
						결재 UNIT BOM
					</div>
				</td>
				<td class="right">
					<input type="button" value="UNIT BOM 추가" title="UNIT BOM 추가" class="blue" onclick="addRow3();">
					<input type="button" value="UNIT BOM 삭제" title="UNIT BOM 삭제" class="red" onclick="deleteRow3();">
				</td>
			</tr>
		</table>
		<div id="grid_wrap3" style="height: 100px; border-top: 1px solid #3180c3;"></div>
		<script type="text/javascript">
			let myGridID3;
			const columns3 = [ {
				dataField : "",
				headerText : "품번",
				dataType : "string"
			}, {
				dataField : "",
				headerText : "품명",
				dataType : "string"
			}, {
				dataField : "",
				headerText : "규격",
				dataType : "string"
			}, {
				dataField : "",
				headerText : "기준단위",
				dataType : "string"
			}, {
				dataField : "",
				headerText : "메이커",
				dataType : "string"
			}, {
				dataField : "",
				headerText : "기본구매처",
				dataType : "string"
			}, {
				dataField : "",
				headerText : "통화",
				dataType : "string"
			}, {
				dataField : "",
				headerText : "단가",
				dataType : "string"
			} ];

			function createAUIGrid3(columnLayout) {
				const props = {
					showRowCheckColumn : true,
					autoGridHeight : true,
					headerHeight : 30,
					showRowNumColumn : true,
					rowNumHeaderText : "번호",
					showAutoNoDataMessage : false,
					enableFilter : true,
					selectionMode : "multipleCells",
				};
				myGridID3 = AUIGrid.create("#grid_wrap3", columnLayout, props);
			}
		</script>

		<script type="text/javascript">
			function addRow1() {
				const url = getCallUrl("/part/popup?method=insert1&multi=true");
				popup(url, 1600, 700);
			}

			function addRow2() {
				const url = getCallUrl("/part/popup-library?method=insert2&multi=true");
				popup(url, 1600, 700);
			}

			function addRow3() {
				const url = getCallUrl("/unit/popup?method=insert3&multi=true");
				popup(url, 1600, 700);
			}

			function insert1(data, callBack) {
				for (let i = 0; i < data.length; i++) {
					const item = data[i].item;
					const isUnique = AUIGrid.isUniqueValue(myGridID1, "oid", item.oid);
					if (isUnique) {
						AUIGrid.addRow(myGridID1, item, "first");
					}
				}
				callBack(true);
			}

			function insert2(data, callBack) {
				for (let i = 0; i < data.length; i++) {
					const item = data[i].item;
					const isUnique = AUIGrid.isUniqueValue(myGridID2, "oid", item.oid);
					if (isUnique) {
						AUIGrid.addRow(myGridID2, item, "first");
					}
				}
				callBack(true);
			}

			function insert3(arr) {
				AUIGrid.setGridData(myGridID3, arr);
			}

			function deleteRow1() {
				const arr = AUIGrid.getCheckedRowItems(myGridID1);
				if (arr.length == 0) {
					alert("삭제할 도면을 선택하세요.");
					return false;
				}
				AUIGrid.removeCheckedRows(myGridID1);
			}

			function deleteRow2() {
				const arr = AUIGrid.getCheckedRowItems(myGridID2);
				if (arr.length == 0) {
					alert("삭제할 라이브러리 선택하세요.");
					return false;
				}
				AUIGrid.removeCheckedRows(myGridID2);
			}

			function deleteRow3() {
				const arr = AUIGrid.getCheckedRowItems(myGridID3);
				if (arr.length == 0) {
					alert("삭제할 UNIT BOM 선택하세요.");
					return false;
				}
				AUIGrid.removeCheckedRows(myGridID3);
			}

			function create() {
				const url = getCallUrl("/part/create");
				const params = new Object();
				const name = document.getElementById("name");
				if (name.value === "") {
					alert("결재 제목을 입력하세요.");
					name.focus();
					return false;
				}
				const arr1 = AUIGrid.getGridData(myGridID1);
				const arr2 = AUIGrid.getGridData(myGridID2);

				params.name = name.value;
				params.arr1 = arr1;
				params.arr2 = arr2;
				parent.openLayer();

				if (!confirm("저장 하시겠습니까?")) {
					return false;
				}

				call(url, params, function(data) {
					alert(data.msg);
					if (data.result) {
						document.location.reload();
					}
					parent.closeLayer();
				})
			}

			document.addEventListener("DOMContentLoaded", function() {
				createAUIGrid1(columns1);
				createAUIGrid2(columns2);
				createAUIGrid3(columns3);
				AUIGrid.resize(columns1);
				AUIGrid.resize(columns2);
				AUIGrid.resize(columns3);
			})
		</script>
	</form>
</body>
</html>