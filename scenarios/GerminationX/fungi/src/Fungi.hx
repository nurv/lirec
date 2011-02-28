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
import truffle.Bone;

import Critters;

// todo: remove this
import flash.display.Graphics;
import flash.display.Shape;

 
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
    var Layer:String;

	public function new(world:World, owner:String, pos, type:String, state:String)
	{
		super(world,pos,Resources.Get(type+"-"+state),false);
        PlantType=type;
        State=state;
		Owner=owner;
        PlantScale=0;
        //NeedsUpdate=true;
        Seeds=[];

        Layer="none";
        if (type=="plant-001") Layer="cover";
        if (type=="plant-002") Layer="canopy";
        if (type=="plant-003") Layer="vertical";

        Spr.Hide(false);
        
        var tf = new flash.text.TextField();
        tf.text = "This plant belongs to the "+type+" species, part of the "+ 
            Layer+" layer. "+Owner+" planted this.";
        tf.x=Spr.Pos.x-50;
        tf.y=Spr.Pos.y-30-Spr.Height*Spr.MyScale.y;
        tf.height=40;
        tf.background = true;
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
	var BG:Graphics;
    var RawEmotions:Dynamic;
    var Emotions:Dynamic;
    var DesiredPos:Vec2;
    public var LastData:Array<Dynamic>;
    
	public function new(world:World, name:String, pos)
	{
		super(world,pos);
        Name = name;
        Speed=0.1;
        UpdateFreq=5;
        Hide(true);
        LastData=[];
        DesiredPos=new Vec2(LogicalPos.x,LogicalPos.y);
        RawEmotions={Love:0,Hate:0,Hope:0,Fear:0,Satisfaction:0,
                     Relief:0,/*Fears_Confirmed:0,*/Disappointment:0,
                     Joy:0,Distress:0,/*Happy_For:0,*/Pitty:0,
                     Resentment:0,Gloating:0,Pride:0,Shame:0,
                     Gratification:0,Remorse:0,Admiration:0,
                     Reproach:0,Gratitude:0,Anger:0};
        Emotions={Love:0,Hate:0,Hope:0,Fear:0,Satisfaction:0,
                  Relief:0,/*Fears_Confirmed:0,*/Disappointment:0,
                  Joy:0,Distress:0,/*Happy_For:0,*/Pitty:0,
                  Resentment:0,Gloating:0,Pride:0,Shame:0,
                  Gratification:0,Remorse:0,Admiration:0,
                  Reproach:0,Gratitude:0,Anger:0};
    }

	public function BuildDebug(c)
    {
        var tf = new flash.text.TextField();
        tf.text = "nowt yet.";
        tf.x=Pos.x-150;
        tf.y=Pos.y-25;
        tf.height=150;
        tf.width=140;
        tf.background = false;
        tf.autoSize = flash.text.TextFieldAutoSize.LEFT;
        //tf.backgroundColor = 0x8dd788;
        tf.border = true;
        tf.wordWrap = true;
        var t = new flash.text.TextFormat();
        t.font = "Verdana"; 
        t.size = 8;                
        t.color= 0x000000;           
        tf.setTextFormat(t);

        var figures:Shape = new Shape();
        BG = figures.graphics;
        BG.beginFill(0xffffff,0.5);
        BG.drawRect(tf.x,tf.y,tf.width,tf.height);
        BG.endFill();
        figures.visible=false;
        cast(c,truffle.flash.FlashWorld).addChild(figures);

        c.addChild(tf);
        Debug=tf;
        tf.visible=false;

        Root.MouseDown(c,function(c)
        {
            tf.visible=!tf.visible;
            figures.visible=!figures.visible;
        });
	}

    public function UpdateEmotions(e:Dynamic,world:World)
    {
        SetTilePos(new Vec2(Std.parseInt(e.tile.x),
                            Std.parseInt(e.tile.y)));

        
        var dst = new Vec2(Std.parseInt(e.emotionalloc.x),
                              Std.parseInt(e.emotionalloc.y));

        if (dst.x!=DesiredPos.x || dst.y!=DesiredPos.y)
        {
            DesiredPos = dst;
            while (world.Get("Spirit",dst)!=null)
            {
                dst = dst.Add(new Vec2(world.MyRndGen.Choose([-2,0,2]),
                                       world.MyRndGen.Choose([-2,0,2])));
            }
            LogicalPos = new Vec3(dst.x,dst.y,4);
        }

        RawEmotions = e.emotions;

        var ee = e.fatemotions.content;
        var mood=Std.parseFloat(ee[0].content[0]);

        var text=Name+"\nMood:"+ee[0].content[0]+"\n";
        text+="Emotions:\n";
        for (i in 1...ee.length)
        {
            text+=ee[i].attrs.type+" "+ee[i].attrs.direction+"\n";
            //text+=ee[i].attrs.cause+"\n";
        }

        text+="Actions:\n";
        var acs = cast(e.fatactions,Array<Dynamic>);
        for (i in 0...acs.length)
        {
            text+=acs[i].msg+"\n";
        }

        Debug.text=text;

        var t = new flash.text.TextFormat();
        t.font = "Verdana"; 
        t.size = 8;                
        t.color= 0x000000;           
        Debug.setTextFormat(t);
        Debug.x=Pos.x-150;
        Debug.y=Pos.y-25;

        BG.clear();
        BG.beginFill(0xffffff,0.5);
        BG.drawRect(Debug.x,Debug.y,Debug.width,Debug.height);
        BG.endFill();

        //trace(text);
    }

    override function Update(frame:Int, world:World)
    {
        for (f in Reflect.fields(Emotions))
        {
            // do a linear blend to smooth out changes in emotions
            Reflect.setField(Emotions,f,
            Reflect.field(Emotions,f)*0.95+Reflect.field(RawEmotions,f)*0.05);
            //trace(f);
        }

        //Draw(cast(world,truffle.World));
        var c=this;
        Root.Recurse(function(b:Bone,depth:Int) 
        {
            var excitement = c.Emotions.Love+c.Emotions.Admiration;
            if (excitement>10) excitement=10;
            
            b.SetRotate(excitement*5*Math.sin(
                             (((10-depth)+frame*0.04+c.Emotions.Gratitude*0.01)+
                             c.Emotions.Joy*0.1)) +
            ((world.MyRndGen.RndFlt()-0.5)*10*(c.Emotions.Hate+
                    c.Emotions.Distress)));
           
        });

        super.Update(frame,world);
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
    public var TheCritters:Critters;
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

        TheCritters = new Critters(this,3);

        Update(0);
        SortScene();
        var names = ["CanopySpirit","VerticalSpirit","CoverSpirit"];
        var positions = [new Vec3(0,5,4), new Vec3(7,0,4), new Vec3(2,10,4)];

        for (i in 0...3)
        {
            Server.Request("spirit-sprites/"+names[i],
            this,
            function (c,data:Array<Dynamic>)
            {
                var sp:Spirit = new Spirit(c,names[i],positions[i]);
                sp.NeedsUpdate=true;
                sp.LastData=data;
                sp.Build(c,data);
                sp.BuildDebug(c);
                c.SortScene();
                c.Spirits.push(sp);
            });
        }
	}

    function CompareLists(a:Array<Dynamic>,b:Array<Dynamic>): Bool
    {
        if (a.length!=b.length) return false;
        for (i in 0...a.length)
        {
            if (a[i].name!=b[i].name) return false;
        }
        return true;
    }

    public function UpdateSpiritSprites()
    {
        for (s in Spirits)
        {
            Server.Request("spirit-sprites/"+s.Name,
            this,
            function (c,data:Array<Dynamic>)
            {
                if (!c.CompareLists(data,s.LastData))
                {
                    s.Build(cast(c,World),data);
                    s.LastData=data;
                }
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
        SetCurrentTilePos(new Vec2(pos.x,pos.y));
		
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
                    g.UpdateEmotions(t[i],this);
                }
            }
        }
    }

    override function Update(time:Int)
    {
        super.Update(time);

        Server.Update();
        TheCritters.Update();
        
        if (time>TickTime)
        {
            UpdateSpiritSprites();
            
            Server.Request("spirit-info",this,UpdateGhosts);

            Server.Request("get-tile/"+Std.string(cast(WorldPos.x,Int))+"/"
            +Std.string(cast(WorldPos.y,Int)),
            this,
            function (c:truffle.World,d)
            {
                var data:Array<Dynamic>=cast(d.entities,Array<Dynamic>);
                for (p in data)
                {
                    var worldpos = new Vec2(p.pos.x,p.pos.y);
                    var e = c.Get("Plant",worldpos);
                    if (e==null)
                    {
                        //trace("making new plant");
                        var cube = c.Get("Cube",worldpos);
                        if (cube!=null)
                        {
                            var pos = new Vec3(p.pos.x,p.pos.y,cube.LogicalPos.z+1);   
                            var plant = new Plant(c,p.owner,pos,p.type,p.state);
                            c.Plants.push(plant);
                        }
                    }
                    else
                    {
                        //trace("updating plant");
                        //trace(e);
                        //trace(p.state);
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
