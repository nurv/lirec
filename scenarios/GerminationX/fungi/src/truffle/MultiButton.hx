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

class MultiButton extends Button
{
	public var State:Int;
	public var Bitmaps:Array<BitmapData>;
	
	public function new(pos:Vec3, size:Vec3, bitmaps:Array<TextureDesc>) 
	{
		super(pos,size,bitmaps[0],Click);
		State=0;
		Bitmaps=bitmaps;
	}

	public function SetState(s:Int)
	{
		State=s;
		ChangeBitmap(Bitmaps[State]);
	}

	function Click(_)
	{
		State++;
		if (State>=Bitmaps.length)
		{
			State=0;
		}
		ChangeBitmap(Bitmaps[State]);
	}
}
