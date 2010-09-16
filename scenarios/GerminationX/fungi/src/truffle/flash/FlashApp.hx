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

import flash.events.MouseEvent;
import flash.events.KeyboardEvent;
import flash.events.Event;
import flash.display.Stage;
import flash.display.MovieClip;

import truffle.interfaces.App;
import truffle.interfaces.World;

class FlashApp extends App
{
    var InnerOnUpdate:Void->Void;
    var InnerOnKeyDown:Int->Void;

    public function new(w:World)
    {
        super(w);
        flash.Lib.current.addChild(cast(w,truffle.flash.FlashWorld));		   
        flash.Lib.current.stage.addEventListener("enterFrame",UpdateCallback);
		flash.Lib.current.stage.addEventListener(flash.events.KeyboardEvent.KEY_DOWN,OnKeyDownCallback,false);	
    }

    private function UpdateCallback(e:Event)
    {
        Update();
    }
    
    private function OnKeyDownCallback(e:KeyboardEvent)
    {       
        Handle(e.keyCode);
    }
}
