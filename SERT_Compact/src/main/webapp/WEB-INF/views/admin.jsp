<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<script src="resources/jquery-1.11.0.min.js" type="text/javascript"></script>  
<script>
	window.onload = function(){
		var numHostNames = parseInt("${numHostNames}");
		var hn = document.getElementById("hostNames_field").innerHTML.split("!!!");
		var hi = document.getElementById("hostIPs_field").innerHTML.split("!!!");
		var data = "<div class=\"padded\">";
		data += "<img id=\"add_button\" src=\"resources/Button_Add.png\" onclick=\"addHostName()\">";
		for(var i=0;i<numHostNames;i++){
			data += "<p>";
			data += "<img id=\""+i+"\" src=\"resources/Button_Delete.png\" onclick=\"deleteHostName(this)\">";
			data += "  - "+hn[i]+" ("+hi[i]+")</p>";
		}
		data += "</div>"
		document.getElementById("data_display").innerHTML = data;
	};
	
	function addHostName(){
		var hostName = window.prompt("Enter a new machine name:","");
		if(hostName!=null){
			var hostIP = window.prompt("Enter a host name or IP address:","");
			if(hostIP!=null){
			    $.ajax({
					type : "Get",
					url : "addHostName",
					data : "hostName="+hostName+"&hostIP="+hostIP,
			        complete: function(){
			        	location.reload();
			        }
				});
			}
		}
	}
	
	function deleteHostName(element){
	    $.ajax({
			type : "Get",
			url : "deleteHostName",
			data : "hostNum="+element.id,
	        complete: function(){
	        	location.reload();
	        }
		});
	}
</script>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="resources/AllStyles.css" />
		<title>Display</title>
	</head>
	<body>
		<div id="numHostNames_field" class="hidden">${numHostNames}</div>
		<div id="hostNames_field" class="hidden">${host_names}</div>
		<div id="hostIPs_field" class="hidden">${host_ips}</div>
		<div id="title_bar">
			<img id="titlebar_leftcap" src="resources/TitleBar_LeftCap.png" alt="Shelter Insurance">
			<img id="titlebar_title" src="resources/TitleBar_Title.png" alt="System Environment Reporting Tool (SERT)">
			<img id="titlebar_rightcap" src="resources/TitleBar_RightCap.png" alt="">
			<a id="titlebar_adminlink" href="display"><img src="resources/TitleBar_HomeLink.png" alt=""></a>
		</div>
		<div id="data_display">
			Loading Data...
		</div>
	</body>
</html>
