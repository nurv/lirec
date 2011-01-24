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


//#if flash

import flash.display.BitmapData;
import truffle.Truffle;

class BlueCubeTex extends BitmapData { public function new() { super(0,0); } }
class GrassCube01Tex extends BitmapData { public function new() { super(0,0); } }
class GrassCube02Tex extends BitmapData { public function new() { super(0,0); } }
class GrassCube03Tex extends BitmapData { public function new() { super(0,0); } }
class SeaCube01Tex extends BitmapData { public function new() { super(0,0); } }
class SeaCube02Tex extends BitmapData { public function new() { super(0,0); } }
class SeaCube03Tex extends BitmapData { public function new() { super(0,0); } }
class RBotNorthTex extends BitmapData { public function new() { super(0,0); } }
class RBotSouthTex extends BitmapData { public function new() { super(0,0); } }
class RBotEastTex extends BitmapData { public function new() { super(0,0); } }
class RBotWestTex extends BitmapData { public function new() { super(0,0); } }
class FlowersTex extends BitmapData { public function new() { super(0,0); } }
class LollyPopTex extends BitmapData { public function new() { super(0,0); } }
class ClimberTex extends BitmapData { public function new() { super(0,0); } }
class CanopyTex extends BitmapData { public function new() { super(0,0); } }

class Player extends BitmapData { public function new() { super(0,0); } }
 

class GhostCanopy extends BitmapData { public function new() { super(0,0); } }
class GhostCanopyHappy extends BitmapData { public function new() { super(0,0); } }
class GhostCanopySad extends BitmapData { public function new() { super(0,0); } }

class GhostCover extends BitmapData { public function new() { super(0,0); } }
class GhostCoverHappy extends BitmapData { public function new() { super(0,0); } }
class GhostCoverSad extends BitmapData { public function new() { super(0,0); } }

class GhostVertical extends BitmapData { public function new() { super(0,0); } }
class GhostVerticalHappy extends BitmapData { public function new() { super(0,0); } }
class GhostVerticalSad extends BitmapData { public function new() { super(0,0); } }

class Test extends BitmapData { public function new() { super(0,0); } }
class CursorTex extends BitmapData { public function new() { super(0,0); } }

class Plant001Tex extends BitmapData { public function new() { super(0,0); } }
class Plant002Tex extends BitmapData { public function new() { super(0,0); } }
class Plant003Tex extends BitmapData { public function new() { super(0,0); } }
class Plant004Tex extends BitmapData { public function new() { super(0,0); } }
class Plant005Tex extends BitmapData { public function new() { super(0,0); } }
class Plant006Tex extends BitmapData { public function new() { super(0,0); } }
class Plant007Tex extends BitmapData { public function new() { super(0,0); } }
class Plant008Tex extends BitmapData { public function new() { super(0,0); } }

class SeedTex extends BitmapData { public function new() { super(0,0); } }

class Arr1Tex extends BitmapData { public function new() { super(0,0); } }
class Arr2Tex extends BitmapData { public function new() { super(0,0); } }
class Arr3Tex extends BitmapData { public function new() { super(0,0); } }
class Arr4Tex extends BitmapData { public function new() { super(0,0); } }

class Plant000GrowATex extends BitmapData { public function new() { super(0,0); } }
class Plant000GrowBTex extends BitmapData { public function new() { super(0,0); } }
class Plant000GrowCTex extends BitmapData { public function new() { super(0,0); } }
class Plant000GrownTex extends BitmapData { public function new() { super(0,0); } }
class Plant000DecayATex extends BitmapData { public function new() { super(0,0); } }
class Plant000DecayBTex extends BitmapData { public function new() { super(0,0); } }
class Plant000DecayCTex extends BitmapData { public function new() { super(0,0); } }
class Plant000IllATex extends BitmapData { public function new() { super(0,0); } }
class Plant000IllBTex extends BitmapData { public function new() { super(0,0); } }
class Plant000IllCTex extends BitmapData { public function new() { super(0,0); } }
class Plant000FruitATex extends BitmapData { public function new() { super(0,0); } }
class Plant000FruitBTex extends BitmapData { public function new() { super(0,0); } }
class Plant000FruitCTex extends BitmapData { public function new() { super(0,0); } }

class Resources
{
    public static function Get(name:String) : TextureDesc
    {
        var tex = new TextureDesc();
        tex.data = new BlueCubeTex();

        switch(name)
        {
        case "blue-cube": tex.data = new BlueCubeTex();
        case "grass-cube-01": tex.data = new GrassCube01Tex();
        case "grass-cube-02": tex.data = new GrassCube02Tex();
        case "grass-cube-03": tex.data = new GrassCube03Tex();
        case "sea-cube-01": tex.data = new SeaCube01Tex();
        case "sea-cube-02": tex.data = new SeaCube02Tex();
        case "sea-cube-03": tex.data = new SeaCube03Tex();
        case "rbot-north": tex.data = new RBotNorthTex();
        case "rbot-south": tex.data = new RBotSouthTex();
        case "rbot-east": tex.data = new RBotEastTex();
        case "rbot-west": tex.data = new RBotWestTex();
        case "flowers": tex.data = new FlowersTex();
        case "lollypop": tex.data = new LollyPopTex();
        case "climber": tex.data = new ClimberTex();
        case "canopy": tex.data = new CanopyTex();
        case "player": tex.data = new Player();

        case "ghost-canopy": tex.data = new GhostCanopy();
        case "ghost-canopy-happy": tex.data = new GhostCanopyHappy();
        case "ghost-canopy-sad": tex.data = new GhostCanopySad();

        case "ghost-cover": tex.data = new GhostCover();
        case "ghost-cover-happy": tex.data = new GhostCoverHappy();
        case "ghost-cover-sad": tex.data = new GhostCoverSad();

        case "ghost-vertical": tex.data = new GhostVertical();
        case "ghost-vertical-happy": tex.data = new GhostVerticalHappy();
        case "ghost-vertical-sad": tex.data = new GhostVerticalSad();
        case "test": tex.data = new Test();
        case "cursor": tex.data = new CursorTex();
        case "plant-001": tex.data = new Plant001Tex();
        case "plant-002": tex.data = new Plant002Tex();
        case "plant-003": tex.data = new Plant003Tex();
        case "plant-004": tex.data = new Plant004Tex();
        case "plant-005": tex.data = new Plant005Tex();
        case "plant-006": tex.data = new Plant006Tex();
        case "plant-007": tex.data = new Plant007Tex();
        case "plant-008": tex.data = new Plant008Tex();

        case "seed": tex.data = new SeedTex();

        case "arr1": tex.data = new Arr1Tex();
        case "arr2": tex.data = new Arr2Tex();
        case "arr3": tex.data = new Arr3Tex();
        case "arr4": tex.data = new Arr4Tex();

        case "plant-000-grow-a": tex.data = new Plant000GrowATex();
        case "plant-000-grow-b": tex.data = new Plant000GrowBTex();
        case "plant-000-grow-c": tex.data = new Plant000GrowCTex();
        case "plant-000-grown": tex.data = new Plant000GrownTex();
        case "plant-000-ill-a": tex.data = new Plant000IllATex();
        case "plant-000-ill-b": tex.data = new Plant000IllBTex();
        case "plant-000-ill-c": tex.data = new Plant000IllCTex();
        case "plant-000-decay-a": tex.data = new Plant000DecayATex();
        case "plant-000-decay-b": tex.data = new Plant000DecayBTex();
        case "plant-000-decay-c": tex.data = new Plant000DecayCTex();
        case "plant-000-fruit-a": tex.data = new Plant000FruitATex();
        case "plant-000-fruit-b": tex.data = new Plant000FruitBTex();
        case "plant-000-fruit-c": tex.data = new Plant000FruitCTex();

        }

        return tex;
    }
}

/*#else

class Resources
{
    public function Get(name:String)
    {
        return name+".png";
    }
}

#end*/
