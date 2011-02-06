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

class Vec2 
{
	public var x:Float;
	public var y:Float;
	
	public inline function new(px:Float, py:Float)
	{
		x=px; y=py;
	}
	
	public inline function Add(other:Vec2) : Vec2
	{
		return new Vec2(x+other.x,y+other.y);
	}

	public inline function Sub(other:Vec2) : Vec2
	{
		return new Vec2(x-other.x,y-other.y);
	}

	public inline function Div(v:Float) : Vec2
	{
		return new Vec2(x/v,y/v);
	}

	public inline function Mul(v:Float) : Vec2
	{
		return new Vec2(x*v,y*v);
	}

	public inline function Mag() : Float
	{
		return Math.sqrt(x*x+y*y);
	}
	
	public inline function Lerp(other:Vec2,t:Float) : Vec2
	{
		return new Vec2(x*(1-t) + other.x*t,
						y*(1-t) + other.y*t);
	}

	public inline function Eq(other:Vec2) : Bool
	{
		return x==other.x && y==other.y;
	}

    public inline function AsStr()
    {
        return Std.string(x)+", "+Std.string(y);
    }
}
