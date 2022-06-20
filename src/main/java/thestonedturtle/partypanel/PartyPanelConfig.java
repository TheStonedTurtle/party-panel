package thestonedturtle.partypanel;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("partypanel")
public interface PartyPanelConfig extends Config
{
	@ConfigItem(
		keyName = "alwaysShowIcon",
		name = "Always show sidebar",
		description = "<html>Controls whether the sidebar icon is always shown (checked) or only shown while inside a party (unchecked)</html>"
	)
	default boolean alwaysShowIcon()
	{
		return false;
	}

	@ConfigItem(
		keyName = "autoExpandMembers",
		name = "Expand members by default",
		description = "<html>Controls whether party member details are automatically expanded (checked) or collapsed into banners (unchecked)</html>"
	)
	default boolean autoExpandMembers()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showPartyControls",
		name = "Show Party Controls",
		description = "<html>Controls whether we display the party control buttons like create and leave party</html>"
	)
	default boolean showPartyControls()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPartyPassphrase",
		name = "Show Party Passphrase",
		description = "<html>Controls whether the party passphrase is displayed within the UI<br/>If disabled and party controls are shown you can still copy</html>"
	)
	default boolean showPartyPassphrase()
	{
		return true;
	}

	@ConfigItem(
		keyName = "previousPartyId",
		name = "",
		description = "",
		hidden = true
	)
	default String previousPartyId()
	{
		return "";
	}

	@ConfigItem(
		keyName = "previousPartyId",
		name = "",
		description = "",
		hidden = true
	)
	void setPreviousPartyId(String id);
}
