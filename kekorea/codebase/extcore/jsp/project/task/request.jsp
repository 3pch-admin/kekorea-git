<%@page import="e3ps.project.service.ProjectHelper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	boolean isAdmin = Boolean.parseBoolean(request.getParameter("isAdmin"));
	String poid = (String) request.getParameter("poid");
	String toid = (String) request.getParameter("toid");
%>
<div class="info-header">
	<img src="/Windchill/extcore/images/header.png">
	태스크 의뢰서 정보
</div>

<table class="button-table">
	<tr>
		<td class="left">
			<div class="pretty p-switch">
				<input type="checkbox" name="latest" value="true" checked="checked" onchange="reloadOutput();">
				<div class="state p-success">
					<label>
						<b>최신버전</b>
					</label>
				</div>
			</div>
			&nbsp;		
			<input type="button" value="링크 등록" title="링크 등록" class="orange" onclick="connect();">
			<%
				// 			if (isAdmin) {
			%>
			<input type="button" value="삭제" title="삭제" class="red" onclick="_delete();">
			<%
				// 			}
			%>
		</td>
	</tr>
</table>
<div id="grid_wrap" style="height: 450px; border-top: 1px solid #3180c3;"></div>
<script type="text/javascript">
	let myGridID;
	const columns = [ {
		dataField : "name",
		headerText : "의뢰서 제목",
		dataType : "string",
		style : "aui-left",
		renderer : {
			type : "LinkRenderer",
			baseUrl : "javascript",
			jsCallback : function(rowIndex, columnIndex, value, item) {
				const oid = item.oid;
				if(oid.includes('PRJDocument')){
					const url = getCallUrl("/doc/view?oid=" + oid);
					popup(url);
				}else{
					const url = getCallUrl("/requestDocument/view?oid=" + oid);
					popup(url);
				}
			}
		},
	}, {
		dataField : "state",
		headerText : "상태",
		dataType : "string",
		width : 100,
	}, {
		dataField : "creator",
		headerText : "작성자",
		dataType : "string",
		width : 100,
	}, {
		dataField : "createdDate_txt",
		headerText : "작성일",
		dataType : "string",
		width : 100,
	}, {
		dataField : "primary",
		headerText : "주 첨부파일",
		dataType : "string",
		width : 100,
		renderer : {
			type : "TemplateRenderer"
		}
	}, {
		dataField : "secondary",
		headerText : "첨부파일",
		dataType : "string",
		width : 100,
		renderer : {
			type : "TemplateRenderer"
		}
	} ]

	function createAUIGrid(columnLayout) {
		const props = {
			headerHeight : 30,
			showRowCheckColumn : true,
			showRowNumColumn : true,
			rowNumHeaderText : "번호",
			showAutoNoDataMessage : false,
			selectionMode : "multipleCells",
		};
		myGridID = AUIGrid.create("#grid_wrap", columnLayout, props);
		output();
	}
	
	function output() {
		let params = new Object();
		const url = getCallUrl("/project/output");
		const latest = !!document.querySelector("input[name=latest]:checked").value;
		params.poid = "<%=poid%>";
		params.toid = "<%=toid%>";
		params.latest = latest;
		AUIGrid.showAjaxLoader(myGridID);
		parent.parent.openLayer();
		call(url, params, function(data) {
			AUIGrid.removeAjaxLoader(myGridID);
			AUIGrid.setGridData(myGridID, data.list);
			parent.parent.closeLayer();
		});
	}
	
	
	function reloadOutput() {
		let params = new Object();
		const url = getCallUrl("/project/reloadOutput");
		const latest = !!document.querySelector("input[name=latest]:checked");
		params.poid = "<%=poid%>";
		params.toid = "<%=toid%>";
		if(latest == false) {
			params.latest = false;
		} else {
			params.latest = true;	
		}
		
		AUIGrid.showAjaxLoader(myGridID);
		parent.parent.openLayer();
		console.log(params);
		call(url, params, function(data) {
			AUIGrid.removeAjaxLoader(myGridID);
			AUIGrid.clearGridData(myGridID);
			AUIGrid.setGridData(myGridID, data.list);
			parent.parent.closeLayer();
		});
	}

	function connect() {
		const toid = document.getElementById("oid").value;
		const poid = document.getElementById("poid").value;
		const url = getCallUrl("/requestDocument/connect?toid=" + toid + "&poid=" + poid);
		popup(url, 1600, 700);
	}

	function _connect(data, toid, poid, callBack) {
		const arr = new Array();
		for (let i = 0; i < data.length; i++) {
			const item = data[i].item;
			arr.push(item.oid);
		}
		const url = getCallUrl("/requestDocument/connect");
		const params = new Object();
		params.arr = arr;
		params.toid = toid;
		params.poid = poid;
		call(url, params, function(res) {
			callBack(res);
		})
	}

	function _delete() {
		const checkedItems = AUIGrid.getCheckedRowItems(myGridID);
		const arr = new Array();
		if (checkedItems.length === 0) {
			alert("삭제할 의뢰서를 선택하세요.");
			return false;
		}

		for (let i = 0; i < checkedItems.length; i++) {
			const item = checkedItems[i].item;
			const oid = item.oid;
			arr.push(oid);
		}
		const url = getCallUrl("/requestDocument/disconnect");
		const params = new Object();
		const poid = document.getElementById("poid").value;
		params.poid = poid;
		params.arr = arr;
		if (!confirm("삭제 하시겠습니까?\n의뢰서와 태스크의 연결관계만 삭제 되어집니다.")) {
			return false;
		}
		parent.parent.openLayer();
		call(url, params, function(data) {
			alert(data.msg);
			if (data.result) {
				document.location.reload();
			} else {
				parent.parent.closeLayer();
			}
		});
	}
</script>