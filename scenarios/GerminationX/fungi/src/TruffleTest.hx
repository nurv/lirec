// GerminationX Copyright (C) 2010 FoAM vzw    \_\ __     /     \
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

import flash.external.ExternalInterface;

import truffle.Truffle;
import truffle.interfaces.Key;

import truffle.Vec3;
import truffle.Vec2;
import truffle.RndGen;
import truffle.Circle;
import truffle.Entity;
import truffle.SpriteEntity;
import truffle.Bone;
import truffle.SkeletonEntity;
import truffle.Graph;

class TruffleWorld extends World 
{
    var Frame:Int;
    var TickTime:Int;
	public var Server:ServerConnection;
    var Sprites:Array<Sprite>;
    var TestBone:Bone;

	public function new() 
	{
		super();
		Frame=0;
        TickTime=0;
		Server=new ServerConnection();
        Sprites=new Array<Sprite>();

        trace("hello there from flash");
        trace(ExternalInterface.available);
        try 
        {
            var data = flash.external.ExternalInterface.call("helloJS");
            trace(data);
        }
        catch (e:Dynamic)
        {
            trace(e);
        };
        trace("done.");

        var num=10;
        var pos=new Array<Vec2>();
        for (i in 0...num)
        {
            pos.push(new Vec2(Math.random()*320,Math.random()*200));
        }
/*
        var e=new SpriteEntity(this,new Vec3(5,0,0),Resources.Get("blue-cube"));

        TestBone = new Bone(new Vec2(600,159),Resources.Get("lollypop"));
        AddSprite(TestBone);
        Sprites.push(TestBone);
        var ob2:Bone = new Bone(new Vec2(100,0),Resources.Get("blue-cube"));
//        ob2.LoadFromURL("/islands/island-4-313-299.png");
TestBone.AddChild(this,ob2); */
/*
        for (p in pos)
        {
            var ob:Sprite = new Sprite(new Vec2(p.x,p.y),Resources.Get("flowers"));
            ob.SetScale(new Vec2(0.5,0.5));
            AddSprite(ob);
            Sprites.push(ob);
        }

        var g = new Graph(new List<Edge>());
        var xx=0;
        for (x in pos)
        {
            var yy=0;
            for (y in pos)
            {
                g.AddEdge(new Edge(xx,yy,x.Sub(y).Mag()));
                yy++;
            }
            xx++;
        }
        
        g=g.MST(0);

       	graphics.clear();
		graphics.lineStyle(1, 0x000000, 1);	

        for (e in g.Edges)
        {
            var start=pos[e.From];
            var end=pos[e.To];
            graphics.moveTo(start.x,start.y);
            graphics.lineTo(end.x,end.y);
        }
*/      
        UpdateWorld(new Vec3(0,0,0));
        Update(0);
        SortScene();
        
        Server.Request("spirit-sprites",this,UpdateSpiritSprites);
	}

    public function UpdateSpiritSprites(c,data:Array<Dynamic>)
    {
        var sk:SkeletonEntity = new SkeletonEntity(this,new Vec3(5,0,0));
        sk.NeedsUpdate=true;
        sk.Build(this,data);

/*
        for (i in data)
        {
            trace(i.name);
            var ob:SpriteEntity = new SpriteEntity(this,new Vec3(0,0,0),Resources.Get("test"));
            ob.Spr.LoadFromURL(i.name.substr(2));
            Add(ob);
            ob.Spr.SetPos(new Vec2(Std.parseInt(i.position.x),
                                   Std.parseInt(i.position.y)));
		    Objs.push(ob);
            }*/
    }

	public function OnServerPlantsCallback(ServerPlants:Array<ServerPlant>)
	{
	}
	
	public function UpdateWorld(pos:Vec3)
	{
	}
		
   	override public function Handle(e:Int)
	{
        super.Handle(e);
	}

    override function Update(time:Int)
    {
        super.Update(time);
        //cast(Scene[0],SpriteEntity).Spr.SetRotate(time);
        //cast(Objs[1],SpriteEntity).Spr.SetRotate(time/2);
        //      TestBone.SetRotate(time*3);

        /*for (s in Sprites)
        {
            s.Update(time,this,null);
            }*/
    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class TruffleTest extends App
{
    public function new() 
	{
        super(new TruffleWorld());
    }
	
    static function main() 
	{
        var m:TruffleTest = new TruffleTest();
    }
}
