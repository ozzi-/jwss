'use strict';

var authHeaderName = "X-LOGIN-NAME";

// prevent bug of JS not running on page back, thus no XHR triggered and "stale" data shown
// https://stackoverflow.com/questions/2638292/after-travelling-back-in-firefox-history-javascript-wont-run
window.onunload = function(){}; 

window.onerror = function(msg, url, line, col, error) {
	showAlert("Something went wrong","\""+error+"\"","error");
};

var fullURL = window.location.href;
if(fullURL.indexOf('frontend//')!==-1){
	window.location.replace(fullURL.replace("frontend//","frontend/"));
}

var page = getQueryParams(document.location.search).page;
if(page === undefined || page == "undefined" || page == "index" ){
	page = "main";
}
page = page.replace(/[^A-Za-z0-9]/g,'');

document.addEventListener("DOMContentLoaded", function(event) { 
	if(sessionStorage.getItem('name')==null){
		doRequest("GET", "../whoami", whoami);
	}else{
		doRequest("GET", page+".html", pageLogicXSRF, []);
	}
});


window.addEventListener("pageshow", function ( event ) {
	var historyTraversal = event.persisted || ( typeof window.performance != "undefined" &&  window.performance.navigation.type === 2 );
	if ( historyTraversal ) {
		console.log("Detected history traversal - Reloading to eliminate caching issues");
		window.location.reload();
	}
});

function whoami(myself){
	sessionStorage.setItem('name', myself.name);
	doRequest("GET", page+".html", pageLogicXSRF, []);
}

function showAlert(title,text,type){
	Swal.fire({
		title: title,
		text: text,
		icon: type,
		confirmButtonText: 'OK'
	})	
}

function pageLogicXSRF(response){
	doRequest("GET", "../csrf", updateCSRF,[pageLogic,response]);
}

function updateCSRF(data,callback,responseForCallback){
	sessionStorage.setItem("csrfToken",data.csrfToken);
	if(callback!==undefined && typeof callback === "function"){
		callback(responseForCallback);
	}
}

// ***********
// * Network *
// ***********
window.errorReported = false;
var openRequests=0;
var timeout = 10000;

setTimeout(killLoader, timeout);

function killLoader(){
	document.getElementById("loading").style.display="none";
}

function killLoaderIfNoRequestsOpen(){
	setTimeout(killLoaderIfNoRequestsOpenInt, 300);
}

function killLoaderIfNoRequestsOpenInt(){
	if(openRequests==0){
		killLoader();
	}
}


function doRequestBody(method, data, type, url, callback, params) {
	doRequestBodyInternal(method,data,type,url,callback,params);
}

function doRequest(method, url, callback, params) {
	doRequestBodyInternal(method,null,null,url,callback,params);
}

function doRequestBodyInternal(method,data,type,url,callback,params){
	openRequests++;
	var request = new XMLHttpRequest();

	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			openRequests--;
			killLoaderIfNoRequestsOpen();
			if (request.status == 200) {
				var response = "";
				if(url.includes(".html")){
					response = request.responseText;
				}else{
					try { 
						var responseJSON = JSON.parse(request.responseText);
						response = responseJSON;
					} catch (e) {
						showAlert("Something went wrong","Error receiving information from backend - "+e.message,"error");
					}
				}
				params = [response].concat(params);
				params.push(request.status); 				
				callback.apply(this,params);
			} else if(request.status == 409){
				response = JSON.parse(request.responseText);
				showAlert("Something went wrong",response.error,"error");
				if(document.getElementById("requestButton")!=null){
					document.getElementById("requestButton").disabled=false;							
				}
			} else if(request.status == 401){
				if(!window.errorReported){
					window.errorReported=true;
					showAlert("Unauthorized","","error");
				}
			} else if(request.status == 400){
				if(!window.errorReported){
					window.errorReported=true;
					showAlert("Something went wrong",request.responseText,"error");
				}
			} else if(request.status == 412){
				if(!window.errorReported){
					window.errorReported=true;
					doRequest("GET", "../csrf", updateCSRF,[]);
					showAlert("XSRF Security Token invalid","Please try again or reload the page","error");
				}
			} else if(request.status == 428){
				if(!window.errorReported){	
					showAlert("Precondition failed",JSON.parse(request.responseText).message,"warning");
					if(document.getElementById("requestButton")!=null){
						document.getElementById("requestButton").disabled=false;
					}
				}
			} else if(request.status==404){
				window.location.replace("index.html");
			} else if(request.status==0){
				openRequests--;
				killLoaderIfNoRequestsOpen();
				// request was interrupted
			}else{
				try { 
					response = JSON.parse(request.responseText);
					if(!window.errorReported){
						window.errorReported=true;
						if(request.status == 410){
							var privID = getQueryParams(document.location.search).id;
							window.location.replace("index.html?page=privilege&id="+privID);
						}else{
							showAlert("Something went wrong",response.error,"error");
						}
					}
				}catch (e){
					if(!window.errorReported){
						window.errorReported=true;
						var body = request.responseText.substring(0,20);
						alert("Unknown error - HTTP Return Code: '"+request.status+"' - Exception Message: '"+e.message+"' - Response Body: "+body);						
					}
				}
			}
		}
	};
	if(method.toUpperCase()==="GET"){
		var cacheBusterStrng = "cacheBuster=";
		if(!url.includes(cacheBusterStrng)){
			var appendChar = url.includes("?") ? "&" : "?"; 
			url = url + appendChar + cacheBusterStrng + (Math.random()*1000000);
		}
	}
	request.open(method, url);
	request.timeout = timeout;
	request.ontimeout = function (e) {
		killLoaderIfNoRequestsOpen();
		if(!window.errorReported){
			window.errorReported=true;
			showAlert("Something went wrong","The request for " + url + " timed out. Please try again.","error");
		}
	};
	
	
	if(type!==null){
		request.setRequestHeader("Content-Type", type);		
	}
	var xsrf = sessionStorage.getItem("csrfToken");
	if(xsrf!=null){
		request.setRequestHeader("X-XSRF", xsrf);		
	}
	request.setRequestHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	if(sessionStorage.getItem('impers')!=null){
		request.setRequestHeader(authHeaderName, sessionStorage.getItem('impers'));		
	}
	if(data!=null){
		request.send(data);
	}else{
		request.send();
	}
}

// ********************
// * Helper Functions *
// ********************

function getQueryParams(qs) {
	qs = qs.split('+').join(' ');
	var params = {}, tokens, re = /[?&]?([^=]+)=([^&]*)/g;
	while (tokens = re.exec(qs)) {
		params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
	}
	return params;
}
