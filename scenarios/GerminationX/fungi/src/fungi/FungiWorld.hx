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
import flash.events.MouseEvent;

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
    public var PlayerInfo:Dynamic;
    var LogicalCameraPos:Vec2;

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
        PlayerInfo = {};
        MyName = "";
        MyID = -1;
        NumPlants = 0;
        Season="no season";
        LogicalCameraPos=new Vec2(0,0);

        GUIFrameTextures.Init();
        
		for (y in 0...h)
		{
			for (x in 0...w)
			{
				var ob:Cube = new Cube(this,new Vec3(0,0,0));
             
                ob.Spr.MouseUp(this,function(c)
                {
                    if (c.Seeds.Carrying())
                    {
                        // look for a plant here already
                        // - done on the server, but need to do it
                        // here too
                        var e = c.Get("fungi.Plant",
                                      new Vec2(ob.LogicalPos.x,
                                               ob.LogicalPos.y));
                        
                        if (e==null)
                        {
                            var type=c.Seeds.Seeds[0].Type;
                            var id=c.Seeds.Seeds[0].ID;
                            c.Seeds.Remove(cast(c,truffle.World));
                            if (type!="")
                            {
                                c.SpiralScale=1;
                                c.Spiral.SetPos(new Vec2(ob.Pos.x,ob.Pos.y-128));
                                c.AddServerPlant(ob.LogicalPos.Add(new Vec3(0,0,1)),type,id);
                            }
                        }
                    }
                });
				Objs.push(ob);
			}
		}

		UpdateWorld(new Vec3(0,0,0));
		
        NewsFeed = new Feed(this);
		MyTextEntry=new TextEntry(150,50,250,20,NameCallback);
		addChild(MyTextEntry);	

        Spiral = new Sprite(new Vec2(0,0), Resources.Get("spiral"), true);
        AddSprite(Spiral);

        TheCritters = new Critters(this,10);

        Update(0);
        SortScene();
        var names = ["TreeSpirit",/*"ShrubSpirit",*/"CoverSpirit"];
        var positions = [new Vec3(0,5,4), new Vec3(7,0,4), new Vec3(2,10,4)];
 
        for (i in 0...names.length)
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

        var arrow1 = new Sprite(new Vec2(470,40), Resources.Get("arr3"));
        arrow1.MouseUp(this,function(c) { c.MoveWorld(new Vec3(1,0,0)); });
        arrow1.MouseOver(this,function(c) { arrow1.Colour=new Vec3(0.8,1,0.7); arrow1.Update(0,null); });
        arrow1.MouseOut(this,function(c) { arrow1.Colour=new Vec3(1,1,1); arrow1.Update(0,null); });
        addChild(arrow1);

        var arrow2=new Sprite(new Vec2(50,540), Resources.Get("arr4"));
        arrow2.MouseUp(this,function(c) { c.MoveWorld(new Vec3(-1,0,0)); });
        arrow2.MouseOver(this,function(c) { arrow2.Colour=new Vec3(0.8,1,0.7); arrow2.Update(0,null); });
        arrow2.MouseOut(this,function(c) { arrow2.Colour=new Vec3(1,1,1); arrow2.Update(0,null); });
        addChild(arrow2);

        var arrow3=new Sprite(new Vec2(40,40), Resources.Get("arr2"));
        arrow3.MouseUp(this,function(c) { c.MoveWorld(new Vec3(0,-1,0)); });
        arrow3.MouseOver(this,function(c) { arrow3.Colour=new Vec3(0.8,1,0.7); arrow3.Update(0,null); });
        arrow3.MouseOut(this,function(c) { arrow3.Colour=new Vec3(1,1,1); arrow3.Update(0,null); });
        addChild(arrow3);

        var arrow4=new Sprite(new Vec2(470,540), Resources.Get("arr1"));
        arrow4.MouseUp(this,function(c) { c.MoveWorld(new Vec3(0,1,0)); });
        arrow4.MouseOver(this,function(c) { arrow4.Colour=new Vec3(0.8,1,0.7); arrow4.Update(0,null); });
        arrow4.MouseOut(this,function(c) { arrow4.Colour=new Vec3(1,1,1); arrow4.Update(0,null); });
        addChild(arrow4);

        var c=this;
        MouseMove(this, function(e) { c.Seeds.Update(e.stageX,e.stageY); });
	}

    function MoveWorld(dir)
    {
        LogicalCameraPos.x+=dir.x;
        LogicalCameraPos.y+=dir.y;

        if (LogicalCameraPos.x>5)
        {
            UpdateWorld(new Vec3(1,0,0));
            LogicalCameraPos.x=0;
        }
        if (LogicalCameraPos.y>5)
        {
            UpdateWorld(new Vec3(0,1,0));
            LogicalCameraPos.y=0;
        }
        if (LogicalCameraPos.x<-5)
        {
            UpdateWorld(new Vec3(-1,0,0));
            LogicalCameraPos.x=0;
        }
        if (LogicalCameraPos.y<-5)
        {
            UpdateWorld(new Vec3(0,-1,0));
            LogicalCameraPos.y=0;
        }

        var t=ScreenSpaceTransform(new Vec3(-LogicalCameraPos.x,
                                            -LogicalCameraPos.y,0));

        SetTranslate(new Vec2(ScreenCentre.x+t.x,
                              ScreenCentre.y+t.y));
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
        Server.Request("login/"+name+"/0",
                       this,
                       function (c,d:Dynamic)
                       {
                           c.PlayerInfo=d;
                           // todo: remove MyID, MyName
                           c.MyID=c.PlayerInfo.id;
		                   c.MyName=c.PlayerInfo.name;
		                   c.removeChild(c.MyTextEntry);
                       });

		//WorldClient.GetPlants(cast(WorldPos.x,Int),cast(WorldPos.y,Int));
	}
	
	public function UpdateWorld(dir:Vec3)
	{
		WorldPos=WorldPos.Add(dir);
        SetCurrentTilePos(new Vec2(WorldPos.x,WorldPos.y));
		
        var TileSize=5;

		var circles = [];
        for (x in -2...3)
		{
			for (y in -2...3)
			{
				MyRndGen.Seed(cast((WorldPos.x+x)+(WorldPos.y+y)*139,Int));
				for (i in 0...2)
				{
					var pos = new Vec3((MyRndGen.RndFlt()*TileSize+x*TileSize)+5,
                                       (MyRndGen.RndFlt()*TileSize+y*TileSize)+5,
									   0);
					circles.push(new Circle(pos, MyRndGen.RndFlt()*4));
				}
			}
		}
		
		
		for (i in 0...Objs.length)
		{
			var pos=new Vec3(i%Width,Math.floor(i/Width),-1);
			for (c in circles)
			{
				if (c.Inside(pos)) pos.z=0;
			}

            // seed the rng for this position
			MyRndGen.Seed(cast((pos.x%5)*236+(pos.y%5)*139,Int));			
			Objs[i].LogicalPos=pos;
			Objs[i].UpdateTex(MyRndGen);
            Objs[i].Update(0,this);
		}

        MovePlants(new Vec2(dir.x*5,dir.y*5));		
        SortScene();
	}

	public function AddServerPlant(pos:Vec3,type,FruitID)
	{        
        if (MyName!=null && SpaceClear(pos))
        {
            var size=MyRndGen.RndFlt()+0.5;
            // find the server tile and relative position
            // from the client tile position
            var ServerTileWidth:Int=5;
            var PlantPosX:Int = cast(pos.x,Int)%ServerTileWidth;
            var PlantPosY:Int = cast(pos.y,Int)%ServerTileWidth;
            var TilePosX:Int = cast(WorldPos.x,Int)+Math.floor(pos.x/ServerTileWidth)-1;
            var TilePosY:Int = cast(WorldPos.y,Int)+Math.floor(pos.y/ServerTileWidth)-1;

            Server.Request("make-plant/"+
                           Std.string(TilePosX)+"/"+
                           Std.string(TilePosY)+"/"+
                           Std.string(PlantPosX)+"/"+
                           Std.string(PlantPosY)+"/"+
                           type+"/"+
                           MyID+"/"+
                           Math.round(size*100)+"/"+
                           Std.string(FruitID),
            this, function (c,data) {});       
        }
	}

    override public function PostSortScene(depth:Int)
    {
        Seeds.SortScene(depth);
    }

    public function ClearPlants() : Void
    {
        for (plant in Plants)
        {
            Remove(plant);
        }
        Plants = [];
	}

    public function MovePlants(dir:Vec2) : Void
    {
        var NewPlants=[];
        for (plant in Plants)
        {
            plant.LogicalPos.x+=-dir.x;
            plant.LogicalPos.y+=-dir.y;
            if (plant.LogicalPos.x<0 || plant.LogicalPos.y<0 ||
                plant.LogicalPos.x>Width || plant.LogicalPos.y>Width)
            {
                Remove(plant);
            }
            else
            {
                plant.Update(0,this);
                NewPlants.push(plant);
            }
        }
        Plants=NewPlants;
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
        // we get a list of tiles
        var tiles:Array<Dynamic>=cast(d,Array<Dynamic>);

        // a client tile is composed of 9 server tiles:
        //  ###
        //  ### <- central tile is the current one
        //  ###

        for (tile in tiles)
        {
            
            Season=tile.season;
            // find the relative tile position
            var TilePos=new Vec2(((tile.pos.x-WorldPos.x)+1)*5,
                                 ((tile.pos.y-WorldPos.y)+1)*5);
           
            var plants:Array<Dynamic>=cast(tile.entities,Array<Dynamic>);
            for (plant in plants)
            {
                // offset the plant to find the client tile position
                var WorldPos = new Vec2(plant.pos.x+TilePos.x,
                                        plant.pos.y+TilePos.y);
                // check for plant already there (shouldn't happen, but...)
                var e = Get("fungi.Plant",WorldPos);
                if (e==null)
                {
                    // check there is a ground cube there
                    var cube = Get("fungi.Cube",WorldPos);
                    if (cube!=null)
                    {
                        var pos = new Vec3(WorldPos.x,WorldPos.y,cube.LogicalPos.z+1);   
                        Plants.push(new Plant(this,plant,pos,tile.pos));
                    }
                }
                else
                {
                    // update the existing plant
                    cast(e,Plant).StateUpdate(this,plant);
                }
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
        //SetScale(new Vec2(2,2));
    }

    public function AddSpiritMsg(msg,text)
    {
        for (s in Spirits)
        {
            if (s.Name==msg.from)
            {
                s.AddMsg(msg,text);
            }
        }
    }

    override function Update(time:Int)
    {
        super.Update(time);

        Server.Update();
        TheCritters.Update();

        if (SpiralScale>0.1)
        {
            Spiral.Hide(false);
            SpiralScale-=0.01;
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
            if (MyName=="")
            {
                Server.Request("get-msgs/"+Std.string(MyID),this,
                               function(c,d){ c.NewsFeed.Update(cast(c,World),d);});
            }
            else
            {
                Server.Request("player/"+Std.string(MyID), this, 
                               function(c,d){c.PlayerInfo=d;
                                             c.NewsFeed.Update(cast(c,World),d.log.msgs);});
            }
            // todo: get-msgs or player - same info within

            if (MyName=="")
            {        
                try 
                {
                    var data = flash.external.ExternalInterface.call("game.get");
                    if (data.name!="")
                    {
                        Server.Request("login/"+data.name+"/"+data.id,
                                       this,
                                       function (c,d:Dynamic)
                                       {
                                           c.PlayerInfo=d;
                                           // todo: remove MyID, MyName
                                           c.MyID=c.PlayerInfo.id;
		                                   c.MyName=c.PlayerInfo.name;
		                                   c.removeChild(c.MyTextEntry);
                                       });
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
