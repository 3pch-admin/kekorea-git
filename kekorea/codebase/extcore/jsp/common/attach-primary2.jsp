<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String oid = request.getParameter("oid");
String cmd = request.getParameter("cmd");
%>
<div class="AXUpload5" id="primary_layer"></div>
<div class="AXUpload5QueueBox_list" id="uploadQueueBox" style="height: 150px;"></div>
<script type="text/javascript">
	const primary = new AXUpload5();
	function load() {
		primary.setConfig({
			isSingleUpload : false,
			targetID : "primary_layer",
			uploadFileName : "primary",
			buttonTxt : "파일 선택",
			uploadMaxFileSize : (1024 * 1024 * 1024),
			uploadUrl : getCallUrl("/content/upload"),
			dropBoxID : "uploadQueueBox",
			queueBoxID : "uploadQueueBox",
			uploadPars : {
				roleType : "primary"
			},
			uploadMaxFileCount : 100,
			deleteUrl : getCallUrl("/content/delete"),
			fileKeys : {
				name : "name",
				type : "type",
				saveName : "saveName",
				fileSize : "fileSize",
				uploadedPath : "uploadedPath",
				roleType : "roleType",
				cacheId : "cacheId",
			},
			onStart : function() {
				openLayer();
			},
			onComplete : function(res) {
				const form = document.querySelector("form");
				//form.removeChild("input");
				for (let i = 0; i < this.length; i++) {
					
					const befo = document.getElementById(this[i].tagId);
					console.log(befo);
					//befo.parentMode.removeChild(befo);
					//form.appendChild(befo);
					if (isNull(befo)) {
						const primaryTag = document.createElement("input");
						primaryTag.type = "hidden";
						primaryTag.name = "primarys";
						primaryTag.value = this[i].cacheId;
						primaryTag.id = this[i].tagId;
						console.log("this[i].tagId=="+this[i].tagId);
						form.appendChild(primaryTag);
					}
				}
				const data = res.primaryFile;
				primary.setUploadedList(data);
				console.log(form);
				closeLayer();
			},
			onDelete : function() {
				const key = this.file.tagId;
				const el = document.getElementById(key);
				el.parentNode.removeChild(el);
			}
		})

		new AXReq("/Windchill/plm/content/list", {
			pars : "oid=<%=oid%>&roleType=primary",
			onsucc : function(res) {
				if (!res.e) {
					const form = document.querySelector("form");
					const data = res.primaryFile;
					const len = data.length;
					for (let i = 0; i < len; i++) {
						const primaryTag = document.createElement("input");
						primaryTag.type = "hidden";
						primaryTag.name = "primarys";
						primaryTag.value = data[i].cacheId;
						primaryTag.id = data[i].tagId;
						form.appendChild(primaryTag);
					}
					console.log(form);
					primary.setUploadedList(data);
				}
			}
		});
	}
	load();
	
	function deleteAllFiles() {
		const primarys = document.getElementsByName("primarys");
		for (let i = primarys.length - 1; i >= 0; i--) {
			const tag = primarys[i];
			tag.parentNode.removeChild(tag);
		}

		var l = $("form:eq(0)").find("div.readyselect");
		$.each(l, function(idx) {
			var fid = l.eq(idx).attr("id");
			primary.removeUploadedList(fid);
			l.eq(idx).hide();
		})
	}
	
</script>