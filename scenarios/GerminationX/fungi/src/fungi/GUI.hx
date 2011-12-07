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

class GUI
{
    var Msgs:Array<Message>;
    var MaxStories:Int;
    var Rnd:RndGen;
    var Info:Frame;
    var TopItem:Dynamic;
    public var StrMkr:StringMaker;
    var EmotionIndices:Dynamic;
    public var Store:FruitStore;
    var PickPower:Sprite;
    var Flowered:Sprite;
    var NoteActive:Bool;
    var NoteFrames:Array<Frame>;
    var NotesRead:Array<String>;
    public var Instructions:Int;
    var BuiltText:Bool;

    public function new(w:World)
    {
        Msgs = [];
        MaxStories=5;
        Rnd=new RndGen();
        TopItem={};
        StrMkr=new StringMaker();
        Store=new FruitStore();
        NoteActive=false;
        NoteFrames=[];
        NotesRead=[];
        BuiltText=false;
        Instructions=0;

        EmotionIndices={LOVE:0,HATE:1,HOPE:2,FEAR:3,SATISFACTION:4,
                        RELIEF:5,FEARS_CONFIRMED:6,DISAPOINTMENT:7,
                        JOY:8,DISTRESS:9,HAPPY_FOR:10,PITTY:11,
                        RESENTMENT:12,GLOATING:13,PRIDE:14,SHAME:15,
                        GRATIFICATION:16,REMORSE:17,ADMIRATION:18,
                        REPROACH:19,GRATITUDE:20,ANGER:21};

        Info=new Frame("",120,25,64*5,64);
        Info.SetTextSize(12);
        Info.R=1;
        Info.G=1;
        Info.B=0.8;
        Info.UpdateText("Loading...");
        Info.InitTextures(GUIFrameTextures.Get(),Rnd);
        w.AddSprite(Info);

        PickPower = new Sprite(new Vec2(340,54),Resources.Get("pp05"),false,false);
        PickPower.Colour=new Vec3(1,0,0);
        w.AddSprite(PickPower);
        PickPower.Update(0,null); // make the colour take effect

        Flowered = new Sprite(new Vec2(395,54),Resources.Get("flowered-cover-00"),false,false);
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
        var c=this;
        var Box=new InfoBox(w,StrMkr.NoteToString(w.MyName,Note),
                            160,170,64*4,64*4,
                            function()
                            {
                                w.Server.Request("answer/"+
                                                 w.MyID+"/"+
                                                 Note.code+"/"+
                                                 "0", // take index from button
                                                 w,function (c,data) {});
                                if (Note.code=="welcome") c.Instructions=1;
                            },null);
    }

    function ProcessNotes(w:World,Notes:Array<Dynamic>)
    {
        if (!NoteActive)
        {
            for (Note in Notes)
            {
                // may take server a time to update the
                // answered field, so duplicate here
                var AlreadyRead=false;
                for (c in NotesRead)
                {
                    if (c==Note.code) AlreadyRead=true;      
                }

                if (!Note.answer && !AlreadyRead)
                {
                    BuildNote(w,Note);
                    NoteActive=true;
                    NotesRead.push(Note.code);
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

            if (!BuiltText)
            {
                var TextField = new flash.text.TextField();
                TextField.text = "Fruit Store                                                 Pick Power     Flowered";
                TextField.x=125;
                TextField.y=16;
                TextField.width=300;
                TextField.background = false;                
                TextField.selectable = false;
                var t = new flash.text.TextFormat();
                t.font = "Verdana"; 
                t.size = 8;                
                t.color= 0x000000;     
                TextField.setTextFormat(t);
                BuiltText=true;
                w.addChild(TextField);
            }

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
            Store.UpdateFruit(w,fruit);

            if (Instructions==1 && Store.HaveFruit())
            {
                Instructions=2;
                var c=this;
                var Box = new InfoBox(w,"Great! Now drag your fruit from here to an empty space in the world to plant it",
                                      100,140,64*3,64*1,
                                      function()
                                      {
                                          c.Instructions=3;
                                      },
                                      new Sprite(new Vec2(100+40,100),Resources.Get("arrup")));
            }
        }
    }

    function BuildMessage(w:World, i:Dynamic, pos:Vec2)
    {
        Msgs.push(new Message(w,i,pos,this));
    }

    function Clear(w:World)
    {
        for (m in Msgs)
        {
            w.RemoveSprite(m.Block);
            w.RemoveSprite(m.Icon);
        }
        Msgs=[];
    }

    function UpdateMessages(w:World, d:Array<Dynamic>,time:Int)
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

    public function ShakeSpiritMessages(w:World)
    {
        for (m in Msgs)
        {
            if (m.Type=="spirit") m.Shake(w.Time);
        }
    }

    public function UpdateMsgs(w:World,d:Dynamic,time:Int)
    {
        UpdateTopBox(w);
        Rnd.Seed(0);
        Info.InitTextures(GUIFrameTextures.Get(),Rnd);
        UpdateMessages(w,d,time);
    }

    public function UpdateNotes(w:World,d:Dynamic)
    {
        ProcessNotes(w,d);
    }

    public function UpdateData(w:World,d:Dynamic,time:Int)
    {
        UpdateMsgs(w,d.msgs,time);
        UpdateNotes(w,d.notes);
    }

    public function Update(w:World,time:Int)
    {
        // for the drag drop pingback
        Store.Update(w);
        for (m in Msgs)
        {
            m.Update(w,time);
        }
    }
}