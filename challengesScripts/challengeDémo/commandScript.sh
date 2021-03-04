_backState=$_state
case $_state in

1)
_result=(pwd;) #On récupère le chemin actuel dans une variable
if [ $_result = "/home/base" ] #On teste si le chemin actuel est égal à /home/base
then 
_state=2 "On change d'état
_f(){
_dialogue11 #On lance la ligne de dialogue11
}
fi;;

2)
_result=(pwd;) #On récupère le chemin actuel dans une variable
if [ $_result = "/home/base/entrée_principale" ] #On teste si le chemin actuel est égal à /home/base/entrée_principale
then
_state=3
_f(){
_dialogue21
}
fi;;

3)
_result=(pwd;)
if [ $_result = "/home/base/entrée_principale/grand_couloir" ]
then
_state=4
_f(){
_dialogue31
}
fi;;

4)
_result=(pwd;)
if [ $_result = "/home/base/entrée_principale/grand_couloir/salle_du_noyau" ]
then
_state=5
_f(){
_dialogue41
history -c
cd le_cube
_disCommands


}
fi;;

5)
_result=(history | wc -l | cut -d "\t" -f 0;)
if [ $_result -ge 2 ]
then
_state=6
_f(){
_dialogue51
_cube0
chown admin Rules
}
fi;;

6)
_result=(history | grep "nano Rules" | wc -l | cut -d "\t" -f 0;)
if [ $_result -ge 1 ]
then
_state=7
_cube1
chmod 777 cube_défi1
chown admin cube_défi1
_f(){
_dialogue61
}
fi;;

7)
if functions -e face1 
then
face1 550 600
if [ -z $somme ]
then
if [ $somme = 1150 ]
then
_state=8
_cube2
chmod 777 cube_défi2
chown admin cube_défi2
_f(){
_dialogue71
}
fi;
fi;
fi;;

8)
if functions -e face2
then
face2 400 800
if [ -z $max ]
then 
if [ $max = 800 ]
then 
face2 300 200
if [ $max = 300 ]
then
_state=9
_cube3
chmod 777 cube_défi3
chown admin cube_défi3
_f(){
_dialogue81
}
fi;
fi;
fi;
fi;;

9)
if functions -e face3
then
face3 5
if [ -z $fibonacci ]
then
if [ $fibonacci = 5 ]
then
face3 8
if [ $fibonacci = 21 ]
then
_state=10
_f(){
_dialogue91
}
fi;
fi;
fi;
fi;;

10)
_enCommands
cd ..


esac

if [ $_backState != $_state ]
then
_f;
fi;

