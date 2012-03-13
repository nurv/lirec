import rnd_gen

class string_maker:
    def __init__(self):
        self.reasons = {
            "clover": "nutrients",
            "dandelion": "nutrients",
            "aronia": "erosion control",
            "apple": "protection",
            "cherry": "protection",
            "boletus": "increased nutrients",
            "chanterelle": "increased nutrients",
            "flyagaric": "increased magic"
        };


        self.rnd = rnd_gen.rnd_gen()
        self.msg_map={
            "i_have_been_planted": self.i_have_been_planted,
            "i_am_ill": self.i_am_ill,
            "i_have_fruited": self.i_have_fruited,
            "i_have_died": self.i_have_died,
            "i_have_recovered": self.i_have_recovered,
            "i_have_been_picked_by": self.i_have_been_picked_by,
            "your_plant_doesnt_like": self.your_plant_doesnt_like,
            "your_plant_needs": self.your_plant_needs,
            "needs_help": self.needs_help,
            "i_am_recovering": self.i_am_recovering,
            "i_am_detrimented_by": self.i_am_detrimented_by,
            "i_am_detrimental_to": self.i_am_detrimental_to,
            "i_am_benefitting_from": self.i_am_benefitting_from,
            "i_am_beneficial_to": self.i_am_beneficial_to,
            "thanks_for_helping": self.thanks_for_helping,
            "spirit_complaint": self.spirit_complaint,
            "spirit_praise": self.spirit_praise,
            "spirit_received_offering": self.spirit_received_offering,
            "ive_asked_x_for_help": self.ive_asked_x_for_help,
            "one_time_i_have_flowered": self.one_time_i_have_flowered,
            "gift_received": self.gift_received,
            "gift_sent": self.gift_sent
            }



    def i_have_been_planted(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant has just germinated!"
    
    def i_am_ill(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant is feeling ill."
    
    def i_have_fruited(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant has fruited."
    
    def i_have_died(self,fromm,to,owner,extra,emotion):    
        return owner+"'s "+fromm+" plant has died."
    
    def i_have_recovered(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant has recovered."
    
    def i_have_been_picked_by(self,fromm,to,owner,extra,emotion):           
        return owner+"'s "+fromm+" plant has been picked by "+extra[0]
    
    def your_plant_doesnt_like(self,fromm,to,owner,extra,emotion):
        return extra[0]+", your "+extra[3]+" plant doesn't like "+to+"'s "+extra[2]+" plant nearby.";
    
    def your_plant_needs(self,fromm,to,owner,extra,emotion):
        return to+", your "+extra[0]+" plant needs a "+extra[1]+" plant nearby for "+self.reasons[extra[1]]+".";
    
    def needs_help(self,fromm,to,owner,extra,emotion):
        return to+", "+extra[0]+"'s "+extra[2]+" plant needs a "+extra[3]+" near.";
    
    def i_am_recovering(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant is recovering.";
    
    def i_am_detrimented_by(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant is being harmed by "+extra[0]+"'s "+extra[2]+" plant.";
    
    def i_am_detrimental_to(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant is harming "+extra[0]+"'s "+extra[2]+" plant.";
    
    def i_am_benefitting_from(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant is being helped by "+extra[0]+"'s "+extra[2]+" plant.";
    
    def i_am_beneficial_to(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant is helping "+extra[0]+"'s "+extra[2]+" plant.";
    
    def thanks_for_helping(self,fromm,to,owner,extra,emotion):
        return owner+"'s "+fromm+" plant thanks "+to+"'s "+extra[0]+" plant for helping.";
    
    def spirit_complaint(self,fromm,to,owner,extra,emotion):
        e="";
        if (emotion=="LOVE"): e="I am happy, but ";
        if (emotion=="HATE"): e="I am sad, because ";
        if (emotion=="PRIDE"): e="I am great, but ";
        if (emotion=="DISTRESS"): e="This is awful, ";
        if (emotion=="ADMIRATION"): e="Everythings fine, but ";
        if (emotion=="JOY"): e="I'm too happy to care, but ";
        if (emotion=="SHAME"): e="I'm so useless, ";
        if (emotion=="RESENTMENT"): e="It sucks that ";
        
        msg=e+to+"'s " + extra[0] + " plant is ";
        if (extra[1]=="ill-a"): return msg+"a little ill";
        if (extra[1]=="ill-b"): return msg+"quite ill";
        if (extra[1]=="ill-c"): return msg+"very ill";
        if (extra[1]=="detriment"): return msg+"harming my plants.";
        if (extra[1]=="new-fruit"): return msg+"has fruited.";
        if (extra[1]=="picked-fruit"): return msg+"has been picked.";
        return msg+"... something. (bug: "+extra[1]+")";
    
    def spirit_praise(self,fromm,to,owner,extra,emotion):
        e="";
        if (emotion=="LOVE"): e="I am happy that ";
        if (emotion=="HATE"): e="I am sad, but ";
        if (emotion=="PRIDE"): e="I am great, ";
        if (emotion=="DISTRESS"): e="Things are awful, but ";
        if (emotion=="ADMIRATION"): e="It's great, ";
        if (emotion=="JOY"): e="Wow! - ";
        if (emotion=="SHAME"): e="I'm useless but, ";
        if (emotion=="RESENTMENT"): e="Things are bad, but ";
        
        msg=e+to+"'s " + extra[0] + " plant ";
        if (extra[1]=="grow-a"): return msg+"is growing well.";
        if (extra[1]=="fruit-a"): return msg+"has flowered!";
        if (extra[1]=="fruit-b"): return msg+"is fruiting.";
        if (extra[1]=="fruit-c"): return msg+"has fruited.";
        if (extra[1]=="recovery-to-b"): return msg+"was very ill and is recovering.";
        if (extra[1]=="recovery-to-a"): return msg+"is recovering.";
        if (extra[1]=="finished-recovery"): return msg+"has fully recovered.";
        if (extra[1]=="benefit"): return msg+"is helping my plants.";
        if (extra[1]=="new-fruit"): return msg+"has fruited.";
        if (extra[1]=="picked-fruit"): return msg+"has been picked.";
        return msg+"... something. (bug: "+extra[1]+")";
    
    def spirit_received_offering(self,fromm,to,owner,extra,emotion):
        # what caused this message?
        feeling="kindly"; # praise
        if (extra[0]=="complain" or
            extra[0]=="diagnose"):
            feeling="annoyingly";
            
        return to+" has " + feeling + " given me a " + extra[1] + " layer fruit gift!";
        
    def ive_asked_x_for_help(self,fromm,to,owner,extra,emotion):
        msg=to+", I've asked "+extra[0]+" to help with your "+extra[2]+" plant";
        if (extra[3]=="ill-a"): msg+=", which is a little ill.";
        else: 
            if (extra[3]=="ill-b"): msg+=", which is a quite ill.";
            else: 
                if (extra[3]=="ill-c"): msg+=", which is a very ill.";
                else: msg+=".";
        return msg;
        
    def one_time_i_have_flowered(self,fromm,to,owner,extra,emotion):
        return to+", your "+fromm+" plant has flowered, and your score has increased!";
    
    def gift_received(self,fromm,to,owner,extra,emotion):
        return extra[0]+", you have been given a "+extra[2]+" fruit by "+extra[1]+"!";
    
    def gift_sent(self,fromm,to,owner,extra,emotion):
        return extra[0]+", you have given a "+extra[2]+" fruit to "+extra[1]+"!";
    
    
    def msg_to_string(self,msg):
        owner=msg["fromm"];
        return self.msg_map[msg["code"]](msg["fromm"],msg["display"],owner,msg["extra"],msg["emotion"]);
    
