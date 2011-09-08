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
import truffle.Vec2;
import truffle.Vec3;
import truffle.SpriteEntity;
import truffle.RndGen;

class Plant extends SpriteEntity 
{
    public var Id:Int;
	public var Owner:Int;
	var PlantScale:Float;
	public var Age:Int;
    var Scale:Float;
    var PlantType:String;
    public var State:String;
    var Seeds:Array<Seed>;
    var Layer:String;
    var Star:Sprite;
    var Owned:Bool;
    var Rnd:RndGen;

    static var CentrePositions = {
        { 
            clover: new Vec2(0,-50),
            dandelion: new Vec2(0,-200),
            aronia: new Vec2(0,-120),
            apple: new Vec2(0,-140),
            cherry: new Vec2(0,-210)
        }};

    // because not all states are represented by graphics
    function FixState(state:String): String
    {
        if (state=="planted") return "grow-a";
        if (state=="fruit-a") return "grown";
        if (state=="fruit-b") return "grown";
        if (state=="fruit-c") return "grown";
        return state;
    }

	public function new(world:World, id:Int, owner:Int, pos, type:String, state:String, fruit:Int, layer:String)
	{
        State=FixState(state);
		super(world,pos,Resources.Get(type+"-"+State),false);
        Id=id;
        PlantType=type;
		Owner=owner;
        PlantScale=0;
        //NeedsUpdate=true;
        Seeds=[];
        Layer=layer;
        Owned=false;
        Spr.Hide(false);
        Rnd=new RndGen();
        Rnd.Seed(Id);

        for (i in 0...fruit) 
        {
            Fruit(world);
        }
	}

    public function StateUpdate(state,fruit,world:World)
    {
        State=FixState(state);
        if (State!="decayed")
        {
            Spr.ChangeBitmap(Resources.Get(PlantType+"-"+State));
        }

        // see if any seeds have been picked or arrived
        var FruitDiff = fruit-Seeds.length;
        if (FruitDiff!=0)
        {
            if (FruitDiff>0)
            {
                for (i in 0...FruitDiff) Fruit(world);
            }
            else
            {
                for (i in 0...-FruitDiff) Unfruit(world);
            }
        }

        if (state=="fruit-a" ||
            state=="fruit-b" ||
            state=="fruit-c")
        {
            for (s in Seeds)
            {
                s.ChangeState(state);
            }
        };

        if (!Owned && Owner==world.MyID)
        {
            Star = new Sprite(Reflect.field(CentrePositions,PlantType),
                              Resources.Get("star"));
            world.AddSprite(Star);
            Owned=true;
            Star.Update(0,Spr.Transform);
        }

    }

    override function Destroy(world:World)
    {
        super.Destroy(world);
        for (seed in Seeds)
        {
            world.RemoveSprite(seed.Spr);
        }
        if (Owned) world.RemoveSprite(Star);
    }
	
	public override function Update(frame:Int, world:World)
	{
		super.Update(frame,world);
        for (seed in Seeds)
        {
            seed.Spr.Update(frame,Spr.Transform);
        }
        
        if (Owned) Star.Update(frame,Spr.Transform);
	}

    override function OnSortScene(world:World, order:Int) : Int
    {
        Spr.SetDepth(order++);
        for (seed in Seeds)
        {
            world.setChildIndex(seed.Spr,order++);
        }        
        if (Owned) Star.SetDepth(order++);
        return order;
    }

    public function Fruit(world:World)
    {
        var Pos:Vec2=Reflect.field(CentrePositions,PlantType)
            .Add(Rnd.RndCircleVec2().Mul(32));
        var Fruit=new Seed(Pos,PlantType);
        world.AddSprite(Fruit.Spr);
        Seeds.push(Fruit);
        Update(0,world);
        Fruit.Spr.MouseDown(this,function(p) 
        {            
            if (world.MyName!="")// && f.State=="fruit-c")
            {
                // arsing around with the sprites to get
                // better feedback for the player
                p.Seeds.remove(Fruit);
                world.RemoveSprite(Fruit.Spr);

                world.Server.Request(
                    "pick/"+
                        Std.string(cast(world.WorldPos.x,Int))+"/"+
                        Std.string(cast(world.WorldPos.y,Int))+"/"+
                        Std.string(p.Id)+"/"+
                        Std.string(world.MyID),
                    world,
                    function (c,d) 
                    {
                        if (d.ok==true)
                        {
                            // make a brand new one
                            var ns = new Seed(Fruit.Spr.Pos,Fruit.Type);
                            ns.ChangeState("fruit-c");
                            c.Seeds.Add(cast(c,World),ns);
                            c.AddSprite(ns.Spr);
                            c.SortScene();
                        }
                    });
            }
        });
    }

    function Unfruit(world:World)
    {
        if (Seeds.length>0)
        {
            var seed:Seed=Rnd.Choose(Seeds);
            world.RemoveSprite(seed.Spr);
            Seeds.remove(seed);        
        }
    }
}
