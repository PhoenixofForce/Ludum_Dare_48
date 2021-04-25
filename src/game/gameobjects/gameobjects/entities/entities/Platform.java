package game.gameobjects.gameobjects.entities.entities;

import game.Game;
import game.data.hitbox.HitBox;
import game.data.hitbox.HitBoxDirection;
import game.gameobjects.CollisionObject;
import game.gameobjects.gameobjects.entities.BasicStaticEntity;

public class Platform extends BasicStaticEntity {


	public Platform(HitBox hitBox, float drawingPriority) {
		super(hitBox, drawingPriority, HitBox.HitBoxType.BLOCKING);

	}

	@Override
	public void interact(CollisionObject gameObject, HitBox hitBox, InteractionType interactionType) {

	}

	@Override
	public void collide(CollisionObject gameObject, HitBoxDirection direction, float velocity, boolean source) {

	}

	@Override
	public float getCollisionPriority() {
		return 0;
	}

	@Override
	public void update(Game game) {

	}

	@Override
	public float getPriority() {
		return 0;
	}
}
