#!/usr/bin/python 
# Naked on Pluto Copyright (C) 2010 Aymeric Mansoux, Marloes de Valk, Dave Griffiths
#                                       
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
                                                                                                            
# updates all the online stuff from the game's output

import xmlrpclib, sys, os, time, tweepy, time, string_maker

sm = string_maker.string_maker()

def build_msg(msg):
    return sm.msg_to_string(msg)

def read_msg(dir):
    msg={}
    f = open(dir+"/msg", "r")
    print(f.read())
    f.close()
    f = open(dir+"/msg", "r")
    msg["display"] = f.readline()[:-1]
    msg["fromm"] = f.readline()[:-1]
    msg["emotion"] = f.readline()[:-1]
    msg["code"] = f.readline()[:-1]
    c = int(float(f.readline()[:-1]))
    msg["extra"] = []
    for i in range(0,c):
        msg["extra"].append(f.readline()[:-1])
    f.close()
    return msg


def tweet_api(secrets,i):
    return 0
    auth = tweepy.OAuthHandler(secrets["tweetauth0"], secrets["tweetauth1"])
    # how to get the token the first time around:                                                                             
    #print(auth.get_authorization_url())                                                                                      
    #verifier = raw_input('Verifier:')                                                                                        
    #auth.get_access_token(verifier)                                                                                          
    #print(auth.access_token.key)                                                                                             
    #print(auth.access_token.secret)                                                                                          
    auth.set_access_token(secrets["tweetauth2"],secrets["tweetauth3"])
    return tweepy.API(auth)

def tweet(dir,api):
    if os.path.isfile(dir+"/msg"):
        msg = read_msg(dir)
#        print(msg)
#        os.remove(dir+"msg")
        txt = build_msg(msg)
        print(txt)
        #try:
        #    api.update_status(txt)
        #except tweepy.error.TweepError:
        #    print("oops")

def load_secrets():
    secrets = {}
    secretsfile = open("topsecret","r")
    for line in secretsfile: 
        secret=line.split(" ")
        secrets[secret[0]]=secret[1][:-1]
    secretsfile.close()
    return secrets

secrets=0
#secrets = load_secrets()
location = '.'
tweetdirs = [[location+"/TreeSpirit",tweet_api(secrets,0)],
             [location+"/CoverSpirit",tweet_api(secrets,1)],
             [location+"/ShrubSpirit",tweet_api(secrets,2)],]


while True:
    tweet(tweetdirs[0][0],tweetdirs[0][1])
    tweet(tweetdirs[1][0],tweetdirs[1][1])
    tweet(tweetdirs[2][0],tweetdirs[2][1])
    time.sleep(1)


