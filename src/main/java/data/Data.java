package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import commands.Command;
import handlers.CommandHandler;
import handlers.MessageHandler;
import init.InitData;
import init.Launcher;
import net.dv8tion.jda.core.entities.Guild;

public class Data {

	/**
	 * This is initialized at startup
	 */
	public static volatile Map<Guild, List<Command>> command_cache;
	public static volatile Map<Guild, JSONObject> srvr_cache;

	//TODO: Clean this up
	/**
	 * Reads the JSON file from the file parameter
	 * @param file to a JSON file
	 * @return the result, otherwise empty ("")
	 */
	public static String readData(String file) {

		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while(line != null) {
				//System.out.println(line);
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	/**
	 * Writes to the designated filepath in a JSON format
	 * @param file the file-path
	 * @param jLine the JSON line
	 * @return
	 */
	public static boolean writeData(String file, String jLine) {
		try {
			FileWriter w = new FileWriter(new File(file));

			System.out.println("[Data.java] Writing to " + file);

			w.write(jLine);
			w.close();

			if(file.equals(InitData.locationJSON)) //Updates the cache
				initCache();

			return true;

		} catch (IOException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
	}

	//Misc Methods
	public static File createBackup(boolean temp) {
		long inst = Instant.now().getEpochSecond();
		if(temp) {
			writeData(InitData.locationBackup + "BACKUP-TMP-" + inst + ".json", readData(InitData.locationJSON));
			return new File(InitData.locationBackup + "BACKUP-TMP-" + inst + ".json");
		} else {
			writeData(InitData.locationBackup + "BACKUP-" + inst + ".json", readData(InitData.locationJSON));
			return new File(InitData.locationBackup + "BACKUP-" + inst + ".json");
		}
	}

	public static void obtainBackup() {

	}

	public static boolean deleteGuild(Guild gld) {

		String jsonData = readData(InitData.locationJSON);
		JSONObject obj = new JSONObject(jsonData);

		for(String id: obj.keySet()) {
			if(id.equals(gld.getId())) {
				obj.remove(id);

				return writeData(InitData.locationJSON, obj.toString());
			}
		}

		return false;
	}
	public static boolean addGuild(Guild gld) {

		String jsonData = readData(InitData.locationJSON);
		JSONObject obj = new JSONObject(jsonData);

		for(String id: obj.keySet()) {
			if(id.equals(gld.getId()))
				return false;
		}

		obj.put(gld.getId(), obj.get("DEFAULT"));

		return writeData(InitData.locationJSON, obj.toString());

	}
	public static boolean hasGuild(Guild gld) {
		return srvr_cache.containsKey(gld) && command_cache.containsKey(gld);
	}

	public static boolean editGuild(Guild gld, Object obj) {
		return false;
	}

	/**
	 *
	 * @param obj
	 * @return
	 */
	public static boolean checkDefaults(JSONObject obj, String srvr) {

		JSONObject dflt = new JSONObject(readData(InitData.locationJSON)).getJSONObject("DEFAULT");
		ArrayList<String> s_keys = new ArrayList<String>(((JSONObject) obj.get(srvr)).keySet()), d_keys = new ArrayList<String>(dflt.keySet()); //s for server and d for defaults

		for(String key: d_keys) {
			if(!s_keys.contains(key)) {
				System.out.println("[Data.java] MISSING " + key + "! Adding it to the server's data!");

				JSONObject newObj = new JSONObject(obj.toString());
				((JSONObject) newObj.get(srvr)).put(key, dflt.get(key));

				if(writeData(InitData.locationJSON, newObj.toString())) {
					System.out.println("[Data.java] checkDefaults() successfully modified data!");
				} else {
					System.out.println("[Data.java] CRITICAL ERROR!");
				}
			}
		}
		return false;
	}

	/**
	 * Initializes the cache of saved servers from the resources/guildData.json file.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void initCache() throws InstantiationException, IllegalAccessException {

		if(!InitData.acceptMultipleServers) return;

		String jsonData = readData(InitData.locationJSON);

		if(jsonData.isEmpty()) {
			System.out.println("Shutting down! Cache cannot be initialized... Make sure guildData.json isn't empty, at least having the \"DEFAULT\" object");
			System.exit(-1);
		} //TODO: Rewrite to call obtainBackup() to search for backups

		JSONObject obj = new JSONObject(jsonData);

		command_cache = new HashMap<Guild, List<Command>>();
		srvr_cache = new HashMap<Guild, JSONObject>();

		for(String key: obj.keySet()) {

			if(key.equals("DEFAULT")) continue;

			System.out.println("[Data.java]: (initCache()) " + key);
			System.out.println("[Data.java]: (initCache()) " + obj.get(key));

			System.out.println("[Data.java]: Checking if server contains all the needed keys...");
			checkDefaults(obj, key);

			ArrayList<Command> cmds = new ArrayList<Command>();

			for(Command c: CommandHandler.ALL_COMMANDS) {
				try {
					cmds.add((Command) c.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}

			JSONObject cmds_config = obj.getJSONObject(key).getJSONObject("cmds_config"), srvr_config = obj.getJSONObject(key).getJSONObject("srvr_config");

			//COMMANDS CONFIG
			for(String con_key: cmds_config.keySet()) {
				System.out.println("COMMANDS CONFIG: Setting up " + cmds_config.getJSONObject(con_key));

				JSONObject in_config = cmds_config.getJSONObject(con_key);
				Command cmd = null;

				for(Command c: cmds) {
					if(c.getName().equalsIgnoreCase(con_key)) {
						cmd = c;
					}
				}

				if(cmd == null) continue;

				for(String in_key: in_config.keySet()) {

					System.out.println("[Data.java] in_key: " + in_key);

					//START SWITCH STATEMENT
					switch(in_key) {

					case "roleIDs":

						if(cmd.getRequirePerms()) {

							if(cmd.getPerms().isEmpty()) {
								cmd.setPerms(new HashMap<String, ArrayList<Long>>());
								System.out.println("Command \"" + cmd.getName() + "\" is empty!");
							}

							if(in_config.get("roleIDs") != null) {
								try {
									for(Object id: in_config.getJSONArray("roleIDs")) {
										cmd.addPerm("ROLE", (long) id);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}

						break;

					case "userIDs":

						if(cmd.getRequirePerms()) {

							if(cmd.getPerms().isEmpty()) {
								cmd.setPerms(new HashMap<String, ArrayList<Long>>());
								System.out.println("Command \"" + cmd.getName() + "\" is empty!");
							}

							if(in_config.get("userIDs") != null) {
								try {
									for(Object id: in_config.getJSONArray("userIDs")) {
										cmd.addPerm("USER", (long) id);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}

						break;
					case "active":

						cmd.setActive(in_config.getBoolean("active"));

						break;
					case "logging":

						cmd.setLogging(in_config.getBoolean("logging"));

						break;
					}
					//END SWITCH STATEMENT

				}

			}
			//SERVER CONFIG
			System.out.println("[Data.java] PUTTING " + Launcher.api.getGuildById(key).getName() + " INTO THE CACHE!");
			srvr_cache.put(Launcher.api.getGuildById(key), srvr_config);

			command_cache.put(Launcher.api.getGuildById(key), cmds);
			System.out.println(srvr_cache);
			System.out.println(command_cache + "\n\n\n");
		}

	}

}
