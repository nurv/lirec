package cmion.level2.migration;

import ion.Meta.Event;

/** an event that can be raised to signal that we would like an agent to migrate into this
 *  (empty) embodiment. The migration file needs to specify devicename of the local embodiment 
 *  and inviteports for the other known devices for this event to have any effect */
public class InviteMigration extends Event {

}
