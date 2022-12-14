package Server;

import DB.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class Server {
    ArrayList<SocketChannel> allClient;
    ServerSocketChannel socket;
    public Selector selector;
    private ByteBuffer inputBuffer;
    PKT_Serialized pt;

    Server(int port) throws IOException{
        pt = new PKT_Serialized();

        allClient = new ArrayList<SocketChannel>();
        socket = ServerSocketChannel.open();
        socket.bind(new InetSocketAddress(port));

        // 논블로킹 형식 변경
        socket.configureBlocking(false);
        selector = Selector.open();

        // Select 등록
        socket.register(selector, SelectionKey.OP_ACCEPT);
        inputBuffer = ByteBuffer.allocate(1024);
        inputBuffer.clear();
    }

    void Accept(SelectionKey key) throws IOException{
        ServerSocketChannel s = (ServerSocketChannel)key.channel();
        SocketChannel clientSock = s.accept();
        clientSock.configureBlocking(false);

        //테스트 용
        Packet pk;
        Information information = new Information();
        if(allClient.isEmpty()) {
            pk = new Packet(0, 8, 4, Packet.State.Start, false);
            information.setId(0);
        }
        else {
            pk = new Packet(1, 0, 4, Packet.State.Start, false);
            information.setId(1);
        }
        information.setX(1);
        information.setY(2);

        // 추가
        allClient.add(clientSock);
        clientSock.register(selector, SelectionKey.OP_READ, information);
        System.out.println(clientSock.getLocalAddress());

        Write(pk, pk.getId());
        if(pk.getId() == 1)
            Write(pk, 0);
    }
    void Read(SelectionKey key){
        SocketChannel readSocket = (SocketChannel) key.channel();
        Information info = (Information) key.attachment();
        inputBuffer.clear();
        try {
            readSocket.read(inputBuffer);
            inputBuffer.flip();
            Packet pk = pt.DeSerialized(Packet.class, inputBuffer);
            if(pk.getId() == 0) {
                System.out.println(pk.getId() + " : " + pk.getX() + " " + pk.getY());
                Write(pk, 1);
            }
            else {
                System.out.println(pk.getId() + " : " + pk.getX() + " " + pk.getY());
                Write(pk, 0);
            }
            info.setX(pk.getX());
            info.setY(pk.getY());

        }
        catch(IOException e){
            key.cancel();
            allClient.remove(readSocket);
            System.out.println("클라이언트와 연결이 종료되었습니다.");
        }
    }

    void Write(Object data, int index){ //ByteBuffer 전달
        inputBuffer.clear();
        try {
            pt.Serialized(data, inputBuffer);
            inputBuffer.flip();
            SocketChannel sock = allClient.get(index);
            sock.write(inputBuffer);
        }
        catch(Exception e){
            System.out.println("클라이언트와 연결이 종료되었습니다.");
        }
    }

    public static void main(String[] args) throws IOException{
        Server server= new Server(5000);

        while(true){
            server.selector.select();

            Iterator<SelectionKey> iterator = server.selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();

                if(key.isAcceptable()) {
                    server.Accept(key);
                }else if(key.isReadable()){
                    server.Read(key);
                }
            }
        }
    }
}