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
import flash.display.DisplayObject;
import flash.events.MouseEvent;
import truffle.interfaces.World;
import truffle.interfaces.Sprite;
import truffle.Entity;
import truffle.Truffle;
import truffle.Vec2;
import truffle.Vec3;

class FlashWorld implements World, extends MovieClip 
{
    var Scene:Array<Entity>;
    var MouseDownFunc:Dynamic -> Void;
    var MouseDownContext:Dynamic;
    var MouseUpFunc:Dynamic -> Void;
    var MouseUpContext:Dynamic;
    var MouseMoveFunc:Dynamic -> Void;
    var MouseMoveContext:Dynamic;
    var CurrentTilePos:Vec2;
    var ScreenScale:Vec2;
    var ScreenOffset:Vec2;
    var Theta:Float;
    var Alpha:Float;
    var CosTheta:Float;
    var CosAlpha:Float;
    var SinTheta:Float;
    var SinAlpha:Float;
    var ScreenCentre:Vec2;
    var Scale:Vec3;

    function new()
    {
        super();
        Scene = [];
        CurrentTilePos=new Vec2(0,0); // perhaps
        ScreenScale=new Vec2(1,1);
        ScreenCentre=new Vec2(-600,500);
        ScreenOffset=new Vec2(ScreenCentre.x,
                              ScreenCentre.y);
        
        Theta = -66*Math.PI/180;
        Alpha = 59*Math.PI/180;
        CosTheta = Math.cos(Theta);
        SinTheta = Math.sin(Theta);
        CosAlpha = Math.cos(Alpha);
        SinAlpha = Math.sin(Alpha);
        Scale = new Vec3(103,84,80);
    }

    public function ScreenSpaceTransform(pos:Vec3) : Vec3
	{
        var ox=pos.x*Scale.x;
        var oy=pos.y*Scale.y;
        var oz=pos.z*Scale.z;

        var zp=oz;
        var xp=ox*CosAlpha+oy*SinAlpha;
        var yp=oy*CosAlpha-ox*SinAlpha;

        var r= new Vec3(xp,
                        yp*CosTheta+zp*SinTheta,
                        zp*CosTheta-yp*SinTheta
                        );
        return r; 
	}

	public function ScreenTransform(pos:Vec3) : Vec3
	{
/*        var zp=pos.z*80;
        var xp=(pos.x*CosAlpha+pos.y*SinAlpha)*103;
        var yp=(pos.y*CosAlpha-pos.x*SinAlpha)*84;
*/
        var ox=pos.x*Scale.x;
        var oy=pos.y*Scale.y;
        var oz=pos.z*Scale.z;

        var zp=oz;
        var xp=ox*CosAlpha+oy*SinAlpha;
        var yp=oy*CosAlpha-ox*SinAlpha;

        var r= new Vec3(xp,
                        yp*CosTheta+zp*SinTheta,
                        zp*CosTheta-yp*SinTheta
                        );
        r.x+=ScreenOffset.x;
        r.y+=ScreenOffset.y;
        return r;
	}

	public function InverseScreenTransform(pos:Vec2) : Vec2
	{
        var ox=pos.x-ScreenOffset.x;
        var oy=pos.y-ScreenOffset.y;
 
        var xp=ox/CosAlpha+oy/SinAlpha;
        var yp=oy/CosAlpha-ox/SinAlpha;

        var r= new Vec2(xp,
                        yp/CosTheta
                        );

        r.x/=Scale.x;
        r.y/=Scale.y;
        return r;
	}

    public function Add(e:Entity)
    {
        Scene.push(e);
    }

    public function Remove(e:Entity)
    {
        e.Destroy(cast(this,truffle.World));
        Scene.remove(e);
    }

    public function Get(type:String, p:Vec2) : Dynamic
    {
        for (e in Scene)
        {
            if (p.x==e.LogicalPos.x &&
                p.y==e.LogicalPos.y &&
                Type.getClassName(Type.getClass(e))==type)
            {
                return e;
            }
        }
        return null;
    }

    public function GetOther(me:Entity, type:String, p:Vec2) : Dynamic
    {
        for (e in Scene)
        {
            if (p.x==e.LogicalPos.x &&
                p.y==e.LogicalPos.y &&
                Type.getClassName(Type.getClass(e))==type &&
                e!=me)
            {
                return e;
            }
        }
        return null;
    }

    public function AddSprite(s:Dynamic)
    {
        addChild(s);
    }

    public function RemoveSprite(s:Dynamic)
    {
        removeChild(s);
    }

	public function MouseDown(c:Dynamic, f:Dynamic -> Void=null)
	{
        MouseDownFunc=f;
        MouseDownContext=c;
		addEventListener(MouseEvent.MOUSE_DOWN, MouseDownCB);
	}

    public function MouseDownCB(e)
    {
        MouseDownFunc(e);
    }

	public function MouseUp(c:Dynamic, f:Dynamic -> Void=null)
	{
        MouseUpFunc=f;
        MouseUpContext=c;
		addEventListener(MouseEvent.MOUSE_UP, MouseUpCB);
	}

    public function MouseUpCB(e)
    {
        MouseUpFunc(MouseUpContext);
    }

	public function MouseMove(c:Dynamic, f:Dynamic -> Void=null)
	{
        MouseMoveFunc=f;
        MouseMoveContext=c;
		addEventListener(MouseEvent.MOUSE_MOVE, MouseMoveCB);
	}

    public function MouseMoveCB(e)
    {
        MouseMoveFunc(e);
    }

    // override for things on top
    public function PostSortScene(depth:Int)
    {
    }

    public function SortScene()
    {        
        Scene.sort(function(a:Entity, b:Entity)
                   {                       
                       if (a.Depth<b.Depth) return -1;
                       else return 1;
                   });
        var i=0;
        for (e in Scene)
        {
            i=e.OnSortScene(cast(this,truffle.World),i);
        }

        PostSortScene(i);
    }

    public function SetCurrentTilePos(s:Vec2) : Void
    {
        CurrentTilePos=s;
    }

    public function SetScale(amount)
    {
        ScreenScale=amount;
        for (e in Scene)
        {
            e.GetRoot().SetScale(amount);
            e.Update(0,cast(this,truffle.World));
        }
    }

    public function SetTranslate(amount)
    {
        ScreenOffset=amount;
        for (e in Scene)
        {
            e.SetLogicalPos(cast(this,truffle.World),e.LogicalPos);
            e.Update(0,cast(this,truffle.World));
        }
    }

    public function Update(time)
    {
        for (e in Scene)
        {
            if (e.TilePos!=null)
            {
                // the current tile pos is surrounded by 8 other
                // visible ones, so see if we are in one of those
                var diff=e.TilePos.Sub(CurrentTilePos);
                e.Hide(Math.abs(diff.x)>1 || Math.abs(diff.y)>1);
            }

            if (e.NeedsUpdate && !e.Hidden &&
                (e.UpdateFreq==0 ||
                (time % e.UpdateFreq)==0))
            {
                e.Update(time,cast(this,truffle.World));
            }
        }
    }

    public function Handle(event)
    {        
    }
    
}
