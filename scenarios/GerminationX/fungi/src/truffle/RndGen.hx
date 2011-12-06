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

package truffle;

class RndGen
{
	var State:Int;
	
	public inline function new()
	{
		State=0;
	}
	
	public inline function Seed(s:Int)
	{
		State=s;
        WarmUp();
	}

    public inline function GetSeed()
    {
        return State;
    }
	
    public inline function WarmUp()
    {
        for (i in 0...10)
        {
            RndInt();
        }
    }

	public inline function RndInt() : Int
	{
		State=cast(State*214013+2531011,Int);
		return cast(Math.abs(State),Int);
	}

    public inline function RndRange(lo:Int, hi:Int) : Int
    {
        return lo+RndInt()%(hi-lo);
    }
	
	public inline function RndFlt() : Float
	{
		return RndInt()/Math.pow(2,32)*2;
	}

    public inline function RndCentredFlt() : Float
    {
        return (RndFlt()-0.5)*2;
    }

    public inline function Choose(arr:Array<Dynamic>) : Dynamic
    {
        return arr[RndInt()%arr.length];
    }
	
    public inline function RndVec2() : Vec2
    {
        return new Vec2(RndFlt(),RndFlt());
    }

    public inline function RndCentredVec2() : Vec2
    {
        return new Vec2(RndCentredFlt(),
                        RndCentredFlt());
    }

    public inline function RndCircleVec2() : Vec2
    {
        var v:Vec2=RndCentredVec2();
        while (v.Mag()>1)
        {
            v=RndCentredVec2();
        }
        return v;
    }

}
