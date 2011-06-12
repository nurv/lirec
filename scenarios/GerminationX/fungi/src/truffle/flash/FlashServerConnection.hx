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
import truffle.interfaces.ServerRequest;
import truffle.RndGen;

class FlashServerConnection extends ServerConnection
{
    var Loader:URLLoader;
    var LoadedCallback:Dynamic -> Dynamic -> Void;
    var LoadedContext:Dynamic;
    var Rnd:RndGen;
    
    public function new() 
	{
        super();
        Rnd = new RndGen();
        Loader = new URLLoader();
        Loader.dataFormat = URLLoaderDataFormat.TEXT;
        Loader.addEventListener(Event.COMPLETE, CompleteHandler);
        Loader.addEventListener(Event.OPEN, OpenHandler);
        Loader.addEventListener(ProgressEvent.PROGRESS, ProgressHandler);
        Loader.addEventListener(SecurityErrorEvent.SECURITY_ERROR, SecurityErrorHandler);
        Loader.addEventListener(HTTPStatusEvent.HTTP_STATUS, HTTPStatusHandler);
        Loader.addEventListener(IOErrorEvent.IO_ERROR, IOErrorHandler);
	}
	
    override function InnerRequest(r:ServerRequest) : Void
    {        
        Ready=false;
        LoadedCallback = r.Callback;
        LoadedContext = r.Context;
        r.URL+="/"+Std.string(Rnd.RndInt());
        //trace(r.URL);
        var request:URLRequest = new URLRequest(r.URL);
        request.method = URLRequestMethod.POST;
        Loader.load(request);
    }
    
    private function CompleteHandler(event:Event)
    {
        Ready=true;
        //trace(Loader.data);
        var t=JSON.decode(Loader.data);
        LoadedCallback(LoadedContext,JSON.decode(Loader.data));   
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
