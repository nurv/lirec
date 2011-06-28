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

package fungi;

import truffle.Truffle;
import truffle.Vec3;
import truffle.Vec2;
import truffle.RndGen;
import truffle.SpriteEntity;
import truffle.Circle;

// todo: remove this
import flash.external.ExternalInterface;

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
	public var MyName:String;
    var MyID:Int;
    var Frame:Int;
    var TickTime:Int;
    var PerceiveTime:Int;
    var Spirits:Array<Spirit>;
    public var Seeds:SeedStore;
    var Server:ServerConnection;
    var Spiral:Sprite;
    var SpiralScale:Float;
    var NewsFeed:Feed;
    public var NumPlants:Int;
    public var Season:String;

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
        SpiralScale=0;
        Seeds = new SeedStore(1);
		WorldPos = new Vec3(0,0,0);
		MyRndGen = new RndGen();
        Server = new ServerConnection();
        MyName = "";
        MyID = -1;
        NumPlants = 0;
        Season="no season";

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

        // hiscores table
        var Hiscores = new Frame("Loading...",100,100,250,500);
        addChild(Hiscores);
        Hiscores.Hide(true);

        var HiscoresButton = new Sprite(new Vec2(50,50), Resources.Get(""));
        AddSprite(HiscoresButton);
        HiscoresButton.MouseDown(this,function(c)
        {
            Hiscores.Hide(!Hiscores.IsHidden());
            if (!Hiscores.IsHidden())
            {
                cast(c,FungiWorld).Server.Request("hiscores",
                                 c,
                                 function(c,data:Array<Dynamic>)
                                 {
                                     var text="Hi Scores Table\n Number of plants currently alive, by player.\n\n";
                                     for (i in data)
                                     {
                                         text+=i[0]+": "+i[1]+"\n";
                                         if (i[0]=c.MyName) c.NumPlants=Std.int(i[1]);
                                     }
                                     Hiscores.UpdateText(text);
                                 });
                           
            }
        });

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
                        c.ActivatePlants(true);
                        c.SpiralScale=1;
                        c.Spiral.SetPos(new Vec2(ob.Pos.x,ob.Pos.y-32));
                        c.AddServerPlant(ob.LogicalPos.Add(new Vec3(0,0,1)),type);
                    }
                });
				Objs.push(ob);
			}
		}

		UpdateWorld(new Vec3(0,0,0));
		
        NewsFeed = new Feed(this);
		MyTextEntry=new TextEntry(330,50,250,20,NameCallback);
		addChild(MyTextEntry);	

        TheCritters = new Critters(this,3);

        Spiral = new Sprite(new Vec2(0,0), Resources.Get("spiral"), true);
        AddSprite(Spiral);

        Update(0);
        SortScene();
        var names = ["TreeSpirit"];//,"ShrubSpirit","CoverSpirit"];
        var positions = [new Vec3(0,5,4), new Vec3(7,0,4), new Vec3(2,10,4)];
 
        for (i in 0...1)
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
        Server.Request("login/"+name,
                       this,
                       function (c,data:Dynamic)
                       {
                           c.MyID=data;
		                   c.MyName=name;
		                   c.removeChild(c.MyTextEntry);
                       });

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
                                         MyID+"/"+
                                         Math.round(size*100),
            this, function (c,data) {});       
        }
	}

    public function ActivatePlants(s:Bool) : Void
    {
        for (plant in Plants)
        {
            plant.Spr.EnableMouse(s);
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

    public function UpdateTile(d:Dynamic)
    {
        Season=d.season;

        var data:Array<Dynamic>=cast(d.entities,Array<Dynamic>);
        for (p in data)
        {
            var worldpos = new Vec2(p.pos.x,p.pos.y);
            var e = Get("fungi.Plant",worldpos);
            if (e==null)
            {
                var cube = Get("fungi.Cube",worldpos);
                if (cube!=null)
                {
                    var pos = new Vec3(p.pos.x,p.pos.y,cube.LogicalPos.z+1);   
                    var plant = new Plant(this,Std.parseInt(p.id),
                                          p.owner,
                                          pos,
                                          p.type,
                                          p.state,
                                          p.fruit,
                                          p.layer);
                    Plants.push(plant);
                }
            }
            else
            {
                //trace("updating plant");
                //trace(e);
                //trace(p.state);
                cast(e,Plant).StateUpdate(p.state,p.fruit,this);
            }
        }

        var temp=Plants;
        for (plant in Plants)
        {
            if (plant.State=="decayed")
            {
                Remove(plant);
                temp.remove(plant);
            }
        }
        Plants=temp;
        SortScene();
    }

    override function Update(time:Int)
    {
        super.Update(time);

        Server.Update();
        TheCritters.Update();

        if (SpiralScale>0.1)
        {
            Spiral.Hide(false);
            SpiralScale-=0.05;
            Spiral.SetRotate(time*12);
            Spiral.SetScale(new Vec2(SpiralScale,SpiralScale));
            Spiral.Update(time,null);
        }
        
        if (time>TickTime)
        {
            UpdateSpiritSprites();

            Server.Request("spirit-info",this,UpdateGhosts);
            Server.Request("get-tile/"+Std.string(cast(WorldPos.x,Int))+"/"
                           +Std.string(cast(WorldPos.y,Int)),
                           this,
                           function(c,d){c.UpdateTile(d);});          
            Server.Request("get-msgs/"+Std.string(MyID),this,
                           function(c,d){c.NewsFeed.Update(cast(c,World),d);});

            if (MyName=="")
            {        
                try 
                {
                    var data = flash.external.ExternalInterface.call("game.get_name");
                    if (data!="")
                    {
                        MyName=data;
		                removeChild(MyTextEntry);
                    }
                }
                catch (e:Dynamic)
                {
                    trace(e);
                };
            }
            
            TickTime=time+100;
        }
    }

}
