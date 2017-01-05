#!/bin/bash

#
# This script expects a java 1.8.66 (or later) runnable jar file called 'vcellStartStop.jar'
# in cam domain share directory /home/CAM/vcell/sitecontrol that was created
# using Eclipse IDE 'Export->Runnable Jar File' on class 'org.vcell.DeployVCell.java'
# located in project generated by importing 'svn://code3.cam.uchc.edu/vcell/branches/DeployVCell'
# into an Eclipse Java IDE
#

readonly ACTION_STOP="stop"
readonly ACTION_RESTART="restart"

readonly SITE_ALPHA="alpha"
readonly SITE_BETA="beta"
readonly SITE_REL="rel"

readonly SERVICE_VCELL="vcell"
readonly SERVICE_AMQ="amq"
readonly SERVICE_VCELLANDAMQ="vcellandamq"

usage="Usage: $0 {$ACTION_STOP,$ACTION_RESTART} {$SITE_ALPHA,$SITE_BETA,$SITE_REL} {$SERVICE_VCELL,$SERVICE_AMQ,$SERVICE_VCELLANDAMQ}"

if [ $# -ne 3 ]; then
	echo "missing arguments"
        echo $usage
	exit
fi

action=$1
site=$2
service=$3
echo $action $site $service

if [ "$action" != "$ACTION_STOP" ] && [ "$action" != "$ACTION_RESTART" ]; then
	echo "unknown 'action' argument '$action'"
	echo $usage
	exit
fi

if [ "$site" != "$SITE_ALPHA" ] && [ "$site" != "$SITE_BETA" ] && [ "$site" != "$SITE_REL" ]; then
        echo "unknown 'site' argument '$site'"
        echo $usage
        exit
fi

if [ "$service" != "$SERVICE_VCELL" ] && [ "$service" != "$SERVICE_AMQ" ] && [ "$service" != "$SERVICE_VCELLANDAMQ" ]; then
        echo "unknown 'service' argument '$service'"
        echo $usage
        exit
fi

cmd1=

if [ "$action" == "$ACTION_STOP" ]; then
	if [ "$service" = "$SERVICE_AMQ" ]; then
		cmd1="STOPAMQ"
	elif [ "$service" = "$SERVICE_VCELL" ]; then
		cmd1="STOPVC"
	elif [ "$service" = "$SERVICE_VCELLANDAMQ" ]; then
		cmd1="STOPVCELLANDAMQ"
	fi
elif [ "$action" == "$ACTION_RESTART" ]; then
        if [ "$service" = "SERVICE_AMQ" ]; then
                cmd1="RESTARTAMQ"
        elif [ "$service" = "$SERVICE_VCELL" ]; then
                cmd1="RESTART"
        elif [ "$service" = "$SERVICE_VCELLANDAMQ" ]; then
                cmd1="RESTARTVCELLANDAMQ"
        fi
fi

echo $cmd1 $site

if [ "$service" = "$SERVICE_AMQ" ]; then
	# only dealing with ActiveMQ so use reduced args 
	/share/apps/vcell2/java/current8/bin/java -jar /home/CAM/vcell/sitecontrol/vcellStartStop.jar /usr/bin/ cbittech $cmd1 $site
else
	# either vcell or vcellandamq will always do vcell operation first so need extra args
	/share/apps/vcell2/java/current8/bin/java -jar /home/CAM/vcell/sitecontrol/vcellStartStop.jar /usr/bin/ cbittech $cmd1 $site SGE vcellservice.cam.uchc.edu
fi

