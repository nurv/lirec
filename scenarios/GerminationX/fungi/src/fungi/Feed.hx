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
import truffle.Vec2;

class Feed
{
    var Blocks:Array<Frame>;
    var Icons:Array<Sprite>;
    var MaxStories:Int;
    var TheFrameTextures:FrameTextures;
    var Rnd:RndGen;
    var Info:Frame;
    var TopItem:Dynamic;

    public function new(w:World)
    {
        Blocks = [];
        Icons = [];
        MaxStories=5;
        Rnd=new RndGen();
        TopItem={};

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

    function MakeIcon(Pos:Vec2, Type:String, Icon:String)
    {
        if (Type=="plant" || Type=="spirit")
        {
            var s=new Sprite(Pos,Resources.Get(""));
            s.LoadFromURL("images/icons/"+Icon+".png");
            return s;
        }
        else if (Type=="player")
        {
            var s=new Sprite(Pos,Resources.Get(""));
            //s.LoadFromURL("http://graph.facebook.com/"+Icon+"/picture");
            return s;
        }
        else return new Sprite(Pos,Resources.Get("test"));
    }

    function MessagesEq(a:Dynamic, b:Dynamic) : Bool
    {
        if (Reflect.fields(a).length==0) return false;

        for (f in Reflect.fields(a))
        {
            var type=Type.getClassName(Type.getClass(Reflect.field(a,f)));
            // numbers == null type???
            if (("String"==type || type==null)
                && Reflect.field(a,f)!=Reflect.field(b,f))
            {
                return false;
            }
        }
        return true;
    }

    public function Update(w:World,d:Array<Dynamic>)
    {
        if (w.MyName=="")
        {
            Info.UpdateText("Welcome to Germination X");
        }
        else
        {
            var SeedsLeft=Reflect.field(w.PlayerInfo,"seeds-left");
            var txt="Hello "+w.MyName+", it is "+w.Season+" and you have "+Std.string(w.NumPlants)+" plants currently alive. "+
                "You have "+SeedsLeft+" seeds left.";
            var time=Std.parseInt(Reflect.field(w.PlayerInfo,"next-refresh"));
            
            if (time!=0)
            {
                var now=Date.now().getTime();
                var diff=Date.fromTime(time-now);
                txt+=" More seeds in "+diff.getMinutes()+" minutes.";
            }
            Info.UpdateText(txt);
        }

        Rnd.Seed(0);
        Info.InitTextures(TheFrameTextures,Rnd);

        if (!MessagesEq(TopItem,d[0]))
        {
            TopItem=d[0];
            
            for (b in Blocks)
            {
                w.RemoveSprite(b);
            }
            Blocks=[];
            
            for (i in Icons)
            {
                w.RemoveSprite(i);
            }
            Icons=[];
            
            var pos=32;
            var xpos=752;
            for (i in d)
            {
                Rnd.Seed(Std.int(i.time));
                var f=new Frame("",xpos,pos,64*3,64*1);
                f.ExpandLeft=70;
                f.SetTextSize(12);
                
                var subjects="";
                if (i.subjects.length>0)
                {
                    subjects+=i.subjects[0]+" ";
                }
                
                f.UpdateText(Reflect.field(i,"display-from")+" sent "+
                             Reflect.field(i,"msg-id")+
                             subjects+
                             " at "+Date.fromTime(i.time).toString());
                f.R=0.8;
                f.G=0.9;
                f.B=0.7;
                f.InitTextures(TheFrameTextures,Rnd);
                Blocks.push(f);
                w.AddSprite(f);
                
                var Icon=MakeIcon(new Vec2(xpos-30,pos+32),
                                  Reflect.field(i,"icon-type"),
                                  Reflect.field(i,"icon"));
                //            var Icon=MakeIcon(new Vec2(690+64,pos+110),
                //                              "player",
                //                              w.MyFBID);
                w.AddSprite(Icon);
                Icons.push(Icon);
                pos+=90;
            }
        }
    }
}