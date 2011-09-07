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

package truffle;

import truffle.Truffle;

class Client 
{
	public var Name : String;
    var Server : ServerConnection;
    
    public function new()    
    {
        Name = "Unamed";
        Server = new ServerConnection();
    }

	public function Identify(name:String)
	{
        Name = name;
	}

	public function Call(fname,cb:Dynamic->Void) 
	{
        Server.Request({function_name: fname}, cb);
		//api.AddPlant(tx,ty,plant);
	}

	public function AddPlant(tx:Int, ty:Int, plant:ServerPlant) 
	{
        Server.Request
        (
            {
                function_name: "add-plant",
                tx: tx, 
                ty: ty, 
                x: plant.x,
                y: plant.y,
                owner: plant.owner,
                type: plant.type
            },
            function (_) {}
        );

		//api.AddPlant(tx,ty,plant);
	}

	public function GetPlants(tx:Int, ty:Int) : Void
	{
		Server.Request
        (
            {
                function_name: "get-plants",
                tx: tx, 
                ty: ty
            },
            InternalServerPlantsCallback
        );
	}
	
    public function InternalServerPlantsCallback(plants:Dynamic)
    {      
        var a = new Array<ServerPlant>();
        for (i in 0...plants.length)
        {
            a.push(new ServerPlant(
                plants[i].owner,
                plants[i].x,
                plants[i].y,
                plants[i].type));
        }
     
        ServerPlantsCallback(a);
    }

	public function SetPlants(plants:Array<ServerPlant>) : Void
	{
		ServerPlantsCallback(plants);
	}
}
