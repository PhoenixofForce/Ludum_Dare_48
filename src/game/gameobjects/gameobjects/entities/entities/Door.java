package game.gameobjects.gameobjects.entities.entities;

import game.Game;
import game.data.Sprite;
import game.data.hitbox.HitBox;
import game.data.hitbox.HitBoxDirection;
import game.gameobjects.CollisionObject;
import game.gameobjects.gameobjects.entities.BasicStaticEntity;
import game.util.MathUtil;

public class Door extends BasicStaticEntity {

	private Sprite opened = new Sprite("door_open");
	private Sprite closed = new Sprite("door_closed");
	private Sprite opening = new Sprite("door_opening", 5, 75);
	private Sprite closing = new Sprite("door_closing", 5, 75);

	private boolean isOpen;				//Is open
	private boolean turning;			//Is currently opening or closing
	private int startTick;				//The first tick of the opening or closing

	public Door(float x, float y, float drawingPriority) {
		super(new HitBox(x + 6.0f / 16.0f, y, 4.0f / 16.0f, 2), drawingPriority);

		turning = false;
		startTick = 0;
	}

	@Override
	public void init(Game game) {
		super.init(game);

		this.isOpen = false;
		setSprite(isOpen ? opened : closed);
		hitBox.type = isOpen ? HitBox.HitBoxType.NOT_BLOCKING : HitBox.HitBoxType.BLOCKING;
	}

	@Override
	public float getCollisionPriority() {
		return -5;
	}

	@Override
	public void collide(CollisionObject gameObject, HitBoxDirection direction, float velocity, boolean source) {

	}

	@Override
	public void interact(CollisionObject gameObject, HitBox hitBox, InteractionType interactionType) {
		turning = true;
		startTick = game.getGameTick();
		setSprite(isOpen ? closing : opening);
	}

	@Override
	public float getPriority() {
		return -1;
	}

	@Override
	public void update(Game game) {
		if (turning) {
			if (game.getGameTick() - startTick >= MathUtil.getAnimationTicks(sprite) - 1) {
				isOpen = !isOpen;
				setSprite(isOpen ? opened : closed);
				hitBox.type = isOpen ? HitBox.HitBoxType.NOT_BLOCKING : HitBox.HitBoxType.BLOCKING;
				turning = false;
			}

			game.getCamera().addScreenshake(0.004f);
		}
	}
}
