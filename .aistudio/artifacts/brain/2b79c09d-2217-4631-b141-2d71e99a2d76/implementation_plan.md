# Plan de Résolution : Restauration et Correction du Lecteur Vidéo YouTube (Erreur 152-4)

Ce plan détaille la cause racine de l'erreur **152-4** ("Cette vidéo n'est pas disponible") apparue lors du lancement des vidéos, et formalise les ajustements techniques nécessaires dans `VideoPlayerScreen.kt` pour restaurer la lecture fluide tout en maintenant le confinement sécurisé pour les enfants.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Diagnostic confirmé suite à la capture d'écran fournie :**
> - **Symptôme** : Le lecteur s'ouvre, l'iframe YouTube se charge, mais affiche l'écran d'erreur YouTube : *"Cette vidéo n'est pas disponible - Code d'erreur : 152-4 - Regarder la vidéo sur YouTube"*.
> - **Cause racine technique** : Le code `152` (ou `150`) dans l'API IFrame de YouTube correspond à un **rejet de lecture par les règles de sécurité d'intégration (Embedding Policy / Origin Restriction)**.
>   1. La ligne `'origin': 'https://www.youtube.com'` injectée dans `playerVars` est détectée par YouTube comme une usurpation d'origine (spoofing) : un lecteur iframe tiers ne peut pas déclarer comme origine le domaine `youtube.com` lui-même.
>   2. L'absence de balise `<meta name="referrer" content="strict-origin-when-cross-origin">` empêche YouTube de valider les en-têtes Referer exigés par les récentes mises à jour de l'API.
>   3. Le verrouillage de navigation dans `shouldOverrideUrlLoading` doit être ajusté pour ne bloquer strictement que les navigations directes de la fenêtre racine sans perturber les redirections internes nécessaires au flux vidéo.

- **Décision 1 (Suppression de l'origin invalide)** : Retirer `'origin': 'https://www.youtube.com'` des `playerVars` pour revenir au standard officiel de l'API IFrame.
- **Décision 2 (Conformité de la politique de Referrer)** : Intégrer `<meta name="referrer" content="strict-origin-when-cross-origin">` dans l'entête HTML pour satisfaire aux vérifications d'intégrité de YouTube sans autoriser la navigation hors de l'application.
- **Décision 3 (Préservation de la sécurité enfant)** : Maintenir le filtrage strict `isForMainFrame` afin qu'aucun clic ne puisse transformer l'application en navigateur web libre.

---

### 1. Overview & Core Concept

- **Rôle du composant** : `VideoPlayerScreen` est l'environnement sécurisé de visionnage vidéo dédié aux enfants. Il doit lire les vidéos sélectionnées sans pub parasite, sans recommandation cliquable ouvrant YouTube, et sans fuite vers l'extérieur.
- **Problème résolu** : Rétablissement de la compatibilité avec l'API YouTube IFrame Player tout en conservant l'étanchéité stricte mise en place pour la sécurité parentale.
- **Bénéfice direct** : Les enfants retrouvent l'accès immédiat à leurs vidéos autorisées d'un simple clic sur la vignette, sans message d'erreur 152-4.

---

### 2. User Experience & Visual Design

- **Flux utilisateur fluide** :
  1. L'enfant clique sur une vignette dans la grille `ChildHomeScreen`.
  2. L'écran de lecture `VideoPlayerScreen` s'ouvre avec un fond noir cinéma et un indicateur de chargement discret.
  3. La vidéo démarre en lecture automatique (`autoplay: 1`, `playsinline: 1`).
  4. Le bouton retour grand format (adapté aux petites mains, 56dp) permet de revenir en un clic à la bibliothèque.
  5. En cas de vidéo dont l'intégration est désactivée par son créateur sur YouTube (erreur spécifique 150/101), un bandeau explicatif doux et clair est affiché avec un bouton retour immédiat.

```
┌────────────────────────────────────────────────────────┐
│  [← Retour]            Titre de la vidéo               │
│                                                        │
│               ┌──────────────────────┐                 │
│               │                      │                 │
│               │   Lecteur IFrame     │                 │
│               │     (Sécurisé)       │                 │
│               │                      │                 │
│               └──────────────────────┘                 │
│                                                        │
│  [Barre de progression & Contrôles natifs épurés]      │
└────────────────────────────────────────────────────────┘
```

---

### 3. Key Product Decisions & Trade-Offs

- **Ajustement de `playerVars`** :
  - *Choix retenu* : Conserver les options propres (`autoplay: 1`, `controls: 1`, `rel: 0`, `modestbranding: 1`, `iv_load_policy: 3`, `fs: 1`, `playsinline: 1`) tout en retirant le paramètre `'origin': 'https://www.youtube.com'`.
  - *Justification* : C'est ce paramètre spécifique qui a introduit le rejet 152-4 lors de son ajout.
- **Politique de Referrer dans le HTML hôte** :
  - *Choix retenu* : Définir `<meta name="referrer" content="strict-origin-when-cross-origin">` et utiliser `loadDataWithBaseURL("https://www.youtube-nocookie.com", ...)` ou `loadDataWithBaseURL("https://www.youtube.com", ...)`.
  - *Justification* : Permet au lecteur YouTube de recevoir les jetons de validation d'intégration attendus.
- **Filtrage de navigation `shouldOverrideUrlLoading`** :
  - *Choix retenu* : Bloquer toute redirection du document principal (`request.isForMainFrame == true`), mais laisser l'iframe YouTube charger ses scripts de contrôle et flux de streaming.
  - *Justification* : Garantit qu'aucun compte Google ou page externe ne s'ouvre dans l'application, respectant l'objectif strict de PrivaTube.

---

### 4. Technical Architecture & Data Strategy

```
┌──────────────────────────────────────────────────────────────┐
│                    ChildHomeScreen                           │
│           (Clic sur VideoCard avec youtubeId)                │
└──────────────────────────────┬───────────────────────────────┘
                               │ navigation: "player/{id}/{title}"
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                  VideoPlayerScreen                           │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ AndroidView (WebView)                                  │  │
│  │  - WebSettings (JS activé, DOM storage, Mixed content) │  │
│  │  - CookieManager (cookies tiers autorisés)             │  │
│  │  - HTML local avec balise Referrer & script IFrame API │  │
│  │  - shouldOverrideUrlLoading (blocage navigation parent)│  │
│  │  - onRenderProcessGone (protection crash GPU/Chromium) │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

- **Fichier ciblé** : `app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt` uniquement.
- **Garantie** : Aucune modification des profils, de Firebase/Firestore, de la base de données Room, ou du reste de l'interface utilisateur.
