// JavaScript Document

function submitNote(){
	 if(document.getElementById('name').value=="" | document.getElementById('name').value==" Your name")
	 {
		 alert('Please enter at least your name');
		 document.getElementById('name').focus();
		 return false;
	 }
	 if(document.getElementById('note').value=="" | document.getElementById('note').value==" Your notes")
	 {
		 alert('Well, do you really need to send us an empty note ?');
		 document.getElementById('note').focus();
		 return false;
	 }
	
	 if(document.getElementById('antibot').value==' Anti-Bot : clear this field')
	 {
		 alert('Please clear the field to submit your note, this is just a simple anti bot feature');
		 document.getElementById('antibot').focus();
		 return false;
	 }
	createCookie('note',document.getElementById('note').value,3600);
	createCookie('name',document.getElementById('name').value,3600000);
	createCookie('email',document.getElementById('email').value,3600000);
	return true;
}

function checkbugform(){
	 if(document.getElementById('email').value=="")
	 {
		 alert('Please enter at least your email');
		 document.getElementById('email').focus();
		 return false;
	 }
	 if(document.getElementById('description').value=="")
	 {
		 alert('Well, do you really need to send us an empty bug report ?');
		 document.getElementById('note').focus();
		 return false;
	 }
	
	 if(document.getElementById('antibot').value=='clear this field')
	 {
		 alert('Please clear the field to submit your report, this is just a simple anti bot feature');
		 document.getElementById('antibot').focus();
		 return false;
	 }
	createCookie('email',document.getElementById('email').value,3600000);
	return true;
}


function expandTree(id,idselect){
	tree.root.recursive(function(){this.toggle(false);});
	node= Mif.id(id);
	tree.expandTo(node);
	node= Mif.id(idselect);
	tree.select(node);
}

function createCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function eraseCookie(name) {
	createCookie(name,"",-1);
}

function redirect(){
	document.location.href='index.php';
}
