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

import flash.display.MovieClip;
import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;
import flash.text.TextFieldType;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.display.Graphics;
import flash.display.Shape;
import flash.geom.Matrix;
import flash.geom.ColorTransform;

import truffle.interfaces.TextureDesc;
import truffle.interfaces.Frame;
import truffle.FrameTextures;
import truffle.RndGen;

class FlashFrame implements Frame, extends MovieClip
{
    var TextField:flash.text.TextField;
	var BG:Graphics;
    var Figures:Shape;
    var Textures:FrameTextures;
    var UsingTextures:Bool;
    var TextFormat:flash.text.TextFormat;
    public var R:Float;
    public var G:Float;
    public var B:Float;
    public var ExpandLeft:Int;
    var YOff:Int;
    var X:Int;
    var Y:Int;
    var Width:Int;
    var Height:Int;
    var MouseDownFunc:Dynamic -> Void;
    var MouseDownContext:Dynamic;
    var MouseUpFunc:Dynamic -> Void;
    var MouseUpContext:Dynamic;
    var MouseOverFunc:Dynamic -> Void;
    var MouseOverContext:Dynamic;
    var MouseOutFunc:Dynamic -> Void;
    var MouseOutContext:Dynamic;

	public function new(text,x,y,w,h)
	{
        super();
        Textures =  new FrameTextures();
        UsingTextures = false;
        ExpandLeft=0;
        EnableMouse(false);

        X=Std.int(x);
        Y=Std.int(y);
        Width=w;
        Height=h;
        YOff=0;

        TextField = new flash.text.TextField();
        TextField.text = text;
        TextField.x=x;
        TextField.y=y+YOff;
        TextField.height=h;
        TextField.width=w-7;
        TextField.background = false;
        //TextField.autoSize = flash.text.TextFieldAutoSize.LEFT;
        //TextField.backgroundColor = 0x8dd788;
        //TextField.border = true;
        TextField.wordWrap = true;
        TextField.selectable = false;

        TextFormat = new flash.text.TextFormat();
        TextFormat.font = "Verdana"; 
        TextFormat.size = 8;                
        TextFormat.color= 0x000000;  
        TextFormat.leading = -2;
        TextField.setTextFormat(TextFormat);
        R=1.0;
        G=1.0;
        B=1.0;
        
        Figures = new Shape();
        BG = Figures.graphics;
        BG.beginFill(0xffffff,1);
        BG.drawRect(X,Y,Width,Height);
        BG.endFill();
        Figures.visible=false;

        addChild(Figures);
        addChild(TextField);

        TextField.visible=true;
        Figures.visible=true;
 /*
        Root.MouseDown(c,function(c)
        {
            tf.visible=true;
            figures.visible=true;
        });

        Root.MouseOut(c,function(c)
        {
            tf.visible=false;
            figures.visible=false;
        });
*/

        cacheAsBitmap=true; // optimisation!!! - turn off if rotating
    }

    public function Hide(s:Bool) : Void
    {
        TextField.visible=!s;
        Figures.visible=!s;
    }

    public function IsHidden() : Bool
    {
        return !TextField.visible;
    }

    public function SetTextSize(s:Int)
    {
        TextFormat.size = s;                
    }

    public function UpdatePosition(x:Int,y:Int) : Void
    {
        TextField.x=x;
        TextField.y=y+YOff;
        X=x;
        Y=y;
        BG.clear();
        BG.beginFill(0xffffff,0.5);
        BG.drawRect(X,Y,Width,Height);
        BG.endFill();
    }

    public function UpdateText(text:String) : Void
    { 
        TextField.text=text;
        TextField.setTextFormat(TextFormat);
        BG.clear();
        BG.beginFill(0xffffff,0.5);
        BG.drawRect(X,Y,Width,Height);
        BG.endFill();
    }

    public function UpdateHTMLText(text:String) : Void
    { 
        TextField.htmlText=text;
        TextField.setTextFormat(TextFormat);
        BG.clear();
        BG.beginFill(0xffffff,0.5);
        BG.drawRect(X,Y,Width,Height);
        BG.endFill();
    }

    public function InitTextures(t:FrameTextures,r:RndGen)
    {
        YOff=-7;
        TextField.y=Y+YOff;
        Textures=t;
        UsingTextures=true;
        TextField.border = false;

        var TileSize=64;
        var XPos=X-(TileSize/2)-10-ExpandLeft;
        var YPos=Y-(TileSize/2)-10;
        var XCount=Std.int((Width+ExpandLeft)/TileSize)+1;
        var YCount=Std.int(Height/TileSize)+1;

        // dirty dirty hack
        if (ExpandLeft>0) XPos+=10;

        BG.clear();

        for (y in 0...YCount)
        {
            for (x in 0...XCount)
            {
                var tx=if (y==0)
                {
                    if (x==0) r.Choose(t.NW);
                    else if (x==XCount-1) r.Choose(t.NE);
                    else r.Choose(t.N);
                }
                else if (y==YCount-1)
                {
                    if (x==0) r.Choose(t.SW);
                    else if (x==XCount-1) r.Choose(t.SE);
                    else r.Choose(t.S);           
                }
                else if (x==0) r.Choose(t.W);                    
                else if (x==XCount-1) r.Choose(t.E);                    
                else null;
                
                if (tx!=null)
                {
                    var mtx:Matrix = new Matrix();
                    mtx.translate(XPos,YPos);
		            BG.beginBitmapFill(cast(tx,FlashTextureDesc).data,mtx);
                    BG.drawRect(XPos+x*TileSize, 
                                YPos+y*TileSize,
                                TileSize,TileSize);
                    BG.endFill();
                }
                else
                {
		            BG.beginFill(0xffffff,1);
                    BG.drawRect(XPos+x*TileSize,
                                YPos+y*TileSize,
                                TileSize,TileSize);
                    BG.endFill();
                }
            }
        }

        transform.colorTransform = new ColorTransform(R, G, B, 1, 0, 0, 0, 0);

    }

    public function EnableMouse(s:Bool)
    {
        mouseEnabled=s;
        mouseChildren=s;
    }

	public function MouseDown(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseDownFunc=f;
        MouseDownContext=c;
		addEventListener(MouseEvent.MOUSE_DOWN, MouseDownCB);
	}

    public function MouseDownCB(e)
    {
        MouseDownFunc(MouseDownContext);
    }

	public function MouseUp(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseUpFunc=f;
        MouseUpContext=c;
		addEventListener(MouseEvent.MOUSE_UP, MouseUpCB);
	}

    public function MouseUpCB(e)
    {
        MouseUpFunc(MouseUpContext);
    }

	public function MouseOver(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseOverFunc=f;
        MouseOverContext=c;
		addEventListener(MouseEvent.MOUSE_OVER, MouseOverCB);
	}

    public function MouseOverCB(e)
    {
        MouseOverFunc(MouseOverContext);
    }

	public function MouseOut(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseOutFunc=f;
        MouseOutContext=c;
		addEventListener(MouseEvent.MOUSE_OUT, MouseOutCB);
	}

    public function MouseOutCB(e)
    {
        MouseOutFunc(MouseOutContext);
    }

    public function SetDepth(s:Int)
    {
        parent.setChildIndex(this,s);
    }

}
