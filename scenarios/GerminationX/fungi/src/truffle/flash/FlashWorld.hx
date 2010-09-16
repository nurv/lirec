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

import truffle.interfaces.World;
import truffle.Entity;

class FlashWorld implements World, extends MovieClip 
{
    var Scene:Array<Entity>;

    function new()
    {
        super();
        Scene = [];
    }

    public function Add(e:Entity)
    {
        Scene.push(e);
        addChild(e);
    }

    public function Remove(e:Entity)
    {
        Scene.remove(e);
        removeChild(e);
    }
    

    public function SortScene()
    {        
        Scene.sort(function(a:Entity, b:Entity)
                   {        
                       if (a.ScreenPos.z<b.ScreenPos.z) return -1;
                       else return 1;
                   });

        var i=0;
        for (e in Scene)
        {
            setChildIndex(e,i);
            i++;
        }
    }

    public function Update(time)
    {
        for (e in Scene)
        {
            e.Update(time,cast(this,truffle.interfaces.World));
        }
    }

    public function Handle(event)
    {        
    }
    
}
