#!/usr/bin/env bash
docker run --name mysql -v ./mysql.conf:/etc/mysql/conf.d/mysql.conf -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql
