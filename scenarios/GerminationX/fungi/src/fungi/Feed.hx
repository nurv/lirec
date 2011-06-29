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
import truffle.FrameTextures;
import truffle.RndGen;

class Feed
{
    var Blocks:Array<Frame>;
    var MaxStories:Int;
    var TheFrameTextures:FrameTextures;
    var Rnd:RndGen;
    var Info:Frame;

    public function new(w:World)
    {
        Blocks = [];
        MaxStories=5;
        Rnd=new RndGen();

        TheFrameTextures = new FrameTextures();
        TheFrameTextures.N.push(Resources.Get("gui-n-001"));
        TheFrameTextures.N.push(Resources.Get("gui-n-002"));
        TheFrameTextures.N.push(Resources.Get("gui-n-003"));
        TheFrameTextures.NE.push(Resources.Get("gui-ne-001"));
        TheFrameTextures.NE.push(Resources.Get("gui-ne-002"));
        TheFrameTextures.E.push(Resources.Get("gui-e-001"));
        TheFrameTextures.E.push(Resources.Get("gui-e-002"));
        TheFrameTextures.E.push(Resources.Get("gui-e-003"));
        TheFrameTextures.E.push(Resources.Get("gui-e-004"));
        TheFrameTextures.SE.push(Resources.Get("gui-se-001"));
//        TheFrameTextures.SE.push(Resources.Get("gui-se-002"));
        TheFrameTextures.SE.push(Resources.Get("gui-se-003"));
        TheFrameTextures.S.push(Resources.Get("gui-s-001"));
        TheFrameTextures.S.push(Resources.Get("gui-s-002"));
//        TheFrameTextures.S.push(Resources.Get("gui-s-003"));
        TheFrameTextures.S.push(Resources.Get("gui-s-004"));
        TheFrameTextures.SW.push(Resources.Get("gui-sw-001"));
//        TheFrameTextures.SW.push(Resources.Get("gui-sw-002"));
        TheFrameTextures.SW.push(Resources.Get("gui-sw-003"));
        TheFrameTextures.W.push(Resources.Get("gui-w-001"));
        TheFrameTextures.W.push(Resources.Get("gui-w-002"));
        TheFrameTextures.W.push(Resources.Get("gui-w-003"));
        TheFrameTextures.W.push(Resources.Get("gui-w-004"));
        TheFrameTextures.NW.push(Resources.Get("gui-nw-001"));
        TheFrameTextures.NW.push(Resources.Get("gui-nw-002"));
        TheFrameTextures.NW.push(Resources.Get("gui-nw-003"));

        Info=new Frame("",300,25,64*5,64);
        Info.SetTextSize(12);
        Info.UpdateText("Loading...");
        //Info.InitTextures(TheFrameTextures,Rnd);
        Info.R=1;
        Info.G=1;
        Info.B=0.8;
        w.AddSprite(Info);
    }

    public function Update(w:World,d:Array<Dynamic>)
    {
        if (w.MyName=="")
        {
            Info.UpdateText("Welcome to Germination X");
        }
        else
        {
            Info.UpdateText("Hello "+w.MyName+", it is "+w.Season+" and you have "+Std.string(w.NumPlants)+" plants currently alive");
        }
        Rnd.Seed(0);
        Info.InitTextures(TheFrameTextures,Rnd);

        for (b in Blocks)
        {
            w.RemoveSprite(b);
        }
        Blocks=[];
        
        var pos=64;
        for (i in d)
        {
            Rnd.Seed(Std.int(i.time));
            var f=new Frame("",690+32,pos,64*3,64*2);
            f.SetTextSize(12);

            var subjects="";
            if (i.subjects.length>0)
            {
                subjects+=i.subjects[0]+" ";
            }

            f.UpdateText(i.from+" sent "+
                         Reflect.field(i,"msg-id")+
                         subjects+
                         " at "+Date.fromTime(i.time).toString());
            f.R=0.8;
            f.G=0.9;
            f.B=0.7;
            f.InitTextures(TheFrameTextures,Rnd);
            pos+=64*3;
            Blocks.push(f);
            w.AddSprite(f);
        }
    }
}