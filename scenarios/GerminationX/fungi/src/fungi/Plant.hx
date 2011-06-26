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
        Spr.MouseDown(this,function(c) { tf.visible=true; });
        Spr.MouseOut(this,function(c) { tf.visible=false; });

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
                    world.ActivatePlants(false);
                    world.RemoveSprite(f);
                    world.Server.Request("pick/"+
                                         Std.string(cast(world.WorldPos.x,Int))+"/"+
                                         Std.string(cast(world.WorldPos.y,Int))+"/"+
                                         Std.string(p.Id),
                                         p,
                                         function (c) {});
                }
            }
        });
    }
}
