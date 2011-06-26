// GerminationX Copyright (C) 2010 FoAM vzw    \_\ __     /\
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

package fungi;

import truffle.Truffle;
import truffle.Vec2;

class SeedStore
{
    var Seeds:Array<Seed>;
    var Size:Int;

    public function new(size:Int)
    {
        Seeds = [];
        Size = size;
    }

    public function Add(world:World,s:Seed) : Bool
    {
        if (Seeds.length<Size)
        {
            Seeds.push(s);
            s.Spr.SetPos(new Vec2(20+Seeds.length*10,30));
            //cast(world,truffle.flash.FlashWorld).addSprite(s.Spr);
            //trace("did it...");
            s.Spr.Update(0,null);
            return true;
        }
        return false;
    }

    public function Remove(world:World) : String
    {
        if (Seeds.length>0)
        {
            var s = Seeds.pop();
//            world.RemoveSprite(s.Spr);
            return s.Type;
        }
        else
        {
            return "";
        }
    }
}
