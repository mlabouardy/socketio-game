var express=require('express'),
    server=require('http').Server(express),
    io=require('socket.io')(server);

var players=[];

io.on('connection',function(socket){
  console.log('Player connected');
  socket.emit('socketID',{id:socket.id});
  socket.emit('getPlayers',players);
  socket.broadcast.emit('newPlayer',{id:socket.id});
  socket.on('playerMoved',function(data){
    data.id=socket.id;
    socket.broadcast.emit('playerMoved',data);
    for(var i=0;i<players.length;i++){
      if(players[i].id==socket.id){
        players[i].x=data.x;
        players[i].y=data.y;
      }
    }
  });
  socket.on('disconnect',function(){
    console.log('Player disconnect');
    socket.broadcast.emit('playerDisconnect',{id:socket.id});
    for(var i=0;i<players.length;i++){
      if(players[i].id==socket.id)
        players.splice(i,1);
    }
  });
  players.push(new player(socket.id, 0 ,0));
});

function player(id, x ,y){
  this.id=id;
  this.x=x;
  this.y=y;
}


server.listen(3000,function(){
    console.log('listening to port 3000');
});
