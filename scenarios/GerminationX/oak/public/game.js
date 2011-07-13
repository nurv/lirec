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

var facebook_enabled=true;

// add some html to the page
function print(dst,text)
{ 
    var div=document.createElement("div");   
    div.id = "";
    div.innerHTML = text;
    document.getElementById(dst).appendChild(div);
}

function clear(id)
{
    var element=document.getElementById(id);
    while (element.firstChild) 
    {
        element.removeChild(element.firstChild);
    }
}

function debug(text) { print("debug",text); }

function facebook_status(text) 
{ 
    clear("facebook_status");
    print("facebook_status",text); 
}

//////////////////////////////////////////////////////////

function game_world()
{
    this.player_name = "";
    this.player_id = 0;

    this.startup = function()
    {
        debug("starting up");
        if (facebook_enabled)
        {
            // the API key is stored in the untracked api-key.js
            // echo "var gx_api_key = '1234567890abcdef';" > api-key.js
            this.fb = new fb_interface(gx_api_key, this.fb_refresh);
        }
    }

    this.get = function()
    {
        return {"name": this.player_name,
                "id": this.player_id };
    }

    this.fb_refresh = function(name,data)
    {
        if (name=="me")
        {
            var name = data.name; //.replace(/\s/g, "");
            game.player_name=name;
            game.player_id=data.id;
        }
    }
}

var game = new game_world();
game.startup();

function crankfb()
{
    game.fb.poll();
    setTimeout("crankfb()",10); 
}

crankfb();


