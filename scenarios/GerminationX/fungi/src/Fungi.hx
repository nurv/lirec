// GerminationX Copyright (C) 2010 FoAM vzw    \_\ __     /\
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

import haxe.Log;

import truffle.Truffle;
import truffle.interfaces.Key;

import truffle.Vec3;
import truffle.Vec2;
import truffle.RndGen;
import truffle.Circle;
import truffle.Entity;
import truffle.SpriteEntity;
import truffle.SkeletonEntity;
 
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Cube extends SpriteEntity 
{	
	public function new(world:World, pos:Vec3) 
	{
		super(world, pos, Resources.Get("blue-cube"));
	}
	
	public function UpdateTex(rnd:RndGen)
	{
		if (LogicalPos.z<0)
        {
            Spr.ChangeBitmap(Resources.Get(
                rnd.Choose(["sea-cube-01","sea-cube-02","sea-cube-03"])));
		}
		else 
		{
            Spr.ChangeBitmap(Resources.Get(
                rnd.Choose(["grass-cube-01","grass-cube-02","grass-cube-03"])));
		}
	}
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Plant extends SpriteEntity 
{
	public var Owner:String;
	var PlantScale:Float;
	public var Age:Int;
    var Scale:Float;
    var PlantType:String;
    var Seeds:Array<Sprite>;

	public function new(world:World, owner:String, pos, type, maxsize, scale)
	{
		super(world,pos,Resources.Get(type),false);
        PlantType=type;
		Owner=owner;
        PlantScale=0;
        Scale=maxsize;
        NeedsUpdate=true;
        Seeds=[];

        if (scale)
        {
		    PlantScale=0;
            Spr.SetScale(new Vec2(0,0));
        }
        else
        {
            Spr.SetScale(new Vec2(Scale,Scale));
            Age=100;
        }
        
        Spr.Hide(false);
        
        //Spr.MouseOver(this,function(c) { trace("over plant"); });

        //var tf = new flash.text.TextField();
        //tf.text = Owner + " planted this.";
        //addChild(tf);
	}

    override function Destroy(world:World)
    {
        super.Destroy(world);
        for (seed in Seeds)
        {
            world.RemoveSprite(seed);
        }
    }
	
	public override function Update(frame:Int, world:World)
	{
		super.Update(frame,world);
        Age++;
		if (Age<100)
		{
			Spr.SetScale(new Vec2((Age/100)*Scale,(Age/100)*Scale));
		}
        else
        {
            Fruit(world);
            NeedsUpdate=false;
        }
	}

    public function Fruit(world:World)
    {
        var f=new Sprite(Spr.Pos.Add(new Vec2(0,-Spr.Height/2)),
                         Resources.Get("seed"));
        world.AddSprite(f);
        Seeds.push(f);
        f.MouseDown(this,function(p) 
        {            
            var s=new Seed(p.PlantType);
            if (world.Seeds.Add(world,s))
            {
                p.Seeds.remove(f);
                world.RemoveSprite(f);
            }
        });
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Ghost extends SpriteEntity 
{
    public var Name:String;
    var Debug:flash.text.TextField;
    public var TexBase:String;
	
	public function new(world:World, name:String,pos)
	{
        TexBase="ghost-"+name.toLowerCase();
		super(world,pos,Resources.Get(TexBase));
        Name = name;

        Debug = new flash.text.TextField();
        Debug.wordWrap=true;
        Debug.y=-50;
        Debug.width=300;
        Debug.text = "nothing yet";
        Spr.addChild(Debug);
	}

    public function UpdateEmotions(e:Dynamic)
    {
        var ee = e.emotions.content;
        var mood=Std.parseFloat(ee[0].content[0]);
        if (mood>1) Spr.ChangeBitmap(Resources.Get(TexBase+"-happy"));
        else if (mood<-1) Spr.ChangeBitmap(Resources.Get(TexBase+"-sad"));
        else Spr.ChangeBitmap(Resources.Get(TexBase));

        Debug.text=Name+"\nMood:"+ee[0].content[0]+"\n";

        for (i in 1...ee.length)
        {
            Debug.text+=ee[i].attrs.type+" "+ee[i].attrs.direction+"\n";
            Debug.text+=ee[i].attrs.cause+"\n";
        }
    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
class PlayerEntity extends Entity 
{
	public function new(pos:Vec3) 
	{
		super(pos, Resources.Get("player"));
	}
		
	public function Handle(e:Int, world:World)
	{
		var pos=new Vec3(LogicalPos.x,LogicalPos.y,LogicalPos.z);

		if (e==Keyboard.toInt(Key.left)) { pos.x-=1;  }
		if (e==Keyboard.toInt(Key.right)) { pos.x+=1; }
		if (e==Keyboard.toInt(Key.up)) { pos.y-=1;  }
		if (e==Keyboard.toInt(Key.down)) { pos.y+=1;  }
		//if (e==Keyboard.toInt(Key.space)) { world.AddServerPlant(new Vec3(pos.x,pos.y,1)); }	      

        if (e==87)
        {
            world.WorldClient.Call("add-object/WiltedVine",function(d){});
            world.AddServerPlant(new Vec3(pos.x,pos.y,1),2);
        }

        if (e==84)
        {
            world.WorldClient.Call("add-object/AppleTree",function(d){});
            world.AddServerPlant(new Vec3(pos.x,pos.y,1),3);
        }

        if (e==80)
        {
            world.WorldClient.Call("perceive",function(d){});
        }

		var oldworldpos=new Vec3(world.WorldPos.x,world.WorldPos.y,world.WorldPos.z);

		if (pos.x<0)
		{
			pos.x=world.Width-1;
			world.UpdateWorld(world.WorldPos.Add(new Vec3(-1,0,0)));
		}

		if (pos.x>=world.Width)
		{
			pos.x=0;
			world.UpdateWorld(world.WorldPos.Add(new Vec3(1,0,0)));
		}

		if (pos.y<0)
		{
			pos.y=world.Height-1;
			world.UpdateWorld(world.WorldPos.Add(new Vec3(0,-1,0)));
		}

		if (pos.y>=world.Height)
		{
			pos.y=0;
			world.UpdateWorld(world.WorldPos.Add(new Vec3(0,1,0)));
		}	
		
		if (world.GetCube(pos).LogicalPos.z>-1)
		{
			LogicalPos=pos;
			LogicalPos.z=world.GetCube(LogicalPos).LogicalPos.z+1;		
		}	
		else
		{
			world.UpdateWorld(oldworldpos);
		}
	}
}
*/

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Seed
{
    public var Type:String;
    public var Spr:Sprite;

    public function new(t:String)
    {
        Type=t;
        Spr=new Sprite(new Vec2(0,0),Resources.Get("seed"));
    }
}

class SeedStore
{
    var Seeds:Array<Seed>;
    var Size:Int;

    public function new(size:Int)
    {
        Seeds = [];
        Size = size;
    }

    public function Add(world:World,s:Seed) : Bool
    {
        if (Seeds.length<Size)
        {
            Seeds.push(s);
            s.Spr.SetPos(new Vec2(20+Seeds.length*10,30));
            //cast(world,truffle.flash.FlashWorld).addSprite(s.Spr);
            //trace("did it...");
            s.Spr.Update(0,null);
            return true;
        }
        return false;
    }

    public function Remove(world:World) : String
    {
        if (Seeds.length>0)
        {
            var s = Seeds.pop();
//            world.RemoveSprite(s.Spr);
            return s.Type;
        }
        else
        {
            return "";
        }
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class FungiWorld extends World 
{
	public var Width:Int;
	public var Height:Int;
	var Objs:Array<Cube>;
	public var WorldPos:Vec3;
	var MyRndGen:RndGen;
	public var MyTextEntry:TextEntry;
	public var Plants:Array<Plant>;
	var MyName:String;
    var Frame:Int;
    var TickTime:Int;
    var Ghosts:Array<Ghost>;
    var Cursor:Sprite;
    var CursorLogicalPos:Vec3;
    public var Seeds:SeedStore;
    var Server : ServerConnection;

	public function new(w:Int, h:Int) 
	{
		super();
		Frame=0;
        TickTime=0;
		Width=w;
		Height=h;
		Plants = [];
        Objs = [];
        Ghosts = [];
        Seeds = new SeedStore(1);
		WorldPos = new Vec3(0,0,0);
		MyRndGen = new RndGen();
        CursorLogicalPos = new Vec3(5,5,0);
        Server = new ServerConnection();

        Cursor=new Sprite(new Vec2(0,0), Resources.Get("cursor"), true);
        AddSprite(Cursor);

        var arrow = new Sprite(new Vec2(500,0), Resources.Get("test"));
        arrow.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(0,-1,0))); });
        AddSprite(arrow);

        arrow=new Sprite(new Vec2(0,400), Resources.Get("test"));
        arrow.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(0,1,0))); });
        AddSprite(arrow);

        arrow=new Sprite(new Vec2(0,0), Resources.Get("test"));
        arrow.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(-1,0,0))); });
        AddSprite(arrow);

        arrow=new Sprite(new Vec2(500,400), Resources.Get("test"));
        arrow.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(1,0,0))); });
        AddSprite(arrow);

        MyName = "foo";

		for (y in 0...h)
		{
			for (x in 0...w)
			{
				var ob:Cube = new Cube(this,new Vec3(0,0,0));
             
                ob.Spr.MouseOver(this,function(c)
                {
                    c.Cursor.Pos=ob.Spr.Pos;
                    c.CursorLogicalPos = ob.LogicalPos;
                });

                ob.Spr.MouseDown(this,function(c)
                {
                    var type=c.Seeds.Remove(cast(c,truffle.World));
                    if (type!="")
                    {
                        c.AddServerPlant(c.CursorLogicalPos.Add(new Vec3(0,0,1)),type);
                    }
                });
				Objs.push(ob);
			}
		}

		UpdateWorld(new Vec3(0,0,0));
		
        var Names = ["Vertical","Canopy","Cover"];
/*        
        for (i in 0...3)
        {
            var g = new Ghost(this,Names[i],new Vec3(2,(i*3)+2,1));
            Ghosts.push(g);
        }
*/
//		MyTextEntry=new TextEntry(190,10,310,30,NameCallback);
//		addChild(MyTextEntry);	

        Update(0);
        SortScene();
        Server.Request("spirit-sprites",this,UpdateSpiritSprites);
	}
	
	public function NameCallback(name)
	{
		removeChild(MyTextEntry);
		MyName=name;
		//WorldClient.GetPlants(cast(WorldPos.x,Int),cast(WorldPos.y,Int));
	}
	
	public function UpdateWorld(pos:Vec3)
	{
		WorldPos=pos;
		
		var circles = [];
        for (x in -1...2)
		{
			for (y in -1...2)
			{
				MyRndGen.Seed(cast((WorldPos.x+x)+(WorldPos.y+y)*139,Int));
				for (i in 0...5)
				{
					var pos = new Vec3(MyRndGen.RndFlt()*10+x*10,
                                       MyRndGen.RndFlt()*10+y*10,
									   0);		  
					circles.push(new Circle(pos, MyRndGen.RndFlt()*4));
				}
			}
		}
		
		
		for (i in 0...Objs.length)
		{
			var pos=new Vec3(i%Width,Math.floor(i/Width),-1);
			MyRndGen.Seed(cast(WorldPos.x+pos.x+WorldPos.y+pos.y*139,Int));
			var inside:Bool=false;
			for (c in circles)
			{
				if (c.Inside(pos)) pos.z=0;
			}

			Objs[i].LogicalPos=pos;
			Objs[i].UpdateTex(MyRndGen);
            Objs[i].Update(0,this);
		}

        ClearPlants();		
        Server.Request("get-tile/"+Std.string(cast(WorldPos.x,Int))+"/"
                                  +Std.string(cast(WorldPos.y,Int)),
        this,
        function (c:truffle.World,d)
        {
            var data:Array<Dynamic>=cast(d.entities,Array<Dynamic>);
            for (p in data)
            {
                var pos = new Vec3(p.pos.x,p.pos.y,1);
                
                if (pos.x<10 && pos.y<10 && pos.x>0 && pos.y>0 &&
                    c.SpaceClear(pos) && c.GetCube(pos).LogicalPos.z>-1)
                {
                    var plant = new Plant(c,p.owner,pos,p.type,p.size/100,false);
                    c.Plants.push(plant);
                }
            }
            c.SortScene();
        });
	}

	public function AddServerPlant(pos:Vec3,type)
	{        
        if (MyName!=null && SpaceClear(pos) && GetCube(pos).LogicalPos.z>-1)
        {
            var size=MyRndGen.RndFlt()+0.5;
		    var plant = new Plant(this,MyName,pos,type,size,true);
		    Plants.push(plant);
            Server.Request("make-plant/"+Std.string(cast(WorldPos.x,Int))+"/"+
                                         Std.string(cast(WorldPos.y,Int))+"/"+
                                         Std.string(cast(pos.x,Int))+"/"+
                                         Std.string(cast(pos.y,Int))+"/"+
                                         type+"/"+
                                         MyName+"/"+
                                         Math.round(size*100),
            this, function (c,data) {});       
            SortScene();
        }
	}

    public function ClearPlants() : Void
    {
        for (plant in Plants)
        {
            Remove(plant);
        }
        Plants = [];
	}
	
	public function GetCube(pos:Vec3) : Cube
	{
		return Objs[cast(pos.x+pos.y*Width,Int)];
	}
	
    public function UpdateSpiritSprites(c,data:Array<Dynamic>)
    {
        var sk:SkeletonEntity = new SkeletonEntity(this,new Vec3(0,5,4));
        sk.NeedsUpdate=true;
        sk.Build(this,data);
        SortScene();
     }
	
    public function SpaceClear(pos:Vec3)
    {
        for (plant in Plants)
        {
            if (plant.LogicalPos.Eq(pos)) return false;
        }
        return true;
    }
	
   	override public function Handle(e:Int)
	{
        super.Handle(e);
        Update(0);
	}

    public function UpdateGhosts(t:Dynamic)
    {
        for(i in 0...t.length)
        {
            for (g in Ghosts)
            {
                if (g.Name==t[i].name)
                {
                    g.UpdateEmotions(t[i]);
                }
            }
        }
    }

    override function Update(time:Int)
    {
        super.Update(time);

        Server.Update();

        /*if (time>TickTime)
        {
            WorldClient.Call("agent-info",UpdateGhosts);
            TickTime=time+100;
        }
        
        for (plant in Plants)
        {
            if (plant.Age>2000)
            {
                Plants.remove(plant);
                Remove(plant);
            }
            }*/
        
        //Cursor.Update(time,null);
    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Fungi extends App
{
    public function new() 
	{
//        Log.setColor(0xFFFFFF);
        super(new FungiWorld(10,10));
    }
	
    static function main() 
	{
        var m:Fungi = new Fungi();
    }
}
