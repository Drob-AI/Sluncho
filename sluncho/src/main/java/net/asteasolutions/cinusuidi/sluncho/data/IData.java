package net.asteasolutions.cinusuidi.sluncho.data;

import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntity;

public interface IData {
	public List<NamedEntity> toNamedEntity();
}
