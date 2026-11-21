package dev.ingstudios.turtlebrowse.lua;

import dev.ingstudios.turtlebrowse.Main;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua54.Lua54;
import party.iroiro.luajava.value.LuaValue;

public class LuaPluginRunner implements AutoCloseable {
	private final Lua lua;
	private LuaValue canvasApi;

	public LuaPluginRunner() throws Exception {
		this.lua = new Lua54();
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

		setCanvasApis(pluginTable);
	}

	private void setCanvasApis(LuaValue pluginTable) {
		lua.run("plugin.canvas = {}");
		canvasApi = lua.eval("return plugin.canvas")[0];

		lua.run("""
				plugin.canvas.createText = function(text)
					local CanvasApi = java.import('dev.ingstudios.turtlebrowse.lua.LuaCanvasApi')
					CanvasApi:createText(text)
				end
				""");
	}

	public void run(String code) {
		lua.run(code != null ? code : "");
	}

	public LuaValue getCanvasApi() {
		return canvasApi;
	}

	@Override
	public void close() throws Exception {
		lua.close();
	}
}
