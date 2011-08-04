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
            i_have_been_planted: function(from,subjects:Array<Dynamic>)
            {
                return "This "+from+" plant has just germinated!";
            },
            i_am_ill: function(from,subjects:Array<Dynamic>)
            {
                return "This "+from+" plant is feeling ill.";
            },
            i_have_died: function(from,subjects:Array<Dynamic>)
            {
                return "This "+from+" plant has died.";
            },
            i_have_recovered: function(from,subjects:Array<Dynamic>)
            {
                return "This "+from+" plant is recovering.";
            },
            i_have_been_picked_by: function(from,subjects:Array<Dynamic>)
            {
                return "This "+from+" plant has been picked by "+subjects[0];
            },
            your_plant_doesnt_like: function(from,subjects:Array<Dynamic>)
            {
                return "This "+subjects[0]+" plant doesn't like "+subjects[1]+" growing nearby.";
            },
            your_plant_needs: function(from,subjects:Array<Dynamic>)
            {
                return "This "+subjects[0]+" plant needs a "+subjects[1]+" plant nearby.";
            }
        };
    }

    public function MsgToString(msg:Dynamic) : String
    {
        return MsgMap[Reflect.field(msg,"msg-id")]
        (Reflect.field(msg,"display-from"),
         Reflect.field(msg,"subjects"));
    }
}