// GerminationX Copyright (C) 2011 FoAM vzw    \_\ __     /\
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
import truffle.ClusterEntity;
import truffle.Vec2;
import truffle.Vec3;
import truffle.RndGen;
import truffle.Bone;

class Spirit extends ClusterEntity 
{
    public var Name:String;

    var Debug:Frame;
    var RawEmotions:Dynamic;
    var Emotions:Dynamic;
    var DesiredPos:Vec2;
    public var LastData:Array<Dynamic>;
    var Message:Frame;
    var Emitter:Particles;
    var EmotionalColours:Dynamic;
    var Rnd:RndGen;
    var HighestEmotion:String;
    var TotalEmotion:Float;
    var EmotionColour:Float;
    static var EmotionIndices:Dynamic;
    var HighestScore:Float;
    var MessageTime:Float;

    function ToCol(r:Int,g:Int,b:Int)
    {
        return new Vec3(r/255.0,g/255.0,b/255.0);
    }

	public function new(world:World, name:String, pos)
	{
		super(world,pos);
        Name = name;
        HighestEmotion="Not calculated yet";
        Speed=0.02;
        UpdateFreq=2;
        LastData=[];
        Rnd=new RndGen();
        TotalEmotion=0;
        EmotionColour=0;
        HighestScore=0;
        MessageTime=-1;
        
        Message = new Frame("",0,0,64*2,64);
        Message.SetTextSize(10);
        Message.InitTextures(GUIFrameTextures.Get(),Rnd);
        Message.R=1;
        Message.G=1;
        Message.B=0.8;
        world.AddSprite(Message);
        Message.Hide(true);
        
        EmotionalColours = {
            JOY:[ToCol(255,224,1),ToCol(255,224,1),ToCol(255,126,2),ToCol(0,155,2)],
            FEAR:[ToCol(128,128,128),ToCol(128,64,2),ToCol(97,0,98),ToCol(220,2,1)],
            LOVE:[ToCol(225,224,1),ToCol(220,3,0),ToCol(225,117,225),ToCol(0,115,2)], // surprise
            HATE:[ToCol(128,128,128),ToCol(128,64,2),ToCol(97,0,98),ToCol(220,2,1)], // fear
            PRIDE:[ToCol(29,201,33)]
        };

        DesiredPos=new Vec2(LogicalPos.x,LogicalPos.y);

        EmotionIndices={LOVE:0,HATE:1,HOPE:2,FEAR:3,SATISFACTION:4,
                        RELIEF:5,FEARS_CONFIRMED:6,DISAPOINTMENT:7,
                        JOY:8,DISTRESS:9,HAPPY_FOR:10,PITTY:11,
                        RESENTMENT:12,GLOATING:13,PRIDE:14,SHAME:15,
                        GRATIFICATION:16,REMORSE:17,ADMIRATION:18,
                        REPROACH:19,GRATITUDE:20,ANGER:21};

        RawEmotions={LOVE:0,HATE:0,HOPE:0,FEAR:0,SATISFACTION:0,
                     RELIEF:0,FEARS_CONFIRMED:0,DISAPOINTMENT:0,
                     JOY:0,DISTRESS:0,HAPPY_FOR:0,PITTY:0,
                     RESENTMENT:0,GLOATING:0,PRIDE:0,SHAME:0,
                     GRATIFICATION:0,REMORSE:0,ADMIRATION:0,
                     REPROACH:0,GRATITUDE:0,ANGER:0};
        Emotions={LOVE:0,HATE:0,HOPE:0,FEAR:0,SATISFACTION:0,
                     RELIEF:0,FEARS_CONFIRMED:0,DISAPOINTMENT:0,
                     JOY:0,DISTRESS:0,HAPPY_FOR:0,PITTY:0,
                     RESENTMENT:0,GLOATING:0,PRIDE:0,SHAME:0,
                     GRATIFICATION:0,REMORSE:0,ADMIRATION:0,
                     REPROACH:0,GRATITUDE:0,ANGER:0};
        //Emitter = new Particles(new Vec2(Pos.x,Pos.y),50);
        //world.addChild(Emitter);
    }

    static public function GetEmotionColour(Emotion)
    {
        var EmotionMap = Resources.Get("emotion-map").data;
        var EmotionIndex = Reflect.field(EmotionIndices,Emotion);
        var Colour=IntToColourTriple(EmotionMap.getPixel(EmotionIndex,0));              
        Colour.x+=0.6;
        Colour.y+=0.6;
        Colour.z+=0.6;
        return Colour;
    }

    function UpdateEmitter()
    {
        var Colours:Array<Vec3>=Reflect.field(EmotionalColours,HighestEmotion);

        if (HighestEmotion!="" && Colours!=null)
        {
            for (i in 0...Emitter.Colours.length)
            {
                Emitter.Colours[i]=Colours[Rnd.RndInt()%Colours.length];
            }
            Emitter.RecycleRate=80;
        }
        else
        {  
            Emitter.RecycleRate=0;
        }
    }

	public function BuildDebug(c)
    {
        Debug = new Frame("nowt yet.",Pos.x-200,Pos.y-25,140,150);
        Debug.Hide(true);
        c.addChild(Debug);
        
        //Sprites[0].MouseDown(this,function(c) { c.Debug.Hide(false); });
	}

    public function AddMsg(msg,text)
    {
        Message.UpdateText(text);
        Message.Hide(false);    
        var Col=GetEmotionColour(msg.emotion);
        Message.R=Col.x;
        Message.G=Col.y;
        Message.B=Col.z;
        MessageTime=Date.now().getSeconds()+10;
    }

    override function OnSortScene(world:World, order:Int) : Int
    {
        var order=super.OnSortScene(world,order);
        Message.SetDepth(order++);
        return order;
    }

    static function IntToColourTriple(col:Int) : Vec3
    {
        return new Vec3((col >> 16 & 0xFF)/255.0,
		                (col >> 8 & 0xFF)/255.0,
		                (col & 0xFF)/255.0);
    }

    function UpdateDebug(e:Dynamic)
    {
        var ee = e.fatemotions.content;
        if (ee==null) return;
        var mood=Std.parseFloat(ee[0].content[0]);

        var text=Name+"\nMood:"+ee[0].content[0]+"\n";
        text+="Highest Emotion:"+HighestEmotion+"="+HighestScore+"\n";
        text+="Total Emotions:"+TotalEmotion+"\n";
        text+="Causes:\n";
        for (i in 1...ee.length)
        {
            text+=ee[i].attrs.type+" "+ee[i].attrs.direction+"\n";
            //text+=ee[i].attrs.cause+"\n";
        }

        text+="Messages:\n";
        var acs = cast(e.fatactions,Array<Dynamic>);
        for (i in 0...acs.length)
        {
            text+=acs[i].msg+"\n";
        }
        
        Debug.UpdateText(text);
        Debug.UpdatePosition(Std.int(Pos.x-200),Std.int(Pos.y-25));
    }

    public function UpdateEmotions(e:Dynamic,world:World)
    {
        var TilePos=new Vec2(Std.parseInt(e.emotionalloc.tile.x),
                             Std.parseInt(e.emotionalloc.tile.y));
        SetTilePos(TilePos);

        var LocalPos = new Vec2(Std.parseInt(e.emotionalloc.pos.x),
                                Std.parseInt(e.emotionalloc.pos.y));

        // account for tiles complication
        var dst=new Vec2(((TilePos.x-world.WorldPos.x)+1)*5,
                         ((TilePos.y-world.WorldPos.y)+1)*5);        
        dst=dst.Add(LocalPos);
        
        if (dst.x!=DesiredPos.x || dst.y!=DesiredPos.y)
        {
            while (world.Get("fungi.Spirit",dst)!=null)
            {
                dst = dst.Add(new Vec2(world.MyRndGen.Choose([-1,0,1]),
                                       world.MyRndGen.Choose([-1,0,1])));
            }
            SetLogicalPos(world,new Vec3(dst.x,dst.y,3));
        }

        RawEmotions = e.emotions;
        Emotions = RawEmotions;

        // get total amount of emotion
        TotalEmotion=0;
        for (f in Reflect.fields(RawEmotions))
        {
            TotalEmotion+=Reflect.field(RawEmotions,f);
        }

        // get highest emotion
        HighestEmotion = "";
        HighestScore = 0;
        for (f in Reflect.fields(Emotions))
        {
            var Score=Reflect.field(Emotions,f);
            if (Score>HighestScore)
            {
                HighestScore=Score;
                HighestEmotion=f;
            }
        }

        if (!Debug.IsHidden()) UpdateDebug(e);
    }

    override function Hide(s:Bool) : Void
    {
        super.Hide(s);
        if (s) 
        { 
            Debug.Hide(s);
            Message.Hide(s);
        }
    }

    override function Update(frame:Int, world:World)
    {
        super.Update(frame,world);

        if (!Message.IsHidden())
        {
            Rnd.Seed(0);
            var x = Math.floor(Root.Pos.x-(128+64));
            if (Root.Pos.x<320) x=Math.floor(Root.Pos.x+64);
            Message.UpdatePosition(x,Math.floor(Root.Pos.y-20));
            Message.InitTextures(GUIFrameTextures.Get(),Rnd);
            if (Date.now().getSeconds()>MessageTime) Message.Hide(true);
        }

        // get the index of the highest emotion for the emotion map
        var EmotionIndex = Reflect.field(EmotionIndices,HighestEmotion);
        // set the speed by the amount of the highest emotion
        EmotionColour+=HighestScore/100;

        // calculate the animation parameters
        var c=this;
        var excitement = Emotions.LOVE+Emotions.ADMIRATION;
        if (excitement>10) excitement=10;
        var irritation = Emotions.HATE+Emotions.DISTRESS;
        if (irritation>5) irritation=5;

        // get the emotion map
        var EmotionMap = Resources.Get("emotion-map").data;

        var IE=Math.floor(c.EmotionColour);

        Root.Recurse(function(b:Bone,depth:Int) 
        {    
            // pull the colour from the emotion map
            b.Colour=
                IntToColourTriple(
                    EmotionMap.getPixel(
                        EmotionIndex,
                        (depth+IE)%8))
                .Lerp(IntToColourTriple(
                    EmotionMap.getPixel(
                        EmotionIndex,
                        (depth+1+IE)%8)),
                      c.EmotionColour-IE);
            
            // change speed of movement by gratitude and joy with
            // this insane formula which needs rewriting
            b.SetRotate((excitement*5+1)*Math.sin(
                             (((10-depth)+frame*0.04+c.Emotions.GRATITUDE*0.01)+
                             c.Emotions.JOY*0.1)) +
            ((world.MyRndGen.RndFlt()-0.5)*1*irritation));
        });
    }
}
