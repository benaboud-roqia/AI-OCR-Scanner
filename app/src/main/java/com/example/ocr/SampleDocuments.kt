package com.example.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

data class SampleDocument(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val sampleText: String
) {
    /**
     * Generates a clean synthetic visual document bitmap for previewing
     */
    fun generateBitmap(): Bitmap {
        val width = 700
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Clean white paper background with subtle border
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Header banner
        val headerBarPaint = Paint().apply {
            color = when (category) {
                "Facture" -> Color.rgb(37, 99, 235) // Blue
                "Contrat" -> Color.rgb(79, 70, 229) // Indigo
                "Note" -> Color.rgb(13, 148, 136) // Teal
                else -> Color.rgb(71, 85, 105) // Slate
            }
        }
        canvas.drawRect(40f, 40f, width - 40f, 90f, headerBarPaint)

        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(title.uppercase(), 60f, 72f, headerTextPaint)

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 15f
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 13f
            isAntiAlias = true
        }

        var y = 130f
        val lines = sampleText.split("\n")
        for (line in lines) {
            if (y > height - 60f) break
            if (line.startsWith("#") || line.startsWith("===")) {
                textPaint.isFakeBoldText = true
                canvas.drawText(line.replace("#", "").trim(), 50f, y, textPaint)
                textPaint.isFakeBoldText = false
                y += 24f
            } else if (line.isBlank()) {
                y += 14f
            } else {
                canvas.drawText(line.take(65), 50f, y, textPaint)
                y += 22f
            }
        }

        // Bottom watermark
        canvas.drawText("DOCUMENT TEST • AI OCR SCANNER", 50f, height - 30f, metaPaint)

        return bitmap
    }
}

object SampleDocumentProvider {
    val samples = listOf(
        SampleDocument(
            id = "facture_01",
            title = "Facture - TechSolutions SARL",
            category = "Facture",
            description = "Facture de prestation informatique avec TVA et tableau de montants.",
            sampleText = """TECHSOLUTIONS SARL
12 Avenue des Champs-Élysées, 75008 Paris
SIRET : 849 201 942 00018 - TVA : FR 48 849201942
Téléphone : +33 1 42 68 55 00

FACTURE N° FAC-2026-0894
Date d'émission : 14 Septembre 2026
Date d'échéance : 14 Octobre 2026
Client : Cabinet Martin & Associés

DÉSIGNATION DES PRESTATIONS :
------------------------------------------------------------
1. Audit de sécurité infrastructure cloud : 1 200,00 €
2. Déploiement pipeline CI/CD automatisé  : 1 850,00 €
3. Formation des équipes techniques (2j)  :   900,00 €
------------------------------------------------------------
Total Hors Taxes (HT)   : 3 950,00 €
TVA applicable (20.0%)  :   790,00 €
TOTAL TOUTES TAXES (TTC): 4 740,00 €

Mode de règlement : Virement bancaire sous 30 jours
IBAN : FR76 3000 4012 3456 7890 1234 567
BIC : BNPAFRPPXXX
Merci pour votre confiance !"""
        ),
        SampleDocument(
            id = "contrat_02",
            title = "Contrat de Prestation de Service",
            category = "Contrat",
            description = "Extrait de contrat d'engagement avec clauses légales et signataires.",
            sampleText = """CONTRAT DE PRESTATION DE SERVICE INTELLECTUELLE

ENTRE LES SOUSSIGNÉS :
La société NEXUS LABS SAS, au capital de 50 000 €, sise à Lyon,
ci-après dénommée « Le Prestataire »,

ET :
La société HORIZON RETAIL, sise à Nantes,
ci-après dénommée « Le Client ».

ARTICLE 1 - OBJET DU CONTRAT
Le Prestataire s'engage à concevoir, développer et livrer au Client
une solution logicielle d'intelligence artificielle dédiée à la
reconnaissance optique de caractères (OCR) et à l'archivage numérique.

ARTICLE 2 - OBLIGATIONS ET DÉLAIS
Les travaux débuteront le 1er Octobre 2026 et seront échelonnés en 3 phases :
- Phase 1 : Spécifications et maquettes fonctionnelles (Semaine 40)
- Phase 2 : Développement du moteur OCR et tests (Semaine 44)
- Phase 3 : Recette définitive et transfert de compétences (Semaine 48)

ARTICLE 3 - CONFIDENTIALITÉ
Chacune des parties s'engage à conserver la confidentialité absolue
sur l'ensemble des données, codes sources et secrets commerciaux échangés.

Fait à Paris, en deux exemplaires originaux, le 20 Septembre 2026.
Pour le Prestataire : J. Dupont (Président)
Pour le Client : M. Leroux (Directrice Générale)"""
        ),
        SampleDocument(
            id = "notes_03",
            title = "Compte-rendu Réunion Produit",
            category = "Note",
            description = "Notes manuscrites synthétisées lors du point d'équipe stratégique.",
            sampleText = """COMPTE-RENDU RÉUNION STRATÉGIE PRODUIT Q4

Date : 21 Septembre 2026
Participants : Sophie (Lead Product), Thomas (Tech Lead), Clara (UX/UI), David (Ops)

1. RETOUR UTILISATEURS SUR LE SCANNER OCR :
- 94% de satisfaction sur la rapidité d'extraction
- Demande forte d'export PDF en un clic avec mise en page automatique
- Utilité du mode hors-ligne et de l'historique consultable facilement

2. OBJECTIFS PRIORITAIRES :
* Optimiser la latence du modèle de vision à moins de 1.5s
* Ajouter l'exportation vers Google Drive et email
* Intégrer la détection automatique des reçus et factures
* Support multilingue étendu (Français, Anglais, Espagnol, Allemand)

3. PLAN D'ACTION IMMÉDIAT :
- Thomas : Finalisation de la pipeline d'exportation PDF & TXT
- Clara : Nouveau design M3 pour la vue d'aperçu et le scanner
- Sophie : Préparation des tests de charge avant lancement bêta

Prochaine réunion de synchronisation : Vendredi 25 Septembre à 10h00."""
        ),
        SampleDocument(
            id = "recu_04",
            title = "Reçu de Caisse - Bistrot Parisien",
            category = "Facture",
            description = "Ticket de caisse de restaurant avec détails des consommations.",
            sampleText = """LE BISTROT PARISIEN
45 Rue de Rivoli, 75001 Paris
Tél : 01 44 55 66 77
Siret : 798 123 456 00012

TICKET DE CAISSE
Table : 14 - Couverts : 2
Serveur : Julien
Date : 21/09/2026 à 13:42

------------------------------------------------
1x Menu du Jour (Entrée + Plat)      24.50 €
   - Velouté de potimarron
   - Pavé de saumon rôti
1x Plat du Jour (Entrecôte frites)   19.00 €
1x Bouteille Eau Minérale 75cl        5.50 €
2x Café Espresso                      5.00 €
1x Tarte Tatin maison                 8.00 €
------------------------------------------------
TOTAL À PAYER                       62.00 €
Dont TVA 10% (Restauration) :         5.64 €

Règlement : Carte Bancaire (Sans contact)
Autorisation : 049812 - CB : ************4921
Merci de votre visite et à bientôt !"""
        )
    )
}
