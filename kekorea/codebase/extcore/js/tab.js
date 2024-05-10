/**
 * 메인 화면탭 관련 스크립트
 */
function openTab(tabId) {
	const tabContents = document.querySelectorAll(".tabcontent");
	tabContents.forEach(tabContent => {
		tabContent.style.display = "none";
	});

	const tabLinks = document.querySelectorAll(".tablink");
	tabLinks.forEach(tabLink => {
		tabLink.classList.remove("active");
	});

	const selectedTabContent = document.getElementById(tabId);
	if (selectedTabContent) {
		selectedTabContent.style.display = "block";
	}

	const activeTabButton = document.querySelector('.tablink[data-tab="' + tabId + '"]');
	if (activeTabButton) {
		activeTabButton.classList.add("active");
	}
}

function closeTab(tabId) {
	const tabContent = document.getElementById(tabId);
	const tabButton = document.querySelector('.tablink[data-tab="' + tabId + '"]');

	if (tabContent && tabButton) {
		tabContent.parentNode.removeChild(tabContent);
		tabButton.parentNode.removeChild(tabButton);
	}
}

function createNewTab(text, url, tabId) {
	const tab = document.getElementById(tabId);
	if (tab != null) {
		openTab(tabId);
		return;
	}

	const newTabText = document.createTextNode(text);
	const newTabButton = document.createElement("button");
	newTabButton.classList.add("tablink");
	newTabButton.setAttribute("data-tab", tabId);
	newTabButton.appendChild(newTabText);
	newTabButton.innerHTML += '<i class="fas fa-times close-icon" onclick="closeTab(\'' + tabId + '\')"></i>';
	newTabButton.addEventListener("click", function() {
		openTab(tabId);
	});

	document.querySelector(".tabs").appendChild(newTabButton);

	const mainElement = document.createElement("main");
	mainElement.classList.add("tabcontent");
	mainElement.setAttribute("id", tabId);

	const newTabFrame = document.createElement('iframe');
	newTabFrame.setAttribute("src", url);
	mainElement.appendChild(newTabFrame);
	document.body.appendChild(mainElement);
	openTab(tabId);
}