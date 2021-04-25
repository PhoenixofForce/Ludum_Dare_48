package game.gameobjects.gameobjects.entities.entities;

import game.Ability;
import game.Constants;
import game.Game;
import game.data.Sprite;
import game.data.hitbox.HitBox;
import game.gameobjects.gameobjects.entities.BasicDrawingEntity;
import game.util.TimeUtil;
import game.window.Window;

public class Clock extends BasicDrawingEntity {

	private static Sprite[] sprites = new Sprite[8];
	static {
		for (int i = 0; i < 8; i++) {
			sprites[i] = new Sprite(100, "clock_" + i);
		}
	}

	private long size_time = 0;
	private float size;
	public Clock(float size) {
		super(new HitBox(-size / 2, 1-size, size, size), -999999);
		this.size = size;
		setSprite(sprites[0]);
		setUseCamera(false);
	}

	@Override
	public void update(Game game) {
		if (!this.sprite.equals(sprites[((game.getGameTime()+8000*60) / Constants.TPS) % 8])) {
			size_time = TimeUtil.getTime();
			setSprite(sprites[((game.getGameTime()+8000*60) / Constants.TPS) % 8]);
		}
	}

	public static final float SIZE_INCREASE = 0.1f;
	public static final float TIME_INCREASE = 150f;
	@Override
	public void draw(Window window, long time) {

		if (game.getPlayers().isEmpty() || !game.getPlayers().get(0).hasAbility(Ability.TIME_REWIND)) {
			return;
		}

		if (time - size_time > TIME_INCREASE) {
			hitBox.width = size;
			hitBox.height = size * window.getAspectRatio();
			hitBox.x = -hitBox.width / 2;
			hitBox.y = 1-size * window.getAspectRatio() *(1 + SIZE_INCREASE / 2);
		} else {
			float fact = (1 + SIZE_INCREASE * (float) Math.sin(Math.PI * (time-size_time) / TIME_INCREASE));
			hitBox.width = size * fact;
			hitBox.height = size * fact * window.getAspectRatio();
			hitBox.x = -hitBox.width / 2;
			hitBox.y = 1-size * window.getAspectRatio() *(((fact - 1) / 2)+1 + SIZE_INCREASE / 2);
		}

		super.draw(window, time);
	}

	@Override
	public float getPriority() {
		return -999999;
	}
}
