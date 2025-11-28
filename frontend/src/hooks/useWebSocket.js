import { useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export const useWebSocket = (url, topic, onMessage) => {
  const [isConnected, setIsConnected] = useState(false);
  const stompClientRef = useRef(null);

  useEffect(() => {
    const socket = new SockJS(url);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      console.log('✅ WebSocket Connected');
      setIsConnected(true);

      stompClient.subscribe(topic, (message) => {
        const data = JSON.parse(message.body);
        onMessage(data);
      });
    }, (error) => {
      console.error('WebSocket error:', error);
      setIsConnected(false);
    });

    stompClientRef.current = stompClient;

    return () => {
      if (stompClient.connected) {
        stompClient.disconnect();
      }
    };
  }, [url, topic, onMessage]);

  return { isConnected };
};