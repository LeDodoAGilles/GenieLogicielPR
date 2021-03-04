_dialogue0(){
music --play castle
print Nonaca "C'est le moment pour ta première mission $_nomPersonnage , cette fois c'est moi qui t'accompagnerait !"
sleep 2000
print Nonaca "On a réussi à localiser une des planques de l'architecte, on pourrait y trouver des informations importantes le concernant, ton objectif est de les trouver et de les récupérer !"
sleep 2000
print Nonaca "La planque semble protégée par un système de surveillance, commence par trouver la salle de contrôle et désactive les caméras."
sleep 2000
print Nonaca "J'ai repéré une faille dans un des murs, tu devrais pouvoir t'y faufiler !"
}

_dialogue1(){
print Nonaca "Ok, le système semble avoir été déjoué, tu devrais pouvoir te déplacer plus librement."
sleep 2000
print Nonaca "Déguise toi en garde avant de poursuivre, on n'est jamais trop prudent"
}

_dialogue2(){
print Nonaca "Normalement tu devrais pouvoir entrer sans te faire repérer maintenant !"
sleep 2000
print Nonaca "Il faut que tu trouves un moyen d'atteindre le noyau maintenant, il est probablement protégé par un système de sécurité lui aussi"
}

_dialogue3(){
print Nonaca "Cherche dans les affaires personnelles des gardes, tu pourrais peut être y trouver un moyen d'atteindre le centre de la planque."
}

_dialogue4(){
print Nonaca "On dirait qu'il y a quelque chose de coincé entre le matelas et le sommier, fouille un peu plus en profondeur, je pense qu'on tient une piste."
}

_dialogue5(){
music --pause castle
music --play door
print Nonaca "Il semblerait que la porte se soit ouverte, bravo !"
sleep 2000
print Nonaca "Fais attention, la zone suivante doit être sous haute surveillance, ton déguisement ne te suffira sûrement plus."
music --play castle
}

_dialogue6(){
print Nonaca "J'ai repéré une bouche d'aération qui mène droit au noyau de la planque, seulement elle est située en hauteur."
sleep 2000
print Nonaca "Essaie de prendre appui sur quelque chose pour l'atteindre."
}

_dialogue7(){
print Nonaca "Bien, traverse les aérations, au bout du couloir se trouve le noyau, c'est là-bas que sont censées être les données qui nous intéressent."
}

_dialogue8(){
print Nonaca "J'avais raison ! C'est ici que se trouve le centre de contrôle et toutes les données qu'il contient."
sleep 2000
print Nonaca "Vite, trouve ce dont on a besoin et tire toi d'ici !"
sleep 2000
print Nonaca "Je t'ouvrirai une porte de sortie dès que tu auras fini."
}

_dialogue9(){
print Nonaca "Les informations sont dans ce fichier, transfère les vite dans un fichier 'cloud' !"
}

_dialogue10(){
print Nonaca "Je t'ai créé une sortie, dépêche toi de sortir !"
}

_dialogue11(){
music --stop castle
music --play safety
print Nonaca "Bravo $_nomPersonnage ! Je ne t'en pensais pas capable."
sleep 2000
print Nonaca "Avec ces données on devrait pouvoir mettre en place notre plan pour s'attaquer à l'architecte"
sleep 2000
print Nonaca "Tiens le code pour t'échapper d'ici $__password "
}