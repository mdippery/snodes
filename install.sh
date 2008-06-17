#!/bin/sh

# Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
# Copyright (c) 2007-2008 Michael Dippery <mdippery@bucknell.edu>
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
#   Michael Dippery <mdippery@bucknell.edu>


# This could probably be written in a better way, so if any Unix wizard feels
# like taking a look at it, please do. I suck at writing shell scripts. - mpd

SNODES_JAR=Snodes.jar
SNODES_BIN=snodes
SNODES_DIR=/usr/local/share/snodes

mkdir -p $SNODES_DIR &>/dev/null
if [ $? -eq 0 ]; then
    cp $SNODES_JAR $SNODES_DIR &>/dev/null
    if [ $? -eq 0 ]; then
        echo "Installed Snodes JAR in $SNODES_DIR"
    else
        echo "Could not install JAR in $SNODES_DIR"
        exit 1
    fi
    
    mkdir -p /usr/local/bin &>/dev/null
    if [ $? -eq 0 ]; then
        cp $SNODES_BIN /usr/local/bin &>/dev/null
        if [ $? -eq 0 ]; then
            echo "Installed launcher script in /usr/local/bin"        
            echo "Snodes installed successfully! Type 'snodes -h' to view help information."
            exit 0
        else
            echo "Could not copy launcher script to /usr/local/bin"
            exit 1
        fi
    else
        echo "Could not create directory: /usr/local/bin"
        exit 1
    fi
else
    echo "Could not create directory: $SNODES_DIR"
    exit 1
fi
