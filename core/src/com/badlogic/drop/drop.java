package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
// There is a predefined one in the library
// However, we are using this one instead.
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class drop extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle bucket;
	//Basically an arraylist but minimises garbage.
	private Array<Rectangle> raindrops;
	private long lastDropTime;

	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		//Always shows us an area of our game that is 800x400 units wide.
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		//Still need to represent the bucket and raindrop at this point in time.
		//Want the bucket to be 20 pixels above the bottom edge of the screen
		//and centered horizontally.
		//All openGL graphics is defined as the bucket with in the bottom left
		//corner.
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		//Init
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	@Override
	public void render(){
		//Clear the screen with a dark blue colour.
		ScreenUtils.clear(0,0,0.3f,1);
		//Make sure our camera is updated - use matrix
		//to set up coordinate system for rendering.
		camera.update();

		//Use coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);
		//Just individual frames.
		batch.begin();
		//Render everything in as specified in the create().
		batch.draw(bucketImage, bucket.x, bucket.y);
		batch.end();

		//What happens when the user interacts.
		if(Gdx.input.isTouched()){
			//Vector3 = 3d dimensional vector.
			Vector3 touchPos = new Vector3();
			//Center the bucket around what was touched.
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}

		//Want the bucket to move without acceleration at two hundred pixels/units per second.
		//Either to the left or right.
//		//Time that passed in between the last and the current rendering frame
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		//Add a few lines to the render() method that will check how much time
		//has passed since we spawned a new raindrop.
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		//Make our raindrops move.
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			//If it below the bucket, remove it.
			if(raindrop.y + 64 < 0) iter.remove();
			//Check if a rectangle interacts with another rectangle.
			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}

		//Raindrops need to be rendered.
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		// Forces the coordinates within the top bar.
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		//Records in nanoseconds.
		lastDropTime = TimeUtils.nanoTime();
	}

	//Clean up assets on exit.
	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
