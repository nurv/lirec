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

package truffle;
import truffle.Truffle;

class Entity
{
    public var TilePos:Vec2;     // position in the world in tiles 
    public var LogicalPos:Vec3;  // position in it's tile
    public var Pos:Vec3;         // screen position
    public var Depth:Float;      // depth from camera
    public var NeedsUpdate:Bool; 
    public var Hidden:Bool;
    public var Speed:Float;
    public var UpdateFreq:Int;
	
	public function new(w:World,pos:Vec3) 
	{
        LogicalPos=pos;
        Pos = w.ScreenTransform(LogicalPos);
        TilePos = null;
        Depth = Pos.z;
        Speed = 0;
        UpdateFreq=0;
        NeedsUpdate=false;
        w.Add(this);
	}

    // called by world before destruction (cnance to remove sprites)
    public function Destroy(world:World)
    {
    }

    public function SetTilePos(s:Vec2) : Void
    {
        TilePos=s;
    }

	public function Update(frame:Int, world:World)
	{
        if (Speed==0)
        {
            Pos = world.ScreenTransform(LogicalPos);
        }
        else
        {
            var Dst = world.ScreenTransform(LogicalPos);
            if (!Dst.Eq(Pos))
            {
                Pos = Pos.Lerp(Dst,Speed);
            }
        }

        Depth = Pos.z;
	}

    public function GetRoot() : Dynamic
    {
        return null;
    }

    public function OnSortScene(order:Int) : Void
    {
    }

    public function Hide(s:Bool) : Void
    {
        Hidden=s;
        if (GetRoot()) GetRoot().Hide(s);
    }
}
