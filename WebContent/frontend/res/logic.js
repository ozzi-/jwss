'use strict';

function pageLogic (response){
	document.getElementById("pageContainer").innerHTML = response;
	
	if(page=="main"){
		doRequest("GET", "../todo", listTodos);
	}else if(page=="todo"){
		var id = getQueryParams(document.location.search).id;
		doRequest("GET", "../todo/"+id, showTodo);
	}
}

function listTodos(todos){
	var table = new Tabulator("#todoTable", {
	    layout:"fitDataFill",
	    layoutColumnsOnNewData:true,
	    pagination:"local",
	    paginationSize: 10, 
	    columns:[
	    {title:"ID", field:"id"},
	    {title:"Content", field:"content"}
	    ],
	    rowClick:function(e, id, data, row){
	        var id = id._row.data.id;
			window.location.href='index.html?page=todo&id='+id;
	    },
	});

	table.setData(todos);
}

function showTodo(todo){
	todoArea.value=todo.content;
}

function editTodo(){
	var id = getQueryParams(document.location.search).id;
	var value = document.getElementById("todoArea").value;
	var todo = {
		"content": value,
	}
	doRequestBody("PUT", JSON.stringify(todo),"application/json",  "../todo/"+id, handleAPIResponse , ["Todo Saved"]);
}

function deleteTodo(){
	var id = getQueryParams(document.location.search).id;
	doRequestBody("DELETE", JSON.stringify({}),"application/json",  "../todo/"+id, handleAPIResponse , ["Todo Deleted"]);
}


function createTodo(){
	var value = document.getElementById("todoArea").value;
	var todo = {
		"content": value,
	}
	doRequestBody("POST", JSON.stringify(todo),"application/json",  "../todo/", handleAPIResponse , ["Todo Created"]);
}

function handleAPIResponse(response,message,code){
	if(code!=200){
		showAlert("Oops",response.error,"error");
	}else{
		Swal.fire({
			title: message,
			text: "",
			icon: "success",
			confirmButtonText: 'OK'
		}).then(function(isConfirm) {
			window.location.replace("index.html");
	    })
	}
}
