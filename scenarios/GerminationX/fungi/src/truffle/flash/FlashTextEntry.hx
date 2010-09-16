// t r u f f l e Copyright (C) 2010 FoAM vzw   \_\ __     /\
//                                          /\    /_/    / /  
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package truffle.flash;

import flash.display.MovieClip;
import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;
import flash.text.TextFieldType;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;

import truffle.interfaces.TextEntry;

class FlashTextEntry implements TextEntry, extends MovieClip
{
	public var TextField:TextField;
	public var Callback:Dynamic -> Void;
	
	public function new(x,y,w,h,f:Dynamic -> Void)
	{
		super();
		Callback = f;
		TextField = new TextField();
        TextField.background = true;
        TextField.border = true;
		TextField.text = "Enter a name before planting.";
		TextField.type = TextFieldType.INPUT;
		TextField.x = x;
		TextField.y = y;		
		TextField.width = w;
		TextField.height = h;		
		var tf = new flash.text.TextFormat();
        tf.font = "Verdana"; 
        tf.size = 20;                
        tf.color= 0x000000;           
        TextField.setTextFormat(tf);
		addChild(TextField);	           
		
		addEventListener(MouseEvent.MOUSE_DOWN, OnMouseDown);
		addEventListener(flash.events.KeyboardEvent.KEY_DOWN,OnKeyDown,false); 
	}
	
	function OnKeyDown(e:KeyboardEvent) 
	{
		// ENTER pressed ?
		if( e.keyCode == 13 ) 
		{
			var text = TextField.text;
			TextField.text = "";
			Callback(text);
		}
	}
	
	function OnMouseDown(_) 
	{
		if (TextField.text=="Enter a name before planting.")
		{
			TextField.text = "";
		}
	}
	
}
