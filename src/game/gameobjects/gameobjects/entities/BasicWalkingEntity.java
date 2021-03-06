package game.gameobjects.gameobjects.entities;

import game.Ability;
import game.Constants;
import game.Game;
import game.data.hitbox.HitBox;
import game.data.hitbox.HitBoxDirection;
import game.gameobjects.CollisionObject;
import game.gameobjects.gameobjects.entities.entities.Player;
import game.util.MathUtil;

/**
 * A simple entity, that implements gravity, walking and jumping
 */
public abstract class BasicWalkingEntity extends BasicMovingEntity {

	protected boolean onGround, onLadder;
	protected int jumpTicks, aerialTicks;
	private boolean jumpingLastTick;
	protected boolean hasDoubleJumped;

	protected float lastMX;
	protected float mx;
	protected boolean jumping;
	protected boolean down;

	private float maxSpeed = 0.65f;
	private float maxJumpHeight = 1;

	public BasicWalkingEntity(HitBox hitBox, float drawingPriority) {
		super(hitBox, drawingPriority);

		lastMX = -1;
		mx = 0;
		jumping = false;
		down = false;
		jumpingLastTick = false;

		aerialTicks = 0;
		jumpTicks = 0;
		onGround = false;
		onLadder = false;
	}

	@Override
	public void update(Game game) {
		if (game.isFreezeFrame()) {
			return;
		}

		vx = mx * Constants.MAX_WALKING_SPEED * maxSpeed;
		if (Math.abs(mx) >= 0.2f) lastMX = mx;

		if ((onGround  || (aerialTicks < Constants.COYOTE_TICKS && jumpTicks < 1)) && jumping && !jumpingLastTick) {
			vy = Constants.JUMP_ACCELERATION * maxJumpHeight;
			jumpTicks = 1;
		} else if (jumpTicks == 0 && !jumpingLastTick && jumping && !hasDoubleJumped && (this instanceof Player && ((Player) this).hasAbility(Ability.DOUBLE_JUMP))) {
			jumpTicks = 1;
			vy = Constants.JUMP_ACCELERATION * maxJumpHeight;
			hasDoubleJumped = true;
		} else if (jumpTicks > 0 && jumping && vy > 0) {
			jumpTicks++;

			vy -= Constants.GRAVITY_ACCELERATION_JUMPING;
		} else {
			jumpTicks = 0;

			if (-vy < Constants.MAX_GRAVITY_SPEED) vy = Math.max(vy - Constants.GRAVITY_ACCELERATION, -Constants.MAX_GRAVITY_SPEED);
			if (this instanceof Player && down && -vy < Constants.MAX_DOWN_SPEED) vy = Math.max(vy - Constants.DOWN_ACCELERATION, -Constants.MAX_DOWN_SPEED);
		}

		if (onLadder) {
			vy = (jumping ? 0.1f : -0.1f);
		}

		jumpingLastTick = jumping;

		aerialTicks++;
		onGround = false;
		onLadder = false;
		super.update(game);
	}

	@Override
	public void collide(CollisionObject gameObject, HitBoxDirection direction, float velocity, boolean source) {
		super.collide(gameObject, direction, velocity, source);

		aerialTicks = Constants.COYOTE_TICKS + 1;

		if (source && direction == HitBoxDirection.DOWN && velocity != 0) {
			if (velocity > Constants.MAX_GRAVITY_SPEED + 0.01f) {
				game.getCamera().addScreenshake(velocity / 15);

				HitBox stomp = new HitBox(hitBox.x - (2 - hitBox.width) / 2, hitBox.y - 0.25f, 2f, 0.5f);

				for (CollisionObject collisionObject : game.getCollisionObjects()) {
					if (collisionObject.equals(this)) continue;
					for (HitBox hitBox2 : collisionObject.getCollisionBoxes()) {
						if (hitBox2.collides(stomp)) {
							collisionObject.interact(this, stomp, InteractionType.STOMP);
							break;
						}
					}
				}

			}

			aerialTicks = 0;
			onGround = true;
			hasDoubleJumped = false;
		}
	}

	public void setMx(float mx) {
		this.mx = MathUtil.clamp(mx, -1, 1);
	}

	public void setJumping(boolean jumping) {
		this.jumping = jumping;
	}

	public void setDown(boolean down) {
		this.down = down;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public void setMaxJumpHeight(float maxJumpHeight) {
		this.maxJumpHeight = maxJumpHeight;
	}

	@Override
	protected boolean fallThroughBlock() {
		return down;
	}
}
