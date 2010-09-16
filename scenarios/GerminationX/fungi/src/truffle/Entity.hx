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

class Entity extends truffle.Sprite
{
    public var LogicalPos:Vec3;
		
	public function new(pos:Vec3, t:TextureDesc) 
	{
        LogicalPos=pos;        
		super(Pos2PixelPos(LogicalPos),t);
	}

	public function Pos2PixelPos(pos:Vec3) : Vec3
	{
		// do the nasty iso conversion
		// this is actually an orthogonal projection matrix! (I think)
		return new Vec3(250+(pos.x*36-pos.y*26),
                        50+(pos.y*18+pos.x*9)-(pos.z*37),
                        pos.x*0.51 + pos.y*0.71 + pos.z*0.47);             
	}
		
	override public function Update(frame:Int, world:truffle.interfaces.World)
	{
        ScreenPos = Pos2PixelPos(LogicalPos);    
        super.Update(frame,world);
	}

}
