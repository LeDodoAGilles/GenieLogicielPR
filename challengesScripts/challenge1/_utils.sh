print(){
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
esac
echo "("$i${1}"\e[0m) $2"
}
