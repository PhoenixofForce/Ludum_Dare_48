package game.gameobjects.gameobjects.entities.entities;

import game.Game;
import game.data.hitbox.HitBox;
import game.data.hitbox.HitBoxDirection;
import game.data.Sprite;
import game.data.script.Tree;
import game.gameobjects.CollisionObject;
import game.gameobjects.gameobjects.entities.BasicStaticEntity;

/**
 * A door used to go into a new map
 */
public class Exit extends BasicStaticEntity {

	private static Sprite door = new Sprite("portal_idle", 5, 100);
	private static Sprite door_deactivated = new Sprite("portal_empty");

	private String targetMap;				//name of the new map
	private Tree onEntrance;

	public Exit(float x, float y, float drawingPriority, String targetMap, Tree onEntrance) {
		super(new HitBox(x + 0.125f, y, 0.75f, 1), drawingPriority);

		this.targetMap = targetMap;
		this.onEntrance = onEntrance;

		this.setWobble(1);

		setSprite(door);
	}

	@Override
	public void update(Game game) {

	}

	@Override
	public float getPriority() {
		return 1;
	}

	@Override
	public void collide(CollisionObject gameObject, HitBoxDirection direction, float velocity, boolean source) {

	}

	@Override
	public void interact(CollisionObject gameObject, HitBox hitBox, InteractionType interactionType) {
		if (gameObject instanceof Player && interactionType == InteractionType.INTERACT) {
			if (targetMap != null && targetMap.length() > 0 && game.setGameMap(targetMap, true)) {
				if (onEntrance != null) onEntrance.get(game);
			}
		}
	}

	@Override
	public float getCollisionPriority() {
		return -1;
	}

	public String getTargetMap() {
		return targetMap;
	}

	public void setTargetMap(String targetMap) {
		this.targetMap = targetMap;
	}

	public void setOnEntrance(Tree onEntrance) {
		this.onEntrance = onEntrance;
	}
}