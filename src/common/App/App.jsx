import React, { Component } from "react";
import io from 'socket.io-client';
import cls from "./App.css";

class App extends Component {
  constructor(props) {
    super(props);
    this.socket = io('mobile-whiteboard.herokuapp.com/stats_socket');
    this.socket.on('log_received', this.onLogReceived.bind(this));
    this.state = {
      logs: []
    };
  }

  onLogReceived(message) {
    this.setState((prev) => {
      if (prev.logs.length > 1000)
        return { logs: [message] };
      prev.logs.unshift(message);
      return prev;
    });
  }

  render() {
    const items = this.state.logs.map((message, index) => 
      <li key={index}>{message}</li>
    );
    return (
      <div>
        <ul>{items}</ul>
      </div>
    );
  }
}

export default App;
