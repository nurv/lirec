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
import truffle.Graph;

class ClusterEntity extends truffle.Entity
{
    var Sprites:Array<Bone>;
    public var Id:Int;
    var Root:Bone;

	public function new(world:World,pos:Vec3) 
	{
		super(world,pos);
        // hack for the animation
        Id=cast(pos.y,Int);
        Sprites=[];
	}

    private function strSort(a:String, b:String):Int
    {
        a = a.toLowerCase();
        b = b.toLowerCase();
        if (a < b) return -1;
        if (a > b) return 1;
        return 0;
    }

    public function Build(world:World,desc:Array<Dynamic>)
    {
        for (s in Sprites)
        {
            world.RemoveSprite(s);
        }

        var filenames=[];
        for (b in desc)
        {
            filenames.push(b.name);
        }

        // sort so we go from 0->whatever in right order
        filenames.sort(strSort);

        for (b in filenames)
        {
            var s=new Bone(new Vec2(0,0),
                           Resources.Get("test"));
            s.LoadFromURL(b);
            s.InheritTransform=false;
            Sprites.push(s);
        }

        for (i in 1...Sprites.length)
        {
            Sprites[i-1].AddChild(world,Sprites[i]);
        }

        Root=Sprites[0];
        world.AddSprite(Root);
    }

    override function Hide(s:Bool) : Void
    {
        if (Hidden!=s)
        {
            Hidden=s;
            for (Spr in Sprites)
            {
                Spr.Hide(s);
            }
        }
    }

    override function OnSortScene(world:World, order:Int) : Int
    {
        for (i in 0...Sprites.length)
        {
            Sprites[Sprites.length-i-1].SetDepth(order++);
        }
        return order;
    }

	override public function Update(frame:Int, world:World)
	{
        super.Update(frame,world);
        Root.SetPos(new Vec2(Pos.x,Pos.y));        
        Root.Update(frame,null);
	}
 
    override public function GetRoot() : Dynamic
    {
        return Root;
    }
}
