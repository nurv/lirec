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

class AnimatedEntity extends Entity 
{
	public var SrcPos:Vec3;
	public var DestPos:Vec3;
	public var Time:Float;
	public var Speed:Float;
	
	public function new(pos:Vec3, t:TextureDesc) 
	{
		super(pos, t);
		Time=10;
		Speed=0;
	}
	
	public function MoveTo(pos:Vec3,speed:Float)
	{
		SrcPos=Pos;
		DestPos=pos;
		Time=0;
		Speed=speed;
	}
	
	override function Update(frame:Int, world:World)
	{
		super.Update(frame,world);
		
		if (Time<1) 
		{
			Time+=Speed;
			if (Time>1) Time=1;
			Pos=SrcPos.Lerp(DestPos,Time);
			UpdatePos();
		}
	}
}
