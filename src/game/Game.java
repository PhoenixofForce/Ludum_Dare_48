package game;

import game.data.hitbox.HitBox;
import game.gamemap.GameMap;
import game.gamemap.MapLoader;
import game.gameobjects.CollisionObject;
import game.gameobjects.GameObject;
import game.gameobjects.gameobjects.Fade;
import game.gameobjects.gameobjects.entities.BasicDrawingEntity;
import game.gameobjects.gameobjects.entities.entities.Clock;
import game.gameobjects.gameobjects.entities.entities.Player;
import game.gameobjects.gameobjects.particle.ParticleSystem;
import game.util.SaveHandler;
import game.util.TimeUtil;
import game.window.Camera;
import game.window.Drawable;
import game.window.Keyboard;
import game.window.Window;
import jdk.nashorn.internal.runtime.options.Option;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Game {
	private Window window;							//displays the game

	private int gameTick;							//current tick of the game (starts at 0) -> 60 Ticks Per Second

	private List<GameObject> gameObjects;			//list of gameObjects, that are updated every tick
	private List<CollisionObject> collisionObjects;	//list of collisionObjects, that are used when calculating collision
	private List<Player> players;					//list of players, that are current
	private List<Integer> inputs;					//list of inputs, that are used by the players
	private List<Color> playerColors;				//list of playerColors, that are used to recolor the Player
	private GameMap map;							//current GameMap

	private int fadeStart;							//The startTick of a transition between maps
	private String newMap;							//The target map for a map change
	private Queue<GameObject> toRemove;				//list of gameObjects, that are removed next Tick
	private Map<GameObject, Boolean> removeMapChange;
	private Queue<GameObject> toAdd;				//list of gameObjects, that are added next Tick

	private ParticleSystem particleSystem;			//display and store all particles

	private Map<String, Integer> values;			//store all in game variables -> SaveGame

	private int gameTime;
	private int freezeTicks;

	private boolean hasClock;

	public Game(Window window) {
		this.window = window;
		Options.applyOptions(this);

		gameTick = 0;
		freezeTicks = 0;
		hasClock = false;

		players = new ArrayList<>();
		inputs = new ArrayList<>();
		playerColors = new ArrayList<>();
		addPlayerColors("#193D3f", "#327345", "#63c64d", "#ffe762", "#fb922b", "#e53b44", "#9e2835", "#2ce8f4", "#0484d1", "#124e89", "#68386c", "#b55088", "#f6757a", "#181425", "#181425", "#e8b796", "#3a4466", "#c0cbdc");
		Collections.shuffle(playerColors);
		gameObjects = new LinkedList<>();
		collisionObjects = new LinkedList<>();
		toRemove = new ConcurrentLinkedQueue<>();
		toAdd = new ConcurrentLinkedQueue<>();
		removeMapChange = new HashMap<>();

		addGameObject(new Clock(0.15f));

		values = new HashMap<>();

		//Start the game in the "menu" map
		setGameMap(Constants.START_AREA, false);
	}

	/**
	 * Update the game 60 times per second
	 **/
	public void gameLoop() {
		long time;

		while (window.isRunning()) {
			if (freezeTicks > 0) {
				freezeTicks -= 1;
				gameTime -= Constants.REWIND_SPEED;
			} else {
				gameTime += 1;
			}
			gameTick++;
			time = TimeUtil.getTime();
			handleInput();

			//change map
			if (newMap != null && gameTick - fadeStart >= Constants.FADE_TIME / 2) {
				GameMap newGameMap = MapLoader.load(this, newMap);
				if (map != null) {
					for (GameObject gameObject : map.getGameObjects()) {
						this.removeGameObject(gameObject, true);
					}
				}

				for (GameObject gameObject : newGameMap.getGameObjects()) {
					this.addGameObject(gameObject);
				}

				for (Player player : players) {
					player.respawn(newGameMap.getSpawnX(), newGameMap.getSpawnY(), newGameMap.getPlayerDrawingPriority());
				}

				map = newGameMap;
				gameTime = 0;
				freezeTicks = 0;
				map.load(this);
				newMap = null;
			}

			removeObjects();
			boolean addedNew = addObjects();

			//Sort gameObjects for priority
			if(addedNew) {
				collisionObjects.sort((o1, o2) -> Float.compare(o2.getCollisionPriority(), o1.getCollisionPriority()));
				gameObjects.sort((o1, o2) -> Float.compare(o2.getPriority(), o1.getPriority()));
			}

			//Update every gameObject
			map.update(this);
			for (GameObject gameObject : gameObjects) {
				gameObject.update(this);
			}

			/*	//Highlight interactable objects
			for (CollisionObject collisionObject : getCollisionObjects()) {
				if (collisionObject instanceof Player || !(collisionObject instanceof BasicDrawingEntity)) continue;
				for (HitBox hitBox2 : collisionObject.getCollisionBoxes()) {
					Optional<Float> distance = players.stream().map(p -> p.getHitBox().distance(hitBox2)).min(Float::compare);
					if((distance.orElse(Constants.INTERACTION_REACH + 1)) < Constants.INTERACTION_REACH) {
						((BasicDrawingEntity) collisionObject).setColor(Color.WHITE);
					} else {
						((BasicDrawingEntity) collisionObject).setColor(new Color(0, 0, 0, 0));
					}
				}
			}*/


			//Sync the updates to TPS
			long newTime = TimeUtil.getTime();
			TimeUtil.sleep((int) (1000.0f / Constants.TPS - (newTime - time)));
		}

		cleanUp();
	}

	private boolean addObjects() {
		//Add gameObjects
		boolean out = false;
		while (!toAdd.isEmpty()) {
			out = true;

			GameObject gameObject = toAdd.poll();

			gameObject.init(this);

			gameObjects.add(gameObject);
			if (gameObject instanceof CollisionObject) collisionObjects.add((CollisionObject) gameObject);
			if (gameObject instanceof Drawable) window.addDrawable((Drawable) gameObject);
			if (gameObject instanceof ParticleSystem) particleSystem = (ParticleSystem) gameObject;
			if (gameObject instanceof Player) {
				players.add((Player) gameObject);
				// ((Player) gameObject).setColor(playerColors.get(players.size() - 1));
			}
		}

		return out;
	}

	private void removeObjects() {
		//Remove gameObjects
		while (!toRemove.isEmpty()) {
			GameObject gameObject = toRemove.poll();

			boolean mapChange = removeMapChange.get(gameObject);
			removeMapChange.remove(gameObject);

			gameObject.remove(this, mapChange);
			gameObjects.remove(gameObject);
			if (gameObject instanceof CollisionObject) collisionObjects.remove(gameObject);
			if (gameObject instanceof Drawable) window.removeDrawable((Drawable) gameObject);
			if (gameObject instanceof ParticleSystem) particleSystem = null;
			if (gameObject instanceof Player) {
				int id = players.indexOf(gameObject);
				players.remove(id);
				inputs.remove(id);
				Color color = playerColors.get(id);
				playerColors.remove(id);
				playerColors.add(color);
			}
		}
	}

	/**
	 * update the Keyboard and Controller Inputs
	 **/
	private void handleInput() {
		Keyboard keyboard = window.getKeyboard();

		//spawn new players
		for (int i = 0; i < 18; i++) {
			if (keyboard.isPressed(Options.controls.get("UP" + i)) && !inputs.contains(i)) {
				Player newPlayer = new Player(map.getSpawnX(), map.getSpawnY(), map.getPlayerDrawingPriority());
				this.addGameObject(newPlayer);
				inputs.add(i);
			}
		}

		//update the pressed keys for the players
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			int input = inputs.get(i);

			player.setJumping(keyboard.isPressed(Options.controls.get("UP" + input)));
			player.setMx(keyboard.getPressed(Options.controls.get("RIGHT" + input)) - keyboard.getPressed(Options.controls.get("LEFT" + input)));
			player.setDown(keyboard.isPressed(Options.controls.get("DOWN" + input)));
			player.setInteracting(keyboard.isPressed(Options.controls.get("INTERACT" + input)));
			player.setRewinding(keyboard.isPressed(Options.controls.get("ATTACK" + input)));
			if (keyboard.isPressed(Options.controls.get("RESET" + input))) restartMap();
		}
	}

	/**
	 * executed when the window closes -> Save Options
	 **/
	private void cleanUp() {
		Options.save();
	}

	/**
	 * loads the current map again
	 **/
	public void restartMap() {
		if (map.getDirectory() != null && !map.getDirectory().equals("hidden"))
			setGameMap(map.getDirectory() + "/" + map.getName(), true);
	}

	public void rewind() {
		freezeTicks = Constants.REWIND_TICKS;
		gameObjects.forEach(e -> {
			if(e instanceof CollisionObject) {
				((CollisionObject) e).interact(players.get(0), null, CollisionObject.InteractionType.REWIND);
			}
		});
	}

	/**
	 * starts transition into a new map
	 *
	 * @param name the target map
	 * @param fade if the transition should include a fade effect
	 * @return true, if the map can be changed | false, if the map is changing currently
	 **/
	public boolean setGameMap(String name, boolean fade) {
		if (newMap == null) {
			newMap = name;

			if (fade) {
				this.addGameObject(new Fade());
				fadeStart = gameTick;
			} else {
				fadeStart = gameTick - Constants.FADE_TIME;
			}

			return true;
		}

		return false;
	}

	/**
	 * add a new GameObject to the Game
	 *
	 * @param gameObject the gameObject to be added
	 **/
	public void addGameObject(GameObject gameObject) {
		if (!toAdd.contains(gameObject) && !gameObjects.contains(gameObject)) toAdd.add(gameObject);
	}

	/**
	 * remove a GameObject from the Game
	 *
	 * @param gameObject the gameObject to be removed
	 **/
	private void removeGameObject(GameObject gameObject, boolean mapChange) {
		if (!toRemove.contains(gameObject) && gameObjects.contains(gameObject)) {
			toRemove.add(gameObject);
			removeMapChange.put(gameObject, mapChange);
		}
	}

	public void removeGameObject(GameObject gameObject) {
		if (!toRemove.contains(gameObject) && gameObjects.contains(gameObject)) {
			toRemove.add(gameObject);
			removeMapChange.put(gameObject, false);
		}
	}

	/**
	 * @return a list of all CollisionObjects in the game
	 **/
	public List<CollisionObject> getCollisionObjects() {
		return collisionObjects;
	}

	/**
	 * @return the camera used to display the game
	 **/
	public Camera getCamera() {
		return window.getCamera();
	}

	/**
	 * @return the width of the window divided by the height
	 **/
	public float getAspectRatio() {
		return window.getAspectRatio();
	}

	/**
	 * @return the particleSystem used to display and store particles
	 **/
	public ParticleSystem getParticleSystem() {
		return particleSystem;
	}

	/**
	 * @return a list of all Player in the game
	 **/
	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * @return the current gameTick
	 **/
	public int getGameTick() {
		return gameTick;
	}

	/**
	 * @param key the key of the value to be returned
	 * @return the value of the given key in the game save state
	 **/
	public int getValue(String key) {
		return values.getOrDefault(key, 0);
	}

	/**
	 * searches for all set values in the saveGame, where the key contains the given string and where the value is one of the given values or anything when given no values
	 *
	 * @param key   the String that every key has to contain
	 * @param value that the found values may be, or nothing to allow every value
	 * @return the amount of keys found in the save state
	 **/
	public int getKeyAmount(String key, int... value) {
		String[] keySet = values.keySet().toArray(new String[0]);
		int amount = 0;
		for (String s : keySet) {
			if (s.contains(key)) {
				if (value.length == 0) {
					amount++;
				} else {
					for (int v : value) {
						if (v == values.get(s)) amount++;
					}
				}
			}
		}
		return amount;
	}

	/**
	 * @param key   the key to be set in the saveGame
	 * @param value the value that should be assigned to the key in the current saveGame
	 **/
	public void setValue(String key, int value) {
		values.put(key, value);
	}

	/**
	 * @param colors the colors that should become available as colors for the player
	 **/
	private void addPlayerColors(String... colors) {
		for (String s : colors) playerColors.add(Color.decode(s));
	}

	/**
	 * loads all values from a saveGame with the given name
	 *
	 * @param saveName the name of the save that should be loaded
	 **/
	public void loadValues(String saveName) {
		values = SaveHandler.readSave(saveName);
	}

	/**
	 * saves all values of the current saveGame to the save with the given name
	 *
	 * @param saveName name of the save
	 **/
	public void saveValues(String saveName) {
		SaveHandler.writeSave(values, saveName);
	}

	/**
	 * removes all values from the loaded saveGame
	 **/
	public void clearValues() {
		values.clear();
	}

	public int getGameTime() {
		return gameTime;
	}

	public boolean isFreezeFrame() {
		return freezeTicks > 0;
	}

	public boolean hasClock() {
		return hasClock;
	}

	public void getClock() {
		this.hasClock = true;
	}

	public GameMap getMap() {
		return map;
	}
}
