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
import flash.display.DisplayObject;
import flash.events.MouseEvent;
import truffle.interfaces.World;
import truffle.interfaces.Sprite;
import truffle.Entity;
import truffle.Truffle;

class FlashWorld implements World, extends MovieClip 
{
    var Scene:Array<Entity>;
    var MouseDownFunc:Dynamic -> Void;
    var MouseDownContext:Dynamic;

    function new()
    {
        super();
        Scene = [];
    }

    public function Add(e:Entity)
    {
        Scene.push(e);
    }

    public function Remove(e:Entity)
    {
        e.Destroy(cast(this,truffle.World));
        Scene.remove(e);
    }

    public function AddSprite(s:Sprite)
    {
        addChild(cast(s,FlashSprite));
    }

    public function RemoveSprite(s:Sprite)
    {
        removeChild(cast(s,FlashSprite));
    }

	public function MouseDown(c:Dynamic, f:Dynamic -> Void=null)
	{
        MouseDownFunc=f;
        MouseDownContext=c;
		addEventListener(MouseEvent.MOUSE_DOWN, MouseDownCB);
	}

    public function MouseDownCB(e)
    {
        MouseDownFunc(MouseDownContext);
    }

    public function SortScene()
    {        
        Scene.sort(function(a:Entity, b:Entity)
                   {                       
                       if (a.Depth<b.Depth) return -1;
                       else return 1;
                   });

        var i=0;
        for (e in Scene)
        {
            setChildIndex(e.GetRoot(),i);
            e.OnSortScene(i);
            i++;
        }
    }

    public function Update(time)
    {
        for (e in Scene)
        {
            if (e.NeedsUpdate)
            {
                e.Update(time,cast(this,truffle.World));
            }
        }
    }

    public function Handle(event)
    {        
    }
    
}
