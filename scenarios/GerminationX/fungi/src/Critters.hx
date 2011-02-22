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
import truffle.Vec3;
import truffle.Vec2;
import truffle.RndGen;
import truffle.Entity;
import truffle.SkeletonEntity;
import truffle.SpriteEntity;
import truffle.Bone;

class Butterfly extends SkeletonEntity
{
    var LeftWing:Bone;
    var RightWing:Bone;

    public function new(world:World,pos:Vec3)
    {
	    super(world,pos);
        Hide(true);
        NeedsUpdate=true;
        UpdateFreq=5;
        Speed=0.1;

        LeftWing = new Bone(new Vec2(0,0), Resources.Get("wing"));
        Root = LeftWing;
        LeftWing.Centre=new Vec2(11,9);
        world.AddSprite(LeftWing);
        RightWing = new Bone(new Vec2(0,0), Resources.Get("wing"));
        RightWing.SetScale(new Vec2(-1,1));
        RightWing.Centre=new Vec2(11,9);
        LeftWing.AddChild(world,RightWing);
    }

    override function Update(frame:Int,world:World)
    {
        var Rnd=world.MyRndGen;   

        var Rot = Rnd.RndRange(-45,45);
        LeftWing.SetRotate(Rot+Rnd.RndRange(-10,10));
        RightWing.SetRotate(Rot*2);

        // random walk
        LogicalPos = LogicalPos.Add(new Vec3(Rnd.RndRange(-1,2),
                                             Rnd.RndRange(-1,2),0));

        if (LogicalPos.x < 0 ||
            LogicalPos.y < 0 ||
            LogicalPos.x > 15 ||
            LogicalPos.y > 15)
        {
            Hide(true);
        }

        super.Update(frame,world);
    }

}

class Bug extends SpriteEntity
{
    public function new(world:World,pos:Vec3)
    {
	    super(world,pos,Resources.Get(""));
        Hide(true);
        NeedsUpdate=true;
        UpdateFreq=3;
        Speed=0.1;
    }

    override function Update(frame:Int,world:World)
    {
        var Rnd=world.MyRndGen;   
        
        if (Rnd.RndInt()%10==0)
        {
            // random walk
            LogicalPos = LogicalPos.Add(new Vec3(Rnd.RndRange(-1,2),
                                                 Rnd.RndRange(-1,2),0));

            var cube = world.Get("Cube",new Vec2(LogicalPos.x,LogicalPos.y));
            if (cube!=null)
            {
                LogicalPos.z=cube.LogicalPos.z+1;   
            }

            if (LogicalPos.x < 0 ||
                LogicalPos.y < 0 ||
                LogicalPos.x > 15 ||
                LogicalPos.y > 15)
            {
                Hide(true);
            }
        }

        super.Update(frame,world);
    }

}


class Critters
{
    var CritterList:List<Entity>;
    var Rnd:RndGen;

    public function new(world:World,numcritters:Int)
    {
        CritterList=new List<Entity>();
        Rnd = new RndGen();

        for(i in 0...numcritters)
        {
            var critter = new Butterfly(world,new Vec3(0,0,4));
            CritterList.push(critter);
            /*var critter2 = new Bug(world,new Vec3(0,0,4));
            CritterList.push(critter2);*/
        }
    }

    public function Update()
    {
        for (c in CritterList)
        {
            if (c.Hidden && Rnd.RndInt()%1000==0) 
            {
                c.LogicalPos.x=Rnd.RndInt()%15;
                c.LogicalPos.y=Rnd.RndInt()%15;
                c.Hide(false);
            }
        }
    }

}
