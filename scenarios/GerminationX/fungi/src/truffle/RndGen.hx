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
	
	public function new()
	{
		State=0;
	}
	
	public function Seed(s:Int)
	{
		State=s;
	}
	
	public function RndInt() : Int
	{
		State=cast(10331*State+1203432033,Int);
		return State;
	}
	
	public function RndFlt() : Float
	{
		return RndInt()/Math.pow(2,32)+0.5;
	}
	
}
