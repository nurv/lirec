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
import truffle.SkeletonEntity;
import truffle.Vec2;
import truffle.Vec3;
import truffle.RndGen;
import truffle.Bone;

// todo: remove this
import flash.display.Graphics;
import flash.display.Shape;

class Spirit extends SkeletonEntity 
{
    public var Name:String;

    var Debug:flash.text.TextField;
	var BG:Graphics;
    var RawEmotions:Dynamic;
    var Emotions:Dynamic;
    var DesiredPos:Vec2;
    public var LastData:Array<Dynamic>;
    var Action:Sprite;
    var Emitter:Particles;
    var EmotionalColours:Dynamic;
    var Rnd:RndGen;
    var HighestEmotion:String;
    
    function ToCol(r:Int,g:Int,b:Int)
    {
        return new Vec3(r/255.0,g/255.0,b/255.0);
    }

	public function new(world:World, name:String, pos)
	{
		super(world,pos);
        Name = name;
        HighestEmotion="Not calculated yet";
        Speed=0.1;
        UpdateFreq=2;
        Hide(true);
        LastData=[];
        Rnd=new RndGen();
        
        EmotionalColours = {
            JOY:[ToCol(255,224,1),ToCol(255,224,1),ToCol(255,126,2),ToCol(0,155,2)],
            FEAR:[ToCol(128,128,128),ToCol(128,64,2),ToCol(97,0,98),ToCol(220,2,1)],
            LOVE:[ToCol(225,224,1),ToCol(220,3,0),ToCol(225,117,225),ToCol(0,115,2)], // surprise
            HATE:[ToCol(128,128,128),ToCol(128,64,2),ToCol(97,0,98),ToCol(220,2,1)], // fear
            PRIDE:[ToCol(29,201,33)]
        };

        DesiredPos=new Vec2(LogicalPos.x,LogicalPos.y);
        RawEmotions={LOVE:0,HATE:0,HOPE:0,FEAR:0,SATISFACTION:0,
                     RELIEF:0,/*Fears_Confirmed:0,*/DISAPOINTMENT:0,
                     JOY:0,DISTRESS:0,/*Happy_For:0,*/PITTY:0,
                     RESENTMENT:0,GLOATING:0,PRIDE:0,SHAME:0,
                     GRATIFICATION:0,REMORSE:0,ADMIRATION:0,
                     REPROACH:0,GRATITUDE:0,ANGER:0};
        Emotions={LOVE:0,HATE:0,HOPE:0,FEAR:0,SATISFACTION:0,
                     RELIEF:0,/*Fears_Confirmed:0,*/DISAPOINTMENT:0,
                     JOY:0,DISTRESS:0,/*Happy_For:0,*/PITTY:0,
                     RESENTMENT:0,GLOATING:0,PRIDE:0,SHAME:0,
                     GRATIFICATION:0,REMORSE:0,ADMIRATION:0,
                     REPROACH:0,GRATITUDE:0,ANGER:0};
        Emitter = new Particles(new Vec2(Pos.x,Pos.y),50);
        world.addChild(Emitter);
    }

    function UpdateEmitter()
    {
        // get highest emotion
        HighestEmotion = "";
        var HighestScore = 0;
        for (f in Reflect.fields(Emotions))
        {
            var Score=Reflect.field(Emotions,f);
            if (Score>HighestScore)
            {
                HighestScore=Score;
                HighestEmotion=f;
            }
        }

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
        var tf = new flash.text.TextField();
        tf.text = "nowt yet.";
        tf.x=Pos.x-200;
        tf.y=Pos.y-25;
        tf.height=150;
        tf.width=140;
        tf.background = false;
        tf.autoSize = flash.text.TextFieldAutoSize.LEFT;
        //tf.backgroundColor = 0x8dd788;
        tf.border = true;
        tf.wordWrap = true;
        var t = new flash.text.TextFormat();
        t.font = "Verdana"; 
        t.size = 8;                
        t.color= 0x000000;           
        tf.setTextFormat(t);

        var figures:Shape = new Shape();
        BG = figures.graphics;
        BG.beginFill(0xffffff,0.5);
        BG.drawRect(tf.x,tf.y,tf.width,tf.height);
        BG.endFill();
        figures.visible=false;
        cast(c,truffle.flash.FlashWorld).addChild(figures);

        c.addChild(tf);
        Debug=tf;
//        tf.visible=false;
 
        tf.visible=true;
        figures.visible=true;
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
        Action = new Sprite(new Vec2(0,0),Resources.Get(""));
        Action.Hide(true);
        c.AddSprite(Action);

	}

    public function UpdateEmotions(e:Dynamic,world:World)
    {
        SetTilePos(new Vec2(Std.parseInt(e.tile.x),
                            Std.parseInt(e.tile.y)));

        
        var dst = new Vec2(Std.parseInt(e.emotionalloc.x),
                           Std.parseInt(e.emotionalloc.y));

        if (dst.x!=DesiredPos.x || dst.y!=DesiredPos.y)
        {
            DesiredPos = dst;
            while (world.Get("Spirit",dst)!=null)
            {
                dst = dst.Add(new Vec2(world.MyRndGen.Choose([-2,0,2]),
                                       world.MyRndGen.Choose([-2,0,2])));
            }
            LogicalPos = new Vec3(dst.x,dst.y,4);
        }

        RawEmotions = e.emotions;

        var ee = e.fatemotions.content;
        var mood=Std.parseFloat(ee[0].content[0]);

        UpdateEmitter();

        var text=Name+"\nMood:"+ee[0].content[0]+"\n";
        var text=Name+"\nHighest Emotion:"+HighestEmotion+"\n";
        text+="Causes:\n";
        for (i in 1...ee.length)
        {
            text+=ee[i].attrs.type+" "+ee[i].attrs.direction+"\n";
            //text+=ee[i].attrs.cause+"\n";
        }

        text+="Actions:\n";
        var acs = cast(e.fatactions,Array<Dynamic>);
        for (i in 0...acs.length)
        {
            text+=acs[i].msg+"\n";
        }
        
        Action.Hide(true);
        if (acs.length>0 && !Hidden)
        {
            if (StringTools.startsWith(acs[0].msg,"flower"))
            {
                Action.ChangeBitmap(Resources.Get("action-flower"));
                Action.Hide(false);
            }
            
            if (StringTools.startsWith(acs[0].msg,"drop-leaves"))
            {
                Action.ChangeBitmap(Resources.Get("action-drop-leaves"));
                Action.Hide(false);
            }
        }

        Debug.text=text;

        var t = new flash.text.TextFormat();
        t.font = "Verdana"; 
        t.size = 8;                
        t.color= 0x000000;           
        Debug.setTextFormat(t);
        Debug.x=Pos.x-200;
        Debug.y=Pos.y-25;

        BG.clear();
        BG.beginFill(0xffffff,0.5);
        BG.drawRect(Debug.x,Debug.y,Debug.width,Debug.height);
        BG.endFill();

        //trace(text);
    }

    override function Update(frame:Int, world:World)
    {
        for (f in Reflect.fields(Emotions))
        {
            // do a linear blend to smooth out changes in emotions
            Reflect.setField(Emotions,f,
            Reflect.field(Emotions,f)*0.95+Reflect.field(RawEmotions,f)*0.05);
            //trace(f);
        }

        Action.Pos.x=Root.Pos.x-50;
        Action.Pos.y=Root.Pos.y-50;
        Action.Update(0,null);

        //Draw(cast(world,truffle.World));
        var c=this;

        var excitement = c.Emotions.LOVE+c.Emotions.ADMIRATION;
        if (excitement>10) excitement=10;
        var irritation = c.Emotions.HATE+c.Emotions.DISTRESS;
        if (irritation>5) irritation=5;
        var bouncyness = c.Emotions.GRATITUDE*0.2;
        if (bouncyness>5) bouncyness=5;
        var bounce=new Vec2(0,0);

        Root.Recurse(function(b:Bone,depth:Int) 
        {    
            b.SetRotate((excitement*5+1)*Math.sin(
                             (((10-depth)+frame*0.04+c.Emotions.GRATITUDE*0.01)+
                             c.Emotions.JOY*0.1)) +
            ((world.MyRndGen.RndFlt()-0.5)*10*irritation));
            bounce.y=bouncyness*5*Math.abs(Math.sin(frame*0.25));
            b.SetPos(b.BindPos.Add(bounce));
           
        });

        Emitter.Pos.x=Pos.x;
        Emitter.Pos.y=Pos.y;
        Emitter.Update(frame);

        super.Update(frame,world);
    }
}
