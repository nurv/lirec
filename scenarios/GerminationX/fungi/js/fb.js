// Naked on Pluto Copyright (C) 2010 Aymeric Mansoux, Marloes de Valk, Dave Griffiths
//                                       
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

function fb_interface(appid,refresh)
{   
    if (appid!="") 
    {
        debug("FB.init called");
        FB.init({appId: appid, status: true, cookie: true, xfbml: true});
    }

    this.refresh=refresh;
    this.friends=[];
    this.current_friend=-1;

    if (appid!="")
    {
        this.data = {
            me: {},
            likes: [],
            people: [],
            books: [],
	        movies: [],
            locations: [],
            apps: [],
            peoplelikes: []
        }
    }
    else
    {
        this.current_friend=0;
        // some test data
        this.data = {
            me: { id:99, first_name:"Bob", 
                  last_name:"Bloggs", name:"Bob Bloggs"},
            mypic: "",
            people: ["FredFoo","JimJobble","BobbyBoobar"],
            peoplelikes: [
                {name:"FredFoo",likes:["Jam", "Honey"]},
                {name:"JimJobble",likes:["Cars", "Flowers"]},
                {name:"BobbyBoobar", likes:["Red", "Green"]}
            ],
            apps: ["Naked on Pluto"],
            books: ["The Dictionary", "The DaVinci Code"],
	        movies: ["The Third Man"],
            locations: ["Barcelona, Spain"],
            likes: ["One","Two","Three"]
        }
        refresh("me",this.data.me);
    }

    /////////////////////////////////////////////////////////////////
    // the login stuff

    this.login=function()
    {
        var fb=this;
        FB.login(function(response) {
            if (response.session) {
                facebook_status('You are now logged in. <input type="button" value="Logout" onClick="game.fb.logout();">');
                fb.suck();
            } else {
                facebook_status(":( the login didn't work! " + '<input type="button" value="Login to facebook" onClick="login();">');
            }
        }, {perms:'user_about_me'});
    }

    // ... and the logout
    this.logout=function()
    {
        FB.logout(function()
        {
            //location.href='index.htm';
        });
    };
        


    ////////////////////////////////////////////////////////////////
    // get the data from the api

    this.safe_add = function(whence,data)
    { 
	    if (data!=undefined) 
        {
            debug("adding "+data+" to "+whence);
            this.data[whence].push(data);
        }
    }

    this.suck_likes=function(name,likes)
    {
	    var fb=this;
        var all=[];
        //debug(object_to_string(things));
	    $.each(likes,function(index,thing){
            all.push(thing.name);
            // debug(thing.category);
		    if (thing.category=="Books") fb.safe_add("books",thing.name);
		    if (thing.category=="Movie" || thing.category=="Film") 
			    fb.safe_add("movies",thing.name);		
		    if (thing.category=="Application") fb.safe_add("apps",thing.name);
	    });
        debug(name+" has "+all.length+" likes");
        if (all.length>0)
        {
            fb.data["peoplelikes"].push({
                name: name,
                likes: all});
        }
    }

    this.suck_from_friend = function(name,friend)
    {
	    if (friend.location!=undefined) this.safe_add("locations",friend.location.name);
        if (friend.hometown!=undefined) this.safe_add("locations",friend.hometown.name);

        if (friend.work!=undefined)
        {
            $.each(friend.work,function(work){
			    if (work.employer!=undefined)
                {
                    this.safe_add("locations",work.employer.name);
                }
            });
        }

        if (friend.education!=undefined)
        {
            $.each(friend.education,function(education){
                if (education.school!=undefined)
                {
                    this.safe_add("locations",education.school.name);
                }
            });
        }
        var fb=this;
        FB.api('/'+friend.id+'/likes', function(likes){
            if (likes.data!=undefined) fb.suck_likes(name,likes.data);
        });
    }

    this.suck_friend=function(friend) 
    {
	    var fb=this;
        var name=friend.name.replace(/\s/g, "");
        fb.safe_add("people",name);
 
	    FB.api("/"+friend.id, function(friend) {
            fb.suck_from_friend(name,friend)
	    });	
    }

    this.poll = function()
    {
        if (this.current_friend>-1 && appid!="")
        {
            if (this.current_friend<this.friends.length)
            {
                newLoaderKeyword();
                this.suck_friend(this.friends[this.current_friend]);
                this.current_friend=this.current_friend+1;
            }
            else
            {
                this.current_friend=-1;
            }
        }       

        if (appid=="" && this.current_friend>-1)
        {
            this.current_friend=-1;
        }
    }

    this.suck_friends=function()
    {
	    var fb=this;
        FB.api('/me/friends', function(friends)
        {
            fb.friends=friends.data;
            fb.current_friend=0;
        });
    }

    this.suck_me=function()
    {
        var fb=this;
        FB.api('/me', function(response) {
            fb.data.me = response;
            fb.refresh("me",response);

            FB.api('/me/likes', function(likes){
                fb.data.likes=[];
	            $.each(likes.data,function(index,thing){
                    fb.data.likes.push(thing.name); 
	            })});
            
            //fb.suck_friends();
        });
    }

    this.suck = function()
    {
        debug("looking at facebook data...");
        this.suck_me();
    }

    //if (appid!="") this.init();
}
