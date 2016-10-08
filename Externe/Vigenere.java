/*
Vigenere.java
13/11/04
E. Ballerini - ballerinie@yahoo.fr
Permet de decrypter un cryptogramme chiffre avec Vigenere

Le cryptogramme doit etre sauvergarde dans un fichier ASCII, et passe en parametre.
Pour retrouver le texte en clair, il faut passer en parametre la longueur supposee de la cle.

Le decryptement se fait en 2 temps :
1. lancer le programme une premiere fois pour trouver la longueur de la cle (methode des kappas)
2. lancer le programme une deuxieme fois en donnant en entree la longueur supposee de la cle.

Ainsi, il faut d'abord taper :

#java Vigenere <cryptogramme>

Le programme renvoie les valeurs des kappas triees par ordre decroissant (kappas calculees dans 
l'intervalle [1;70]). Cela permet de trouver la longueur de la cle (si le cryptogramme a des proprietes 
statistiques semblables a celles d'un texte quelconque en francais). La longueur de la cle est normalement 
le plus petit denominateur commun des premieres valeurs de kappas (pourcentage superieur a environ 5 %).
Ensuite, il faut taper :

#java Vigenere <cryptogramme> <longueur_supposee_de_la_cle>

Le programme retrouve la cle, l'affiche, et affiche le cryptogramme decrypte (normalement, il s'agit du 
texte en clair, sans les espaces).

Vous pourrez telecharger gratuitement une machine virtuelle directement sur le site de Sun, a l'adresse : 
http://java.sun.com/j2se/1.4.2/download.html
Il s'agit de la version 1.4.2, ce qui ne devrait pas poser de probleme pour faire tourner mon petit programme. 
Vous pouvez choisir de telecharger le JRE (inclus entre autres une machine virtuelle, suffisant pour faire 
tourner le programme) ou le SDK (inclus une machine virtuelle, un compilateur, et d'autres programmes, fait 
pour le developpement).

*/

package decryption;
import java.io.*;

public class Vigenere {
	private String cryptogrammeBrut; // sans les espaces, mais avec les EOL
	private String cryptoFormat; // sans expace, sans EOL
	private String cryptoDecale;
	private String nomFichier;
	private FileInputStream fichier;
	private int longueur; // nb de caracteres du cryptogramme
	private float[] kappa;
	private int[] ordre;
	private	int longueurCle;
	private int[] cle;
	private static final int decalageAscii = 97;

	public Vigenere(String nomFichier) {
		//this.nomFichier = nomFichier;
		this(nomFichier, "0");
	}

	public Vigenere(String nomFichier, String longueurCle) {
		this.nomFichier = nomFichier;
		try {
			this.longueurCle = Integer.parseInt(longueurCle);
		}
		catch(NumberFormatException e) {
			System.out.println("Erreur concernant la longueur de la cle");
			this.longueurCle = 0;
		}
	}

	int recupererCrypto() {
		try {
			fichier = new FileInputStream(nomFichier);
			byte[] b;
			int retour = 0;
			cryptogrammeBrut = new String(); 
			String carUnique;
			while(retour != -1) {
				b = new byte[1];
				try {
					retour = fichier.read(b);
					carUnique = new String(b);
					if((carUnique.compareTo(" ") != 0))
						cryptogrammeBrut = cryptogrammeBrut.concat(carUnique);
				}
				catch(IOException e) {
					System.out.println("Pb d'E/S");
					return -1;
				}
			}
			fichier.close();
			return 0;
		}
		catch(Exception e) {
			return -1;
		}
	}

	void formatterCrypto() {
		cryptoFormat = new String();
		for(int i = 0 ; i < cryptogrammeBrut.length() ; i++) {
			if(Character.isLetter(cryptogrammeBrut.charAt(i))) // Pour supprimer les EOL
				cryptoFormat = cryptoFormat.concat(cryptogrammeBrut.substring(i, i+1));
		}
		cryptoFormat = cryptoFormat.toLowerCase();
		System.out.println("Cryptogramme : " + cryptoFormat);
		longueur = cryptoFormat.length();
		System.out.println("longueur : " + longueur);
	}

	void chercherMax() {
		int nbRecherches = 15;
		if(kappa.length >= nbRecherches)
			ordre = new int[nbRecherches];
		else
			ordre = new int[kappa.length];
		for(int i = 0 ; i < ordre.length ; i++)
			ordre[i] = -1;
		for(int i = 0 ; i < ordre.length ; i++) {
			if(i > 0)
				ordre[i] = trouverSuivant(kappa[ordre[i - 1]]);
			else
				ordre[0] = trouverSuivant(101);
		}
		System.out.println("Kappa dans l'ordre : ");
		for(int i = 0 ; i < ordre.length ; i++)
			System.out.println("i = " + (ordre[i] + 1) + " avec " + kappa[ordre[i]] + " %");
	}

	int trouverSuivant(float kappaPrec) {
		int indexMax = -1;
		float max = -1;
		for(int i = 0 ; i < kappa.length ; i++) {
			if((kappa[i] > max) && (kappa[i] <= kappaPrec)) {
				if(indexDejaPresent(i) == false) {
					max = kappa[i];
					indexMax = i;
				}
			}
		}
		return indexMax;
	}

	boolean indexDejaPresent(int index) {
		int i = 0;
		while(i < ordre.length) {
			if(ordre[i] == index)
				return true;
			i++;
		}
		return false;
	}

	void calcKappa() {
		int nbKappa = 65;
		if(cryptoFormat.length() >= nbKappa)
			kappa = new float[nbKappa];
		else
			kappa = new float[cryptoFormat.length() - 1];
		for(int i = 0 ; i < kappa.length ; i++) {
			cryptoDecale = new String();
			cryptoDecale = cryptoDecale.concat(cryptoFormat.substring(longueur - (i + 1)));
			cryptoDecale = cryptoDecale.concat(cryptoFormat.substring(0, longueur - (i + 1)));
			kappa[i] = effectuerComparaison();	
		}
	}

	float effectuerComparaison() {
		int nbCorrespondances = 0;
		for(int i = 0 ; i < cryptoFormat.length() ; i++) {
			if(cryptoFormat.charAt(i) == cryptoDecale.charAt(i))
				nbCorrespondances++;
		}
		return ((float) ((int) ((float) nbCorrespondances / (float) longueur * 10000))) / 100;
	}

	void trouverCle() {
		cle = new int[longueurCle];
		int[] nbCar;
		int valeurMax;
		int indexValeurMax;
		int index;
		int codeAsciiLettre;
		int nbCorrespondances;
		int carEAscii;
		int aAscii = 97;
		int eAscii = 101;
		int zAscii = 122;
		System.out.print("Cle : ");
		for(int i = 0 ; i < longueurCle ; i++)
		{
			index = i;
			codeAsciiLettre = 0;
			nbCar = new int[26];
			nbCorrespondances = 0;
			valeurMax = -1;
			indexValeurMax = -1;
			while(index < longueur) {
				codeAsciiLettre = (int) cryptoFormat.charAt(index);
				nbCar[codeAsciiLettre - decalageAscii]++;
				index += longueurCle;
				nbCorrespondances++;
			}
			for(int j = 0 ; j < 26 ; j++) {
				if(nbCar[j] > valeurMax) {
					valeurMax = nbCar[j];
					indexValeurMax = j;
				}
			}
			carEAscii = indexValeurMax + decalageAscii;
			if(carEAscii < eAscii) {
				cle[i] = zAscii - (eAscii - carEAscii) + 1;
				System.out.print((char) cle[i]);
			}
			else {
				cle[i] = aAscii + (carEAscii - eAscii);			
				System.out.print((char) cle[i]);
			}
		}
	}
	
	void decrypterCrypto() {
		int index = 0;
		int car;
		int decalage;
		System.out.println("\nTexte en clair : ");
		while(index < cryptoFormat.length()) {
			car = (int) cryptoFormat.charAt(index);
			decalage = cle[index % longueurCle];
			if(car >= decalage) {
				System.out.print((char) (car - decalage + decalageAscii));
			}
			else {
				System.out.print((char) (car + 26 - decalage + decalageAscii));
			}
			index++;
		}
	}

	boolean longueurCleConnue() {
		if(longueurCle > 0)
			return true;
		else
			return false;
	}

	
}

