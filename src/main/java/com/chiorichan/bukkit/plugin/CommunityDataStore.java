package com.chiorichan.bukkit.plugin;

import java.util.HashMap;
import java.util.Map;

import com.chiorichan.bukkit.plugin.iNations.INations;

public class CommunityDataStore {
	@SuppressWarnings("unused")
	private INations plugin;
	
	public Map<String, Community> communities = new HashMap<String, Community>();
	
	public CommunityDataStore(INations plugin) {
		this.plugin = plugin;
	}
}
