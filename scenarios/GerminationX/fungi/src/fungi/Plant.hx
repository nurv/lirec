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
import truffle.SpriteEntity;

class Plant extends SpriteEntity 
{
    public var Id:Int;
	public var Owner:String;
	var PlantScale:Float;
	public var Age:Int;
    var Scale:Float;
    var PlantType:String;
    public var State:String;
    var Seeds:Array<Seed>;
    var Layer:String;

    // because not all states are represented by graphics
    function FixState(state:String): String
    {
        if (state=="planted") return "grow-a";
        if (state=="fruit-a") return "grown";
        if (state=="fruit-b") return "grown";
        if (state=="fruit-c") return "grown";
        return state;
    }

	public function new(world:World, id:Int, owner:String, pos, type:String, state:String, fruit:Bool, layer:String)
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
        Spr.Hide(false);
        if (fruit) Fruit(world);
	}

    public function StateUpdate(state,fruit,world:World)
    {
        State=FixState(state);
        if (State!="decayed")
        {
            Spr.ChangeBitmap(Resources.Get(PlantType+"-"+State));
        }
        if (fruit && Seeds.length==0)
        {
            Fruit(world);
        }
        if (!fruit && Seeds.length!=0)
        {
            // assume only one seed...
            world.RemoveSprite(Seeds[0]);
            Seeds=[];        
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
    }

    override function Destroy(world:World)
    {
        super.Destroy(world);
        for (seed in Seeds)
        {
            world.RemoveSprite(seed.Spr);
        }
    }
	
	public override function Update(frame:Int, world:World)
	{
		super.Update(frame,world);
        for (seed in Seeds)
        {
            seed.Spr.Update(frame,Spr.Transform);
        }
	}

    override function OnSortScene(world:World, order:Int) : Int
    {
        Spr.SetDepth(order++);
        for (seed in Seeds)
        {
            world.setChildIndex(seed.Spr,order++);
        }        
        return order;
    }

    public function Fruit(world:World)
    {
        var f=new Seed(new Vec2(0,-Spr.Height/2),PlantType);
        world.AddSprite(f.Spr);
        Seeds.push(f);
        Update(0,world);
        f.Spr.MouseDown(this,function(p) 
        {            
            if (world.MyName!="" && f.State=="fruit-c")
            {
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
                            // remove the seed now for faster feedback
                            // the server will maintain this state for 
                            // other players
                            p.Seeds.remove(f);
                            c.RemoveSprite(f.Spr);
                            // add to the current list
                            c.Seeds.Add(cast(c,World),f);
                        }
                    });
            }
        });
    }
}
