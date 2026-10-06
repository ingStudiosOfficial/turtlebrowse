package dev.evilbrowse.type_adapters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javafx.scene.paint.Color;

public class ColorTypeAdapter extends TypeAdapter<Color> {
	@Override
	public void write(JsonWriter out, Color value) throws IOException {
		if (value == null) {
			out.nullValue();
		} else {
			out.value(value.toString());
		}
	}

	@Override
	public Color read(JsonReader in) throws IOException {
		if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		return Color.valueOf(in.nextString());
	}
}
