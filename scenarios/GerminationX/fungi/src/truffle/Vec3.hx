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

class Vec3 
{
	public var x:Float;
	public var y:Float;
	public var z:Float;
	
	public inline function new(px:Float, py:Float, pz:Float)
	{
		x=px; y=py; z=pz;
	}
	
 	public inline function Add(other:Vec3) : Vec3
	{
		return new Vec3(x+other.x,y+other.y,z+other.z);
	}

	public inline function Sub(other:Vec3) : Vec3
	{
		return new Vec3(x-other.x,y-other.y,z-other.z);
	}

 	public inline function Mul(v:Float) : Vec3
	{
		return new Vec3(x*v,y*v,z*v);
	}

 	public inline function Div(v:Float) : Vec3
	{
		return new Vec3(x/v,y/v,z/v);
	}
	
	public inline function Mag() : Float
	{
		return Math.sqrt(x*x+y*y+z*z);
	}

    public inline function Normalise() : Vec3
    {
        return Div(Mag());
    }
	
	public inline function Lerp(other:Vec3,t:Float) : Vec3
	{
		return new Vec3(x*(1-t) + other.x*t,
						y*(1-t) + other.y*t,
						z*(1-t) + other.z*t);
	}

	public inline function Eq(other:Vec3) : Bool
	{
		return x==other.x && y==other.y && z==other.z;
	}

    public inline function AsStr()
    {
        return Std.string(x)+", "+Std.string(y)+", "+Std.string(z);
    }
}
