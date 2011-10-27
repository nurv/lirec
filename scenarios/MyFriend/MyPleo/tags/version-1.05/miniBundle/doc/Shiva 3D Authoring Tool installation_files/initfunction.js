function createFx(name,openState)
{
	if($(name+'Slide'))
	{
		var Slide = new Fx.Slide(name+'Slide',{ duration: 200});
		$(name).addEvent('click', function(e){
			e.stop();
			Slide.toggle();
			if(Slide.open) $(name).set('class', 'collapse');
			else $(name).set('class', 'toogle');
		});
		if(!openState){
			Slide.toggle();
			$(name+'Slide').setStyle('display', "block");
			$(name).set('class', 'collapse');
		}
	}
}

window.addEvent('domready', function() {
									 
	createFx('description',true);
	createFx('parameters',true);
	createFx('return',true);
	createFx('sample',true);
	createFx('userNotes',true);
	createFx('addNote',false);
	createFx('see_also',true);
	createFx('concepts',false);
	createFx('prototype',true);
	
	if(document.getElementById('antibot'))document.getElementById('antibot').value=' Anti-Bot : clear this field';
	
});