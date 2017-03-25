#!/bin/bash
docker stop takarabako
docker rm takarabako
docker run -d -p 8088:8088 -p 31777:31777 --name takarabako kordano/takarabako
