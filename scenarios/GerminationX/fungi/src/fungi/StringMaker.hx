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
import truffle.RndGen;

class StringMaker
{
    var MsgMap:Dynamic;
    var NoteMap:Dynamic;

    static var Reasons =
        {{
            clover: "nutrients",
            dandelion: "nutrients",
            aronia: "erosion control",
            apple: "protection",
            cherry: "protection",
            boletus: "increased nutrients",
            chanterelle: "increased nutrients",
            flyagaric: "increased magic",
        }};

    public function new()
    {
        var Rnd = new RndGen();
        MsgMap={
            i_have_been_planted: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant has just germinated!";
            },
            i_am_ill: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant is feeling ill.";
            },
            i_have_fruited: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant has fruited.";
            },
            i_have_died: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant has died.";
            },
            i_have_recovered: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant has recovered.";
            },
            i_have_been_picked_by: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant has been picked by "+extra[0];
            },
            your_plant_doesnt_like: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return extra[0]+", your "+extra[3]+" plant doesn't like "+to+"'s "+extra[2]+" plant nearby.";
            },
            your_plant_needs: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return to+", your "+extra[0]+" plant needs a "+extra[1]+" plant nearby for "+Reflect.field(Reasons,extra[1])+".";
            },
            needs_help: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return to+", "+extra[0]+"'s "+extra[2]+" plant needs a "+extra[3]+" near.";
            },
            i_am_recovering: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant is recovering.";
            },
            i_am_detrimented_by: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant is being harmed by "+extra[0]+"'s "+extra[2]+" plant.";
            },
            i_am_detrimental_to: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant is harming "+extra[0]+"'s "+extra[2]+" plant.";
            },
            i_am_benefitting_from: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant is being helped by "+extra[0]+"'s "+extra[2]+" plant.";
            },
            i_am_beneficial_to: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant is helping "+extra[0]+"'s "+extra[2]+" plant.";
            },
            thanks_for_helping: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return owner+"'s "+from+" plant thanks "+to+"'s "+extra[0]+" plant for helping.";
            },
            spirit_complaint: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                var e="";
                if (emotion=="LOVE" || 
                    emotion=="HAPPY_FOR" ||
                    emotion=="GLOATING") e="I am happy, but ";
                if (emotion=="HATE" || 
                    emotion=="PITTY") e="I am sad, because ";
                if (emotion=="PRIDE") e="I am proud, but ";
                if (emotion=="DISTRESS") e="This is awful, ";
                if (emotion=="ADMIRATION") e="Everythings fine, but ";
                if (emotion=="JOY") e="I'm too happy to care, but ";
                if (emotion=="SHAME") e="I'm ashamed that ";
                if (emotion=="RESENTMENT") e="It sucks that ";
                
                var msg=e+to+"'s " + extra[0] + " plant is ";
                if (extra[1]=="ill-a") return msg+"a little ill";
                if (extra[1]=="ill-b") return msg+"quite ill";
                if (extra[1]=="ill-c") return msg+"very ill";
                if (extra[1]=="detriment") return msg+"harming my plants.";
                if (extra[1]=="new-fruit") return msg+"has fruited.";
                if (extra[1]=="picked-fruit") return msg+"has been picked.";
                return msg+"... something. (bug: "+extra[1]+")";
            },
            spirit_praise: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                var e="";
                if (emotion=="LOVE" || 
                    emotion=="HAPPY_FOR" ||
                    emotion=="GLOATING") e="I am happy that ";
                if (emotion=="HATE" || 
                    emotion=="PITTY") e="I am sad, but ";
                if (emotion=="PRIDE") e="I am proud, ";
                if (emotion=="DISTRESS") e="Things are awful, but ";
                if (emotion=="ADMIRATION") e="It's great, ";
                if (emotion=="JOY") e="Wow! - ";
                if (emotion=="SHAME") e="I'm ashamed, ";
                if (emotion=="RESENTMENT") e="Things are bad, but ";

                var msg=e+to+"'s " + extra[0] + " plant ";
                if (extra[1]=="grow-a") return msg+"is growing well.";
                if (extra[1]=="fruit-a") return msg+"has flowered!";
                if (extra[1]=="fruit-b") return msg+"is fruiting.";
                if (extra[1]=="fruit-c") return msg+"has fruited.";
                if (extra[1]=="recovery-to-b") return msg+"was very ill and is recovering.";
                if (extra[1]=="recovery-to-a") return msg+"is recovering.";
                if (extra[1]=="finished-recovery") return msg+"has fully recovered.";
                if (extra[1]=="benefit") return msg+"is helping my plants.";
                if (extra[1]=="new-fruit") return msg+"has fruited.";
                if (extra[1]=="picked-fruit") return msg+"has been picked.";
                return msg+"... something. (bug: "+extra[1]+")";
            },
            spirit_fortune_of_other: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {   
                var other="TreeSpirit";
                if (extra[3]=="cover") other="CoverSpirit";
                if (extra[3]=="shrub") other="ShrubSpirit";

                if (extra[0]=="happy-for")
                {
                    return other+", you must be pleased with "+to+"'s " + extra[1] + " plant!";
                }
                if (extra[0]=="pity")
                {
                    return "Oh dear "+other+", I'm sorry about "+to+"'s " + extra[1] + " plant.";
                }
                if (extra[0]=="gloat")
                {
                    return "Haha "+other+", "+to+"'s " + extra[1] + " plant is ruining your day!";
                }
                if (extra[0]=="resent")
                {
                    return other+", it's only luck that "+to+" planted a " + extra[1] + " plant there.";
                }
                else return "spirit_fortune_of_other error!";
            },
            spirit_received_offering: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                // what caused this message?
                var feeling="kindly"; // praise
                if (extra[0]=="complain" ||
                    extra[0]=="diagnose")
                    feeling="annoyingly";
                   
                return to+" has " + feeling + " given me a " + extra[1] + " layer fruit gift!";
            },
            ive_asked_x_for_help: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                var msg=to+", I've asked "+extra[0]+" to help with your "+extra[2]+" plant";
                if (extra[3]=="ill-a") msg+=", which is a little ill.";
                else if (extra[3]=="ill-b") msg+=", which is a quite ill.";
                else if (extra[3]=="ill-c") msg+=", which is a very ill.";
                else msg+=".";
                return msg;
            },
            one_time_i_have_flowered: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return to+", your "+from+" plant has flowered, and your score has increased!";
            },
            gift_received: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return extra[0]+", you have been given a "+extra[2]+" fruit by "+extra[1]+"!";
            },
            gift_sent: function(from,to,owner,extra:Array<Dynamic>,emotion)
            {
                return extra[0]+", you have given a "+extra[2]+" fruit to "+extra[1]+"!";
            }
        };

        NoteMap={
            welcome: function(name)
            {
                return 
                "Welcome to Germination X "+name+". \n"+ 
                    "<img src=\"images/welcome.png\"> ";
            },

            levelup1: function (name)
            {
                return "Well done on planting all those ground cover plants "+name+"!\n"+
                    "<img src=\"images/levelup1.png\"> ";

//                return "Well done on planting all those ground cover plants "+name+"!\n"+
//                    "You will find a mystery magic item in your fruit store - use it wisely! Your new role is to plant shrub plants with fruit like this:"+
//                    "<img src=\"images/aronia-fruit-c.png\">\n"+
//                    "The shrub spirit will help you in this quest. You can also send fruit to other players or spirits by dropping them on messages.";
            },

            levelup2: function (name)
            {
                return name+", you are a master gardener!\n"+
                    "<img src=\"images/levelup2.png\"> ";

//                return name+", you are a master gardener!\n"+
//                    "Your next role in Germination X is to plant tree plants with fruit like this:"+
//                    "<img src=\"images/cherry-fruit-c.png\"> <img src=\"images/apple-fruit-c.png\">\n"+
//                    "You should also find another surprise magic item!";
            },

            levelup3: function (name)
            {
                return name+", you have proved your planting prowess, and are now an expert on companion planting and permaculture!\n"+
                    "\nCongratulations, you have completed the current version of Germination X."+
                    "\n\nYou have earned a final surprise as a gift, from now on you can pick and plant any type of plant."+
                    "\n\nThankyou for playing!";                
            }
        };
    }

    public function MsgToString(msg:Dynamic) : String
    {
        if (!Reflect.hasField(MsgMap,msg.code))
        {
            return "oops - no message found for "+msg.code;
        }
        else
        {
            var owner = msg.owner;
            if (msg.type=="spirit") 
            {
                owner=msg.from;
            }
            return MsgMap[msg.code]
            (  
                msg.from,
                msg.display, 
                owner,
                msg.extra,
                msg.emotion
            );
        }
    }

    public function NoteToString(name:String,note:Dynamic) : String
    {
        if (!Reflect.hasField(NoteMap,note.code))
        {
            return "oops - no note found for "+note.code;
        }
        else
        {
            return NoteMap[note.code]
            (  
                name
            );
        }
    }
}