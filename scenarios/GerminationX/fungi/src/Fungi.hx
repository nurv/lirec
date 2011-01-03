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

import truffle.Truffle;
import truffle.interfaces.Key;

import truffle.Vec3;
import truffle.RndGen;
import truffle.Circle;
import truffle.Client;
import truffle.Entity;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Cube extends Entity 
{	
	public function new(pos:Vec3) 
	{
		super(pos, Resources.Get("blue-cube"));
	}
	
	public function UpdateTex(rnd:RndGen)
	{
		if (LogicalPos.z<0)
		{ 
			if (rnd.RndFlt()<0.5)
			{
				ChangeBitmap(Resources.Get("sea-cube-01"));
			}
			else
			{
				if (rnd.RndFlt()<0.5)
				{
					ChangeBitmap(Resources.Get("sea-cube-02"));
				}
				else
				{
					ChangeBitmap(Resources.Get("sea-cube-03"));
				}
			}
		}
		else 
		{
			if (rnd.RndFlt()<0.5)
			{
				ChangeBitmap(Resources.Get("grass-cube-01"));
			}
			else
			{
				if (rnd.RndFlt()<0.5)
				{
					ChangeBitmap(Resources.Get("grass-cube-02"));
				}
				else
				{
					ChangeBitmap(Resources.Get("grass-cube-03"));
				}
			}
		}
	}
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Plant extends Entity 
{
	public var Owner:String;
	var PlantScale:Float;
	public var Age:Int;

	public function new(owner:String,pos,bitmap,scale)
	{
		super(pos,bitmap);
		Owner=owner;
        PlantScale=0;
        if (scale)
        {
		    PlantScale=230;
            //Scale(0.1);
        }

        //var tf = new flash.text.TextField();
        //tf.text = Owner + " planted this.";
        //addChild(tf);
	}
	
	public override function Update(frame:Int, world:truffle.interfaces.World)
	{
		super.Update(frame,world);
        Age++;
		if (PlantScale>0)
		{
			//Scale(1.01);
			PlantScale--;
		}
	}
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Ghost extends Entity 
{
    public var Name:String;
    var Debug:flash.text.TextField;
    public var TexBase:String;
	
	public function new(name:String,pos)
	{
        TexBase="ghost-"+name.toLowerCase();
		super(pos,Resources.Get(TexBase));
        Name = name;

        Debug = new flash.text.TextField();
        Debug.wordWrap=true;
        Debug.y=-50;
        Debug.width=300;
        Debug.text = "nothing yet";
        addChild(Debug);
	}

    public function UpdateEmotions(e:Dynamic)
    {
        var ee = e.emotions.content;
        var mood=Std.parseFloat(ee[0].content[0]);
        if (mood>1) ChangeBitmap(Resources.Get(TexBase+"-happy"));
        else if (mood<-1) ChangeBitmap(Resources.Get(TexBase+"-sad"));
        else ChangeBitmap(Resources.Get(TexBase));

        Debug.text=Name+"\nMood:"+ee[0].content[0]+"\n";

        for (i in 1...ee.length)
        {
            Debug.text+=ee[i].attrs.type+" "+ee[i].attrs.direction+"\n";
            Debug.text+=ee[i].attrs.cause+"\n";
        }
    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class PlayerEntity extends Entity 
{
	public function new(pos:Vec3) 
	{
		super(pos, Resources.Get("player"));
	}
		
	public function Handle(e:Int, world:World)
	{
		var pos=new Vec3(LogicalPos.x,LogicalPos.y,LogicalPos.z);

		if (e==Keyboard.toInt(Key.left)) { pos.x-=1; /*ChangeBitmap(Resources.Get("rbot-north"));*/ }
		if (e==Keyboard.toInt(Key.right)) { pos.x+=1; /*ChangeBitmap(Resources.Get("rbot-south"));*/ }
		if (e==Keyboard.toInt(Key.up)) { pos.y-=1; /*ChangeBitmap(Resources.Get("rbot-east"));*/ }
		if (e==Keyboard.toInt(Key.down)) { pos.y+=1; /*ChangeBitmap(Resources.Get("rbot-west"));*/ }
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

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class FungiWorld extends World 
{
	public var Width:Int;
	public var Height:Int;
	var Objs:Array<Cube>;
	var Player:PlayerEntity;
	public var WorldPos:Vec3;
	var MyRndGen:RndGen;
	public var WorldClient:Client;
	public var MyTextEntry:TextEntry;
	var Plants:Array<Plant>;
	var MyName:String;
    var Frame:Int;
    var TickTime:Int;
    var Ghosts:Array<Ghost>;

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
		WorldPos = new Vec3(0,0,0);
		MyRndGen = new RndGen();
		WorldClient=new Client(OnServerPlantsCallback);
        MyName = "foo";

		for (y in 0...h)
		{
			for (x in 0...w)
			{
				var ob:Cube = new Cube(new Vec3(0,0,0));
                Add(ob);
				Objs.push(ob);
			}
		}

		UpdateWorld(new Vec3(0,0,0));
		
		Player = new PlayerEntity(new Vec3(5,5,1));
        Add(Player);

        var Names = ["Vertical","Canopy","Cover"];
        
        for (i in 0...3)
        {
            var g = new Ghost(Names[i],new Vec3(2,(i*3)+2,1));
            Add(g);
            Ghosts.push(g);
        }

//		MyTextEntry=new TextEntry(190,10,310,30,NameCallback);
//		addChild(MyTextEntry);	

        Update(0);
        SortScene();
	}
	
	public function NameCallback(name)
	{
		removeChild(MyTextEntry);
		WorldClient.Identify(name);
		MyName=name;
		//WorldClient.GetPlants(cast(WorldPos.x,Int),cast(WorldPos.y,Int));
	}
	
	public function UpdateWorld(pos:Vec3)
	{
		WorldPos=pos;
		
		var circles = [];
		
		for (i in Plants) Remove(i);
		Plants=[];
		
		for (x in -1...2)
		{
			for (y in -1...2)
			{
				MyRndGen.Seed(cast((WorldPos.x+x)+(WorldPos.y+y)*139,Int));
				
				for (i in 0...5)
				{
				    MyRndGen.RndFlt();
				    MyRndGen.RndFlt();
				    MyRndGen.RndFlt();
				    MyRndGen.RndFlt();
					var pos = new Vec3(MyRndGen.RndFlt()*10+x*10,
				                   MyRndGen.RndFlt()*10+y*10,
									  0);
									  
						  
					circles.push(new Circle(pos, MyRndGen.RndFlt()*5));
				}
			}
		}
		
		
		for (i in 0...Objs.length)
		{
			var pos=new Vec3(i%Width,Math.floor(i/Width),-1);
			MyRndGen.Seed(cast(WorldPos.x+pos.x+WorldPos.y+pos.y*139,Int));
			MyRndGen.RndFlt();
			MyRndGen.RndFlt();
			MyRndGen.RndFlt();
			MyRndGen.RndFlt();
	
			var inside:Bool=false;
			for (c in circles)
			{
				if (c.Inside(pos)) pos.z=0;
			}

			Objs[i].LogicalPos=pos;
			Objs[i].UpdateTex(MyRndGen);
		}
		
		//WorldClient.GetPlants(cast(WorldPos.x,Int),cast(WorldPos.y,Int));
	}
		
	public function GetCube(pos:Vec3) : Cube
	{
		return Objs[cast(pos.x+pos.y*Width,Int)];
	}
	
	public function PlantTex(i)
	{
		var l = ["flowers","canopy","climber","lollypop"];
        return Resources.Get(l[i]);
	}
	
	public function OnServerPlantsCallback(ServerPlants:Array<ServerPlant>)
	{
		for (splant in ServerPlants)
		{
			var plant = new Plant(splant.owner,new Vec3(splant.x,splant.y,1),PlantTex(splant.type),false);
			Plants.push(plant);
            Add(plant);
		}
        SortScene();
	}
	
    public function SpaceClear(pos:Vec3)
    {
        for (plant in Plants)
        {
            if (plant.LogicalPos==pos) return false;
        }
        return true;
    }

	public function AddServerPlant(pos:Vec3,type)
	{
        if (MyName!=null)
        {
		    // call by reference :S
		    var plant = new Plant(MyName,new Vec3(pos.x,pos.y,1),PlantTex(type),true);
		    Plants.push(plant);
            Add(plant);
		    //WorldClient.AddPlant(cast(WorldPos.x,Int), cast(WorldPos.y,Int), 
			//			         new ServerPlant(MyName,cast(pos.x,Int),cast(pos.y,Int),type));
        }
	}
	
   	override public function Handle(e:Int)
	{
        super.Handle(e);
		Player.Handle(e,this);
        Update(0);
        SortScene();
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
        if (time>TickTime)
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
        }

    }

}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class Fungi extends App
{
    public function new() 
	{
        super(new FungiWorld(10,10));
    }
	
    static function main() 
	{
        var m:Fungi = new Fungi();
    }
}
