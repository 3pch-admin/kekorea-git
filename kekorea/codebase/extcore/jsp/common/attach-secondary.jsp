<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String oid = request.getParameter("oid");
String cmd = request.getParameter("cmd");
%>
<div class="AXUpload5" id="secondary_layer"></div>
<div class="AXUpload5QueueBox_list" id="uploadQueueBox" style=""></div>
<script type="text/javascript">
	const secondary = new AXUpload5();
	function load() {
		secondary.setConfig({
			isSingleUpload : false,
			targetID : "secondary_layer",
			uploadFileName : "secondary",
			buttonTxt : "파일 선택",
			uploadMaxFileSize : (1024 * 1024 * 1024),
			uploadUrl : getCallUrl("/content/upload"),
			dropBoxID : "uploadQueueBox",
			queueBoxID : "uploadQueueBox",
			uploadPars : {
				roleType : "secondary"
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
			onUpload : function() {
				const form = 	document.querySelector("form");
				const secondaryTag = document.createElement("input");
				secondaryTag.type = "hidden";
				secondaryTag.name = "secondarys";
				secondaryTag.value = this.cacheId;
				secondaryTag.id = this._id_;
				form.appendChild(secondaryTag);
				closeLayer();	
			},
			onDelete : function() {
				console.log(this);
				const key = this.file._id_;
				const el = document.getElementById(key);
				console.log(el);
				el.parentNode.removeChild(el);
				
				const secondarys = document.getElementsByName("secondarys");
				for (let i = 0; i < secondarys.length; i++) {
					const tag = secondarys[i];
					if(tag.id === key){
						tag.parentNode.removeChild(tag);						
					}
				}
			}
		})
		
		new AXReq("/Windchill/plm/content/list", {
			pars : "oid=<%=oid%>&roleType=secondary",
			onsucc : function(res) {
				if (!res.e) {
					const form = document.querySelector("form");
					const data = res.secondaryFile;
					const len = data.length;
					for (let i = 0; i < len; i++) {
						const secondaryTag = document.createElement("input");
						
						secondaryTag.type = "hidden";
						secondaryTag.name = "secondarys";
						secondaryTag.value = data[i].cacheId;
						secondaryTag.id = data[i].tagId;
						form.appendChild(secondaryTag);
					}
					secondary.setUploadedList(data);
					//console.log(form);
				}
			}
		});
	}
	load();

	function deleteAllFiles() {
		const secondarys = document.getElementsByName("secondarys");
		for (let i = secondarys.length - 1; i >= 0; i--) {
			const tag = secondarys[i];
			tag.parentNode.removeChild(tag);
		}

		var l = $("form:eq(0)").find("div.readyselect");
		$.each(l, function(idx) {
			var fid = l.eq(idx).attr("id");
			secondary.removeUploadedList(fid);
			l.eq(idx).hide();
		})
	}
</script>