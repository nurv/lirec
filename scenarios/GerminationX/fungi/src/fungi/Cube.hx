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
import truffle.SpriteEntity;
import truffle.Vec3;
import truffle.Vec2;
import truffle.RndGen;

class Cube extends SpriteEntity 
{	
	public function new(world:World, pos:Vec3) 
	{
		super(world, pos, Resources.Get("wire-cube"));
//        Spr.SetScale(new Vec2(2,2));
	}
	
	public function UpdateTex(rnd:RndGen)
	{
        Spr.ChangeBitmap(Resources.Get(
            rnd.Choose(["ground-cube-a-1",
                        "ground-cube-a-2",
                        "ground-cube-a-3",
                        "ground-cube-a-4",
                        "ground-cube-a-5"])));

/*
		if (LogicalPos.z%2==0)
        {
            Spr.ChangeBitmap(Resources.Get(
                rnd.Choose(["rock-cube-01","rock-cube-02","rock-cube-03"])));
		}
		else 
		{
            Spr.ChangeBitmap(Resources.Get(
                rnd.Choose(["rock-cube-04","rock-cube-05","rock-cube-06"])));
        }
*/
	}
}

