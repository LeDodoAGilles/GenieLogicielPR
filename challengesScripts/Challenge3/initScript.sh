useradd -p motdepasse admin

cd /resources
music --load cave_story_scorching_back.mp3 --loop scorch
music --load cave_story_seal_chamber.mp3 --loop seal
music --load cave_story_quiet.mp3 --loop quiet
music --load cave_story_last_battle.mp3 --loop boss
music --load door.mp3 door
music --load undertale_heart.mp3 heart
cd ..

cd /home
mkdir base
cd base
mkdir entrée_principale
cd entrée_principale
mkdir grand_couloir
cd grand_couloir
mkdir salle_du_noyau
cd salle_du_noyau
mkdir le_cube
chmod 755 le_cube
cd le_cube
cd /home


_cube0(){
echo "#Tu es coincé dans le cube :
#   +------+.
#   |`.    | `.
#   |  `+--+---+
#   |   |  |   |
#   +---+--+.  |
#    `. |    `.|
#      `+------+
#Le seul moyen que tu as de t'en échapper est de suivre les règles que je vais t'imposer.
#Tu devras créer 3 fonctions de difficulté croissante.
#Ce n'est qu'au bout de ces trois épreuves que tu seras libéré
#Pour commencer ta première épreuve, quitte ce fichier et ouvre le fichier cube_défi1 ." > /home/base/entrée_principale/grand_couloir/salle_du_noyau/le_cube/Rules
}



_cube1(){
echo "#La première fonction que l'on te demande est basique. On souhaite qu'elle affiche la somme de deux variables prises en argument de ta fonction.
#Pour rappel, lorsqu'on souhaite récupérer les valeurs des arguments ou d'une variable on utilise le symbole '\$'.
#Par exemple dans le cas de la somme, le premier argument s'écrit '\$1' et le deuxième '\$2'

face1(){
somme=
}" > cube_défi1
}


_cube2(){
echo "#La deuxième fonction prendra deux variables entières en arguments et renverra la valeur maximum entre ces deux variables.

face2(){
max=
}" > cube_défi2
}

_cube3(){
echo "#La dernière fonction que tu dois implémenter est la fonction de Fibonnacci.
#Elle prend en argument une seule variable entière et renvoie son résultat dans la variable fibonacci.


face3(){
fibonacci=
}" > cube_défi3
}


