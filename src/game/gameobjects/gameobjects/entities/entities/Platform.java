package game.gameobjects.gameobjects.entities.entities;

import game.Constants;
import game.Game;
import game.data.Sprite;
import game.data.hitbox.HitBox;
import game.data.hitbox.HitBoxDirection;
import game.gameobjects.CollisionObject;
import game.gameobjects.gameobjects.entities.BasicMovingEntity;
import game.gameobjects.gameobjects.entities.BasicStaticEntity;

public class Platform extends BasicStaticEntity {
	public static final int PLATFORM_TIME = 4 * Constants.TPS;
	private static Sprite platform = new Sprite("platform_now");

	private float dist;
	private int startTick;
	private float start_x;
	public Platform(float x, float y, float drawingPriority, float dist) {
		super(new HitBox(x, y, 2, 6 / 16f), drawingPriority, HitBox.HitBoxType.BLOCKING);

		this.dist = dist;
		this.start_x = x;


		setSprite(platform);
	}

	@Override
	public void interact(CollisionObject gameObject, HitBox hitBox, InteractionType interactionType) {

	}

	@Override
	public void collide(CollisionObject gameObject, HitBoxDirection direction, float velocity, boolean source) {
		if(direction == HitBoxDirection.UP && gameObject instanceof BasicMovingEntity) {
			float y = dcubic(sawtooth((((game.getGameTime() - startTick) + 1000*PLATFORM_TIME) % PLATFORM_TIME) / (1f * PLATFORM_TIME)));

			((BasicMovingEntity) gameObject).addVx(((game.getGameTime() - startTick + 1000 * PLATFORM_TIME) % PLATFORM_TIME) < PLATFORM_TIME / 2 ? 2 * dist *y  / PLATFORM_TIME : -2 * y* dist / PLATFORM_TIME);
		}
	}

	@Override
	public void init(Game game) {
		super.init(game);
		this.startTick = game.getGameTime();
	}

	@Override
	public float getCollisionPriority() {
		return 0;
	}


	public float cubic(float y) {
		return -2 * y*y*y + 3*y*y;
	}

	public float dcubic(float y) {
		return -6*y*y + 6*y;
	}

	public float sawtooth(float x) {
		float y = 0;
		if (x <= 0.5f) {
			y = 2 * x;
		} else {
			y =  2-2*x;
		}
		return y;
	}

	@Override
	public void update(Game game) {
		hitBox.x = start_x + cubic(sawtooth((((game.getGameTime() - startTick) + 1000*PLATFORM_TIME) % PLATFORM_TIME) / (1f * PLATFORM_TIME))) * dist;
	}

	@Override
	public float getPriority() {
		return 0;
	}
}
