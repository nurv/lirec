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

class Message
{
    public var Type:String;
    public var Block:Frame;
    public var Icon:Sprite;
    public var OriginalPos:Vec2;
    public var ShakeTime:Int;
    public var Shaking:Bool;
    public var Rnd:RndGen;
    public var Seed:Int;

    public function new(w:World, i:Dynamic, pos:Vec2, GameGUI:GUI)
    {
        ShakeTime=0;
        Type=i.type;
        Shaking=false;
        Rnd=new RndGen();
        OriginalPos=new Vec2(pos.x,pos.y);
        Seed=Std.int(i.time);
        Rnd.Seed(Seed);
        Block=new Frame("",pos.x,pos.y,64*2,64*1);
        Block.ExpandLeft=70;
        Block.SetTextSize(10);
        
        Block.UpdateText(GameGUI.StrMkr.MsgToString(i));
        
        var Colour = new Vec3(0.8,0.9,0.7);
        if (i.type=="spirit") Colour=Spirit.GetEmotionColour(i.emotion);
        
        Block.R=Colour.x;
        Block.G=Colour.y;
        Block.B=Colour.z;
        
        Block.InitTextures(GUIFrameTextures.Get(),Rnd);
        w.AddSprite(Block);
        
        Icon=MakeIcon(new Vec2(pos.x-20,pos.y+32),
                          i.type, i.from, Colour);
        w.AddSprite(Icon);

        if (i.type=="plant" && 
            GameGUI.Instructions==3) 
        {
            GameGUI.Instructions=4;
            var x=pos.x-64*3-60;
            var y=pos.y;
            var Box = new InfoBox(w,"Click on these messages to go to the plants",
                                  x,y,
                                  64*2,64*1,
                                  function()
                                  {
                                      GameGUI.Instructions=5;
                                  },
                                  new Sprite(new Vec2(x+62*2+20,y+20),Resources.Get("arrright")));
        }


        // goto sender on click
        Block.MouseDown(this,function(c){
            if (!GameGUI.Store.Carrying())
            {
                w.SetWorldPos(new Vec3(i.tile.x,i.tile.y,0),
                              new Vec2(0,0));
                w.Highlight(new Vec2(i.pos.x+5,i.pos.y+5));
            }
        });

        var ToolTip=null;
        var x=pos.x;
        var y=pos.y;

        // overridden below for spirit messages
        Block.MouseOut(this,function(c) {
            if (ToolTip!=null)
            {
                w.RemoveSprite(ToolTip);
            }
        });

        Block.MouseOver(this,function(c){
            if (!GameGUI.Store.Carrying())
            {
                ToolTip=new Frame("Click to go to this plant",x,y,100,20);
                w.AddSprite(ToolTip);
            }
        });

        // don't have owner id for recipients of plant messages :(
        if (i.type=="spirit")
        {
            if (GameGUI.Instructions==5)
            {
                GameGUI.Instructions=6;
                var x=pos.x-64*4-60;
                var y=pos.y;
                var Box = new InfoBox(w,"Drop fruit on these messages to send gifts to players or spirits",
                                      x,y,
                                      64*3,64*1,
                                      function()
                                      {
                                          GameGUI.Instructions=7;
                                      },
                                      new Sprite(new Vec2(x+62*3+20,y+20),Resources.Get("arrright")));

           }

            var ToolTip=null;
            var x=pos.x;
            var y=pos.y;
            Block.MouseOver(this,function(c){
                if (GameGUI.Store.Carrying())
                {
                    if (IsGift(i.code))
                    {
                        ToolTip=new Frame("Give fruit to "+i.extra[0],x,y,100,20);
                    }
                    else
                    {
                        ToolTip=new Frame("Give fruit to the spirits",x,y,100,20);
                    }

                    w.AddSprite(ToolTip);
                }
                else
                {
                    ToolTip=new Frame("Click to go to this plant",x,y,100,20);
                    w.AddSprite(ToolTip);
                }
            });

            Block.MouseOut(this,function(c){
                if (ToolTip!=null)
                {
                    w.RemoveSprite(ToolTip);
                }
            });

            Block.MouseUp(this,function(c){
                if (GameGUI.Store.Carrying())
                {
                    c.Shake(w.Time);
                    var Fruit=GameGUI.Store.Drop(w);
                    if (IsGift(i.code))
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

    static function IsGift(code:String) : Bool
    {
        // should match codes in game-world-process-msg
        return (code == "your_plant_doesnt_like" ||
                code == "i_am_detrimented_by" ||
                code == "i_am_detrimental_to" ||
                code == "i_am_benefitted_by" ||
                code == "i_am_beneficial_to" ||
                code == "needs_help" ||
                code == "ive_asked_x_for_help");
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

    public function Shake(time:Int)
    {
        Shaking=true;
        ShakeTime=time+10;
    }

    public function Update(w:World,time:Int)
    {
        if (Shaking)
        {
            Rnd.Seed(Seed+time*43);
            var Shake=Rnd.RndCircleVec2().Mul(4);
            
            var x:Int=Std.int(OriginalPos.x+Shake.x);
            var y:Int=Std.int(OriginalPos.y+Shake.y);

            Block.UpdatePosition(x,y);
            Rnd.Seed(Seed);
            Block.InitTextures(GUIFrameTextures.Get(),Rnd);

            Icon.SetPos(new Vec2(x-25,y+25));
            Icon.Update(0,null);
            
            if (ShakeTime<time)
            {
                Shaking=false;
                Block.UpdatePosition(Std.int(OriginalPos.x),
                                     Std.int(OriginalPos.y));
                Rnd.Seed(Seed);
                Block.InitTextures(GUIFrameTextures.Get(),Rnd);
                Icon.SetPos(OriginalPos.Add(new Vec2(-25,25)));
                Icon.Update(0,null);
            }
        }
    }

}