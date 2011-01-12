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

class SpriteEntity extends Entity
{
    public var Spr:Sprite;
		
	public function new(world:World, pos:Vec3, t:TextureDesc, viz=true) 
	{
		super(world,pos);
        Spr = new Sprite(new Vec2(Pos.x,Pos.y),t,true,viz);
        world.AddSprite(Spr);
	}
		
	override public function Update(frame:Int, world:truffle.interfaces.World)
	{
        super.Update(frame,world);
        Spr.SetPos(new Vec2(Pos.x,Pos.y));
        Spr.Update(frame,null);
	}

    override public function GetRoot() : Dynamic
    {
        return Spr;
    }
}
