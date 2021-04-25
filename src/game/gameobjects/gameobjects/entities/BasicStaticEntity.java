package game.gameobjects.gameobjects.entities;

import game.data.hitbox.HitBox;
import game.gameobjects.CollisionObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple entity that implements collision without moving
 */
public abstract class BasicStaticEntity extends BasicDrawingEntity implements CollisionObject {

	private List<HitBox> hitBoxes;

	public BasicStaticEntity(HitBox hitBox, float drawingPriority) {
		this(hitBox, drawingPriority, HitBox.HitBoxType.NOT_BLOCKING);
	}

	public BasicStaticEntity(HitBox hitBox, float drawingPriority, HitBox.HitBoxType type) {
		super(hitBox, drawingPriority);

		hitBox.type = type;

		hitBoxes = new ArrayList<>();
		hitBoxes.add(hitBox);
	}

	@Override
	public List<HitBox> getCollisionBoxes() {
		return hitBoxes;
	}
}
