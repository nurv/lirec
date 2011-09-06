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
    var Rnd:RndGen;
    var MsgMap:Dynamic;

    public function new()
    {
        Rnd = new RndGen();
        MsgMap={
            i_have_been_planted: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has just germinated!";
            },
            i_am_ill: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is feeling ill.";
            },
            i_have_died: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has died.";
            },
            i_have_recovered: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has recovered.";
            },
            i_have_been_picked_by: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has been picked by "+extra[0];
            },
            your_plant_doesnt_like: function(from,to,owner,extra:Array<Dynamic>)
            {
                return extra[0]+"'s "+extra[2]+" plant doesn't like "+to+"'s "+extra[1]+" plant nearby.";
            },
            your_plant_needs: function(from,to,owner,extra:Array<Dynamic>)
            {
                return to+"'s "+extra[0]+" plant needs a "+extra[1]+" plant nearby.";
            },
            needs_help: function(from,to,owner,extra:Array<Dynamic>)
            {
                return to+", "+extra[0]+"'s "+extra[1]+" plant needs a "+extra[2]+" near.";
            },
            i_am_recovering: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is recovering.";
            },
            i_am_detrimented_by: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is being harmed by "+extra[0]+"'s "+extra[1]+" plant.";
            },
            i_am_detrimental_to: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is harming "+extra[0]+"'s "+extra[1]+" plant.";
            },
            i_am_benefitting_from: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is being helped by "+extra[0]+"'s "+extra[1]+" plant.";
            },
            i_am_beneficial_to: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is helping "+extra[0]+"'s "+extra[1]+" plant.";
            },
            thanks_for_helping: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant thanks "+to+"'s "+extra[0]+" plant for helping.";
            }
        };
    }

    public function MsgToString(msg:Dynamic) : String
    {
        if (!Reflect.hasField(MsgMap,msg.code))
        {
            trace("can't find string for message "+msg.code);
            return "oops";
        }
        else
        {
            var owner = msg.owner;
            if (msg.type=="spirit") owner=msg.from;
            return MsgMap[msg.code]
            (  
                msg.from,
                msg.display, 
                owner,
                msg.extra
            );
        }
    }
}