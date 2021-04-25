package game.gameobjects.gameobjects.entities.entities;

import game.Constants;
import game.Game;
import game.data.Sprite;
import game.data.hitbox.HitBox;
import game.gameobjects.gameobjects.entities.BasicDrawingEntity;

public class Clock extends BasicDrawingEntity {

	private static Sprite[] sprites = new Sprite[8];
	static {
		for (int i = 0; i < 8; i++) {
			sprites[i] = new Sprite(100, "clock_" + i);
		}
	}


	public Clock(float size) {
		super(new HitBox(-size / 2, 1-size, size, size), -999999);
		setSprite(sprites[0]);
		setUseCamera(false);
	}

	@Override
	public void update(Game game) {
		if (!this.sprite.equals(sprites[((game.getGameTime()+8000*60) / Constants.TPS) % 8])) {
			//TODO: Make Clock bigger for short period of time
			setSprite(sprites[((game.getGameTime()+8000*60) / Constants.TPS) % 8]);
		}
	}

	@Override
	public float getPriority() {
		return -999999;
	}
}
