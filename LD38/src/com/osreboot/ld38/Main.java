package com.osreboot.ld38;

import static com.osreboot.ridhvl.painter.painter2d.HvlPainter2D.hvlDrawQuad;
import static com.osreboot.ridhvl.painter.painter2d.HvlPainter2D.hvlDrawQuadc;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.action.HvlAction0;
import com.osreboot.ridhvl.action.HvlAction1;
import com.osreboot.ridhvl.display.collection.HvlDisplayModeDefault;
import com.osreboot.ridhvl.input.HvlInput;
import com.osreboot.ridhvl.painter.HvlRenderFrame;
import com.osreboot.ridhvl.painter.HvlRenderFrame.FBOUnsupportedException;
import com.osreboot.ridhvl.painter.HvlShader;
import com.osreboot.ridhvl.painter.painter2d.HvlFontPainter2D;
import com.osreboot.ridhvl.template.HvlTemplateInteg2D;

public class Main extends HvlTemplateInteg2D{

	public static void main(String args[]){
		new Main();
	}
	
	public static float TILE_SIZE = 48f, SCORE_LOSS = 0.5f;
	
	public static int TEX_FONT = 0, 
			TEX_TILE = 1,
			TEX_TILEC = 2,
			TEX_CURSOR = 3,
			TEX_BACKGROUND = 4,
			TEX_PLASMA = 5,
			TEX_DOT = 6,
			TEX_TUT1 = 7,
			TEX_TUT2 = 8,
			SND_SONG = 0,
			SND_LEVEL = 1,
			SND_PLACE = 2,
			SND_FAIL = 3,
			SND_START = 4;
	
	public Game game;
	public static HvlFontPainter2D font;
	public boolean intro;
	
	public float noClick, preGameOpacity;
	
	public HvlRenderFrame frame1, frame2;
	public HvlShader shader;
	
	public HvlInput inputClick, inputMute;
	
	public static boolean muted = false;
	
	public Main(){
		super(60, 1280, 720, "Iota Nor by Os_Reboot", "Icon", new HvlDisplayModeDefault());
	}

	@Override
	public void initialize(){
		getTextureLoader().loadResource("Font");
		getTextureLoader().loadResource("Tile");
		getTextureLoader().loadResource("TileCover");
		getTextureLoader().loadResource("Cursor");
		getTextureLoader().loadResource("Background");
		getTextureLoader().loadResource("Plasma");
		getTextureLoader().loadResource("Dot");
		getTextureLoader().loadResource("Tut1");
		getTextureLoader().loadResource("Tut2");
		
		getSoundLoader().loadResource("Iotanor");
		getSoundLoader().loadResource("Level");
		getSoundLoader().loadResource("Place");
		getSoundLoader().loadResource("Fail");
		getSoundLoader().loadResource("Start");
		
		noClick = 1f;
		preGameOpacity = 0.9f;
		intro = true;
		
		game = new Game();
		font = new HvlFontPainter2D(getTexture(TEX_FONT), HvlFontPainter2D.Preset.FP_INOFFICIAL, 0.5f, 8f, 0f);
		
		try{
			frame1 = new HvlRenderFrame(1280, 720);
			frame2 = new HvlRenderFrame(1280, 720);
		}catch(FBOUnsupportedException e){
			e.printStackTrace();
		}
		shader = new HvlShader(HvlShader.PATH_SHADER_DEFAULT + "Shader" + HvlShader.SUFFIX_FRAGMENT);
		System.out.println(shader.getFragLog());
		
		inputClick = new HvlInput(new HvlInput.InputFilter(){
			@Override
			public float getCurrentOutput(){
				return Mouse.isButtonDown(0) ? 1 : 0;
			}
		});
		inputClick.setPressedAction(new HvlAction1<HvlInput>(){
			@Override
			public void run(HvlInput a){
				if(noClick == 0){
					if(intro){
						if(!game.isRunning) Main.getSound(Main.SND_PLACE).playAsSoundEffect(5f, 1f, false);
						game.isRunning = true;
					}else{
						game = new Game();
						intro = true;
					}
				}
				game.onClick();
			}
		});
		inputMute = new HvlInput(new HvlInput.InputFilter(){
			@Override
			public float getCurrentOutput(){
				return Keyboard.isKeyDown(Keyboard.KEY_M) ? 1 : 0;
			}
		});
		inputMute.setPressedAction(new HvlAction1<HvlInput>(){
			@Override
			public void run(HvlInput a){
				muted = !muted;
				if(muted){
					if(getSound(SND_SONG).isPlaying()) getSound(SND_SONG).stop();
				}
			}
		});
	}

	public float plasmaOffset = 0f;
	
	@Override
	public void update(final float delta){
		if(!getSound(SND_SONG).isPlaying() && !muted) getSound(SND_SONG).playAsSoundEffect(1f, 1f, false);
		
		if(game.isRunning) preGameOpacity = HvlMath.stepTowards(preGameOpacity, delta, 0);
		plasmaOffset += delta/32f;
		final Main main = this;
		frame1.doCapture(new HvlAction0(){
			@Override
			public void run(){
				hvlDrawQuad(0, 0, 1280, 720, getTexture(TEX_BACKGROUND));
				hvlDrawQuadc(((float)Math.sin(plasmaOffset*Math.PI)*1280)+(1280/2), 360, 3840, 720, getTexture(TEX_PLASMA), new Color(1f, 1f, 1f, 0.1f));
				game.draw(delta, main);
			}
		});
		tickPreGame(delta);
		frame2.doCapture(new HvlAction0(){
			@Override
			public void run(){
				game.drawCursor(delta);
			}
		});
		hvlDrawQuad(0, 0, 1280, 720, frame1);
		shader.doShade(new HvlAction0(){
			@Override
			public void run(){
				shader.sendRenderFrame("frame1", 2, frame1);
				hvlDrawQuad(0, 0, 1280, 720, frame2);
			}
		});
		hvlDrawQuad(0, 0, 1280, 720, new Color(0f, 0f, 0f, preGameOpacity));
		if(game != null && !intro){
			Main.font.drawWordc("-end-", 1280/2,  720*2/16, Color.white, 1f);
			Main.font.drawWordc("not enough white tiles left", 1280/2,  720*7/32, Color.white, 0.5f);
			Main.font.drawWordc("score: " + Math.round(game.score*100f)/100f, 1280/2, 720*21/64, Color.white, 1f);
			Main.font.drawWordc("reached level " + game.level, 1280/2, 720*27/64, Color.white, 0.5f);
			Main.font.drawWordc("survived " + Math.round(game.totalTime*100f)/100f + " seconds", 1280/2, 720*16/32, Color.white, 0.5f);
			Main.font.drawWordc("<click to restart>", 1280/2, 720*22/32, Color.white, 0.5f);
			Main.font.drawWordc("game by os_reboot", 1280/2, 720*28/32, Color.white, 0.5f);
			Main.font.drawWordc("www.noctiphyte.com", 1280/2, 720*30/32, Color.white, 0.5f);
		}else if(intro){
			Main.font.drawWordc("Iota Nor", 1280/2,  720*3/16, new Color(1f, 1f, 1f, preGameOpacity), 1f);
			Main.font.drawWordc("Place pieces to invert tile colors.", 1280*5/16, 720*12/32, new Color(1f, 1f, 1f, preGameOpacity), 0.5f);
			hvlDrawQuadc(1280*11/16, 720*12/32, 384/2, 192/2, getTexture(TEX_TUT1), new Color(1f, 1f, 1f, preGameOpacity));
			hvlDrawQuadc(1280*17/64, 720*18/32, 384/2, 192/2, getTexture(TEX_TUT2), new Color(1f, 1f, 1f, preGameOpacity));
			Main.font.drawWordc("Keep the board white and \nstop the dark tiles from spreading\n for as long as possible.", 1280*11/16, 720*19/32, new Color(1f, 1f, 1f, preGameOpacity), 0.5f);
			Main.font.drawWordc("<click to start>", 1280/2, 720*26/32, new Color(1f, 1f, 1f, preGameOpacity), 0.5f);
			Main.font.drawWordc("[m] to mute music", 1280/2, 720*30/32, new Color(1f, 1f, 1f, preGameOpacity), 0.5f);
		}
	}
	
	public void tickPreGame(float delta){
		noClick = HvlMath.stepTowards(noClick, delta, 0);
	}
	
	public void fail(){
		Main.getSound(Main.SND_FAIL).playAsSoundEffect(1f, 1f, false);
		game.isRunning = false;
		noClick = 1f;
		preGameOpacity = 0.9f;
		intro = false;
	}

}
