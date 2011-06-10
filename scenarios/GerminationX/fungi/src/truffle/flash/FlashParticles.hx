// t r u f f l e Copyright (C) 2010 FoAM vzw   \_\ __     /\
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

package truffle.flash;
import flash.display.MovieClip;
import flash.display.Sprite;
import truffle.Vec2;
import truffle.Vec3;
import truffle.RndGen;

class FlashParticles extends MovieClip
{
    public var Pos:Vec2;         // emitter position
    var Rnd:RndGen;
    public var Positions:Array<Vec2>;
    public var Velocities:Array<Vec2>;
    public var Colours:Array<Vec3>;
    public var RecycleRate:Int;
    public var Sprites:Array<flash.display.Sprite>;

	public function new(pos:Vec2,count:Int) 
	{
        super();
        Pos = pos;
        Rnd = new RndGen();
        Positions = [];
        Velocities = [];
        Colours = [];
        RecycleRate = 80;
        Sprites = [];

        for (i in 0...count)
        {
            Positions.push(pos);
            Velocities.push(new Vec2(2*(Rnd.RndFlt()*2-1),2*(Rnd.RndFlt()*2-1)));
            Colours.push(new Vec3(Rnd.RndFlt(),Rnd.RndFlt(),Rnd.RndFlt()));
            var s=new flash.display.Sprite();

            s.graphics.clear();
		    s.graphics.beginFill(0xffff00,0.5);
            s.graphics.drawEllipse(0,0,10,10);
		    s.graphics.endFill();
            addChild(s);
           
            Sprites.push(s);
        }

        ColourTripleToInt(new Vec3(1,0.5,0.2));
	}

    function ColourTripleToInt(col:Vec3) : Int
    {
        var r:Int = Std.int(col.x*255)<<16;
        var g:Int = Std.int(col.y*255)<<8;
        var b:Int = Std.int(col.z*255);
        return r|g|b;
    }

	public function Update(frame:Int)
	{
        for (i in 0...Sprites.length)
        {
            Positions[i]=Positions[i].Add(Velocities[i]);
            Sprites[i].x=Positions[i].x;
            Sprites[i].y=Positions[i].y;
        }
        if (Rnd.RndInt()%100<RecycleRate)
        {
            var i=Rnd.RndInt()%Sprites.length;

            Positions[i]=Pos;
            Sprites[i].graphics.clear();
		    Sprites[i].graphics.beginFill(ColourTripleToInt(Colours[i]),0.5);
            Sprites[i].graphics.drawEllipse(0,0,10,10);
		    Sprites[i].graphics.endFill();
        }
	}
}
