_backState=$_state
case $_state in

1)
_result=(pwd;) #On récupère le chemin actuel dans une variable
if [ $_result = "/home/base" ] #On teste si le chemin actuel est égal à /home/base
then 
_state=2 #On change d'état
_f(){
_dialogue11 #On lance la ligne de dialogue11
}
fi;;

2)
_result=(pwd;) #On récupère le chemin actuel dans une variable
if [ $_result = "/home/base/entrée_principale" ] #On teste si le chemin actuel est égal à /home/base/entrée_principale
then
_state=3 #On change d'état
_f(){
_dialogue21 #On lance la ligne de dialogue21
}
fi;;

3)
_result=(pwd;)
if [ $_result = "/home/base/entrée_principale/grand_couloir" ]
then
_state=4 #On change d'état
_f(){
_dialogue31 #On lance la ligne de dialogue31
}
fi;;

4)
_result=(pwd;)
if [ $_result = "/home/base/entrée_principale/grand_couloir/salle_du_noyau" ]
then
_state=5 #On change d'état
_f(){
_dialogue41 #On lance la ligne de dialogue41
history -c
cd le_cube #On déplace le joueur dans le dossier le_cube
_disCommands #On appelle une fonction qui permet de lui interdire l'utilisation de commandes en particulier


}
fi;;

5)
_result=(history | wc -l | cut -d "\t" -f 0;)
if [ $_result -ge 2 ]
then
_state=6 #On change d'état
_f(){
_dialogue51 #On lance la ligne de dialogue51
_cube0 #On appelle une fonction qui génère un fichier contenant les règles d'écriture du bash
chown admin Rules
}
fi;;

6)
_result=(history | grep "nano Rules" | wc -l | cut -d "\t" -f 0;)
if [ $_result -ge 1 ]
then
_state=7 #On change d'état
_cube1 #On appelle une fonction qui génère un fichier contenant le script bash à écrire
chmod 777 cube_défi1
chown admin cube_défi1
_f(){
_dialogue61 #On lance la ligne de dialogue61
}
fi;;

7)
if functions -e face1 #On teste si la fonction crée par le joueur ne renvoie pas d'erreur
then
face1 550 600
if [ -z $somme ] #On teste que la variable qui contient la valeur renvoyée par la fonction n'est pas vide
then
if [ $somme = 1150 ] #On teste que la veur renvoyée par la fonction crée par le joueur est celle à laquelle on s'attend
then
_state=8 #On change d'état
_cube2 #On appelle une fonction qui génère un fichier contenant le script bash à écrire
chmod 777 cube_défi2
chown admin cube_défi2
_f(){
_dialogue71 #On lance la ligne de dialogue71
}
fi;
fi;
fi;;

8)
if functions -e face2 #On teste si la fonction crée par le joueur ne renvoie pas d'erreur
then
face2 400 800
if [ -z $max ] #On teste que la variable qui contient la valeur renvoyée par la fonction n'est pas vide
then 
if [ $max = 800 ] #On teste que la veur renvoyée par la fonction crée par le joueur est celle à laquelle on s'attend
then 
face2 300 200
if [ $max = 300 ] #On teste que la veur renvoyée par la fonction crée par le joueur est celle à laquelle on s'attend
then
_state=9 #On change d'état
_cube3 #On appelle une fonction qui génère un fichier contenant le script bash à écrire
chmod 777 cube_défi3
chown admin cube_défi3
_f(){
_dialogue81 #On lance la ligne de dialogue81
}
fi;
fi;
fi;
fi;;

9)
if functions -e face3 #On teste si la fonction crée par le joueur ne renvoie pas d'erreur
then
face3 5
if [ -z $fibonacci ] #On teste que la variable qui contient la valeur renvoyée par la fonction n'est pas vide
then
if [ $fibonacci = 5 ] #On teste que la veur renvoyée par la fonction crée par le joueur est celle à laquelle on s'attend
then
face3 8 
if [ $fibonacci = 21 ] #On teste que la veur renvoyée par la fonction crée par le joueur est celle à laquelle on s'attend
then
_state=10 #On change d'état
_f(){ 
_dialogue91 #On lance la ligne de dialogue91
}
fi;
fi;
fi;
fi;;

10)
_enCommands #On réactive l'utilisation des commandes à l'utilisateur
cd ..


esac

if [ $_backState != $_state ]
then
_f;
fi;

