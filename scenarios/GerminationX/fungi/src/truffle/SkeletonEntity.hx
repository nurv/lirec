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

class SkeletonEntity extends truffle.Entity
{
    public var Root:Bone;
		
	public function new(world:World,pos:Vec3) 
	{
		super(world,pos);
        Root = null;
	}

    function GetClosest(pos:Vec2, bones:List<Bone>) : Bone
    {
        var dist=99999.0;
        var closest:Bone=null;
        for (b in bones)
        {
            var d=pos.Sub(b.Pos).Mag();
            if (d>0.00001 && d<dist)
            {
                dist=d;
                closest=b;
            }
        }
        return closest;
    }
    
    public function Build(world:World,desc:Array<Dynamic>)
    {
        var bones=new List<Bone>();
        var root=new Vec2(desc[0].position.x,
                          desc[0].position.y);
        for (d in desc)
        {
            var b=new Bone(new Vec2(root.x-Std.parseInt(d.position.x),
                                    root.y-Std.parseInt(d.position.y)),
                                    Resources.Get("test"));
            b.LoadFromURL(d.name);
            bones.add(b);
        }

        Root=bones.first();
        Root.LoadFromURL(desc[0].name);
        world.AddSprite(Root);

        bones.remove(bones.first());
        var current=Root;
        var next=Root;

        for (b in bones)
        {
            var c=GetClosest(b.Pos,bones);
            trace(c);
            b.AddChild(world,c);
        }

//        Root.Print();

        var g=new Graph(new List<Edge>());
        g.AddEdge(new Edge(0,1,9.5));
        g.AddEdge(new Edge(1,2,1.9));
        g.AddEdge(new Edge(0,2,0.3));
        g.Print();
        g.MST(0).Print();

    }
		
	override public function Update(frame:Int, world:truffle.interfaces.World)
	{
        Root.Recurse(function(b:Bone,depth:Int) 
        {
            b.SetRotate(45*Math.sin(frame*0.1));
        }
        );

        super.Update(frame,world);
        Root.SetPos(new Vec2(Pos.x,Pos.y));
        Root.Update(frame,world,null);
	}
 
    override public function GetRoot() : Dynamic
    {
        return Root;
    }
}
