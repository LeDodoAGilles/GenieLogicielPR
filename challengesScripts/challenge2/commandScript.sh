_backState=$_state
case $_state in

-1)
_f(){
echo "Système de sécurité : ON
Code accès sécurité : CROSSED" > /home/planque/entrée/couloir2/salle_de_surveillance/Sécurité.txt #On crée un fichier texte que l'on remplit avec du texte
}
_state=0;; #On change d'état

0)
_result=(cat /home/planque/entrée/couloir2/salle_de_surveillance/Sécurité.txt;) #On teste si l'utilisateur a bien modifié le fichier
if [ $_result = "Système de sécurité : OFF
Code accès sécurité : CROSSED"]
then
_state=1 #On change d'état
_f(){
_dialogue1 #On affiche le dialogue1
}
fi;;

1)
_result=(whoami;) #On récupère l'utilisateur en cours
if [ $_result = "garde" ] #S'il a bien changé d'utilisateur pour l'utilisateur garde
then
_state=2 #On change d'état
chmod 7 /home/planque/entrée/couloir2
chmod 7 /home/planque/entrée
_f(){
_dialogue2
}
fi;;

2)
_result=(pwd;)
if [ $_result = "/home/planque/entrée/couloir1/salle_de_repos" ] #On teste si l'utilisateur se trouve bien au bon chemin
then
_state=3 #On change d'état
_f(){
_dialogue3
}
fi;;

3)
_result=(pwd;)
if [ $_result = "/home/planque/entrée/couloir1/salle_de_repos/lit_garde_2"] #On teste si l'utilisateur se trouve bien au bon chemin
then
_state=4 #On change d'état
_f(){ 
_dialogue4
}
fi;;


4)
_result=(cat /home/planque/entrée/couloir2/porte_vérrouillée/digicode.txt;) #On teste si l'utilisateur rentre bien le bon mot de passe dans le fichier texte
if [ $_result = "Mot de passe : ehcabbedmirak"]
then
_state=5 #On change d'état
chmod 7 /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte #On change les droits d'accès sur un certain fichier
_f(){
_dialogue5
}
else
echo "Mot de passe : " > /home/planque/entrée/couloir2/porte_vérrouillée/digicode.txt
fi;;

5)
_result=(pwd;)
if [ $_result = "/home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte" ] #On teste si l'utilisateur se trouve bien au bon chemin
then
_state=6 #On change d'état
_f(){
_dialogue6
}
fi;;



6)
_result=(ls /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte | grep table | wc -l | cut -d "\t" -f 0;) #On teste si l'utilisateur a bien ajouté un dossier table au bon endroit
if [ $_result -ge 1 ]
then
_state=7 #On change d'état
chmod 7 air_conduct #On change les droits d'accès sur un certain fichier
_f(){
_dialogue7
}
fi;;


7)
_result=(pwd;)
if [ $_result = "/home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien/salle_noyau" ] #On teste si l'utilisateur se trouve bien au bon chemin
then
_state=8 #On change d'état
_f(){
_dialogue8
}
fi;;

8)
_result=(pwd;)
if [ $_result = "/home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien/salle_noyau/centre_controle" ] #On teste si l'utilisateur se trouve bien au bon chemin
then
_state=9 #On change d'état
chmod 700 /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien/salle_noyau
disable nano
_f(){
_dialogue9
}
fi;;

9)
_result1=(cat fichier_confidentiel.txt;)
if [ -f /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien/salle_noyau/centre_controle/cloud ] #On teste si l'utilisateur a bien transféré les données vers un fichier "cloud"
then
_result=(cat cloud;)
fi;
if [ $_result = $_result1 ]
then
_state=10 #On change d'état
ln -s /home/planque/cour_intérieure sortie #On ajoute un lien symbolique vers un autre dossier pour permettre à l'utilisateur de se déplacer
_f(){
_dialogue10
}
fi;;

10)
_result=(pwd;)
if [ $_result = "/home/planque/cour_intérieure" ] #On teste si l'utilisateur se trouve bien au bon chemin
then
_state=11 #On change d'état
_f(){ 
_dialogue11
}
fi;;
esac

if [ $_backState != $_state ]
then
_f;
fi;
