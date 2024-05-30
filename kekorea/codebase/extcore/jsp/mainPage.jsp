<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title></title>
<link rel="stylesheet" type="text/css" href="/Windchill/extcore/css/tab.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
<script type="text/javascript" src="/Windchill/extcore/js/tab.js?v=11"></script>
<script type="text/javascript">


	function updateHeader() {
		parent.updateWorkData();
		parent.updateWorkspace();
		parent.updateActivity();
	}

	function updateWorkData() {
		parent.updateWorkData();
	}

	function updateWorkspace() {
		parent.updateWorkspace();
	}

	function updateActivity() {
		parent.updateActivity();
	}

	function openLayer() {
		parent.openLayer();
	}

	function closeLayer() {
		parent.closeLayer();
	}
</script>
</head>
<body>
	<div class="tabs">
		<button class="tablink active" data-tab="tab0" onclick="openTab('tab0')">
			메인 페이지
			<i class="fas fa-times close-icon" onclick="closeTab('tab0')"></i>
		</button>
	</div>
	<main class="tabcontent" id="tab0" style="display: block;">
		<iframe src="/Windchill/plm/firstPage"></iframe>
	</main>
</body>
</html>
