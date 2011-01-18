// t r u f f l e Copyright (C) 2010 FoAM vzw   \_\ __     /\
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

package truffle.interfaces;
import truffle.interfaces.ServerRequest;

class ServerConnection
{
    var RequestQueue:Array<ServerRequest>;
    var Ready:Bool;

    public function new()
    {
        RequestQueue=[];
        Ready=true;
    }

    public function Request(URL:String, 
                            Context:Dynamic,
                            Callback:Dynamic -> Dynamic -> Void) : Void
    {
        RequestQueue.push(new ServerRequest(URL,Context,Callback));
    }

    public function InnerRequest(r:ServerRequest) : Void
    {
    }

    public function Update() : Void
    {
        if (RequestQueue.length>0 && Ready)
        {
            InnerRequest(RequestQueue.pop());
        }
    }
}
