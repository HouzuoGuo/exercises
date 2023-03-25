package main

import (
	"bufio"
	"encoding/json"
	"errors"
	"io"
	"log"
	"net"
	"strconv"
	"sync"
)

type EchoReply struct {
	RemoteAddr string `json:"addr"`
	Content    string `json:"content"`
	Counter    int    `json:"counter"`
}

type EchoServer struct {
	addr          *net.TCPAddr
	listener      net.Listener
	ipLineCounter map[string]int
	mutex         *sync.Mutex
}

func (srv *EchoServer) Start() error {
	srv.ipLineCounter = make(map[string]int)
	srv.mutex = new(sync.Mutex)
	var err error
	srv.listener, err = net.ListenTCP("tcp", srv.addr)
	if err != nil {
		return err
	}
	log.Println("listening on port", srv.Port())
	return err
}

func (srv *EchoServer) Port() int {
	return srv.listener.(*net.TCPListener).Addr().(*net.TCPAddr).Port
}

func (srv *EchoServer) Loop() error {
	for {
		client, err := srv.listener.Accept()
		if errors.Is(err, net.ErrClosed) {
			return nil
		} else if err != nil {
			return err
		}
		go srv.Handle(client)
	}
}

func (srv *EchoServer) Handle(conn net.Conn) error {
	reader := bufio.NewReader(conn)
	writer := json.NewEncoder(conn)
	log.Printf("connected from %s", conn.RemoteAddr())
	defer func() {
		log.Printf("disconnected from %s", conn.RemoteAddr())
	}()
	for {
		ip := conn.RemoteAddr().(*net.TCPAddr).IP.String()
		srv.mutex.Lock()
		ctr := srv.ipLineCounter[ip]
		srv.ipLineCounter[ip] += 1
		srv.mutex.Unlock()
		line, err := reader.ReadString('\n')
		if errors.Is(err, io.EOF) {
			return nil
		} else if err != nil {
			return err
		}
		err = writer.Encode(EchoReply{
			RemoteAddr: conn.RemoteAddr().String(),
			Content:    line,
			Counter:    ctr,
		})
		if errors.Is(err, io.EOF) {
			return nil
		} else if err != nil {
			return err
		}
	}
}

func (srv *EchoServer) Stop() {
	if listener := srv.listener; listener != nil {
		listener.Close()
	}
}

type Client struct {
	remoteAddr *net.TCPAddr
	conn       net.Conn
}

func (client *Client) Connect() error {
	var err error
	client.conn, err = net.DialTCP("tcp", nil, client.remoteAddr)
	return err
}

func (client *Client) WriteTo(conn net.Conn, in string) (EchoReply, error) {
	_, err := client.conn.Write([]byte(in))
	if err != nil {
		return EchoReply{}, err
	}
	decoder := json.NewDecoder(conn)
	var echoReply EchoReply
	err = decoder.Decode(&echoReply)
	return echoReply, err
}

func (client *Client) Write(in string) (EchoReply, error) {
	return client.WriteTo(client.conn, in)
}

func (client *Client) Disconnect() {
	if conn := client.conn; conn != nil {
		_ = conn.Close()
	}
}

func main() {
	addr, err := net.ResolveTCPAddr("tcp4", "127.0.0.1:0")
	if err != nil {
		log.Panic(err)
	}
	srv := &EchoServer{addr: addr}
	defer srv.Stop()
	if err := srv.Start(); err != nil {
		log.Panic(err)
	}
	go func() {
		if err := srv.Loop(); err != nil {
			log.Panic(err)
		}
	}()

	srvAddr, err := net.ResolveTCPAddr("tcp4", net.JoinHostPort("127.0.0.1", strconv.Itoa(srv.Port())))
	if err != nil {
		log.Panic(err)
	}
	client1 := &Client{remoteAddr: srvAddr}
	defer client1.Disconnect()
	if err := client1.Connect(); err != nil {
		log.Panic(err)
	}
	client2 := &Client{remoteAddr: srvAddr}
	defer client2.Disconnect()
	if err := client2.Connect(); err != nil {
		log.Panic(err)
	}
	for _, in := range []string{"alef", "bet", "chet"} {
		reply, err := client1.Write(in + "\n")
		log.Printf("client1: %#v %#v", reply, err)
	}
}
