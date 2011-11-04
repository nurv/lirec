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

class Feed
{
    var Blocks:Array<Frame>;
    var Icons:Array<Sprite>;
    var MaxStories:Int;
    var Rnd:RndGen;
    var Info:Frame;
    var TopItem:Dynamic;
    var StrMkr:StringMaker;
    var EmotionIndices:Dynamic;
    var Fruit:Array<Dynamic>;
    var FruitSprites:Array<Sprite>;
    var PickPower:Sprite;
    var Flowered:Sprite;
    var NoteActive:Bool;
    var NoteFrames:Array<Frame>;

    public function new(w:World)
    {
        Blocks = [];
        Icons = [];
        MaxStories=5;
        Rnd=new RndGen();
        TopItem={};
        StrMkr=new StringMaker();
        Fruit=[];
        FruitSprites=[];
        NoteActive=false;
        NoteFrames=[];

        EmotionIndices={LOVE:0,HATE:1,HOPE:2,FEAR:3,SATISFACTION:4,
                        RELIEF:5,FEARS_CONFIRMED:6,DISAPOINTMENT:7,
                        JOY:8,DISTRESS:9,HAPPY_FOR:10,PITTY:11,
                        RESENTMENT:12,GLOATING:13,PRIDE:14,SHAME:15,
                        GRATIFICATION:16,REMORSE:17,ADMIRATION:18,
                        REPROACH:19,GRATITUDE:20,ANGER:21};

        Info=new Frame("",120,25,64*5,64);
        Info.SetTextSize(12);
        Info.UpdateText("Loading...");
        Info.InitTextures(GUIFrameTextures.Get(),Rnd);
        Info.R=1;
        Info.G=1;
        Info.B=0.8;
        w.AddSprite(Info);

        PickPower = new Sprite(new Vec2(340,50),Resources.Get("pp05"),false,false);
        PickPower.Colour=new Vec3(1,0,0);
        w.AddSprite(PickPower);
        PickPower.Update(0,null); // make the colour take effect

        Flowered = new Sprite(new Vec2(395,50),Resources.Get("flowered-cover-00"),false,false);
        Flowered.Colour=new Vec3(1,0,0);
        w.AddSprite(Flowered);
        Flowered.Update(0,null);
    }

    static function IntToColourTriple(col:Int) : Vec3
    {
        return new Vec3((col >> 16 & 0xFF)/255.0,
		                (col >> 8 & 0xFF)/255.0,
		                (col & 0xFF)/255.0);
    }

    function MakeIcon(Pos:Vec2, Type:String, Icon:String, Colour:Vec3)
    {
        if (Type=="plant" || Type=="spirit")
        {
            var s=new Sprite(Pos,Resources.Get(""));
            s.LoadFromURL("images/icons/"+Icon+".png");

            if (Type=="spirit")
            {
                s.Colour=new Vec3(Colour.x,Colour.y,Colour.z);
                s.Update(0,null);
            }

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
            if (("String"==type)
                && Reflect.field(a,f)!=Reflect.field(b,f))
            {
                return false;
            }
        }
        return true;
    }

    function FruitEq(a:Array<Dynamic>, b:Array<Dynamic>) : Bool
    {
        if (a.length!=b.length) return false;
        for (i in 0...a.length)
        {
            if (a[i].id!=b[i].id) return false;
        }
        return true;
    }

    function GetLayerName(n:Int)
    {
        switch(n) {
        case 0: return "cover";
        case 1: return "shrub";
        case 2: return "tree";
        }
        return "all";
    }

    function BuildNote(w:World,Note)
    {
        var f=new Frame("",200,200,64*4,64*4);
        f.SetTextSize(10);
        f.UpdateHTMLText(StrMkr.NoteToString(w.MyName,Note));
        w.AddSprite(f);
        
        f.InitTextures(GUIFrameTextures.Get(),Rnd);

        var b=new Frame("Ok",300,400,100,64);
        b.SetTextSize(10);
        b.InitTextures(GUIFrameTextures.Get(),Rnd);
        w.AddSprite(b);
        NoteFrames=[f,b];

        // todo - deal with the options and call server
        b.MouseDown(this,function(c) {
            
            w.Server.Request("answer/"+
                             w.MyID+"/"+
                             Note.code+"/"+
                             "0", // take index from button
                             c,function (c,data) {});

            var Feed=cast(c,Feed);
            for (f in Feed.NoteFrames) 
            {
                w.RemoveSprite(f);
            }
            Feed.NoteFrames=[];
            Feed.NoteActive=false;
        });
    }

    function ProcessNotes(w:World,Notes:Array<Dynamic>)
    {
        if (!NoteActive)
        {
            for (Note in Notes)
            {
                if (!Note.answer)
                {
                    BuildNote(w,Note);
                    NoteActive=true;
                }
            }
        }
    }

    function UpdateTopBox(w:World) 
    {
        if (w.MyName=="")
        {
            Info.UpdateText("Welcome to Germination X, it is now "+w.Season+".");
        }
        else
        {
            PickPower.Hide(false);
            Flowered.Hide(false);

            PickPower.ChangeBitmap(Resources.Get("pp0"+Std.string(Reflect.field(w.PlayerInfo,"seeds-left"))));

            // pad zeros
            var Flowers=Std.string(Reflect.field(w.PlayerInfo,"flowered-plants").length+100).substr(1,3);
            Flowered.ChangeBitmap(
                Resources.Get("flowered-"+
                             GetLayerName(w.PlayerInfo.layer)+
                             "-"+ Flowers));
                
            Info.UpdateText("");

/*            Info.UpdateText(
                "Hello "+ w.MyName+" it is "+w.Season+", your layer is currently "+
                    GetLayerName(Std.parseInt(w.PlayerInfo.layer))+
                " and " +
                Std.string(Reflect.field
                           (w.PlayerInfo,
                            "flowered-plants").length)
                + " of your plants from this layer have flowered."
            ); */
            
            var fruit:Array<Dynamic>=w.PlayerInfo.seeds;
            fruit.reverse();
            if (!FruitEq(Fruit,fruit))
            {
                Fruit=fruit;
                for (s in FruitSprites) w.RemoveSprite(s);
                FruitSprites=[];
                
                var Pos=new Vec2(140,50);
                for (f in Fruit)
                {
                    var feed=this;
                    var s=new Sprite(Pos,Resources.Get(f.type+"-fruit-c"));
                    var ppx=Pos.x; // things you need to do when
                    var ppy=Pos.y; // closure captures refs :(
                    s.MouseDown(w,function(c)
                                {
                                    var ns = new Seed(new Vec2(ppx,ppy),f.type,f.id);
                                    ns.ChangeState("fruit-c");
                                    c.RemoveSprite(s);
                                    feed.FruitSprites.remove(s);
                                    c.Seeds.Add(cast(c,World),ns);
                                    c.AddSprite(ns.Spr);
                                    c.SortScene();
                                });
                    Pos.x+=30;
                    FruitSprites.push(s);
                    w.AddSprite(s);
                }
            }
        }
    }

    function BuildMessage(w:World, i:Dynamic, pos:Vec2)
    {
        Rnd.Seed(Std.int(i.time));
        var f=new Frame("",pos.x,pos.y,64*2,64*1);
        f.ExpandLeft=70;
        f.SetTextSize(10);
        
        f.UpdateText(StrMkr.MsgToString(i));
        
        var Colour = new Vec3(0.8,0.9,0.7);
        if (i.type=="spirit") Colour=Spirit.GetEmotionColour(i.emotion);
        
        f.R=Colour.x;
        f.G=Colour.y;
        f.B=Colour.z;
        
        f.InitTextures(GUIFrameTextures.Get(),Rnd);
        Blocks.push(f);
        w.AddSprite(f);
        
        var Icon=MakeIcon(new Vec2(pos.x-20,pos.y+32),
                          i.type, i.from, Colour);
        w.AddSprite(Icon);
        Icons.push(Icon);
        
        if (i.type=="spirit")
        {
            f.MouseUp(this,function(c){
                if (w.Seeds.Carrying())
                {
                    var Fruit=w.Seeds.Remove(w);
                    if (i.code == "your_plant_doesnt_like" ||
                        i.code == "i_am_detrimented_by" ||
                        i.code == "i_am_detrimental_to" ||
                        i.code == "i_am_benefitted_by" ||
                        i.code == "i_am_beneficial_to" ||
                        i.code == "needs_help" ||
                        i.code == "ive_asked_x_for_help")
                    {
                        w.Server.Request("gift/"+
                                         w.MyID+"/"+
                                         Fruit.ID+"/"+
                                         i.extra[1],
                                         c,function (c,data) {});
                        
                    }
                    else
                    {
                        w.Server.Request("offering/"+
                                         w.MyID+"/"+
                                         Fruit.ID+"/"+
                                         i.from,
                                         c,function (c,data) {});
                    }
                }
            });
        }
    }

    function Clear(w:World)
    {
        for (b in Blocks) w.RemoveSprite(b);
        Blocks=[];
        for (i in Icons) w.RemoveSprite(i);
        Icons=[];
    }

    function UpdateMessages(w:World, d:Array<Dynamic>)
    {
        if (d.length>0 && !MessagesEq(TopItem,d[0]))
        {
            TopItem=d[0]; 
            if (TopItem.type=="spirit")
            {
                w.AddSpiritMsg(TopItem,StrMkr.MsgToString(TopItem));
            }

            Clear(w);
            
            var pos=new Vec2(595,32);
            for (i in d) 
            {
                BuildMessage(w,i,pos);
                pos.y+=90;
            }
        }
    }

    public function UpdateMsgs(w:World,d:Dynamic)
    {
        UpdateTopBox(w);
        Rnd.Seed(0);
        Info.InitTextures(GUIFrameTextures.Get(),Rnd);
        UpdateMessages(w,d);
    }

    public function UpdateNotes(w:World,d:Dynamic)
    {
        ProcessNotes(w,d);
    }

    public function Update(w:World,d:Dynamic)
    {
        UpdateMsgs(w,d.msgs);
        UpdateNotes(w,d.notes);
    }

}