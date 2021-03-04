_backState=$_state
case $_state in

0)
_result=(history |grep ls |wc -l| cut -d "\t" -f 0;) #Récupère le nombre de fois que l'utilisateur a tapé la commande ls
if [ $_result -ge 1 ] #On teste s'il a tapé ls au moins une fois
then
_state=1; #S'il a tapé une fois la commande ls alors on change son état
_f(){_dialogue01;} #On lance le dialogue01
fi;;

1)
_result=(history |grep cd |wc -l| cut -d "\t" -f 0;) #Récupère le nombre de fois que l'utilisateur a tapé la commande cd
if [ $_result -ge 3 ] #On teste s'il a tapé au mois 3 fois la commande cd
then
_state=2; #On change d'état

_f(){
_dialogue11; #On lance le dialogue11
}
fi;;

2)
_result=(pwd;)
if [ $_result = "/home/Le_concept_d_information/Représenter_les_données_de_manière_symbolique" ] #On teste si l'utilisateur se situe dans le dossier dans lequel on souhait qu'il aille
then
_state=3; #On change d'état
_f(){
_dialogue21 #On lance le dialogue21
}
fi;;

3)
_result=(ls -l /home/Le_concept_d_information/Représenter_les_données_de_manière_symbolique | cut -d " " -f "5,6" | grep "s" | grep "/home/Le_concept_de_machine/Les_machines_parallèles_et_spécialisées/sortie" | wc -l | cut -d "\t" -f 0;) #On compte le nombre de liens symbolique créés dans un dossier précis et visant un dossier précis
if [ $_result -ge 1 ] #Si il y au moins un lien symbolique alors on passe la condition
then
_state=4 #On change d'état
_f(){
_dialogue31 #On lance le dialogue31
} 
fi;;

4)
_result=(pwd;)
if [ $_result = "/home/Le_concept_de_machine/Les_machines_parallèles_et_spécialisées/sortie" ] #On teste le chemin de l'utilisateur
then
_state=5; #On change d'état
_f(){
_dialogue41 #On lance le dialogue41
}
fi;;

5)
_result=(ls /home/Le_concept_de_machine/Les_machines_parallèles_et_spécialisées/sortie | grep "RECEPTACLE" | wc -l | cut -d "\t" -f 0;) #Teste s'il exitste bien un fichier du nom de RECEPTACLE
if [ $_result -ge 1 ]
then
_state=6 #On change d'état
_f(){
_dialogue51 #On lance le dialogue51
}
fi;;

6)
_result=(ls -la /home/Le_concept_de_machine/Les_machines_parallèles_et_spécialisées/sortie/Evolution_semantique | grep Evolution_semantique.txt | cut -d " " -f 0;) #On récupère les droits d'accès d'un utilisateur sur un fichier précis
if [ $_result = "------rwx" ]
then
_state=7 #On change d'état
_f(){
_dialogue61 #On lance le dialogue61
}
fi;;

7)
_result=(cat /home/Le_concept_de_machine/Les_machines_parallèles_et_spécialisées/sortie/RECEPTACLE | grep  ${__passwordPart0}${__passwordPart1}  | wc -l | cut -d "\t" -f 0;) #On compte le nombre de fois que la séquence mdp 1 et mdp 2 est écrite dans le fichier RECEPTACLE
if [ $_result -ge 1 ]
then
_state=8 #On change d'état
_f(){
_dialogue71 #On lance le dialogue71
}
fi;;

8)
_backstate=8; #On change d'arrière état
_f(){
_result=(wc -l /home/Le_concept_de_machine/Les_machines_parallèles_et_spécialisées/sortie/Mécanographie/Mécanographie.txt | cut -d "\t" -f 0;) #On compte le nombre de lignes que contient un fichier précis
_dialogue81 #On lance le dialogue81
read _result1 #On attend une entrée de l'utilisateur
if [ $_result1 = $_result ]
then
_state=9 #On change d'état
_dialogue82 #On lance le dialogue82
else
_dialogue13Error
fi
};;


9)
_result=(cat /home/Le_concept_de_machine/Les_machines_parallèles_et_spécialisées/sortie/RECEPTACLE | grep $__password | wc -l | cut -d "\t" -f 0;) #On compte le nombre de fois que le mot de passe est écrit dans le fichier RECEPTACLE
if [ $_result -ge 1 ]
then
_state=10 #On change d'état
_f(){ 
_dialogue91 #On lance le dialogue91
}
fi;;

10)
_result=(whoami;) #On récupère le nom de l'utilisateur actuel
if [ $_result = "root" ] #Si c'est l'utilisateur root on rentre dans la condition
then
_state=11 #On change d'état
fi;;

esac

if [ $_backState != $_state ]
then
_f;
fi;
