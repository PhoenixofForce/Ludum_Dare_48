package game.gameobjects.gameobjects.entities.entities;

import game.Ability;
import game.Game;
import game.data.Sprite;
import game.data.hitbox.HitBox;
import game.data.hitbox.HitBoxDirection;
import game.gameobjects.CollisionObject;
import game.gameobjects.gameobjects.entities.BasicStaticEntity;

import java.awt.*;

public class ClockTower extends BasicStaticEntity {

	public Sprite idle = new Sprite("clock_tower", 8, 150);
	public Sprite deactivated = new Sprite("clock_tower_empty");

	public boolean interacted = false;

	public ClockTower(float x, float y, float drawingPriority) {
		super(new HitBox(x, y + 0.5f, 0.75f, 1), drawingPriority);
		this.setSprite(idle);
	}

	@Override
	public float getCollisionPriority() {
		return -1;
	}

	@Override
	public void collide(CollisionObject gameObject, HitBoxDirection direction, float velocity, boolean source) {

	}

	@Override
	public void interact(CollisionObject gameObject, HitBox hitBox, InteractionType interactionType) {
		if(interactionType == InteractionType.INTERACT && !interacted && gameObject instanceof Player) {
			Player p = (Player) gameObject;

			interacted = true;
			this.setSprite(deactivated);

			game.getClock();
		}
	}

	@Override
	public float getPriority() {
		return 0;
	}

	@Override
	public void update(Game game) {

	}
}
