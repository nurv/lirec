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
		if (LogicalPos.z%2==0)
        {
            Spr.ChangeBitmap(Resources.Get(
                rnd.Choose(["rock-cube-01","rock-cube-02","rock-cube-03"])));
		}
		else 
		{
            Spr.ChangeBitmap(Resources.Get(
                rnd.Choose(["rock-cube-04","rock-cube-05","rock-cube-06"])));
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
    var State:String;
    var Seeds:Array<Sprite>;

	public function new(world:World, owner:String, pos, type:String, state:String)
	{
		super(world,pos,Resources.Get(type+"-"+state),false);
        PlantType=type;
        State=state;
		Owner=owner;
        PlantScale=0;
        //NeedsUpdate=true;
        Seeds=[];

        Spr.Hide(false);
        
        var tf = new flash.text.TextField();
        tf.text = Owner + " planted this.";
        tf.x=Spr.Pos.x-50;
        tf.y=Spr.Pos.y-30-Spr.Height*Spr.MyScale.y;
        tf.height=40;
        tf.background = true;
        //tf.autoSize = true;
        //tf.backgroundColor = 0x8dd788;
        tf.border = true;
        tf.wordWrap = true;
        tf.autoSize = flash.text.TextFieldAutoSize.LEFT;
        var t = new flash.text.TextFormat();
        t.font = "Verdana"; 
        t.size = 8;                
        t.color= 0x000000;           
        tf.setTextFormat(t);
        Spr.parent.addChild(tf);
        tf.visible=false;
        Spr.MouseOver(this,function(c) { tf.visible=true; });
        Spr.MouseOut(this,function(c) { tf.visible=false; });
	}

    public function StateUpdate(state,world:World)
    {
        State=state;
        if (State!="decayed")
        {
            Spr.ChangeBitmap(Resources.Get(PlantType+"-"+State));
        }
        if (State=="fruit-c" && Seeds.length==0)
        {
            Fruit(world);
        }
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
	}

    public function Fruit(world:World)
    {
        var f=new Sprite(Spr.Pos.Add(new Vec2(0,-Spr.Height/2)),
                         Resources.Get("seed"));
        world.AddSprite(f);
        Seeds.push(f);
        f.MouseDown(this,function(p) 
        {            
            if (world.MyName!="")
            {
                var s=new Seed(p.PlantType);
                if (world.Seeds.Add(world,s))
                {
                    p.Seeds.remove(f);
                    world.RemoveSprite(f);
                }
            }
        });
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Spirit extends SkeletonEntity 
{
    public var Name:String;
    var Debug:flash.text.TextField;
	
	public function new(world:World, name:String, pos)
	{
		super(world,pos);
        Name = name;
    }

	public function BuildDebug(c)
    {
        var tf = new flash.text.TextField();
        tf.text = "nowt yet.";
        tf.x=Pos.x-50;
        tf.y=Pos.y-25;
        tf.height=150;
        tf.width=100;
        tf.background = true;
        tf.autoSize = flash.text.TextFieldAutoSize.LEFT;
        //tf.backgroundColor = 0x8dd788;
        tf.border = true;
        tf.wordWrap = true;
        var t = new flash.text.TextFormat();
        t.font = "Verdana"; 
        t.size = 8;                
        t.color= 0x000000;           
        tf.setTextFormat(t);
        c.addChild(tf);
        Debug=tf;
        /*tf.visible=false;
        Root.MouseOver(this,function(c) { tf.visible=true; });
        Root.MouseOut(this,function(c) { tf.visible=false; });*/

        Root.MouseDown(c,function(c)
        {
            trace("perc...");
            c.Server.Request("perceive",1,function(c,d){});
        });
	}

    public function UpdateEmotions(e:Dynamic)
    {
        var ee = e.emotions.content;
        var mood=Std.parseFloat(ee[0].content[0]);

        var text=Name+"\nMood:"+ee[0].content[0]+"\n";

        for (i in 1...ee.length)
        {
            text+=ee[i].attrs.type+" "+ee[i].attrs.direction+"\n";
            text+=ee[i].attrs.cause+"\n";
        }

        Debug.text=text;

        var t = new flash.text.TextFormat();
        t.font = "Verdana"; 
        t.size = 8;                
        t.color= 0x000000;           
        Debug.setTextFormat(t);


        //trace(text);
    }
}

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
    var PerceiveTime:Int;
    var Spirits:Array<Spirit>;
    public var Seeds:SeedStore;
    var Server : ServerConnection;

	public function new(w:Int, h:Int) 
	{
		super();
		Frame=0;
        TickTime=0;
        PerceiveTime=0;
		Width=w;
		Height=h;
		Plants = [];
        Objs = [];
        Spirits = [];
        Seeds = new SeedStore(1);
		WorldPos = new Vec3(0,0,0);
		MyRndGen = new RndGen();
        Server = new ServerConnection();
        MyName = "";

        var arrow1 = new SpriteEntity(this,new Vec3(7,-2,1), Resources.Get("arr3"));
        arrow1.Spr.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(0,-1,0))); });
        arrow1.Spr.MouseOver(this,function(c) { arrow1.Spr.SetScale(new Vec2(1.1,1.1)); arrow1.Spr.Update(0,null); });
        arrow1.Spr.MouseOut(this,function(c) { arrow1.Spr.SetScale(new Vec2(1,1)); arrow1.Spr.Update(0,null); });
 
        var arrow2=new SpriteEntity(this,new Vec3(10,21,1), Resources.Get("arr4"));
        arrow2.Spr.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(0,1,0))); });
        arrow2.Spr.MouseOver(this,function(c) { arrow2.Spr.SetScale(new Vec2(1.1,1.1)); arrow2.Spr.Update(0,null); });
        arrow2.Spr.MouseOut(this,function(c) { arrow2.Spr.SetScale(new Vec2(1,1)); arrow2.Spr.Update(0,null); });
 
        var arrow3=new SpriteEntity(this,new Vec3(-2,7,1), Resources.Get("arr2"));
        arrow3.Spr.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(-1,0,0))); });
        arrow3.Spr.MouseOver(this,function(c) { arrow3.Spr.SetScale(new Vec2(1.1,1.1)); arrow3.Spr.Update(0,null); });
        arrow3.Spr.MouseOut(this,function(c) { arrow3.Spr.SetScale(new Vec2(1,1)); arrow3.Spr.Update(0,null); });

        var arrow4=new SpriteEntity(this,new Vec3(20,10,1), Resources.Get("arr1"));
        arrow4.Spr.MouseUp(this,function(c) { c.UpdateWorld(c.WorldPos.Add(new Vec3(1,0,0))); });
        arrow4.Spr.MouseOver(this,function(c) { arrow4.Spr.SetScale(new Vec2(1.1,1.1)); arrow4.Spr.Update(0,null); });
        arrow4.Spr.MouseOut(this,function(c) { arrow4.Spr.SetScale(new Vec2(1,1)); arrow4.Spr.Update(0,null); });

		for (y in 0...h)
		{
			for (x in 0...w)
			{
				var ob:Cube = new Cube(this,new Vec3(0,0,0));
             
                ob.Spr.MouseDown(this,function(c)
                {
                    var type=c.Seeds.Remove(cast(c,truffle.World));
                    if (type!="")
                    {
                        c.AddServerPlant(ob.LogicalPos.Add(new Vec3(0,0,1)),type);
                    }
                });
				Objs.push(ob);
			}
		}

		UpdateWorld(new Vec3(0,0,0));
		
		MyTextEntry=new TextEntry(300,10,310,30,NameCallback);
		addChild(MyTextEntry);	

        Update(0);
        SortScene();
        var names = ["VerticalSpirit","CoverSpirit"];
        var positions = [new Vec3(0,5,4), new Vec3(7,0,4), new Vec3(2,10,4)];

        for (i in 0...2)
        {
            Server.Request("spirit-sprites/"+names[i],
            this,
            function (c,data:Array<Dynamic>)
            {
                var sp:Spirit = new Spirit(c,names[i],positions[i]);
                sp.NeedsUpdate=true;
                sp.Build(c,data);
                sp.BuildDebug(c);
                c.SortScene();
                c.Spirits.push(sp);
            });
        }
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
					var pos = new Vec3(MyRndGen.RndFlt()*15+x*15,
                                       MyRndGen.RndFlt()*15+y*15,
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
	}

	public function AddServerPlant(pos:Vec3,type)
	{        
        if (MyName!=null && SpaceClear(pos))
        {
            var size=MyRndGen.RndFlt()+0.5;
            Server.Request("make-plant/"+Std.string(cast(WorldPos.x,Int))+"/"+
                                         Std.string(cast(WorldPos.y,Int))+"/"+
                                         Std.string(cast(pos.x,Int))+"/"+
                                         Std.string(cast(pos.y,Int))+"/"+
                                         type+"/"+
                                         MyName+"/"+
                                         Math.round(size*100),
            this, function (c,data) {});       
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

    public function UpdateGhosts(c,t:Dynamic)
    {
        for(i in 0...t.length)
        {
            for (g in Spirits)
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

        if (time>TickTime)
        {
            Server.Request("spirit-info",this,UpdateGhosts);

            Server.Request("get-tile/"+Std.string(cast(WorldPos.x,Int))+"/"
            +Std.string(cast(WorldPos.y,Int)),
            this,
            function (c:truffle.World,d)
            {
                var data:Array<Dynamic>=cast(d.entities,Array<Dynamic>);
                for (p in data)
                {
                    var e = c.Get(new Vec2(p.pos.x,p.pos.y));
                    if (!Std.is(e,Plant))
                    {
                        var pos = new Vec3(p.pos.x,p.pos.y,e.LogicalPos.z+1);   
                        var plant = new Plant(c,p.owner,pos,p.type,p.state);
                        c.Plants.push(plant);                        
                    }
                    else
                    {
                        //trace("updating plant");
                        cast(e,Plant).StateUpdate(p.state,c);
                    }
                }
                c.SortScene();
            });            
            
            TickTime=time+100;
        }
    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Fungi extends App
{
    public function new() 
	{
//        Log.setColor(0xFFFFFF);
        super(new FungiWorld(15,15));
    }
	
    static function main() 
	{
        var m:Fungi = new Fungi();
    }
}
