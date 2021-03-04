_dialogue0(){
music --play scorch
_print Thadeus "Nous y voilà $_nomPersonnage ! Grâce aux données récupérées lors de ta précédente mission on a pu localiser le Shaper. La base qui se trouve devant nous serait son repère."
#sleep 2000
_print Nonaca "Il est enfin temps de mettre fin à ses sombres dessins."
}

_dialogue11(){
_print Nonaca "On dirait qu'il n'y a qu'une seule entrée."
#sleep 2000
_print Thadeus "Et elle n'a pas l'air gardée, c'est étrange."
#sleep 2000
_print Nonaca "On a pas de temps à perdre, entrons !"
}

_dialogue21(){
_print Thadeus "On a croisé personne jusqu'ici, c'est de plus en plus bizarre, ça pourrait être un piège."
#sleep 2000
_print Nonaca "C'est étrange effectivement, restons sur nos gardes."
}

_dialogue31(){
_print Thadeus "L'endroit que l'on recherche est au bout du couloir."

}

_dialogue41(){
music --stop scorch
music --play door
_textItalique "En entrant dans la salle vous entendez la porte se fermer derrière vous, la lumière s'éteint et vous ne percevez plus aucun bruits."
#sleep 2000
_textItalique "Quelques secondes plus tard la lumière se rallume. Vos compagnons ne sont cependant plus à vos côtés."
#sleep 2000
_print $_nomPersonnage "Nonaca ? Thadeus ? Vous êtes là ?"
#sleep 2000
music --play seal
_textItalique "Votre phrase finie, une voix s'adresse à vous, mais vous ne parvenez pas à déterminer d'où elle provient"
#sleep 2000
_print ??? "Ils ne sont plus là, ça ne sert à rien de les appeler."
#sleep 4000
_print $_nomPersonnage "Qu'avez vous fait d'eux ? Ou sont-ils ? et qui êtes vous !"
#sleep 4000
_print ??? "Allons, vous savez très bien qui je suis, si vous êtes venus ici c'est pour me trouver. Je ne sais pas ce qui vous a poussé à venir ici mais je vous laisserai pas faire !"
#sleep 4000
_print $_nomPersonnage "A vrai dire, je ne sais pas vraiment non plus pourquoi je suis ici. Mais Nonaca et Thadeus sont mes compagnons, je ne peux pas les laisser tomber."
#sleep 4000
_print Shaper "Soit, alors rejoignez les vous aussi !"
music --stop seal
#sleep 2000
music --play heart
_textItalique "Au moment ou vous entendez ces mots, une épée vous transperce le ventre et vous perdez connaissance.
      /| ________________
O|===|* >________________>
     \\| "
#sleep 5000
music --play quiet
_textItalique "Vous vous réveillez alors dans une nouvelle salle, et constatez que votre blessure au ventre a disparu."
_print $_nomPersonnage "Il y a quelqu'un ?"
#sleep 2000
_textItalique "Personne ne vous répond."
}

_dialogue51(){
_textItalique "Après quelques essais, vous vous rendez compte que vous n'avez plus accès à vos outils habituels, seules quelques commandes sont encore utilisables."
#sleep 2000
_print $_nomPersonnage "On dirait que je vais devoir me débrouiller avec ce que j'ai. Je dois bien pouvoir faire quelque chose de ce fichier."
}

_dialogue61(){
_print $_nomPersonnage "J'ai jamais fais de bash moi, c'est quoi cette merde !"
}

_dialogue71(){
_print $_nomPersonnage "Bon, j'ai réussi à m'en sortir, en même temps c'était pas grand chose."
}

_dialogue81(){
_print $_nomPersonnage "Je m'en sors pas trop mal on dirait ! Plus qu'une épreuve, restons concentrés."
}

_dialogue91(){
music --stop quiet
music --play boss
_textItalique "Au moment où vous validez votre troisième fonction, vous voyez des fissures apparaître sur les murs qui vous entourent"
_textItalique "Les murs s'effondrent petit à petit, pour ne laisser que des débris au sol"
_textItalique "Vous reconnaissez alors la pièce dans laquelle vous êtes, c'est la salle du noyau dans laquelle vous vous êtes évanoui"
sleep 2000
_print Shaper "Il semblerait que tu sois venu à bout du cube, félicitation ! Cependant ce n'était là que les prémisses de nos futurs affrontements. Tu ne reverras pas tes amis avant de m'avoir vaincu."
sleep 2000
_print Shaper "Je te laisse pour cette fois, utilise cette clé : $__password pour t'enfuir, et revient me voir lorsque tu maîtriseras mieux le bash !"
}
