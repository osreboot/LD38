package com.osreboot.ld38;

import org.newdawn.slick.Color;

public class ScoreParticle {
	
	public static final float DRAW_LIFE = 2f;
	
	public float score, life;
	
	public ScoreParticle(float scoreArg){
		score = scoreArg;
	}
	
	public void draw(float delta){
		life += delta;
		Main.font.drawWordc("+" + Math.round(score*10f)/10f, (1280/2) + ((life/DRAW_LIFE)*256f) + 64,  720*4/32, new Color(1f, 1f, 1f, ((1 - life)/DRAW_LIFE)), 0.25f);
	}
	
}
