var express=require('express'),
    server=require('http').Server(express),
    io=require('socket.io')(server);

io.on('connection',function(socket){
  console.log('Player connected');
  socket.emit('socketID',{id:socket.id});
  socket.broadcast.emit('newPlayer',{id:socket.id});
  socket.on('disconnect',function(){
    console.log('Player disconnect');
  });
});


server.listen(3000,function(){
    console.log('listening to port 3000');
});
