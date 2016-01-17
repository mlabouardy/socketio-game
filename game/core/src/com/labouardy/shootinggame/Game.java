package com.labouardy.shootinggame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.labouardy.shootinggame.sprites.Starship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Game extends ApplicationAdapter {
	private final float UPDATE_TIME=1/60f;
	private float timer;
	SpriteBatch batch;
	private io.socket.client.Socket socket;
	private Texture playerShip;
	private Texture friendship;
	private Starship player;
	private HashMap<String, Starship> friends=new HashMap();
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		playerShip=new Texture("playerShip2.png");
		friendship=new Texture("playerShip.png");
		connectSocket();
		configEvent();
	}

	public void configEvent(){
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				player=new Starship(playerShip);
				Gdx.app.log("SocketIO","connected");
			}
		});

		socket.on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Gdx.app.log("SocketIO", "ID " + data.getString("id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		socket.on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Starship friend = new Starship(friendship);
					friends.put(data.getString("id"), friend);
					Gdx.app.log("SocketIO", "New player connected:" + data.getString("id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});


		socket.on("playerDisconnect", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Starship friend = new Starship(friendship);
					friends.remove(data.getString("id"));
					Gdx.app.log("SocketIO", "New player disconnected:" + data.getString("id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		socket.on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONArray data = (JSONArray) args[0];
				try {
					for (int i = 0; i < data.length(); i++) {
						JSONObject s = data.getJSONObject(i);
						Starship friend = new Starship(friendship);
						Vector2 position = new Vector2();
						position.x = ((Double) s.getDouble("x")).floatValue();
						position.y = ((Double) s.getDouble("y")).floatValue();
						friend.setPosition(position.x, position.y);
						friends.put(s.getString("id"), friend);
					}
					Gdx.app.log("SocketIO", "Current players" + data.length());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		socket.on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String playerID=data.getString("id");
					Double x=data.getDouble("x");
					Double y=data.getDouble("y");
					if(friends.get(playerID)!=null)
						friends.get(playerID).setPosition(x.floatValue(), y.floatValue());
					Gdx.app.log("SocketIO", "Current players" + data.length());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void updateServer(float dt){
		timer+=dt;
		if(timer>=UPDATE_TIME && player!=null && player.hasMoved()){
			JSONObject data=new JSONObject();
			try {
				data.put("x",player.getX());
				data.put("y",player.getX());
				socket.emit("playerMoved",data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	public void connectSocket(){
		try{
			socket= IO.socket("http://localhost:3000");
			socket.connect();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void handleInput(float dt){
		if(player!=null){
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
				player.setPosition(player.getX()+(-200*dt),player.getY());
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
				player.setPosition(player.getX()+(200*dt),player.getY());
		}
	}
	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput(Gdx.graphics.getDeltaTime());
		updateServer(Gdx.graphics.getDeltaTime());
		batch.begin();
		if(player!=null)
			player.draw(batch);
		for(String key:friends.keySet()){
			Starship startship=friends.get(key);
			startship.draw(batch);
		}
		batch.end();
	}

	@Override
	public void dispose() {
		super.dispose();
		playerShip.dispose();
		friendship.dispose();
	}
}
