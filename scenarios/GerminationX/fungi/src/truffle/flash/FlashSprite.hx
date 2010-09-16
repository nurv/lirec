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

import flash.display.Sprite;
import flash.display.BitmapData;
import flash.events.MouseEvent;
import flash.geom.Matrix;
import flash.geom.Point;

import truffle.Vec3;
import truffle.interfaces.Sprite;
import truffle.interfaces.World;
import truffle.interfaces.TextureDesc;

class FlashSprite implements truffle.interfaces.Sprite, extends flash.display.Sprite
{	
    public var ScreenPos:Vec3;

	public function new(pos:Vec3, t:TextureDesc) 
	{
		super();
        ScreenPos=pos;
        ChangeBitmap(t);
	}

	public function MouseDown(f:Dynamic -> Void=null)
	{
		addEventListener(MouseEvent.MOUSE_DOWN, f);
	}

	public function ChangeBitmap(t:TextureDesc)
	{
		graphics.clear();
		graphics.beginBitmapFill(cast(t,truffle.flash.FlashTextureDesc).data);
        graphics.drawRect(0,0,64,112);
		graphics.endFill();
	}

	public function Scale(size:Float)
	{
		var m:Matrix = transform.matrix;
		var x=32;
		var y=112;
		var p:Point = m.transformPoint(new Point(x, y));
		m.translate(-p.x, -p.y);
		m.scale(size,size);
	    m.translate(p.x, p.y);
		transform.matrix = m;
	}
	
	public function Rotate(angle:Float)
	{
		var m:Matrix = transform.matrix;
		var x=32;
		var y=112;
		var p:Point = m.transformPoint(new Point(x, y));
		m.translate(-p.x, -p.y);
		m.rotate(angle*(Math.PI/180));
		m.translate(p.x, p.y);
		transform.matrix = m;
	}

	public function Update(frame:Int, world:World)
	{
		x = ScreenPos.x;
		y = ScreenPos.y;
	}

}
