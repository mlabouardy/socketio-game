package com.labouardy.shootinggame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Socket;

public class Game extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	private io.socket.client.Socket socket;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		connectSocket();
		configEvent();
	}

	public void configEvent(){
		socket.on(Socket.EVENT_OPEN, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO","connected");
			}
		});

		socket.on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data=(JSONObject)args[0];
				try {
					Gdx.app.log("SocketIO","ID "+data.getString("id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		socket.on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data=(JSONObject)args[0];
				try {
					Gdx.app.log("SocketIO","New player connected:"+data.getString("id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void connectSocket(){
		try{
			socket= IO.socket("http://localhost:3000");
			socket.connect();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
}
