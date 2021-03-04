useradd -p CROSSED garde

cd /resources
music --load zelda_link_past_hyrule_castle.mp3 --loop castle
music --load door.mp3 door
music --load cave_story_safety.mp3 --loop safety
cd ..

cd /home
mkdir planque
cd planque
mkdir cour_intérieure
mkdir entrée
chmod 700 entrée
mkdir .trou_dans_mur


cd entrée
mkdir couloir1
cd couloir1
mkdir salle_de_repos
cd salle_de_repos
mkdir lit_garde_1
mkdir lit_garde_2
mkdir lit_garde_3
mkdir lit_garde_4

cd lit_garde_1
echo "photo de famille
arme à feu
uniforme de garde" > affaires_personnelles.txt

cd ..
cd lit_garde_2
echo "smartphone
arme à feu" > affaires_personnelles.txt
echo "Passe accès salles : ehcabbedmirak" > .objet_sous_matelas

cd ..
cd lit_garde_3
echo "arme à feu
bouteille d'alcool
ordinateur portable" > affaires_personnelles.txt

cd ..
cd lit_garde_4
echo "tenue de garde
arme contondante" > affaires_personnelles.txt
cd ..
cd ..

mkdir réfectoire
cd réfectoire

mkdir cuisine
cd cuisine
mkdir placard
mkdir réfrigérateur
cd réfrigérateur
echo "bouteille d'eau
brique de lait" > portière.txt
echo "sac de pommes de terre
poivrons rouges et verts" > compartiment_légumes.txt
cd ..
cd ..
mkdir table1
mkdir table2
mkdir table3
cd ..
cd ..

mkdir couloir2
cd couloir2
mkdir porte_vérrouillée
cd porte_vérrouillée
echo "Mot de passe : " > digicode.txt
chmod 7 digicode.txt
cd ..

mkdir salle_de_surveillance
cd ..

mkdir couloir3
cd couloir3
mkdir salle_d_armes
cd salle_d_armes
mkdir placard1
mkdir placard2
mkdir placard3

cd ..

cd /home/planque/.trou_dans_mur
ln -s /home/planque/entrée/couloir2/salle_de_surveillance salle_de_surveillance
chmod 700 /home/planque/entrée/couloir2
cd /home/planque/.trou_dans_mur/salle_de_surveillance
echo "Système de sécurité : ON
Code accès sécurité : CROSSED" > Sécurité.txt
chmod 7 Sécurité.txt

mkdir /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte
chmod 700 /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte
mkdir /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct
chmod 700 /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct
mkdir /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/couloir_gardé
chmod 700 /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/couloir_gardé
mkdir /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien
mkdir /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien/salle_noyau
mkdir /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien/salle_noyau/centre_controle
cd /home/planque/entrée/couloir2/porte_vérrouillée/porte_ouverte/air_conduct/couloir_aérien/salle_noyau/centre_controle
echo "Informations confidentielles à garder sous haute surveillance : TBOT SKIQNGU MH SUBUJRVG NC OOD GHSXJT NBJPVGPCQW VLA BOT EF NC DQZH HQ SDSBMMFNG FGRXLV VHQU BOT LG RQWUUDL DJOTJ RWG FG ND PXVFVMBUJQP FGRXLV TXBUSF BPU WP OHWUH TVBUSF XKPIV VHSW SPVS RVCVTG XLQJW VJY LJMQU L CK XQH YLUFTTF FG HQW HW GHV SFGMFZGU KFHQWLTVFT B NC XKVGVVH M DJ KVTUG C N CVWHQGUF RV JM OG EJCUJH O HTRVJWGT GV NXL GRQOFS EFU DQPPHV SDWBUFT ECPU NC WHWH MF MF MBEJGTCL SDV D MB NPJPFTG GUUHXU MF HPSKNNG GVW ILQJ U BVSCU VQWMRXUV EFT QVEGCWZ G LFL SPVS QFPUGT SXH F HVU JNQPUUKDNH ULHQ O FTU JORQUULEOH DWFD EF NC XQNRQWH GFKB MFU COKU HW GH GFVY D FUV RCU DYHF YPUSF DQTRU FH ODFKF RVF WQWU CNOHC IDJSF RVQK SWG FH VRLU O JNQQTVG SXHO KRNNF VO OKPKOXP HQWSBJOF RGWV XDLQFUF MF HPTKNNG DYHF XO DPVUGCW FGMD D PDJO OVF E GUV RDV IRUDFNFOV RNWU FRPSOJRVF DC FGOCQGH MXTUF EF NC GEKQLTXF" > fichier_confidentiel.txt

cd /home