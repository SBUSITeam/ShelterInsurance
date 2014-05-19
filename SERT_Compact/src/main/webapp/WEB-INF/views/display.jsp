<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<script src="resources/jquery-1.11.0.min.js" type="text/javascript"></script>  
<script>
	window.onload = function(){
		var numTrees = parseInt("${numTrees}");
		var width = parseInt(100/(numTrees+1));
		var data = "";
		var si = document.getElementById("data_field").innerHTML.split("!!!");
		var dividerHTML = "<div id=\"divider\"><img id=\"divider_topcap\" src=\"resources/Divider_TopCap.png\"><img id=\"divider_bottomcap\" src=\"resources/Divider_BottomCap.png\"></div>\n";
		
		data += dividerHTML;
		for(var i=0;i<numTrees;i++){
			data += "<div class=\"data_panel\" style=\"width: "+width+"%;\" id=\""+i+"\">";
			data += "<img id=\""+i+"\" src=\"resources/Button_Search.png\" onclick=\"searchTree(this)\">";
			data += "<img id=\""+i+"\" src=\"resources/Button_Refresh.png\" onclick=\"refreshTree(this)\">";
			data += "<img id=\""+i+"\" src=\"resources/Button_Delete.png\" onclick=\"deleteTree(this)\">";
			data += si[i]+"</div>\n";
			data += dividerHTML;
		}
		
		var numHostNames = parseInt("${numHostNames}");
		var hn = document.getElementById("hostNames_field").innerHTML.split("!!!");
		var hi = document.getElementById("hostIPs_field").innerHTML.split("!!!");
		data += "<img id=\"add_button\" src=\"resources/Button_Add.png\" onclick=\"addTree()\">";
		
		data += "<select id=\"hostList\">";
		for(var i=0;i<numHostNames;i++){
			data += "<option>"+hn[i];
			if(hn[i]!="(Custom)"){
				data +=" ("+hi[i]+")";
			}
			data += "</option>";
		}
		data += "</select>";
		
		document.getElementById("data_display").innerHTML = data;
	};
	
	function elementClicked(element){
	    var parent = element.parentNode;
	    $.ajax({
			type : "Get",
			url : "clickElement",
			data : "treeNum="+parent.id+"&elementNum="+element.id,
	        complete: function(){
	        	location.reload();
	        }
		});
	}
	
	function searchTree(element){
		var phrase = window.prompt("Enter a search phrase:","");
		if(phrase!=null){
		    $.ajax({
				type : "Get",
				url : "searchTree",
				data : "phrase="+phrase+"&treeNum="+element.id,
		        complete: function(){
		        	location.reload();
		        }
			});
		}
	}
	
	function refreshTree(element){
	    $.ajax({
			type : "Get",
			url : "refreshTree",
			data : "treeNum="+element.id,
	        complete: function(){
	        	location.reload();
	        }
		});
	}
	
	function deleteTree(element){
	    if(window.confirm("This will remove this set of data.\nAre you sure?")==true){
		    $.ajax({
				type : "Get",
				url : "deleteTree",
				data : "treeNum="+element.id,
		        complete: function(){
		        	location.reload();
		        }
			});
	    }
	}
	
	function addTree(element){
		var e = document.getElementById("hostList");
		var ips = document.getElementById("hostIPs_field").innerHTML.split("!!!");
		var ipaddress = ips[e.selectedIndex];
		if(ipaddress=="(Custom)"){
 			ipaddress = window.prompt("Please enter a host name or IP address:","localhost");
		}
 		if(ipaddress!=null){
		    $.ajax({
				type : "Get",
				url : "addTree",
				data : "ipaddress="+ipaddress,
		        complete: function(){
		        	location.reload();
		        }
			});
		}
	}
</script>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="resources/AllStyles.css" />
		<title>Display</title>
	</head>
	<body>
		<div id="numTrees_field" class="hidden">${numTrees}</div>
		<div id="data_field" class="hidden">${full_data}</div>
		<div id="numHostNames_field" class="hidden">${numHostNames}</div>
		<div id="hostNames_field" class="hidden">${host_names}</div>
		<div id="hostIPs_field" class="hidden">${host_ips}</div>
		<div id="title_bar">
			<img id="titlebar_leftcap" src="resources/TitleBar_LeftCap.png" alt="Shelter Insurance">
			<img id="titlebar_title" src="resources/TitleBar_Title.png" alt="System Environment Reporting Tool (SERT)">
			<img id="titlebar_rightcap" src="resources/TitleBar_RightCap.png" alt="">
			${admin_link}
		</div>
		<div id="data_display">
			Loading Data...
		</div>
	</body>
</html>
