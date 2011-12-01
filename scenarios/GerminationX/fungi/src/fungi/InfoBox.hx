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
import truffle.RndGen;
import truffle.Vec2;
import truffle.Vec3;

class InfoBox
{
    var Rnd:RndGen;

    // todo, take options for more buttons
    public function new(world:World,HTMLText:String,x,y,w,h,OnClose,AdditionalSprite)
    {
        if (AdditionalSprite!=null)
        {
            AdditionalSprite.Colour=new Vec3(0.55,0.85,0.95);
            AdditionalSprite.Update(0,null);
            world.AddSprite(AdditionalSprite);
        }

        Rnd = new RndGen();
        var f=new Frame("",x,y,w,h);
        f.R=0.55;
        f.G=0.83;
        f.B=0.95;        
        f.SetTextSize(10);
        f.UpdateHTMLText(HTMLText);
        world.AddSprite(f);
        
        f.InitTextures(GUIFrameTextures.Get(),Rnd);

        var b=new Frame("Ok",x+w/2,y+h-35,50,20);
        b.R=1;
        b.G=1;
        b.B=0.8;
        b.SetTextSize(10);
       // b.InitTextures(GUIFrameTextures.Get(),Rnd);
        world.AddSprite(b);

        // todo - deal with the options and call server
        b.MouseDown(this,function(c) {
            OnClose();
            world.RemoveSprite(f);
            world.RemoveSprite(b);
            if (AdditionalSprite!=null)
            {
                world.RemoveSprite(AdditionalSprite);
            }
        });
    }
}
