package game.gameobjects.gameobjects.entities.entities;

import game.Ability;
import game.Game;
import game.data.Sprite;
import game.data.hitbox.HitBox;
import game.data.hitbox.HitBoxDirection;
import game.gameobjects.CollisionObject;
import game.gameobjects.gameobjects.entities.BasicWalkingEntity;
import game.gameobjects.gameobjects.particle.ParticleType;
import game.window.Window;
import game.window.light.Light;

import java.util.HashSet;
import java.util.Set;

/**
 * The player class
 */
public class Player extends BasicWalkingEntity implements Light {
	private static final int INTERACT_TICKS = 5;

	private static Sprite move = new Sprite("player_walk", 4, 100);
	private static Sprite idle = new Sprite(100, "player_normal", "player_normal", "player_normal", "player_normal", "player_normal", "player_normal", "player_normal", "player_normal", "player_idle_0", "player_idle_0", "player_idle_1", "player_idle_1");
	private static Sprite idle_special = new Sprite("player_idle_special", 10, 100);
	private static Sprite jump_prep = new Sprite("player_jump_prep");
	private static Sprite jump_air = new Sprite("player_jump_air");
	private static Sprite jump_fall = new Sprite("player_jump_fall");

	private Set<Ability> abilities;								//The abilities of the player
	private boolean interactingLastTick;
	private boolean interacting;
	private int attack, interact;

	public Player(float x, float y, float drawingPriority) {
		super(new HitBox(x, y, 0.25f, 0.999f), drawingPriority);

		abilities = new HashSet<>();
		interacting = false;
		interactingLastTick = false;

		attack = 0;
		interact = 0;

		setWobble(0);

		setSprite(idle);
	}

	@Override
	public void init(Game game) {
		super.init(game);
	}

	@Override
	public void setup(Window window) {
		super.setup(window);

		window.getLightHandler().addLight(this);
	}

	@Override
	public void remove(Game game, boolean mapChange) {
		super.remove(game, mapChange);
	}

	@Override
	public void collide(CollisionObject gameObject, HitBoxDirection direction, float velocity, boolean source) {
		super.collide(gameObject, direction, velocity, source);
	}

	@Override
	public void update(Game game) {
		super.update(game);

		if (game.getMap().getDirectory() == null)
			this.addAbility(Ability.DOUBLE_JUMP);

		Sprite newSprite = null;
		if (!onGround && mx != 0) newSprite = jump_fall;
		if (!onGround && mx == 0) newSprite = jump_fall;
		if (onGround && mx == 0) newSprite = idle;
		if (onGround && mx != 0) newSprite = move;

		if (!sprite.equals(newSprite)) setSprite(newSprite);

		setMirrored(lastMX < 0);

		if (interact > 0) {
			for (CollisionObject collisionObject : game.getCollisionObjects()) {
				if (collisionObject.equals(this)) continue;
				for (HitBox hitBox2 : collisionObject.getCollisionBoxes()) {
					if (hitBox2.collides(hitBox)) {
						collisionObject.interact(this, hitBox, InteractionType.INTERACT);
						break;
					}
				}
			}

			interact++;

			if (interact > INTERACT_TICKS) {
				interact = 0;
			}

		} else if (interacting && !interactingLastTick && attack == 0) {
			interact++;
		}

		interactingLastTick = interacting;
	}

	@Override
	public void cleanUp(Window window) {
		super.cleanUp(window);

		window.getLightHandler().removeLight(this);
	}

	@Override
	public float getPriority() {
		return 1;
	}


	@Override
	public void getLightColor(float[] values) {
		values[0] = 0.4f;
		values[1] = 0.4f;
		values[2] = 0.6f;
	}

	@Override
	public void getLightPosition(float[] values) {
		values[0] = hitBox.x + hitBox.width / 2;
		values[1] = hitBox.y + hitBox.height / 2;
		values[2] = 0.9f;
	}

	@Override
	public boolean updateLight() {
		return true;
	}

	public void respawn(float x, float y, float drawingPriority) {
		hitBox.x = x;
		hitBox.y = y;
		vx = 0;
		vy = 0;
		onGround = false;
		removeAllAbilities();
		setDrawingPriority(drawingPriority);
	}

	@Override
	public float getCollisionPriority() {
		return -10;
	}

	public void setInteracting(boolean interacting) {
		this.interacting = interacting;
	}

	public void addAbility(Ability ability) {
		abilities.add(ability);
	}

	public void removeAbility(Ability ability) {
		abilities.remove(ability);
	}

	public void removeAllAbilities() {
		abilities = new HashSet<>();
	}

	public boolean hasAbility(Ability ability) {
		return abilities.contains(ability);
	}
}
