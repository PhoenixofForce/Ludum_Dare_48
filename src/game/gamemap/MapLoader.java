package game.gamemap;

import game.Constants;
import game.Game;
import game.data.hitbox.HitBox;
import game.data.script.Parser;
import game.gameobjects.gameobjects.Text;
import game.gameobjects.gameobjects.cameracontroller.Area;
import game.gameobjects.gameobjects.entities.entities.*;
import game.gameobjects.gameobjects.wall.Background;
import game.gameobjects.gameobjects.wall.Wall;
import game.util.ErrorUtil;
import game.util.FileHandler;
import game.util.TextureHandler;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MapLoader {
	private static final File mapFolder = new File(System.getProperty("user.dir") + File.separator + "maps" + File.separator);

	/**
	 * load a map
	 * @param g context
	 * @param mapName the name of the map
	 * @return a GameMap instance containing the data of the given level
	 */
	public static GameMap load(Game g, String mapName) {
		Scanner fileScanner;
		GameMap map = new GameMap();
		Map<Integer, String> textureReplacements = new HashMap<>();
		Map<Float, Map<HitBox, String>> layers = new HashMap<>();

		if (!mapName.startsWith(Constants.SYS_PREFIX)) {
			File f = new File(mapFolder, mapName + ".map");

			if (!f.exists()) {
				GameMap returnMap = load(g, Constants.START_AREA);
				Text text = new Text(-0.9f, -0.9f, -100, "Something went wrong. We send you back to the Tutorial", 0.05f, false, 0f, 0f, null);
				text.setTimer(300);
				returnMap.addGameObject(text);
				return returnMap;
			}

			map.setMapInfo(mapName.split("/")[0], mapName.split("/")[1]);
			fileScanner = new Scanner(Objects.requireNonNull(FileHandler.loadFile(f)));

		} else {
			fileScanner = new Scanner(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("res/files/systemMaps/" + mapName.replace(Constants.SYS_PREFIX, "") + ".map")));
		}

		{
			String[] lineOne = fileScanner.nextLine().replaceAll(" ", "").replaceAll("\\[", "").replaceAll("]", "").split(";");
			Constants.PIXEL_PER_TILE = Integer.parseInt(lineOne[0]);

			Map<String, String> tags = new HashMap<>();

			int i = 1;
			while (i < lineOne.length) {
				if (lineOne[i].equals("tag")) {
					tags.put(lineOne[i + 1], lineOne[i + 2].replaceAll("\\?", ";").replaceAll("??", ";"));
					i++;
					i++;
				}

				i++;
			}

			if (tags.containsKey("update"))
				map.setOnUpdate(Parser.loadScript(Parser.COMMAND_BLOCK, tags.get("update")));
			if (tags.containsKey("load")) map.setOnLoad(Parser.loadScript(Parser.COMMAND_BLOCK, tags.get("load")));
		}
		float tileSize = Constants.PIXEL_PER_TILE;

		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();
			line = line.replaceAll(" ", "");

			if (line.startsWith("#")) {
				String[] values = line.substring("#".length()).split("-");

				int key = Integer.parseInt(values[0]);
				if (!values[1].startsWith("textures_")) ErrorUtil.printError("No such image: " + values[1]);
				String textureName = values[1].substring("textures_".length());

				textureReplacements.put(key, textureName);
			}

			if (line.startsWith("[layer;")) {
				handleLayer(line, map, layers, textureReplacements, tileSize);
			}

			if (line.startsWith("[put;")) {
				handlePut(line, map, layers, textureReplacements, tileSize);
			}

			if (line.startsWith("[area;")) {
				handleArea(line, map, layers, textureReplacements, tileSize);
			}
		}

		for (float drawingPriority : layers.keySet()) {
			Map<HitBox, String> layer = layers.get(drawingPriority);
			if (drawingPriority <= 0.55 && drawingPriority >= 0.45) map.addGameObject(new Wall(layer, drawingPriority));
			else map.addGameObject(new Background(layer, drawingPriority));

		}
		return map;
	}

	public static void handleLayer(String line, GameMap map, Map<Float, Map<HitBox, String>> layers, Map<Integer, String> textureReplacements, float tileSize) {
		String[] values = line.substring("[layer;".length(), line.length() - 1).split(";");

		float drawingPriority = Float.parseFloat(values[0]);
		int width = Integer.parseInt(values[1]);
		int height = Integer.parseInt(values[2]);

		Map<HitBox, String> hitBoxList;
		if (layers.containsKey(drawingPriority)) {
			hitBoxList = layers.get(drawingPriority);
		} else {
			hitBoxList = new HashMap<>();
			layers.put(drawingPriority, hitBoxList);
		}

		for (int x = 0; x < width; x++) {
			String[] valuesLine = values[3 + x].split(",");
			for (int y = 0; y < height; y++) {
				int tile = Integer.parseInt(valuesLine[y]);

				if (tile != 0) {
					String texture = textureReplacements.get(tile);
					Rectangle textureBounds = TextureHandler.getSpriteSheetBounds("textures_" + texture);

					HitBox hitBox = new HitBox(x, -y - textureBounds.height / tileSize, textureBounds.width / tileSize, textureBounds.height / tileSize);

					switch (texture) {
						case "platform":
						case "platform_left":
						case "platform_middle":
						case "platform_right":
							hitBox.type = HitBox.HitBoxType.HALF_BLOCKING;
							break;

					}

					hitBoxList.put(hitBox, texture);
				}
			}
		}
	}

	public static void handlePut(String line, GameMap map, Map<Float, Map<HitBox, String>> layers, Map<Integer, String> textureReplacements, float tileSize) {
		String[] values = line.substring("[put;".length(), line.length() - 1).replaceAll("\\[", "").replaceAll("]", "").split(";");

		float drawingPriority = Float.parseFloat(values[0]);
		String texture = textureReplacements.get(Integer.parseInt(values[1]));
		Rectangle textureBounds = TextureHandler.getSpriteSheetBounds("textures_" + texture);
		float x = Float.parseFloat(values[2]);
		float y = -Float.parseFloat(values[3]) - textureBounds.height / tileSize;

		Map<String, String> tags = new HashMap<>();

		int i = 4;
		while (i < values.length) {
			if (values[i].equals("tag")) {
				tags.put(values[i + 1], values[i + 2].replaceAll("\\?", ";").replaceAll("??", ";"));
				i++;
				i++;
			}

			i++;
		}

		if(texture.matches("portal_.*")) {
			String target = "";
			if (tags.containsKey("target")) target = map.getDirectory() + "/" + tags.get("target");
			map.addGameObject(new Exit(x, y+0.5f, drawingPriority, target, null));
		}

		else if(texture.matches("player_.*")) {
			map.setSpawnPoint(x, y, drawingPriority);
			map.getCameraController().setSpawn(x, y);
		}

		else if(texture.matches("[A-z0-9]")) {
			map.addGameObject(new Text(x, y, drawingPriority, tags.getOrDefault("text", ""), Float.valueOf(tags.getOrDefault("size", "0.5")), true, Float.valueOf(tags.getOrDefault("anchorX", "0")), Float.valueOf(tags.getOrDefault("anchorY", "0")), null));
		}

		else if(texture.matches("clock_tower_.*")) {
			map.addGameObject(new ClockTower(x, y, drawingPriority));
		}

		else if(texture.matches("door_.*")) {
			map.addGameObject(new Door(x, y, drawingPriority));
		}

		else if(texture.matches("platform_.*")) {
			String target = "0";
			if (tags.containsKey("dist")) target = tags.get("dist");
			map.addGameObject(new Platform(x, y, drawingPriority, Float.parseFloat(target)));
			map.addGameObject(new GhostPlatform(x, y, drawingPriority, Float.parseFloat(target)));
		}

		else {
			HitBox hitBox = new HitBox(x, y, textureBounds.width / tileSize, textureBounds.height / tileSize);
			add(layers, hitBox, texture, drawingPriority);
		}
	}

	public static void handleArea(String line, GameMap map, Map<Float, Map<HitBox, String>> layers, Map<Integer, String> textureReplacements, float tileSize) {
		String[] values = line.substring("[area;".length(), line.length() - 1).replaceAll("\\[", "").replaceAll("]", "").split(";");

		float x1 = Float.parseFloat(values[0]);
		float y2 = -Float.parseFloat(values[1]);
		float x2 = Float.parseFloat(values[2]);
		float y1 = -Float.parseFloat(values[3]);

		Map<String, String> tags = new HashMap<>();

		int i = 4;
		while (i < values.length) {
			if (values[i].equals("tag")) {
				tags.put(values[i + 1], values[i + 2].replaceAll("\\?", ";").replaceAll("??", ";"));
				i++;
				i++;
			}

			i++;
		}

		map.getCameraController().addCameraArea(new Area(x1, y1, x2, y2));
	}

	/**
	 * adds a texture to a a list of layers
	 * @param layers the list of layers
	 * @param hitBox the hitBox of the texture
	 * @param texture the texture name
	 * @param drawingPriority the drawing priority of the texture
	 */
	private static void add(Map<Float, Map<HitBox, String>> layers, HitBox hitBox, String texture, float drawingPriority) {
		if (layers.containsKey(drawingPriority)) {
			layers.get(drawingPriority).put(hitBox, texture);
		} else {
			Map<HitBox, String> layer = new HashMap<>();
			layer.put(hitBox, texture);
			layers.put(drawingPriority, layer);
		}
	}

	/**
	 * returns all files found in a folder
	 * @param folder the start folder
	 * @param all whether all or only the map files should be returned
	 * @return a String[] containing all of them
	 */
	private static String[] getMaps(File folder, boolean all) {
		File[] packages = folder.listFiles(File::isDirectory);

		List<String> maps = new ArrayList<>();

		if (packages == null) return maps.toArray(new String[0]);
		for (File f : packages) {
			File initialMap = new File(f.getAbsolutePath() + "/" + f.getName() + ".map");
			if (initialMap.exists()) maps.add(f.getName() + "/" + f.getName());
			if (all) {
				for (File f2 : Objects.requireNonNull(f.listFiles())) {
					if (f2.getName().endsWith(".map")) maps.add(f.getName() + "/" + f2.getName().replace(".map", ""));
				}
			}
		}
		return maps.toArray(new String[0]);
	}

	/**
	 * loads all maps once
	 * @param game context
	 */
	public static void loadAllMaps(Game game) {
		String[] maps = getMaps(mapFolder, true);

		for (String mapName : maps) {
			try {
				load(game, mapName);
			} catch (Exception e) {
				ErrorUtil.printError("Loading Map " + mapName);
			}
		}
	}
}
