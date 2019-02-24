package org.l2j.gameserver.data.string;

import org.l2j.commons.data.xml.AbstractHolder;
import org.l2j.gameserver.Config;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.utils.Language;
import io.github.joealisson.primitive.maps.IntObjectMap;
import io.github.joealisson.primitive.maps.impl.HashIntObjectMap;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.l2j.commons.configuration.Configurator.getSettings;

/**
 * @author: Bonux
 */
public final class ItemNameHolder extends AbstractHolder
{
	private static final ItemNameHolder _instance = new ItemNameHolder();

	private final Map<Language, IntObjectMap<String>> _itemNames = new HashMap<>();

	public static ItemNameHolder getInstance()
	{
		return _instance;
	}

	private ItemNameHolder()
	{
		//
	}

	public String getItemName(Language lang, int itemId)
	{
		IntObjectMap<String> itemNames = _itemNames.get(lang);
		String name = itemNames.get(itemId);
		if(name == null)
		{
			if(lang == Language.ENGLISH)
			{
				itemNames = _itemNames.get(Language.RUSSIAN);
				name = itemNames.get(itemId);
			}
			else
			{
				itemNames = _itemNames.get(Language.ENGLISH);
				name = itemNames.get(itemId);
			}
		}
		return name;
	}

	public String getItemName(Player player, int itemId)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getItemName(lang, itemId);
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_itemNames.put(lang, new HashIntObjectMap<>());

			if(!Config.AVAILABLE_LANGUAGES.contains(lang))
				continue;

			var file = getSettings(ServerSettings.class).dataPackRootPath().resolve("data/string/itemname/" + lang.getShortName() + ".txt");
			if(Files.notExists(file)) {
				if(lang == Config.DEFAULT_LANG)
					logger.warn("Not find file: {}", file);
			}
			else
			{
				LineNumberReader reader = null;
				try
				{
					reader = new LineNumberReader(new FileReader(file.toFile()));
					String line = null;
					while((line = reader.readLine()) != null)
					{
						StringTokenizer token = new StringTokenizer(line, "\t");
						if(token.countTokens() < 2)
						{
							logger.error("Error on line: {}; file {}", line, file);
							continue;
						}

						int id = Integer.parseInt(token.nextToken());
						String value = token.nextToken();

						_itemNames.get(lang).put(id, value);
					}
				}
				catch(Exception e)
				{
					logger.error("Exception: " + e, e);
				}
				finally
				{
					try
					{
						reader.close();
					}
					catch(Exception e)
					{
						//
					}
				}
			}
		}

		log();
	}

	public void reload()
	{
		clear();
		load();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, IntObjectMap<String>> entry : _itemNames.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
			logger.info("load item names: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _itemNames.size();
	}

	@Override
	public void clear()
	{
		_itemNames.clear();
	}
}