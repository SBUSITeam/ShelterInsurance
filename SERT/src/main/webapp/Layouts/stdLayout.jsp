<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="now" class="java.util.Date"  />


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		
		<title><tiles:getAsString name="title" /></title>
			
				
		<!-- Bootstrap import - Check https://ui.oreillyauto.com/ui to make sure you have the latest! -->
		<link rel="stylesheet" type="text/css" href="/SERT/src/main/webapp/resources/css/bootstrap.css"/>
		
		<!-- Project CSS import -->
				
		<!-- JavaScript import -->
		

	</head>

	<body onload=""> <!-- THIS IS WHERE I CAN LIST FUNCTIONS TO HAPPEN ONLOAD -->
		<div id="bodyContentTile" class="container-fluid">
		
			<div class="content">
				<tiles:insertAttribute name="body" />
			</div>
		</div>
		
		<script type="text/javascript" >
			$(document).ready(function() {
				refreshSession();	
				var refreshReady = true;
				$(document).bind("click scroll", function () {
					if(refreshReady)
					{
						refreshSession();
						refreshReady = false;
						setTimeout(function(){refreshReady = true;}, 5000);
					}
				});
			});
			
		</script>
	</body>

</html>
