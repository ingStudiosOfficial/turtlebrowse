package dev.ingstudios.turtlebrowse.lua;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LuaPluginTestRunner {
	@Test
	void canPrint() {
		try (LuaPluginRunner runner = new LuaPluginRunner()) {
			assertDoesNotThrow(() -> runner.run("print('code in lua')"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void canRunApis() {
		try (LuaPluginRunner runner = new LuaPluginRunner()) {
			assertDoesNotThrow(() -> runner.run("print('Browser name: ' .. plugin.browser)"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void canRunCanvasApis() {
		try (LuaPluginRunner runner = new LuaPluginRunner()) {
			assertDoesNotThrow(() -> {
				runner.run("""
						plugin.canvas.testApi = function()
							print('Called test API')
							local CanvasApi = java.import('dev.ingstudios.turtlebrowse.lua.LuaCanvasApi')
							print('Imported canvas API')
							print(CanvasApi)
							local testResult = CanvasApi:testApi()
							print(testResult)
						end
						""");
				runner.run("""
						print('Running test api')
						plugin.canvas.testApi()
						""");
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
