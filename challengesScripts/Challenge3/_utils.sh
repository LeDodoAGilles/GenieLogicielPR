_print(){
case $1 in
Thadeus)
i="\e[32m"
;;

Shaper)
i="\e[31m"
;;
Nonaca)
i="\e[36m"
;;
$_nomPersonnage)
i="\e[33m"
;;
"\?\?\?")
i="\e[31m"
;;
esac
echo "("$i${1}"\e[0m) $2"
}


_textItalique(){
echo "\e[3;37m$1\e[0m"
}

_disCommands(){
disable -u $_nomPersonnage alias
disable -u $_nomPersonnage cat
disable -u $_nomPersonnage cd
disable -u $_nomPersonnage chgrp
disable -u $_nomPersonnage chmod
disable -u $_nomPersonnage clean
disable -u $_nomPersonnage clear
disable -u $_nomPersonnage cp
disable -u $_nomPersonnage cut
disable -u $_nomPersonnage echo
disable -u $_nomPersonnage escape
disable -u $_nomPersonnage false
disable -u $_nomPersonnage find
disable -u $_nomPersonnage font
#disable -u $_nomPersonnage functions
disable -u $_nomPersonnage grep
disable -u $_nomPersonnage groupadd
disable -u $_nomPersonnage groups
disable -u $_nomPersonnage history
disable -u $_nomPersonnage ln
disable -u $_nomPersonnage mkdir
disable -u $_nomPersonnage mv
disable -u $_nomPersonnage pwd
disable -u $_nomPersonnage read
disable -u $_nomPersonnage rm
disable -u $_nomPersonnage rmdir
disable -u $_nomPersonnage save
disable -u $_nomPersonnage sed
disable -u $_nomPersonnage sleep
disable -u $_nomPersonnage sort
#disable -u $_nomPersonnage su
disable -u $_nomPersonnage sudo
disable -u $_nomPersonnage test
disable -u $_nomPersonnage touch
disable -u $_nomPersonnage true
disable -u $_nomPersonnage unset
disable -u $_nomPersonnage useradd
disable -u $_nomPersonnage users
disable -u $_nomPersonnage var
disable -u $_nomPersonnage wc
disable -u $_nomPersonnage whoami
disable -u $_nomPersonnage exit
disable -u $_nomPersonnage seq
disable -u $_nomPersonnage expr
}