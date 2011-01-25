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
    public var LogicalPos:Vec3;
    public var Depth:Float;
    public var Pos:Vec3;
    public var NeedsUpdate:Bool;
		
	public function new(w:World,pos:Vec3) 
	{
        LogicalPos=pos;
        Pos = Pos2PixelPos(LogicalPos);
        Depth = Pos.z;
        NeedsUpdate=false;
        w.Add(this);
	}

    // called by world before destruction (cnance to remove sprites)
    public function Destroy(world:World)
    {
    }

	public function Pos2PixelPos(pos:Vec3) : Vec3
	{
		// do the nasty iso conversion
		// this is actually an orthogonal projection matrix! (I think)
		return new Vec3(400+(pos.x*36-pos.y*26),
                        120+(pos.y*18+pos.x*9)-(pos.z*37),
                        pos.x*0.51 + pos.y*0.71 + pos.z*0.47);             
	}
		
	public function Update(frame:Int, world:World)
	{
        Pos = Pos2PixelPos(LogicalPos);
        Depth = Pos.z;
	}

    public function GetRoot() : Dynamic
    {
        return null;
    }

    public function OnSortScene(order:Int) : Void
    {
    }
}
