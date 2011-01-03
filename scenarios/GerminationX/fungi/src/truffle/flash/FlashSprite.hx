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
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.events.MouseEvent;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.geom.Matrix;
import flash.geom.Point;
import flash.net.URLRequest;
import flash.display.Loader;
import truffle.Vec2;
import truffle.interfaces.Sprite;
import truffle.interfaces.World;
import truffle.interfaces.TextureDesc;

class FlashSprite implements truffle.interfaces.Sprite, extends flash.display.Sprite
{	
    public var Pos:Vec2;
    var Angle:Float;
    var MyScale:Vec2;
    var Transform:Matrix;
    var Width:Int;
    var Height:Int;

	public function new(pos:Vec2, t:TextureDesc) 
	{
		super();
        ChangeBitmap(t);
        Pos=pos;
        Angle=0;
        MyScale = new Vec2(1,1);
        Transform = new Matrix();
        Width=64;
        Height=112;
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

    public function LoadFromURL(url:String)
    {
        var loader:Loader = new Loader();
        loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, 
                                                  function(e:IOErrorEvent):Void 
                                                  {  
                                                      trace(e.text+' '+url);
                                                  });

        loader.contentLoaderInfo.addEventListener(Event.COMPLETE, ImageLoaded);
        loader.load(new URLRequest(url)); 
    }
    
    function ImageLoaded(e:Event)
    {        
        e.target.content.smoothing = true;
        var dupBitmap:Bitmap = new Bitmap(cast(e.target.content,Bitmap).bitmapData);
        Width=cast(dupBitmap.width,Int);
        Height=cast(dupBitmap.height,Int);
		graphics.clear();
		graphics.beginBitmapFill(dupBitmap.bitmapData);
        graphics.drawRect(0,0,dupBitmap.width,dupBitmap.height);
		graphics.endFill();   
    }

    /*public function ScreenPos() 
    { 
        var p:Point = Transform.transformPoint(new Point(0, 0)); 
        return new Vec3(p.x,p.y,0);
    }*/

    public function SetPos(s:Vec2) { Pos=s; }
	public function SetScale(s:Vec2) { MyScale=s; }
	public function SetRotate(angle:Float) { Angle=angle; }
    public function GetTransform() : Dynamic { return Transform; }

	public function Update(frame:Int, world:World, tx:Dynamic)
	{
        Transform.identity();

		var cx=Width/2;
		var cy=Height/2;

        Transform.translate(-cx, -cy);
		Transform.rotate(Angle*(Math.PI/180));
        Transform.scale(MyScale.x,MyScale.y);
        Transform.translate(cx, cy);
        Transform.translate(Pos.x,Pos.y);
		
        if (tx!=null)
		{
            Transform.concat(tx);
        }
		
        transform.matrix = Transform;
	}

}
