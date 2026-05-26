#!/bin/bash

java \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mindguard \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=root \
  -Dserver.port=8081 \
  -jar target/mindguard-1.0.0.jar
