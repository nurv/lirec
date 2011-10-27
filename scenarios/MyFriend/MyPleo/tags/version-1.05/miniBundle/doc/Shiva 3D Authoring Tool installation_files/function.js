// JavaScript Document<script type="text/javascript">
function translate() {
	//parent.document.getElementById('centerdiv').style.height = document.body.scrollHeight+20+ "px";
	var language = window.parent.readCookie("language");
	if (language != null)
	{
		location.href='http://66.102.9.104/translate_c?hl='+language+'&sl=en&tl='+language+'&u='+location.href;
	}
}

function fullscreen(){
	 	
	// Again we are able to create a morph instance
	var anotherEl = $('centerdiv');

	//var morph = new Fx.Morph('centerdiv');
	
	$('fullscreen').addEvent('click', function(e) {
		e.stop();
		anotherEl.morph('.centerdivFullscreen');
	});
}

function original()
{
	eraseCookie("language");
	eraseCookie("istranslated");
	window.location.reload();
}

function checklanguage()
{
		var language = window.parent.readCookie("istranslated");
		if (language != null)
		{
			var lang = window.parent.readCookie("language");
			translateIndex(lang);
			document.getElementById('backto').style.display = 'block';			
		}
}

function changelanguage(lang)
{
	if(lang != "select")
	{
		var language = window.parent.readCookie("istranslated");
		if (language != null)
		{
			alert("Please go back to English first, then choose the language");			
		}
		else
		{
			createCookie("language",lang,1);
			createCookie("istranslated","true",1);
			translateIndex(lang);
			window.frames['basefrm'].location.reload();
			document.getElementById('backto').style.display = 'block';	
		}
	}
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

var TRange=null
function disableEnterKey(e)
{
	var key;     
     if(window.event)
          key = window.event.keyCode; //IE
     else
          key = e.which; //firefox     

     if (e.keyCode==13)
	 {
	 	SearchSubmit();
	 }
	 return (key != 13);
}

function SearchSubmit(value){
	//document.getElementById('search_suggest').innerHTML = '';
	//document.getElementById('search_suggest').style.visibility = 'hidden';
	if (value) document.getElementById('keyword').value = value;
	/*if (document.getElementById('InFile').checked)
	{
		if(document.getElementById('keyword').value!=null && document.getElementById('keyword').value!='')
		{
			findString(document.getElementById('keyword').value);
		}
		return false;
	}
	else
	{*/
		//document.getElementById('go').focus();
		document.location.href='http://www.stonetrip.com/developer/doc/search&highlight=true&keyword='+value;
	//}

}

function findString (str) {
 if (parseInt(navigator.appVersion)<4) return;
 var strFound;
 if (navigator.appName=="Netscape") {

  // NAVIGATOR-SPECIFIC CODE

  strFound=basefrm.find(str);
  if (!strFound) {
   strFound=basefrm.find(str,0,1)
   while (basefrm.find(str,0,1)) continue
  }
 }
 if (navigator.appName.indexOf("Microsoft")!=-1) {

  // EXPLORER-SPECIFIC CODE

  if (TRange!=null) {
   TRange.collapse(false)
   strFound=TRange.findText(str)
   if (strFound) TRange.select()
  }
  if (TRange==null || strFound==0) {
   TRange=basefrm.document.body.createTextRange()
   strFound=TRange.findText(str)
   if (strFound) TRange.select()
  }
 }
} 
	function translateIndex(lang){
		$$('[rel="translate"]').each(
			function(e) {
				google.language.translate(e.innerHTML, 'en', lang,
					function(result) {
						if (result.translation) {
							e.innerHTML = result.translation;
						}
					}
				);
			}
		);
	}