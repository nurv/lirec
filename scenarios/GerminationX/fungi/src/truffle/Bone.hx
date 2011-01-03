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

class Bone extends Sprite
{
    public var Children:List<Bone>;

	public function new(pos:Vec2, t:TextureDesc) 
    {
        super(pos,t);
        Children = new List<Bone>();
    }

    public function AddChild(world:World,c:Bone)
    {
        Children.add(c);
        world.AddSprite(c);
    }

	override function Update(frame:Int, world:truffle.interfaces.World, tx:Dynamic)
    {
        super.Update(frame,world,tx);
        var tx=GetTransform();
        for (c in Children)
        {
            c.Update(frame,world,tx);
        }
    }

    public function Recurse(fn:Bone->Int->Void)
    {
        DoRecurse(fn,0);
    }

    public function DoRecurse(fn:Bone->Int->Void,depth:Int)
    {
        fn(this,depth);
        for (c in Children)
        {
            c.DoRecurse(fn,depth+1);
        }
    }

    public function Print()
    {
        trace("Bone");
        if (Children.length>0)
        {
            trace("Children start");
            for (c in Children) { c.Print(); }
            trace("Children end");
        }
        
    }
}
