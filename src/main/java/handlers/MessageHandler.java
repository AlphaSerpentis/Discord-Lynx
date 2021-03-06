package handlers;

import java.io.File;

import init.InitData;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class MessageHandler implements EventListener {

	@Override
	public void onEvent(Event event) {

		System.out.println(event);

		/*
		 * Checks if the event triggered is a message type and ISN'T A BOT (you can remove it from the if statement if you wish).
		 */
		if((event instanceof MessageReceivedEvent || (InitData.acceptPriv && event instanceof PrivateMessageReceivedEvent)) && !((MessageReceivedEvent) event).getAuthor().isBot()) {

			/*
			 * Checks the message uses the defined prefix found in InitData.java (you can change the prefix if you need to)
			 */
			if(((MessageReceivedEvent) event).getMessage().getContentDisplay().indexOf(InitData.prefix) == 0) {

				String fullMsg = ((MessageReceivedEvent) event).getMessage().getContentDisplay(), msg;

				if(fullMsg.indexOf(" ") == -1) //If there's no space
					msg = fullMsg.substring(1);
				else
					msg = fullMsg.substring(1, fullMsg.indexOf(" "));

				//Checks the result of the command request
				boolean[] result = CommandHandler.useCommand(msg, event);

				if(!result[0]) {
					System.out.println("Command not found!");
				} else {
					if(!result[1]) {
						System.out.println("Command could not execute! (Are you allowed to use the command?)");
						//sendMessage(((MessageReceivedEvent) event).getTextChannel(), "Command failed to execute"); //Uncomment if you wish.
					}
				}

			}

		}

	}

	/**Sends a message to the specified channel
	 *
	 * @param chn is REQUIRED in order to send a message
	 * @param s is the message
	 */
	public static void sendMessage(TextChannel chn, String s) {

		if(chn.canTalk())
			chn.sendMessage(s).queue();
		else
			System.out.println("Unable to send message, check permissions?");

	}

	public static void sendMessage(TextChannel chn, Message m) {

		if(chn.canTalk())
			chn.sendMessage(m).queue();
		else
			sendMessage(m.getAuthor().openPrivateChannel().complete(), m.getContentDisplay());

	}

	public static void sendMessage(TextChannel chn, String s, File f) {

		if(f != null)
			chn.sendMessage(s).addFile(f).queue();
		else
			System.out.println("Unable to send a message, file doesn't exist?");

	}

	/**Sends a message to the DMs (it may execute, but not go through)
	 *
	 * @param chn is REQUIRED in order to send a message privately
	 * @param s is the message
	 */
	public static void sendMessage(PrivateChannel chn, String s) {

		chn.sendMessage(s).queue();

	}

	//TODO: Do this...
	public void embedMessage() {

	}

}
