_dialogue0(){

music --stop main
music --play main


print Thadeus "Bienvenue dans la Matrice $_nomPersonnage !"
sleep 2000
print Thadeus "Mon nom est Thadeus, tu as été recruté pour rejoindre notre groupe et tu vas devoir nous prouver que tu en es à la hauteur !"
sleep 2000
print Thadeus "Commence par regarder ce qui se trouve autour de toi. (tape la commande ls pour découvrir quels fichiers se trouvent dans ton environnement)"
}

_dialogue01(){
print Thadeus "C’est l’heure de passer ton \e[5mpremier test\e[25m!"
sleep 2000
print Thadeus "Comme tu le sais déjà, on va avoir besoin de ton aide pour \e[4matteindre le Shaper\e[24m, mais on veut d'abord s'assurer de ce que tu vaux vraiment."
sleep 2000
print Thadeus "Pour ce test tu vas devoir infiltrer une base de données pour y trouver et récupérer une clé."
sleep 2000
print Thadeus "Tout d'abord explore un peu ton environnement, apprends en plus sur les fichiers que le \e[9mfourbe\e[29m Shaper a laissé ici."
}

_dialogue11(){
print Thadeus "Bien, maintenant que tu as pris tes marques, le but de la mission comme je te l'ai expliqué est de récupérer une clé, tu ne pourras pas sortir avant de l'avoir récupérée."
sleep 2000
print Thadeus "Mais avant de la récupérer il va falloir la trouver ! Cherche un peu parmi les fichiers si tu trouves quelque chose qui la mentionne, tu peux te servir de l’outil grep pour rechercher un mot parmi des fichiers texte."
}

_dialogue21(){
print Thadeus "C'est ici que se trouvera la porte vers la deuxième partie, si tu ne sais pas encore ce que c'est, c'est que tu dois encore fouiller dans les fichiers."
}


_dialogue13Error(){
print Thadeus "tu as commis une erreur."
}

_dialogue31(){
print Thadeus "Utilise le lien que tu viens de créer pour te déplacer."
}

_dialogue41(){
print Thadeus "Te voilà dans la deuxième partie du test, comme tu as pu le lire la clé est fragmentée en trois morceaux et tu vas devoir les rassembler pour pouvoir l'utiliser."
sleep 2000
print Thadeus "Je vais t'indiquer comment trouver le premier morceau. Avant tout commence par créer le réceptacle à la clé. Ici fera l’affaire."
}

_dialogue51(){
print Thadeus "Le premier morceau se trouve dans un des fichiers, mais le fourbe Shaper t'en a interdit l'accès, trouve un moyen d'ouvrir ce document !"
sleep 2000
print Thadeus "Pour ce faire donne toi les droits d'accès !"
}

_dialogue61(){
print Thadeus "Une fois que tu auras trouvé le deuxième morceau, inscrit les deux premiers dans le réceptacle."
sleep 2000
print Thadeus "C’est moi qui te fournirait le dernier mais j’aurai besoin que tu trouves quelque chose pour moi d’abord"
}

_dialogue71(){
print Thadeus "Bien, comme je te l’ai dit c’est moi qui détient le dernier morceau, mais avant que je te le donne j’aimerai m’assurer que tu sois capable d’un minimum de choses"
sleep 2000
print Thadeus "Le Shaper cherche à cacher des données qui nous seront importantes par la suite, prouve moi que tu es capable de récupérer ces données et récupère le nombre de mots et de lignes que contient le fichier Mécanographie.txt"
}

_dialogue81(){
print Thadeus "Insère le nombre de lignes que tu as trouvées!"

}

_dialogue82(){
print Thadeus "Le dernier morceau est $__passwordPart2 , ajoute le au fichier réceptacle et ton test se terminera."
}

_dialogue91(){

music --stop main
music --play victory
print Thadeus "Bravo $_nomPersonnage , tu as su montrer de quoi tu étais capable ! Si tu souhaites t’échapper du test tu n’as plus qu’à taper la commande "su" suivie du mot de passe"
music --play badguy

print Thadeus "Bravo $_nomPersonnage , tu as su montrer de quoi tu étais capable ! Si tu souhaites t’échapper du test tu n’as plus qu’à taper la commande "su" suivie du mot de passe"

print Shaper "\e[3mOh que fais tu dans mon domaine, mortel?\e[23m"
}

_dialogue101(){
print Shaper "Je vois qu’il y a une nouvelle recrue chez les rebelles … "
sleep 2000
print Shaper "Ne t’avise pas d’interférer avec mes plans ou je me verrai obligé d'intervenir !"
}


