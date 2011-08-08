Element.implement({
 
	highlight: function(search, insensitive, klass){
		var regex = new RegExp('(<[^>]*>)|('+ search.escapeRegExp() +')', insensitive ? 'ig' : 'g');
		return this.set('html', this.get('html').replace(regex, function(a, b, c){
			return (a.charAt(0) == '<') ? a : '<strong class="'+ klass +'">' + c + '</strong>'; 
		}));
		
		//var scroller = new Fx.Scroll(window).toElement('.'+klass);

	}
 
})