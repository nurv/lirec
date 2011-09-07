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

package fungi;

import truffle.FrameTextures;

class GUIFrameTextures
{
    static var TheFrameTextures:FrameTextures;

    public static function Init()
    {
        TheFrameTextures = new FrameTextures();
        TheFrameTextures.N.push(Resources.Get("gui-n-001"));
        TheFrameTextures.N.push(Resources.Get("gui-n-002"));
        TheFrameTextures.N.push(Resources.Get("gui-n-003"));
        TheFrameTextures.NE.push(Resources.Get("gui-ne-001"));
        TheFrameTextures.NE.push(Resources.Get("gui-ne-002"));
        TheFrameTextures.E.push(Resources.Get("gui-e-001"));
        TheFrameTextures.E.push(Resources.Get("gui-e-002"));
        TheFrameTextures.E.push(Resources.Get("gui-e-003"));
        TheFrameTextures.E.push(Resources.Get("gui-e-004"));
        TheFrameTextures.SE.push(Resources.Get("gui-se-001"));
//        TheFrameTextures.SE.push(Resources.Get("gui-se-002"));
        TheFrameTextures.SE.push(Resources.Get("gui-se-003"));
        TheFrameTextures.S.push(Resources.Get("gui-s-001"));
        TheFrameTextures.S.push(Resources.Get("gui-s-002"));
//        TheFrameTextures.S.push(Resources.Get("gui-s-003"));
        TheFrameTextures.S.push(Resources.Get("gui-s-004"));
        TheFrameTextures.SW.push(Resources.Get("gui-sw-001"));
//        TheFrameTextures.SW.push(Resources.Get("gui-sw-002"));
        TheFrameTextures.SW.push(Resources.Get("gui-sw-003"));
        TheFrameTextures.W.push(Resources.Get("gui-w-001"));
        TheFrameTextures.W.push(Resources.Get("gui-w-002"));
        TheFrameTextures.W.push(Resources.Get("gui-w-003"));
        TheFrameTextures.W.push(Resources.Get("gui-w-004"));
        TheFrameTextures.NW.push(Resources.Get("gui-nw-001"));
        TheFrameTextures.NW.push(Resources.Get("gui-nw-002"));
        TheFrameTextures.NW.push(Resources.Get("gui-nw-003"));

    }

    public static function Get()
    {
        return TheFrameTextures;
    }
}