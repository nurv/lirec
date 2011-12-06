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

import haxe.Log;

import truffle.Truffle;
import truffle.Vec3;
import truffle.Vec2;
import truffle.RndGen;
import truffle.Entity;
import truffle.SkeletonEntity;
import truffle.SpriteEntity;
import truffle.Bone;

enum ButterflyState
{
    SearchingPlant;
    ApproachingPlant;
    ExaminePlant;
    Wait;
}

class Butterfly extends SkeletonEntity
{
    var LeftWing:Bone;
    var RightWing:Bone;
    var Rnd:RndGen;
    var Message:Frame;
    var MessageTime:Float;
    var Seed:Int;
    var State:ButterflyState;

    public function new(world:World,pos:Vec3,seed:Int)
    {
	    super(world,pos);
        NeedsUpdate=true;
        UpdateFreq=3;
        Speed=0.1;
        Rnd=new RndGen();
        Seed=seed;
        Rnd.Seed(seed);
        MessageTime=-1;
        State=SearchingPlant;

        Message = new Frame("",0,0,64*2,64);
        Message.SetTextSize(10);
        Message.InitTextures(GUIFrameTextures.Get(),Rnd);
        Message.R=1;
        Message.G=1;
        Message.B=0.8;
        world.AddSprite(Message);
        Message.Hide(true);

        LeftWing = new Bone(new Vec2(0,0), Resources.Get("wing"));
        Root = LeftWing;
        LeftWing.Centre=new Vec2(11,9);
        world.AddSprite(LeftWing);
        RightWing = new Bone(new Vec2(0,0), Resources.Get("wing"));
        RightWing.SetScale(new Vec2(-1,1));
        RightWing.Centre=new Vec2(11,9);
        LeftWing.AddChild(world,RightWing);

        var down=function(c) {
            c.OverridePos=true;
            c.LeftWing.EnableMouse(false);
            c.RightWing.EnableMouse(false);
            var ps=cast(world.Plants,Array<Dynamic>);
            for (p in ps)
            {
                p.EnableSelection();
            }
        };

        RightWing.MouseDown(this,down);
        LeftWing.MouseDown(this,down);
    }

    public function Drop(world:World)
    {
        if (OverridePos)
        {
            OverridePos=false;
            for (pp in cast(world.Plants,Array<Dynamic>))
            {
                pp.Spr.EnableMouse(false);
                pp.GotSelect=false;
            }
            LeftWing.EnableMouse(true);
            RightWing.EnableMouse(true);
        }
    }

    public function AddMsg(text)
    {
        Message.UpdatePosition(Math.floor(Pos.x-64),
                               Math.floor(Pos.y-100));
        Message.UpdateText(text);
        var s=Rnd.GetSeed();
        Rnd.Seed(Seed);
        Message.InitTextures(GUIFrameTextures.Get(),Rnd);
        Rnd.Seed(s);
        Message.Hide(false);    
        MessageTime=Date.now().getSeconds()+10;
    }

    // todo put the message stuff in the entity base class
    override function OnSortScene(world:World, order:Int) : Int
    {
        var order=super.OnSortScene(world,order);
        Message.SetDepth(order++);
        return order;
    }

    override public function UpdateMouse(x, y)
    {
        if (OverridePos)
        {
            Pos=new Vec3(x,y,0);
        }
//        Update(0,w);
    }

    override function Update(frame:Int,world:World)
    {
        if (!Message.IsHidden())
        {
            Message.UpdatePosition(Math.floor(Pos.x-64),
                                   Math.floor(Pos.y-100));
            var s=Rnd.GetSeed();
            Rnd.Seed(Seed);
            Message.InitTextures(GUIFrameTextures.Get(),Rnd);
            Rnd.Seed(s);
            if (Date.now().getSeconds()>MessageTime) 
            {
                Message.Hide(true);
                State=SearchingPlant;
            }
        }

        var Rot = Rnd.RndRange(-45,45);
        LeftWing.SetRotate(Rot+Rnd.RndRange(-10,10));
        RightWing.SetRotate(Rot*2);

        if (!OverridePos && Rnd.RndInt()%10==0)
        {            

            switch (State)
            {
            case SearchingPlant:
                {
                    if (world.Plants.length>0)
                    {
                        SetLogicalPos(world,Rnd.Choose(world.Plants).LogicalPos.Add(
                            new Vec3(0,0,3)));
                        State=ApproachingPlant;
                    }
                }
            case ApproachingPlant:
                {
                    if (MoveTime>1.0) // have we arrived?
                    {
                        State=ExaminePlant;
                    }
                }
            case ExaminePlant:
                {
                    var plant = world.Get("fungi.Plant",new Vec2(LogicalPos.x,LogicalPos.y));
                    if (plant!=null)
                    {
                        AddMsg("This " + plant.PlantType + " was planted by " + plant.OwnerName);
                        State=Wait;
                    }
                    else
                    {
                        State=SearchingPlant;
                    }
                }
            case Wait:
                {}
            }
        }

        if (OverridePos)
        {
            // poll the plants for a selection
            for (p in cast(world.Plants,Array<Dynamic>))
            {
                if (!p.Spr.IsMouseEnabled()) Drop(world);
                if (p.GotSelect)
                {
                    AddMsg("This " + p.PlantType + " was planted by " + p.OwnerName);
                    p.GotSelect=false;
                }
            }
        }
        
        super.Update(frame,world);
    }

}

class Bug extends SpriteEntity
{
    var Rnd:RndGen;

    public function new(world:World,pos:Vec3,seed:Int)
    {
	    super(world,pos,Resources.Get("spider-a"));
        NeedsUpdate=true;
        UpdateFreq=3;
        Speed=0.02;
        Rnd=new RndGen();
        Rnd.Seed(seed);
    }

    override function Update(frame:Int,world:World)
    {
//        if (Rnd.RndInt()%21==0)
        {
            Spr.ChangeBitmap(Resources.Get(
                Rnd.Choose(["spider-a",
                            "spider-b",
                            "spider-c"])));
        }
        
        if (Rnd.RndInt()%10==0)
        {
            // random walk
            var lp=LogicalPos.Add(new Vec3(Rnd.RndRange(-1,2),
                                           Rnd.RndRange(-1,2),0));
        
            var cube = world.Get("fungi.Cube",new Vec2(LogicalPos.x,LogicalPos.y));
            if (cube!=null)
            {
                lp.z=cube.LogicalPos.z+2;   
            }

            if (lp.x < 0) lp.x=0;
            if (lp.y < 0) lp.y=0;
            if (lp.x > 14) lp.x=14;
            if (lp.y > 14) lp.y=14;

            SetLogicalPos(world,lp);
        }

        super.Update(frame,world);
    }

}


class Critters
{
    var CritterList:List<Entity>;
    var ButterflyList:List<Butterfly>;
    var Rnd:RndGen;

    public function new(world:World,numcritters:Int)
    {
        CritterList=new List<Entity>();
        ButterflyList=new List<Butterfly>();
        Rnd = new RndGen();

        for(i in 0...numcritters)
        {
            var critter = new Butterfly(world,new Vec3(Rnd.RndInt()%15,Rnd.RndInt()%15,4), i);
            ButterflyList.push(critter);
            var critter2 = new Bug(world,new Vec3(Rnd.RndInt()%15,Rnd.RndInt()%15,1), i);
            CritterList.push(critter2);
        }
    }

    public function UpdateMouse(x,y)
    {
        for (c in ButterflyList)
        {
            c.UpdateMouse(x,y);
        }
    }

    public function DropButterfly(world:World)
    {
        for (c in ButterflyList)
        {
            c.Drop(world);
        }        
    }

    public function Update()
    {
        for (c in ButterflyList)
        {
            if (c.Hidden)
            {
         //       c.LogicalPos.x=Rnd.RndInt()%15;
         //       c.LogicalPos.y=Rnd.RndInt()%15;
                c.Hide(false);
            }
        }
    }

}
