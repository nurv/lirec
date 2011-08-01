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
    var Seeds:Array<Sprite>;
    var Layer:String;

	public function new(world:World, id:Int, owner:String, pos, type:String, state:String, fruit:Bool, layer:String)
	{
		super(world,pos,Resources.Get(type+"-"+state),false);
        Id=id;
        PlantType=type;
        State=state;
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
        State=state;
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
        for (seed in Seeds)
        {
            seed.Update(frame,Spr.Transform);
        }
	}

    public function Fruit(world:World)
    {
        var f=new Sprite(new Vec2(0,-Spr.Height/2),
                         Resources.Get("seed"));
        world.AddSprite(f);
        Seeds.push(f);
        f.MouseDown(this,function(p) 
        {            
            if (world.MyName!="")
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
                            c.RemoveSprite(f);
                            // add to the current list
                            var s=new Seed(p.PlantType);
                            c.Seeds.Add(cast(c,World),s);
                        }
                    });
            }
        });
    }
}
