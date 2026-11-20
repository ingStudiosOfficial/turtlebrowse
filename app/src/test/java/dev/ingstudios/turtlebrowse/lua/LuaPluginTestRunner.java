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
}
