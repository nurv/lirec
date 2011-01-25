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

package truffle.interfaces;

import truffle.Entity;
import truffle.Vec2;

interface World  
{
    public function Add(e:Entity) : Void;
    public function Remove(e:Entity) : Void;
    public function Get(p:Vec2) : Dynamic;
    public function AddSprite(s:Sprite) : Void;
    public function RemoveSprite(s:Sprite) : Void;
	public function MouseDown(c:Dynamic, f:Dynamic -> Void=null) : Void;
    public function SortScene() : Void;
    public function Update(time:Int) : Void;
    public function Handle(event:Int) : Void;
}
