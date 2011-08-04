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

    function new()
    {
        super();
        Scene = [];
        CurrentTilePos=new Vec2(0,0); // perhaps
        ScreenScale=new Vec2(1,1);
        ScreenOffset=new Vec2(300,220);
    }

	public function ScreenTransform(pos:Vec3) : Vec3
	{
		// do the nasty iso conversion
		// this is actually an orthogonal projection matrix! (I think)
		return new Vec3(ScreenOffset.x+(pos.x*36-pos.y*26)*ScreenScale.x,
                        ScreenOffset.y+((pos.y*18+pos.x*9)-(pos.z*37))*ScreenScale.y,
                        pos.x*0.51 + pos.y*0.71 + pos.z*0.47);             
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
            setChildIndex(e.GetRoot(),i);
            e.OnSortScene(cast(this,truffle.World),i);
            i++;
        }
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
            e.Update(0,cast(this,truffle.World));
        }
    }

    public function Update(time)
    {
        for (e in Scene)
        {
            if (e.TilePos!=null)
            {
                e.Hide(!e.TilePos.Eq(CurrentTilePos));
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
