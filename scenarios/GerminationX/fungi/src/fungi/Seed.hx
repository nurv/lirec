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

class Seed
{
    public var Type:String;
    public var State:String;
    public var Spr:Sprite;
    public var ID:Int;

    public function new(pos:Vec2,t:String,id:Int)
    {
        Type=t;
        State="fruit-a";
        ID=id;
        Spr=new Sprite(pos,Resources.Get(Type+"-"+State));
    }

    public function ChangeState(s:String)
    {
        State=s;
        Spr.ChangeBitmap(Resources.Get(Type+"-"+State));
    }
}
