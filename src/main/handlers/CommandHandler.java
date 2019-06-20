package main.handlers;

import main.commands.*;
import main.init.InitData;
import main.init.Launcher;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandHandler {
	
	/**
	 * The types of command types there are
	 * 
	 */
	public static final String[] cmdTypes = {
			"General", "Moderation", "Misc", "Bot Owner"
	};
	
	/**Searches for commands
	 * 
	 * @param search
	 * @param event
	 * @return {true, true} if everything is cleared, {true, false} if command is valid, but unable to execute, {false, false} if the command is not found
	 */
	public static boolean[] useCommand(String search, Event event) {
		
		User usr = ((MessageReceivedEvent) event).getMember().getUser();
		TextChannel chn = ((MessageReceivedEvent) event).getTextChannel();
		boolean priv = (chn.getType() == ChannelType.PRIVATE);
		
		boolean[] returnThis = new boolean[2];

		switch(search.toLowerCase()) {
		
		//General
		case "help":
			returnThis[0] = true;
			returnThis[1] = new Help().action(chn, ((MessageReceivedEvent) event).getMessage().getContentDisplay(), null);
			return returnThis;
		case "about":
			returnThis[0] = true;
			returnThis[1] = new About().action(chn, null, null);
			return returnThis;
		//Moderator
		case "kick":
			returnThis[0] = true;
			returnThis[1] = new Kick().action(chn, null, event);
			return returnThis;
			
		//Bot Owner Commands
		case "shutdown":
			returnThis[0] = true;
			returnThis[1] = shutdown(chn, usr);
			return returnThis;
		
		}
		
		return returnThis;
		
	}
	
	//Bot Owner Command
	
	/**
	 * TODO: Shutdown needs to have its own class
	 * 
	 * @param chn the channel where the shutdown was requested
	 * @param usr is the user requesting the shutdown
	 * @return TRUE if the shutdown request was done by a Bot Owner, otherwise false
	 */
	public static boolean shutdown(TextChannel chn, User usr) {
		
		for(Long id: InitData.botOwnerIDs) {
			if(id.equals(usr.getIdLong())) {
				MessageHandler.sendMessage(chn, "Shutting down!");
				
				Launcher.api.shutdown();
				return true;
			}
		}
		
		return false;
	}

}
