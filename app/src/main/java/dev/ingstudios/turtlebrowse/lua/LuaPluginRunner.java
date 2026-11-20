package dev.ingstudios.turtlebrowse.lua;

import dev.ingstudios.turtlebrowse.Main;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua55.Lua55;
import party.iroiro.luajava.value.LuaValue;

public class LuaPluginRunner implements AutoCloseable {
	private final Lua lua;

	public LuaPluginRunner() throws Exception {
		this.lua = new Lua55();
		try {
			setGlobals();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void setGlobals() {
		lua.run("plugin = {}");
		final LuaValue pluginTable = lua.eval("return plugin")[0];
		pluginTable.set("browser", "turtlebrowse");
		final String browserVersion = Main.getBrowserVersion();
		pluginTable.set("browser_version", browserVersion != null ? browserVersion : "v1.0.0");
	}

	public void run(String code) {
		lua.run(code != null ? code : "");
	}

	@Override
	public void close() throws Exception {
		lua.close();
	}
}
