#!/bin/bash

docker run --rm --name dddwj-db -e POSTGRES_USER=dddwj -e POSTGRES_DATABASE=dddwj -e POSTGRES_PASSWORD=asdfasdf -p 5432:5432 postgres:10 
