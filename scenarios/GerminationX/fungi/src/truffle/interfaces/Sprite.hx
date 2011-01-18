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

import truffle.Vec2;
import truffle.interfaces.TextureDesc;

interface Sprite
{
    public var Pos:Vec2;
    public var Width:Int;
    public var Height:Int;

	public function MouseDown(c:Dynamic, f:Dynamic -> Void=null) : Void;
	public function MouseUp(c:Dynamic, f:Dynamic -> Void=null) : Void;
	public function MouseOver(c:Dynamic, f:Dynamic -> Void=null) : Void;
	public function ChangeBitmap(t:TextureDesc) : Void;

    public function SetPos(s:Vec2) : Void;
	public function SetScale(s:Vec2) : Void;
	public function SetRotate(angle:Float) : Void;	
    public function SetDepth(s:Int) : Void;
    public function GetDepth(): Int;
    public function CentreMiddleBottom(s:Bool) : Void;
    public function Hide(s:Bool) : Void;

	public function Update(frame:Int, tx:Dynamic) : Void;

    public function GetTx() : Dynamic;
    public function LoadFromURL(url:String) : Void;
}

