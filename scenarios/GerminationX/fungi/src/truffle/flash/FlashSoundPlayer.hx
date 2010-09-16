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

package truffle.flash;

import flash.media.Sound;
import flash.net.URLRequest;
import flash.media.SoundLoaderContext;

import truffle.interfaces.SoundPlayer;

class FlashSoundPlayer implements SoundPlayer
{
	var Sounds:Array<Sound>;
	
	public function new(sounds:Array<String>)
	{
		Sounds = [];
		
		for (i in 0...sounds.length)
		{
			LoadSound(sounds[i]);
		}

	}
	
	public function LoadSound(filename) : Int
    {
		var sound: Sound = new Sound();
        var req:URLRequest = new URLRequest(filename);
        var context:SoundLoaderContext = new SoundLoaderContext(8000,true);
        //sound.addEventListener(Event.COMPLETE, SoundLoaded);
        sound.load(req,context);
		Sounds.push(sound);
        return 0;
    }
	
	public function Play(id:Int)
	{
		if (id<Sounds.length)
		{
			Sounds[id].play(0);
		}
	}
	
}
