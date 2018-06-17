#!/usr/bin/env bash
# TODO: experiment with eden/mn size
nohup java -jar fscviewer.jar -Xms16M -Xmn32M -Xmx768M </dev/null >/dev/null 2>&1 &