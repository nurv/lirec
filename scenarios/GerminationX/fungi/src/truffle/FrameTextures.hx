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

package truffle;

import truffle.interfaces.TextureDesc;

class FrameTextures
{
    public var N: Array<TextureDesc>;
    public var NE: Array<TextureDesc>;
    public var E: Array<TextureDesc>;
    public var SE: Array<TextureDesc>;
    public var S: Array<TextureDesc>;
    public var SW: Array<TextureDesc>;
    public var W: Array<TextureDesc>;
    public var NW: Array<TextureDesc>;

    public function new()
    {
        N=[];
        NE=[];
        E=[];
        SE=[];
        S=[];
        SW=[];
        W=[];
        NW=[];
    }
}
