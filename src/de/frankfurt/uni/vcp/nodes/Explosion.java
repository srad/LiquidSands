package de.frankfurt.uni.vcp.nodes;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import de.frankfurt.uni.vcp.game.Game;

/**
 * <p>
 * Based on the demo class provided by jMonkeyEngine 3 to demonstrate effects:
 * <a href=
 * "http://jmonkeyengine.googlecode.com/svn/trunk/engine/src/test/jme3test/effect/"
 * >Source</a>.
 * </p>
 */
public class Explosion extends Node {

	/** In case we have multiple explosion. */
	private int index;

	/** Timer for the explosion animation. */
	private float time = 0;

	/** Determines the current explosion states. */
	private int state = 0;

	/** The explosion consists of multiple particle effects. */
	private ParticleEmitter flame, flash, spark, roundspark, smoketrail, debris, shockwave;

	private static final int COUNT_FACTOR = 1;

	private static final float COUNT_FACTOR_F = 1f;

	/** Usage of point sprites determined, by default yes! */
	private static final boolean POINT_SPRITE = true;
	
	/** Initialized the type of the particle emitter based on {@link #POINT_SPRITE}. */
	private static final Type EMITTER_TYPE = POINT_SPRITE ? Type.Point : Type.Triangle;

	public Explosion() {
		initialize();
	}

	/**
	 * Create a flame particle emitter.
	 */
	private void createFlame() {
		flame = new ParticleEmitter("Flame", EMITTER_TYPE, 32 * COUNT_FACTOR);
		flame.setSelectRandomImage(true);
		flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
		flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
		flame.setStartSize(1.3f);
		flame.setEndSize(2f);
		flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
		flame.setParticlesPerSec(0);
		flame.setGravity(0, -5, 0);
		flame.setLowLife(.4f);
		flame.setHighLife(.5f);
		flame.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 7, 0));
		flame.getParticleInfluencer().setVelocityVariation(1f);
		flame.setImagesX(2);
		flame.setImagesY(2);
		Material mat = new Material(Game.getInstance().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
		mat.setTexture("Texture", Game.getInstance().getAssetManager().loadTexture("Effects/Explosion/flame.png"));
		mat.setBoolean("PointSprite", POINT_SPRITE);
		flame.setMaterial(mat);

		this.attachChild(flame);
	}

	/**
	 * <p>Create a flash light particle emitter.</p>
	 * <p>Some methods within this methods are deprecated, but that is no issue right now.</p>
	 */
	@SuppressWarnings("deprecation")
	private void createFlash() {
		flash = new ParticleEmitter("Flash", EMITTER_TYPE, 24 * COUNT_FACTOR);
		flash.setSelectRandomImage(true);
		flash.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1f / COUNT_FACTOR_F)));
		flash.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
		flash.setStartSize(.1f);
		flash.setEndSize(3.0f);
		flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
		flash.setParticlesPerSec(0);
		flash.setGravity(0, 0, 0);
		flash.setLowLife(.2f);
		flash.setHighLife(.2f);
		flash.setInitialVelocity(new Vector3f(0, 5f, 0));
		flash.setVelocityVariation(1);
		flash.setImagesX(2);
		flash.setImagesY(2);
		Material mat = new Material(Game.getInstance().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
		mat.setTexture("Texture", Game.getInstance().getAssetManager().loadTexture("Effects/Explosion/flash.png"));
		mat.setBoolean("PointSprite", POINT_SPRITE);
		flash.setMaterial(mat);

		this.attachChild(flash);
	}

	/**
	 * <p>Creates the big chunk particle for the explosion.</p>
	 * <p>Some methods within this methods are deprecated, but that is no issue right now.</p>
	 */
	@SuppressWarnings("deprecation")
	private void createRoundSpark() {
		roundspark = new ParticleEmitter("RoundSpark", EMITTER_TYPE, 20 * COUNT_FACTOR);
		roundspark.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, (float) (1.0 / COUNT_FACTOR_F)));
		roundspark.setEndColor(new ColorRGBA(0, 0, 0, (float) (0.5f / COUNT_FACTOR_F)));
		roundspark.setStartSize(1.2f);
		roundspark.setEndSize(1.8f);
		roundspark.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
		roundspark.setParticlesPerSec(0);
		roundspark.setGravity(0, -.5f, 0);
		roundspark.setLowLife(1.8f);
		roundspark.setHighLife(2f);
		roundspark.setInitialVelocity(new Vector3f(0, 3, 0));
		roundspark.setVelocityVariation(.5f);
		roundspark.setImagesX(1);
		roundspark.setImagesY(1);
		Material mat = new Material(Game.getInstance().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
		mat.setTexture("Texture", Game.getInstance().getAssetManager().loadTexture("Effects/Explosion/roundspark.png"));
		mat.setBoolean("PointSprite", POINT_SPRITE);
		roundspark.setMaterial(mat);

		this.attachChild(roundspark);
	}

	/**
	 * This particle emitter creates small particles.
	 */
	private void createSpark() {
		spark = new ParticleEmitter("Spark", Type.Triangle, 30 * COUNT_FACTOR);
		spark.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
		spark.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
		spark.setStartSize(.5f);
		spark.setEndSize(.5f);
		spark.setFacingVelocity(true);
		spark.setParticlesPerSec(0);
		spark.setGravity(0, 5, 0);
		spark.setLowLife(1.1f);
		spark.setHighLife(1.5f);
		spark.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 20, 0));
		spark.getParticleInfluencer().setVelocityVariation(1);
		spark.setImagesX(1);
		spark.setImagesY(1);
		Material mat = new Material(Game.getInstance().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
		mat.setTexture("Texture", Game.getInstance().getAssetManager().loadTexture("Effects/Explosion/spark.png"));
		spark.setMaterial(mat);

		this.attachChild(spark);
	}

	/**
	 * Smoke particle emitter.
	 */
	@SuppressWarnings("deprecation")
	private void createSmokeTrail() {
		smoketrail = new ParticleEmitter("SmokeTrail", Type.Triangle, 22 * COUNT_FACTOR);
		smoketrail.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
		smoketrail.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
		smoketrail.setStartSize(.2f);
		smoketrail.setEndSize(1f);

		// smoketrail.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
		smoketrail.setFacingVelocity(true);
		smoketrail.setParticlesPerSec(0);
		smoketrail.setGravity(0, 1, 0);
		smoketrail.setLowLife(.4f);
		smoketrail.setHighLife(.5f);
		smoketrail.setInitialVelocity(new Vector3f(0, 12, 0));
		smoketrail.setVelocityVariation(1);
		smoketrail.setImagesX(1);
		smoketrail.setImagesY(3);
		Material mat = new Material(Game.getInstance().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
		mat.setTexture("Texture", Game.getInstance().getAssetManager().loadTexture("Effects/Explosion/smoketrail.png"));
		smoketrail.setMaterial(mat);

		this.attachChild(smoketrail);
	}

	/**
	 * Also creates big particle chunks.
	 */
	@SuppressWarnings("deprecation")
	private void createDebris() {
		debris = new ParticleEmitter("Debris", Type.Triangle, 15 * COUNT_FACTOR);
		debris.setSelectRandomImage(true);
		debris.setRandomAngle(true);
		debris.setRotateSpeed(FastMath.TWO_PI * 4);
		debris.setStartColor(new ColorRGBA(1f, 0.59f, 0.28f, (float) (1.0f / COUNT_FACTOR_F)));
		debris.setEndColor(new ColorRGBA(.5f, 0.5f, 0.5f, 0f));
		debris.setStartSize(.2f);
		debris.setEndSize(.2f);

		// debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
		debris.setParticlesPerSec(0);
		debris.setGravity(0, 12f, 0);
		debris.setLowLife(1.4f);
		debris.setHighLife(1.5f);
		debris.setInitialVelocity(new Vector3f(0, 15, 0));
		debris.setVelocityVariation(.60f);
		debris.setImagesX(3);
		debris.setImagesY(3);
		Material mat = new Material(Game.getInstance().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
		mat.setTexture("Texture", Game.getInstance().getAssetManager().loadTexture("Effects/Explosion/Debris.png"));
		debris.setMaterial(mat);

		this.attachChild(debris);
	}

	/**
	 * Creates a radial shockwave which moves away from the start location.
	 */
	@SuppressWarnings("deprecation")
	private void createShockwave() {
		shockwave = new ParticleEmitter("Shockwave", Type.Triangle, 1 * COUNT_FACTOR);
		// shockwave.setRandomAngle(true);
		shockwave.setFaceNormal(Vector3f.UNIT_Y);
		shockwave.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, (float) (.8f / COUNT_FACTOR_F)));
		shockwave.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));

		shockwave.setStartSize(0f);
		shockwave.setEndSize(7f);

		shockwave.setParticlesPerSec(0);
		shockwave.setGravity(0, 0, 0);
		shockwave.setLowLife(0.5f);
		shockwave.setHighLife(0.5f);
		shockwave.setInitialVelocity(new Vector3f(0, 0, 0));
		shockwave.setVelocityVariation(0f);
		shockwave.setImagesX(1);
		shockwave.setImagesY(1);
		Material mat = new Material(Game.getInstance().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
		mat.setTexture("Texture", Game.getInstance().getAssetManager().loadTexture("Effects/Explosion/shockwave.png"));
		shockwave.setMaterial(mat);

		this.attachChild(shockwave);
	}

	/**
	 * Sets up all particles.
	 */
	public void initialize() {
		createFlame();
		createFlash();
		createSpark();
		createRoundSpark();
		createSmokeTrail();
		createDebris();
		createShockwave();
		this.setLocalScale(0.5f);

		// Move the explosion to the current player and slightly above him.
		this.setLocalTranslation(Game.getInstance().getActiveUnit().getLocalTranslation().clone().add(0, 1f, 0));

		Game.getInstance().getRenderManager().preloadScene(this);
		Game.getInstance().getRootNode().attachChild(this);
	}

	/** Speed ratio of the particle animation. */
	private float speed = 0.5f;

	/** Needs to be called within each tick from the game. */ 
	public void update(float tpf) {
		time += tpf / speed;
		if (time > 1f && state == 0) {
			flash.emitAllParticles();
			spark.emitAllParticles();
			smoketrail.emitAllParticles();
			debris.emitAllParticles();
			shockwave.emitAllParticles();
			state++;
		}
		if (time > 1f + .05f / speed && state == 1) {
			flame.emitAllParticles();
			roundspark.emitAllParticles();
			// SOUND: we play the sound for timing reason just yet.
			Game.getInstance().getExplosionSound().play();
			state++;
		}

		// rewind the effect
		if (time > 5 / speed && state == 2) {
			state = 0;
			time = 0;

			flash.killAllParticles();
			spark.killAllParticles();
			smoketrail.killAllParticles();
			debris.killAllParticles();
			flame.killAllParticles();
			roundspark.killAllParticles();
			shockwave.killAllParticles();

			Game.getInstance().removeExplosion();
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
