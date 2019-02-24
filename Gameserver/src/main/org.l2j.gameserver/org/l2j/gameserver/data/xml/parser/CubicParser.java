package org.l2j.gameserver.data.xml.parser;

import io.github.joealisson.primitive.maps.IntIntMap;
import io.github.joealisson.primitive.maps.impl.HashIntIntMap;
import org.dom4j.Element;
import org.l2j.commons.data.xml.AbstractParser;
import org.l2j.gameserver.Config;
import org.l2j.gameserver.data.xml.holder.CubicHolder;
import org.l2j.gameserver.data.xml.holder.SkillHolder;
import org.l2j.gameserver.model.Skill;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.templates.CubicTemplate;

import java.io.File;
import java.util.Iterator;

import static org.l2j.commons.configuration.Configurator.getSettings;

/**
 * @author VISTALL
 * @date  15:24/22.12.2010
 */
public final class CubicParser extends AbstractParser<CubicHolder>
{
	private static CubicParser _instance = new CubicParser();

	public static CubicParser getInstance()
	{
		return _instance;
	}

	protected CubicParser()
	{
		super(CubicHolder.getInstance());
	}

	@Override
	public File getXMLPath() {
		return getSettings(ServerSettings.class).dataPackRootPath().resolve("data/cubics.xml").toFile();
	}

	@Override
	public String getDTDFileName()
	{
		return "cubics.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element cubicElement = iterator.next();
			int id = Integer.parseInt(cubicElement.attributeValue("id"));
			int level = Integer.parseInt(cubicElement.attributeValue("level"));
			int slot = cubicElement.attributeValue("slot") == null ? id : Integer.parseInt(cubicElement.attributeValue("slot"));
			int duration = Integer.parseInt(cubicElement.attributeValue("duration"));
			int delay = Integer.parseInt(cubicElement.attributeValue("delay"));
			int max_count = cubicElement.attributeValue("max_count") == null ? Integer.MAX_VALUE : Integer.parseInt(cubicElement.attributeValue("max_count"));
			CubicTemplate.UseUpType use_up = cubicElement.attributeValue("use_up") == null ? CubicTemplate.UseUpType.INCREASE_DELAY : CubicTemplate.UseUpType.valueOf(cubicElement.attributeValue("use_up").toUpperCase());
			double power = cubicElement.attributeValue("power") == null ? 0 : Double.parseDouble(cubicElement.attributeValue("power"));
			CubicTemplate.TargetType target_type = cubicElement.attributeValue("target_type") == null ? CubicTemplate.TargetType.TARGET : CubicTemplate.TargetType.valueOf(cubicElement.attributeValue("target_type").toUpperCase());
			CubicTemplate template = new CubicTemplate(id, level, slot, duration, delay, max_count, use_up, power, target_type);
			getHolder().addCubicTemplate(template);

			// skill
			for(Iterator<Element> skillIterator = cubicElement.elementIterator("skill"); skillIterator.hasNext();)
			{
				Element skillElement = skillIterator.next();
				int chance2 = skillElement.attributeValue("chance") == null ? 100 : Integer.parseInt(skillElement.attributeValue("chance"));
				int id2 = Integer.parseInt(skillElement.attributeValue("id"));
				int level2 = Integer.parseInt(skillElement.attributeValue("level"));
				int use_chance2 = skillElement.attributeValue("use_chance") == null ? 100 : Integer.parseInt(skillElement.attributeValue("chance"));
				boolean canAttackDoor = Boolean.parseBoolean(skillElement.attributeValue("can_attack_door"));
				CubicTemplate.ActionType type = CubicTemplate.ActionType.valueOf(skillElement.attributeValue("action_type"));

				IntIntMap set = new HashIntIntMap();
				for(Iterator<Element> chanceIterator = skillElement.elementIterator(); chanceIterator.hasNext();)
				{
					Element chanceElement = chanceIterator.next();
					int min_hp_percent = Integer.parseInt(chanceElement.attributeValue("min_hp_percent"));
					int max_hp_percent = Integer.parseInt(chanceElement.attributeValue("max_hp_percent"));
					int value = Integer.parseInt(chanceElement.attributeValue("value"));
					for(int i = min_hp_percent; i <= max_hp_percent; i++)
						set.put(i, value);
				}

				if(use_chance2 == 0 && set.isEmpty())
				{
					logger.warn("Wrong skill chance. Cubic: " + id + "/" + level);
				}

				Skill skill = SkillHolder.getInstance().getSkill(id2, level2);
				if(skill == null)
					continue;

				template.putSkill(new CubicTemplate.SkillInfo(skill, use_chance2, type, canAttackDoor, set), chance2);
			}
		}
	}
}