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

import flash.events.IEventDispatcher;
import flash.events.Event;
import flash.events.ProgressEvent;
import flash.events.SecurityErrorEvent;
import flash.events.HTTPStatusEvent;
import flash.events.IOErrorEvent;
import flash.net.URLRequest;
import flash.net.URLLoader;
import flash.net.URLLoaderDataFormat;
import flash.net.URLRequestMethod;
//import flash.net.URLVariables;
import hxjson2.JSON;

import truffle.interfaces.ServerConnection;

class FlashServerConnection implements ServerConnection
{
    var Loader:URLLoader;
    var Loading:Bool;
    var LoadedCallback:Dynamic -> Void;

    public function new() 
	{
        Loading = false;
        Loader = new URLLoader();
        Loader.dataFormat = URLLoaderDataFormat.TEXT;
        Loader.addEventListener(Event.COMPLETE, CompleteHandler);
        Loader.addEventListener(Event.OPEN, OpenHandler);
        Loader.addEventListener(ProgressEvent.PROGRESS, ProgressHandler);
        Loader.addEventListener(SecurityErrorEvent.SECURITY_ERROR, SecurityErrorHandler);
        Loader.addEventListener(HTTPStatusEvent.HTTP_STATUS, HTTPStatusHandler);
        Loader.addEventListener(IOErrorEvent.IO_ERROR, IOErrorHandler);
	}
	
    public function MakeParams(p:Dynamic) : String
    {
        var s = "?";
        var first=true;
        for (field in Reflect.fields(p)) 
        {
            var value:String = Reflect.field(p, field);
            if (!first) s+="&";            
            first=false;
            s+=field+"="+value;
        }     
        return s;
    }

    public function Request(Args:Dynamic, Callback:Dynamic -> Void) : Bool
    {        
        if (!Loading)
        {
            LoadedCallback = Callback;
            // can't get URLVariables to work so doing it by hand :/
            //var urlvars:URLVariables = new URLVariables("function_name=ping");      
            var request:URLRequest = new URLRequest(Args.function_name/*+MakeParams(Args)*/);
            request.method = URLRequestMethod.POST;
            //request.data = urlvars;
            Loader.load(request);
            return true;
        }
        return false;
    }
    
    private function CompleteHandler(event:Event)
    {
        //trace(Loader.data);
        return LoadedCallback(JSON.decode(Loader.data));   
    }
    
    private function OpenHandler(event:Event)
    {
        //trace("openHandler: " + event);
    }
    
    private function ProgressHandler(event:ProgressEvent)
    {
        //trace("progressHandler loaded:" + event.bytesLoaded + " total: " + event.bytesTotal);
    }
    
      private function SecurityErrorHandler(event:SecurityErrorEvent)
    {
        trace("securityErrorHandler: " + event);
    }
    
    private function HTTPStatusHandler(event:HTTPStatusEvent)
    {
        //trace("httpStatusHandler: " + event.status);
    }
    
    private function IOErrorHandler(event:IOErrorEvent)
    {
        trace("ioErrorHandler: " + event.text);
    }
    
}
