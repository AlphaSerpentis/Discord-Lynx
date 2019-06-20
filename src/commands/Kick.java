package commands;

import java.util.ArrayList;

import init.InitData;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Kick extends Command {

	public Kick() {
		super("kick", "Moderators can use this to kick users from the discord.\n\n`" + InitData.prefix + "kick @AlphaSerpentis#3203 \"Because he bad (please don't use that as an actual reason)\"`");
		ArrayList<Long> ids = new ArrayList<Long>();
		
	}

	@Override
	public boolean action(TextChannel chn, String msg, Object misc) { 
		
		for(long id: getRoleIDs()) {
			
			if(id == ((MessageReceivedEvent) misc).getAuthor().getIdLong()) {
				
				String fullMsg, user, reason;
				
				return true;
				
			}
			
		}
		
		return false;
	}

}
