#!/bin/sh -e
# Simple startup script for Canopy's Game Server
# [CP] 2010 Aymeric Mansoux
#      Released under the COPYPASTE License
#      http://cp.kuri.mu/
                                       

APP="oak-server"
PID_FILE="/var/run/${APP}.pid"
LOG_FILE="/var/log/${APP}.log"

check_root ()
{
    if [ ${UID} != 0 ]
    then
        echo "[!] You need to run $0 as root"
        exit 1
    fi
}

start ()
{
    echo "[ ] Starting ${APP} Game Server on port ${SERVER_PORT}."
    if [ -f ${PID_FILE} ]
    then
        echo "[!] PID file locked!"
        status
        exit 1
    fi

    sudo -u www-data nohup java -classpath $( echo lib/*.jar . | sed 's/ /:/g'):src clojure.main src/oak/core.clj > ${LOG_FILE} 2>&1 &

    # < /dev/null > ${LOG_FILE} 2>&1 &
    echo $! > ${PID_FILE}

    # start the agents...
    sudo -u www-data nohup ./run.sh &

    echo "[*] ${APP} Game Server on port ${SERVER_PORT} started."
    exit 0
}

stop () 
{
    if [ -f ${PID_FILE} ]
    then 
        read PID < ${PID_FILE}
        echo "[!] Stopping ${APP} Game server on port ${SERVER_PORT}."
        kill ${PID}
        RETVAL=$?
        [ ${RETVAL} = 0 ] && rm -f ${PID_FILE}
    else
        echo "[*] ${APP} Gamer Server on port ${SERVER_PORT} is already down."
    fi
}

status () 
{
    if [ -f ${PID_FILE} ]
    then
        read PID < ${PID_FILE}
        if [ "${PID}" = "" ]
        then
            echo "${PID_FILE} is empty"
            exit 1
        elif ps ${PID} > /dev/null
        then
            echo "[*] ${APP} Game Server on port ${SERVER_PORT} (PID ${PID}) is up."
            exit 0
        else
            echo "[!] ${APP} Game Server on port ${SERVER_PORT} is dead but PID file exists."
            echo "[!] Delete ${PID_FILE} before trying again."
            exit 1
        fi
    fi
    echo "[*] ${APP} Gamer Server on port ${SERVER_PORT} is down."
    exit 1
}

main ()
{
#    check_root
    SERVER_PORT="8001"

    if [ "$2" = "debug" ]
    then
        LOG_FILE="stdout"
    else
        LOG_FILE="/var/log/${APP}-${SERVER_PORT}.log"
    fi
   
    PID_FILE="/var/run/${APP}-${SERVER_PORT}.pid"

    case "$1" in
        start)
    	    start ${SERVER_PORT}
    	    ;;
        stop)
	        stop
	        ;;
        status)
	        status
	        ;;
        restart)
	        stop
	        start ${SERVER_PORT}
	        ;;
        *)
	        echo "[!] I don't understand..."
            echo "[ ] Usage: $0 {start|stop|status|restart} <server-port>"
	        exit 1
esac
}

main $1 $2
exit 0
