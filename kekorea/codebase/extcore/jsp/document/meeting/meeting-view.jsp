<%@page import="e3ps.common.util.CommonUtils"%>
<%@page import="e3ps.doc.meeting.Meeting"%>
<%@page import="e3ps.doc.meeting.dto.MeetingDTO"%>
<%@page import="com.lowagie.text.Meta"%>
<%@page import="e3ps.doc.meeting.dto.MeetingTemplateDTO"%>
<%@page import="e3ps.admin.commonCode.CommonCode"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
boolean isAdmin = (boolean) request.getAttribute("isAdmin");
//boolean isAdmin = Boolean.parseBoolean(request.getParameter("isAdmin"));
MeetingDTO dto = (MeetingDTO) request.getAttribute("dto");
Meeting mm = (Meeting)CommonUtils.getObject(dto.getOid());
%>
<%@include file="/extcore/jsp/common/tinymce.jsp"%>
<%@include file="/extcore/jsp/common/aui/auigrid.jsp"%>
<input type="hidden" name="isAdmin" id="isAdmin" value="<%=isAdmin%>">
<input type="hidden" name="oid" id="oid" value="<%=dto.getOid()%>">
<input type="hidden" name="poid" id="poid" value="<%=dto.getPoid()%>">
<input type="hidden" name="loid" id="loid" value="<%=dto.getLoid()%>">

<table class="button-table">
	<tr>
		<td class="left">
			<div class="header">
				<img src="/Windchill/extcore/images/header.png">
				회의록 정보
			</div>
		</td>
		<td class="right">
			<input type="button" value="수정" title="수정" class="blue" onclick="modify();">
			<%
			if (isAdmin) {
			%>
			<input type="button" value="삭제" title="삭제" class="red" onclick="_delete();">
			<%
			}
			%>
			<input type="button" value="닫기" title="닫기" onclick="self.close();" style="background-color: navy;">
		</td>
	</tr>
</table>
<div id="tabs">
	<ul>
		<li>
			<a href="#tabs-1">기본정보</a>
		</li>
		<li>
			<a href="#tabs-2">결재이력</a>
		</li>
	</ul>
	<div id="tabs-1">
		<table class="view-table">
			<colgroup>
				<col style="width: 10%;">
				<col style="width: 40%;">
				<col style="width: 10%;">
				<col style="width: 40%;">
			</colgroup>
			<tr>
				<th class="lb">회의록 제목</th>
				<td class="indent5"><%=dto.getName()%></td>
				<th>회의록 템플릿</th>
				<td class="indent5"><%=dto.getTname() != null ? dto.getTname() : ""%></td>
			</tr>
			<tr>
				<th class="lb">KEK 작번</th>
				<td class="indent5" colspan="3">
					<jsp:include page="/extcore/jsp/common/project-include.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
						<jsp:param value="view" name="mode" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<th class="lb">내용</th>
				<td class="indent5" colspan="3">
					<textarea rows="5" readonly="readonly"><%=dto.getContent()%></textarea>
				</td>
			</tr>
			<tr>
				<th class="lb">첨부파일</th>
				<td class="indent5" colspan="3">
					<jsp:include page="/extcore/jsp/common/secondary-view.jsp">
						<jsp:param value="<%=dto.getOid()%>" name="oid" />
					</jsp:include>
				</td>
			</tr>
		</table>
	</div>
	<div id="tabs-2">
		<!-- 결재이력 -->
		<jsp:include page="/extcore/jsp/common/approval-history.jsp">
			<jsp:param value="<%=dto.getOid()%>" name="oid" />
		</jsp:include>
	</div>
</div>
<script type="text/javascript">

function loadTinymce() {
	tinymce.init({
		selector : 'textarea',
		height : 400,
		menubar : false,
		statusbar : false,
		language : 'ko_KR',
		toolbar : false,
		readonly : true,
		setup : function(editor) {
			editor.on('init', function() {
				const content = editor.getContent();
				editor.setContent(content);
			});
		}
	});
}


document.addEventListener("DOMContentLoaded", function() {
	$("#tabs").tabs({
		active : 0,
		activate : function(event, ui) {
			var tabId = ui.newPanel.prop("id");
			switch (tabId) {
			case "tabs-1":
				const isCreated9 = AUIGrid.isCreated(myGridID9);
				if (isCreated9) {
					AUIGrid.resize(myGridID9);
				} else {
					createAUIGrid9(columns9);
				}
				break;
			case "tabs-2":
				const isCreated100 = AUIGrid.isCreated(myGridID100);
				if (isCreated100) {
					AUIGrid.resize(myGridID100);
				} else {
					createAUIGrid100(columns100);
				}
				break;
			}
		},
	});
	loadTinymce();
	createAUIGrid9(columns9);
	createAUIGrid100(columns100);
	AUIGrid.resize(myGridID9);
	AUIGrid.resize(myGridID100);
});


window.addEventListener("resize", function() {
	AUIGrid.resize(myGridID9);
});

function modify() {
	const oid = document.getElementById("oid").value;
	const url = getCallUrl("/meeting/update?oid=" + oid);
	openLayer();
	document.location.href = url;
}

function _delete() {
	if (!confirm("삭제 하시겠습니까?")) {
		return false;
	}

	const oid = document.getElementById("oid").value;
	const url = getCallUrl("/meeting/delete?oid=" + oid);
	openLayer();
	console.log(oid);
	call(url, null, function(data) {
		alert(data.msg);
		if (data.result) {
//				opener.loadGridData();
			self.close();
		} else {
			closeLayer();
		}
	}, "GET");
}
</script>