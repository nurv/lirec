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
	var g:Graph;
    var bones:Array<Bone>;
    public var Id:Int;

	public function new(world:World,pos:Vec3) 
	{
		super(world,pos);
        Root = null;
        // hack for the animation
        Id=cast(pos.y,Int);
        bones=[];
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
    
    function FindTop(desc:Array<Dynamic>)
    {
        var highest=9999;
        var top=0;
        var c=0;
        for (d in desc)
        {
            if (d.position.y<highest)
            {
                highest=d.position.y;
                top=c;
            }
            c++;
        }
        return top;
    }
    
    function BuildBones(desc:Array<Dynamic>)
    {
        var bones=new Array<Bone>();
        for (d in desc)
        {
            var b=new Bone(new Vec2(-Std.parseInt(d.position.x),
                                    -Std.parseInt(d.position.y)),
                                    Resources.Get("test"));
            b.LoadFromURL(d.name);
            bones.push(b);
        }
        return bones;
    }

    function CalculateMST(bones:Array<Bone>,root:Int)
    {
        var g=new Graph(new List<Edge>());
        var x=0;
        var y=0;
        for (xb in bones)
        {
            for (yb in bones)
            {
                g.AddEdge(new Edge(x,y,xb.Pos.Sub(yb.Pos).Mag()));
                y++;
            }
            y=0;
            x++;
        }
        return g.MST(root);
    }

    public function Build(world:World,desc:Array<Dynamic>)
    {
        for (b in bones)
        {
            world.RemoveSprite(b);
        }

        bones=BuildBones(desc);
        var top=FindTop(desc);
        g=CalculateMST(bones,top);
        Root=bones[top];
        world.AddSprite(Root);
        var relative = new Array<Vec2>();
        for (i in 0...bones.length) relative.push(new Vec2(0,0));

        for (edge in g.Edges)
        {
            bones[edge.From].AddChild(world,bones[edge.To]);
            relative[edge.To]=bones[edge.From].Pos.Sub(bones[edge.To].Pos);
        } 

        for (b in 0...bones.length)
        {
            bones[b].Pos=relative[b];//.Mul(0.5);
            bones[b].BindPos=bones[b].Pos;
        }
    }

    override function OnSortScene(order:Int) : Void
    {
        Root.SetDepth(order+10);
        Root.Recurse(function(b:Bone,depth:Int) 
        {
            b.SetDepth(order+1);
        });        
    }

	override public function Update(frame:Int, world:World)
	{
        super.Update(frame,world);
        Root.SetPos(new Vec2(Pos.x,Pos.y));
        Root.Update(frame,null);
	}
 
	public function Draw(world:World)
	{
       	world.graphics.clear();
		world.graphics.lineStyle(1, 0x00aa00, 1);	

        for (e in g.Edges)
        {
            var start=bones[e.From].GetGlobalPos();
            var end=bones[e.To].GetGlobalPos();
            world.graphics.moveTo(start.x,start.y);
            world.graphics.lineTo(end.x,end.y);
            world.graphics.beginFill( 0x99ff33 , 1 );
            // drawing circle 
            world.graphics.drawCircle( start.x , start.y , 4 );
            world.graphics.drawCircle( end.x , end.y , 4 );
        }
    }

    override public function GetRoot() : Dynamic
    {
        return Root;
    }
}
