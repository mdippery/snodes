#!/bin/sh

# Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
# Copyright (c) 2007-2008 Michael Dippery <mpd@cs.wm.edu>
#
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the Free
# Software Foundation; either version 2 of the License, or (at your option)
# any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#
#
# Contributors:
#   Michael Dippery <mpd@cs.wm.edu>


SNODES_BASE=/usr/local
SNODES_JAR=Snodes.jar
SNODES_SCRIPT=snodes
SNODES_BIN=$SNODES_BASE/bin
SNODES_DIR=$SNODES_BASE/share/snodes

if [ -e Snodes.jar ]; then
    install -v -d $SNODES_DIR
    install -v -m 0644 $SNODES_JAR $SNODES_DIR
    install -v $SNODES_SCRIPT $SNODES_BIN
    exit 0
else
    echo -n "Snodes has not been built. Would you like to build it? "
    read prompt
    prompt=`echo $prompt | tr '[:upper:]' '[:lower:]'`
    prompt=${prompt:0:1}
    if [ $prompt = "y" ]; then
        ant
        exec ./install.sh
    else
        echo "Installation failed, exiting."
        exit 1
    fi
fi
