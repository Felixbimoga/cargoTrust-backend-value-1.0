# CargoTrust — Profils & Onboarding (Guide d'intégration Frontend)

> Public : développeurs frontend (Angular / Ionic).
> Couvre les profils **Importateur (CLIENT)** et **Transitaire (ADMIN_TRANSITAIRE / TRANSITAIRE)**,
> le référentiel géographique, les catégories de produits, les adresses et les documents KYC.
> Pour l'authentification (login, OTP, refresh, Google), voir [`README_IAM.md`](./README_IAM.md).

---

## 1. Essentiel à connaître

| Élément | Valeur |
|---|---|
| **Base URL** | `http://localhost:8081/cargo-trust-api` |
| **Préfixe API** | `/api/v1` |
| **Auth** | En-tête `Authorization: Bearer <accessToken>` (JWT obtenu via `POST /api/v1/auth/login`) |
| **Content-Type** | `application/json` (sauf uploads = `multipart/form-data`) |
| **CORS** | `http://localhost:4200`, `:3000`, `:8100` autorisés |

Exemple d'URL complète : `http://localhost:8081/cargo-trust-api/api/v1/importer/me`

### Format d'erreur (uniforme sur toute l'API)

```json
{
  "code": "ERR_FORWARDER_NOT_OWNER",
  "httpStatus": 403,
  "timestamp": "2026-07-14T10:12:00Z",
  "details": null
}
```

Sur une erreur de validation (`400 ERR_VALIDATION`), `details` contient les champs fautifs :

```json
{ "code": "ERR_VALIDATION", "httpStatus": 400, "timestamp": "...",
  "details": { "legalName": "ne doit pas être vide" } }
```

👉 **Côté front, testez toujours `code`** (stable) plutôt que le message.

### Rôles

| Rôle | Description |
|---|---|
| `ROLE_CLIENT` | Importateur (acheteur) |
| `ROLE_ADMIN_TRANSITAIRE` | Représentant légal / propriétaire d'une entreprise de transit |
| `ROLE_TRANSITAIRE` | Agent de terrain rattaché à une entreprise de transit |
| `ROLE_SUPER_ADMIN` | Administration CargoTrust (validation, catalogue) |

---

## 2. Architecture des profils (à comprendre avant d'intégrer)

L'identité **commune** à tout compte et les données **spécifiques au rôle** sont séparées :

| Donnée | Endpoint | Pour qui |
|---|---|---|
| Identité (nom, téléphone WhatsApp, pays, ville, photo) | `/api/v1/profile` | Tous |
| Profil importateur (activité, besoins, escrow) | `/api/v1/importer` | CLIENT |
| Entreprise de transit (légal, corridors, entrepôts) | `/api/v1/forwarders` | ADMIN_TRANSITAIRE / TRANSITAIRE |
| Équipe / agents (membres de l'entreprise) | `.../forwarders/me/members` | OWNER (ajout/retrait), membre (lecture) |
| Documents KYC (CNI, RCCM, patente, agrément) | `.../forwarders/me/documents` | ADMIN_TRANSITAIRE |

Briques **partagées** consommées par les formulaires :

| Brique | Endpoint | Usage |
|---|---|---|
| Référentiel géographique | `/api/v1/geo/**` | Listes déroulantes pays → région → ville |
| Catégories de produits | `/api/v1/product-categories` | Cases à cocher « catégories » |
| Adresses | *(imbriquées dans les payloads)* | Objet `address` avec latitude/longitude pour carte |

> Une **adresse** n'est jamais créée seule : on l'envoie imbriquée dans le payload du profil
> (siège transitaire, entrepôt, livraison importateur). Le backend la stocke et renvoie son `id`.

---

## 3. Le processus par type d'utilisateur

### 🟦 Importateur (CLIENT) — « 2 minutes chrono »

```
1. S'inscrire / se connecter                → POST /api/v1/auth/... (README_IAM)
2. Compléter l'identité                       → PATCH /api/v1/profile/me
3. Compléter le profil importateur            → PATCH /api/v1/importer/me
   (activité, catégories, modes, origines, escrow, charte)
✔ Terminé (champ `complete: true` quand le minimum est rempli)
```

Le profil importateur est **créé automatiquement** à la première lecture/écriture (aucun appel de création). `complete` passe à `true` quand : `importerType`, `importFrequency`, au moins une catégorie, et la charte acceptée sont renseignés.

### 🟩 Transitaire — propriétaire (ADMIN_TRANSITAIRE) — parcours en 4 étapes

```
1. S'inscrire / se connecter
2. Compléter l'identité du représentant       → PATCH /api/v1/profile/me
3. Créer l'entreprise                          → POST  /api/v1/forwarders          (statut DRAFT)
4. Remplir le dossier (multi-écrans PATCH)     → PATCH /api/v1/forwarders/me
     • logo / couverture                       → POST  /api/v1/forwarders/me/logo | /cover
     • entrepôts à l'origine                    → POST  /api/v1/forwarders/me/warehouses
     • documents (RCCM, agrément, CNI, patente) → POST  /api/v1/forwarders/me/documents
5. Attendre la vérification des documents par l'admin (RCCM + agrément → VERIFIED)
6. Soumettre le dossier                         → POST  /api/v1/forwarders/me/submit  (→ SUBMITTED)
7. Validation finale par l'admin                → statut VERIFIED  ✔ tiers de confiance

À tout moment (indépendant du cycle de vérification) :
   • Ajouter/retirer des agents (employés)       → POST | DELETE /api/v1/forwarders/me/members  (voir §9.2)
```

⚠️ L'étape 6 (`submit`) **échoue** tant que les documents **RCCM** et **agrément douane** ne sont pas `VERIFIED` par l'admin (`ERR_FORWARDER_DOCUMENTS_NOT_VERIFIED`), ou tant que le dossier est incomplet (`ERR_FORWARDER_INCOMPLETE`).

Cycle de vie de l'entreprise : `DRAFT → SUBMITTED → VERIFIED` (ou `REJECTED` → corriger → re-`submit`).

### 🟩 Transitaire — agent (ROLE_TRANSITAIRE)

Un agent (employé) n'est **pas auto-inscrit** : c'est le **OWNER** de l'entreprise qui le crée depuis son espace équipe (voir §9.2). À la création, le backend :

1. Provisionne un **compte `ROLE_TRANSITAIRE` actif** (pas d'OTP à saisir) ;
2. Génère un **mot de passe temporaire** et l'envoie à l'agent **par email** (email de bienvenue) ;
3. Rattache le compte comme membre `AGENT` de l'entreprise.

```
Parcours de l'agent :
1. Reçoit l'email de bienvenue (email + mot de passe temporaire)
2. Se connecte                                 → POST /api/v1/auth/login   (README_IAM)
3. Change son mot de passe (recommandé)         → (flux mot de passe, README_IAM)
4. Complète son identité                         → PATCH /api/v1/profile/me
✔ Accède à son tableau de bord agent
```

> Un agent partage l'entreprise (données légales, corridors, entrepôts) de son OWNER : il lit `GET /api/v1/forwarders/me` et `GET /api/v1/forwarders/me/members`, mais **ne peut pas** modifier l'entreprise ni gérer d'autres agents (réservé au OWNER → `403 ERR_FORWARDER_NOT_OWNER`).

### 🟥 Administration (SUPER_ADMIN)

```
• Gérer les catégories de produits   → /api/v1/admin/product-categories
• Vérifier les documents KYC          → /api/v1/admin/documents
• Valider / rejeter les transitaires  → /api/v1/forwarders/{id}/verify
```

---

## 4. Référentiel géographique — `/api/v1/geo` (public, sans token)

Sert à peupler les formulaires d'adresse en cascade. Résultats mis en cache côté backend.

| Méthode | Path | Query | Réponse |
|---|---|---|---|
| GET | `/api/v1/geo/countries` | — | `[{ "name","iso2","iso3" }]` |
| GET | `/api/v1/geo/states` | `country` (nom, ex : `Cameroon`) | `{ "country","iso3","iso2","states":[{ "name","stateCode" }] }` |
| GET | `/api/v1/geo/cities` | `country`, `state` *(optionnel)* | `["Douala","Kribi", ...]` |

**Cascade type dans un formulaire d'adresse :**
```
GET /geo/countries                                  → choix du pays
GET /geo/states?country=Cameroon                    → choix de la région/état
GET /geo/cities?country=Cameroon&state=Littoral     → choix de la ville
(quartier = champ texte libre ; lat/long = sélecteur de carte)
```

---

## 5. Catégories de produits — `/api/v1/product-categories`

Utilisées par le transitaire (spécialisations) **et** l'importateur (catégories importées). La lecture est **publique**.

| Méthode | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/product-categories?onlyActive=true` | public | Liste |
| GET | `/api/v1/product-categories/{id}` | public | Détail |
| POST | `/api/v1/admin/product-categories` | `catalog:manage` | Créer |
| PATCH | `/api/v1/admin/product-categories/{id}` | `catalog:manage` | Modifier |
| DELETE | `/api/v1/admin/product-categories/{id}` | `catalog:manage` | Désactiver (soft delete) |

**`ProductCategoryView`**
```json
{ "id": 1, "code": "ELECTRONICS_HIGHTECH", "name": "Électronique & High-Tech",
  "description": "Smartphones, écouteurs...", "costCoefficient": 1.200, "active": true }
```

Catégories seedées par défaut : `ELECTRONICS_HIGHTECH`, `FASHION_COSMETICS`, `AUTO_PARTS_TOOLS`, `PERISHABLES_FOOD`, `CONSTRUCTION_MATERIALS`.

---

## 6. Objet Adresse (partagé)

Envoyé **imbriqué** dans les payloads (`headquartersAddress`, `warehouses[].address`, `deliveryAddress`).

**En entrée (`AddressCommand`)** — tous les champs sont optionnels :
```json
{
  "countryCode": "CMR",
  "countryName": "Cameroon",
  "stateRegion": "Littoral",
  "city": "Douala",
  "neighborhood": "Akwa",
  "line": "Rue de la Joie, Bâtiment B",
  "latitude": 4.0510,
  "longitude": 9.7679,
  "placeLabel": "Siège Douala"
}
```

**En sortie (`AddressView`)** = les mêmes champs + `id` (UUID). C'est `latitude`/`longitude` qui permettent l'affichage sur une carte.

---

## 7. Profil commun — `/api/v1/profile`

| Méthode | Path | Auth | Body |
|---|---|---|---|
| GET | `/api/v1/profile/me` | authentifié | — |
| PATCH | `/api/v1/profile/me` | authentifié | `UpdateProfileRequest` |
| POST | `/api/v1/profile/me/photo` | authentifié | multipart `file` (image, ≤ 5 Mo) |
| DELETE | `/api/v1/profile/me/photo` | authentifié | — |
| GET | `/api/v1/profile/{accountId}` | `users:read` | — |

**`UpdateProfileRequest`** (PATCH partiel) : `firstName`, `lastName`, `phoneNumber` (WhatsApp), `country`, `city`, `bio`.
> Le champ `roleMetadata` existe encore mais est **désormais superflu** pour CLIENT/TRANSITAIRE : les données spécifiques passent par `/importer` et `/forwarders`.

---

## 8. Profil Importateur — `/api/v1/importer` (rôle CLIENT)

| Méthode | Path | Auth | Body → Réponse |
|---|---|---|---|
| GET | `/api/v1/importer/me` | `ROLE_CLIENT` | — → `ImporterProfileResponse` |
| PATCH | `/api/v1/importer/me` | `ROLE_CLIENT` | `UpdateImporterProfileRequest` → `ImporterProfileResponse` |
| GET | `/api/v1/importer/{accountId}` | `users:read` | — → `ImporterProfileResponse` |

> Le profil est créé à la volée au 1er appel. Un compte non-CLIENT reçoit `403 ERR_IMPORTER_NOT_A_CLIENT`.

**`UpdateImporterProfileRequest`** (PATCH partiel — champs `null` ignorés ; les collections **remplacent** l'existant) :
```json
{
  "importerType": "COMMERCANT",
  "businessName": "ODIO Store",
  "taxId": null,
  "importFrequency": "MULTIPLE_TIMES_PER_MONTH",
  "escrowEnabled": true,
  "refundMethod": "MOBILE_MONEY",
  "deliveryAddress": { "countryCode": "CMR", "countryName": "Cameroon",
                       "city": "Douala", "neighborhood": "Akwa", "latitude": 4.05, "longitude": 9.76 },
  "securedPurchaseCharterAccepted": true,
  "productCategoryIds": [1, 2],
  "transportModes": ["FRET_MARITIME_LCL", "FRET_AERIEN"],
  "originCountryCodes": ["CHN", "ARE"],
  "selectionPriorities": ["SECURITY", "PRICE", "SPEED"]
}
```

**`ImporterProfileResponse`** (extrait) :
```json
{
  "id": "uuid", "accountId": "uuid",
  "importerType": "COMMERCANT", "businessName": "ODIO Store", "taxId": null,
  "importFrequency": "MULTIPLE_TIMES_PER_MONTH",
  "escrowEnabled": true, "refundMethod": "MOBILE_MONEY",
  "deliveryAddress": { "id": "uuid", "city": "Douala", "latitude": 4.05, "longitude": 9.76, "...": "..." },
  "deliveryCountryCode": "CMR",
  "securedPurchaseCharterAccepted": true, "charterAcceptedAt": "2026-07-14T...",
  "complete": true,
  "categories": [ { "id": 1, "code": "ELECTRONICS_HIGHTECH", "name": "...", "costCoefficient": 1.2, "active": true } ],
  "transportModes": ["FRET_MARITIME_LCL","FRET_AERIEN"],
  "originCountryCodes": ["CHN","ARE"],
  "selectionPriorities": ["SECURITY","PRICE","SPEED"],
  "createdAt": "...", "updatedAt": "..."
}
```

---

## 9. Entreprise de Transit — `/api/v1/forwarders`

### 9.1 Espace transitaire (propriétaire du compte)

| Méthode | Path | Auth | Body → Réponse |
|---|---|---|---|
| POST | `/api/v1/forwarders` | `ROLE_ADMIN_TRANSITAIRE` | `CreateForwarderRequest` → `201 ForwarderResponse` |
| GET | `/api/v1/forwarders/me` | membre | — → `ForwarderResponse` |
| PATCH | `/api/v1/forwarders/me` | OWNER | `UpdateForwarderRequest` → `ForwarderResponse` |
| POST | `/api/v1/forwarders/me/logo` | OWNER | multipart `file` (image ≤ 5 Mo) |
| POST | `/api/v1/forwarders/me/cover` | OWNER | multipart `file` (image ≤ 5 Mo) |
| POST | `/api/v1/forwarders/me/warehouses` | OWNER | `WarehouseRequest` → `ForwarderResponse` |
| DELETE | `/api/v1/forwarders/me/warehouses/{warehouseId}` | OWNER | — → `ForwarderResponse` |
| POST | `/api/v1/forwarders/me/submit` | OWNER | — → `ForwarderResponse` |

**`CreateForwarderRequest`** : `{ "legalName": "Sino-Cargo SARL", "position": "Directeur Général" }`
> Une seule entreprise par compte (`409 ERR_FORWARDER_ALREADY_EXISTS` sinon). Le créateur devient `OWNER`.

**`UpdateForwarderRequest`** (PATCH partiel) :
```json
{
  "legalName": "Sino-Cargo Logistics SARL",
  "structureType": "SARL",
  "creationDate": "2019-05-01",
  "employeeCount": 12,
  "websiteUrl": "https://...",
  "taxId": "P0000...",            "rccm": "RC/DLA/...",  "customsLicenseNumber": "AGR-2024-...",
  "headquartersAddress": { "countryCode": "CMR", "city": "Douala", "neighborhood": "Bonanjo", "latitude": 4.05, "longitude": 9.70 },
  "hasOriginWarehouse": true,
  "departureFrequency": "HEBDOMADAIRE",
  "variableSchedule": false,
  "insuranceOffered": true, "insuranceCoverageRate": "valeur déclarée sur facture", "insuranceCompany": "Activa",
  "originTeamLanguages": "fr, zh", "originTeamEquipped": true,
  "transparencyCharterAccepted": true,
  "transportModes": ["FRET_MARITIME_FCL", "FRET_MARITIME_LCL", "FRET_AERIEN"],
  "originCountryCodes": ["CHN", "ARE", "TUR"],
  "destinationCountryCodes": ["CMR", "GAB", "CIV"],
  "specializationCategoryIds": [1, 3]
}
```

**`WarehouseRequest`** (entrepôt à l'origine) :
```json
{ "label": "Dépôt Guangzhou",
  "address": { "countryCode": "CHN", "countryName": "China", "city": "Guangzhou",
               "line": "Baiyun District ...", "latitude": 23.12, "longitude": 113.25 } }
```

### 9.2 Équipe — agents (`/api/v1/forwarders/me/members`)

Permet au **OWNER** d'ajouter/retirer des **agents** (employés) qui obtiennent un compte pour se connecter et accéder à leur tableau de bord. Le mapping renvoie un **membre enrichi** : l'`id` du rattachement + les infos du compte IAM (email, nom) résolues automatiquement côté backend.

| Méthode | Path | Auth | Body → Réponse |
|---|---|---|---|
| POST | `/api/v1/forwarders/me/members` | OWNER | `AddMemberRequest` → `201 MemberResponse` |
| GET | `/api/v1/forwarders/me/members` | membre | — → `MemberResponse[]` (OWNER + agents) |
| DELETE | `/api/v1/forwarders/me/members/{memberId}` | OWNER | — → `204` |

**Ce que fait le POST** : crée un compte `ROLE_TRANSITAIRE` **actif**, envoie le mot de passe temporaire par email à l'agent, puis le rattache comme membre `AGENT`. Le front n'a **pas** à gérer de mot de passe : il saisit juste l'email + (optionnel) nom/fonction.

**`AddMemberRequest`** :
```json
{
  "email": "awa.diallo@example.com",
  "firstName": "Awa",
  "lastName": "Diallo",
  "position": "Déclarant en douane"
}
```
> `email` obligatoire (valide). `firstName`/`lastName`/`position` optionnels (pré-remplissent le profil / la fiche membre).

**`MemberResponse`** :
```json
{
  "id": 12,
  "accountId": "uuid",
  "email": "awa.diallo@example.com",
  "firstName": "Awa",
  "lastName": "Diallo",
  "memberRole": "AGENT",
  "position": "Déclarant en douane",
  "joinedAt": "2026-07-14T10:30:00Z"
}
```
> `memberRole` vaut `OWNER` ou `AGENT`. Le `GET` renvoie **tous** les membres (dont le OWNER) — filtrez sur `memberRole` pour n'afficher que les agents dans la liste « équipe ». Utilisez l'`id` (numérique, celui du rattachement) pour le `DELETE`, **pas** l'`accountId`.

**Règles métier (à refléter dans l'UI)** :
- Bouton « Ajouter un agent » / « Retirer » visible **uniquement pour le OWNER** (sinon `403 ERR_FORWARDER_NOT_OWNER`).
- Email déjà utilisé sur la plateforme → `409 ERR_ACCOUNT_ALREADY_EXISTS` (afficher « cet email a déjà un compte »).
- On ne peut **pas retirer le OWNER** → `400 ERR_FORWARDER_CANNOT_REMOVE_OWNER` (masquer l'action de suppression sur la ligne OWNER).
- `memberId` inconnu ou d'une autre entreprise → `404 ERR_FORWARDER_MEMBER_NOT_FOUND`.
- ⚠️ Le `DELETE` détache l'agent de l'entreprise **mais ne supprime pas son compte IAM** (il peut être suspendu séparément par l'administration).

### 9.3 Documents KYC du transitaire

| Méthode | Path | Auth | Body |
|---|---|---|---|
| POST | `/api/v1/forwarders/me/documents` | OWNER | multipart : `docType`, `expiresAt?` (ISO date), `file` (PDF ou image ≤ 10 Mo) → `201 DocumentView` |
| GET | `/api/v1/forwarders/me/documents` | membre | — → `DocumentView[]` |
| DELETE | `/api/v1/forwarders/me/documents/{documentId}` | OWNER | — → `204` |

Exemple d'upload (multipart) : champs `docType=CUSTOMS_LICENSE`, `expiresAt=2026-12-31`, `file=<binaire>`.

### 9.4 Administration (SUPER_ADMIN)

| Méthode | Path | Auth | Body |
|---|---|---|---|
| GET | `/api/v1/forwarders?status=SUBMITTED&page=0&size=20` | `forwarders:read` | — → page de `ForwarderSummaryResponse` |
| GET | `/api/v1/forwarders/{id}` | `forwarders:read` | — → `ForwarderResponse` |
| POST | `/api/v1/forwarders/{id}/verify` | `forwarders:manage` | `VerifyForwarderRequest` → `ForwarderResponse` |
| GET | `/api/v1/admin/documents?status=PENDING&page=0` | `forwarders:read` | — → page de `DocumentView` |
| GET | `/api/v1/admin/documents/{id}` | `forwarders:read` | — → `DocumentView` |
| POST | `/api/v1/admin/documents/{id}/verify` | `forwarders:manage` | `VerifyDocumentRequest` → `DocumentView` |

**`VerifyForwarderRequest`** / **`VerifyDocumentRequest`** :
```json
{ "approved": true }
{ "approved": false, "rejectionReason": "Agrément expiré" }
```

**`ForwarderResponse`** (extrait) :
```json
{
  "id": "uuid", "legalName": "Sino-Cargo Logistics SARL", "structureType": "SARL",
  "logoUrl": "http://.../logo.png", "coverPhotoUrl": "http://.../cover.jpg",
  "taxId": "...", "rccm": "...", "customsLicenseNumber": "...",
  "headquartersAddress": { "id": "uuid", "city": "Douala", "latitude": 4.05, "longitude": 9.70, "...": "..." },
  "headquartersCountryCode": "CMR",
  "hasOriginWarehouse": true,
  "warehouses": [ { "id": 5, "label": "Dépôt Guangzhou", "address": { "id": "uuid", "city": "Guangzhou", "...": "..." } } ],
  "departureFrequency": "HEBDOMADAIRE", "variableSchedule": false,
  "insuranceOffered": true, "insuranceCoverageRate": "...", "insuranceCompany": "Activa",
  "originTeamLanguages": "fr, zh", "originTeamEquipped": true,
  "transparencyCharterAccepted": true, "charterAcceptedAt": "...",
  "verificationStatus": "DRAFT", "rejectionReason": null,
  "transportModes": ["FRET_MARITIME_FCL","FRET_MARITIME_LCL"],
  "originCountryCodes": ["CHN","ARE"], "destinationCountryCodes": ["CMR","GAB"],
  "specializations": [ { "id": 1, "code": "ELECTRONICS_HIGHTECH", "name": "...", "costCoefficient": 1.2 } ],
  "createdAt": "...", "updatedAt": "..."
}
```

**`DocumentView`** :
```json
{ "id": "uuid", "ownerType": "FORWARDER", "ownerId": "uuid",
  "docType": "CUSTOMS_LICENSE", "fileUrl": "http://.../documents/xxx.pdf",
  "originalFilename": "agrement.pdf", "contentType": "application/pdf",
  "status": "PENDING", "expiresAt": "2026-12-31", "rejectionReason": null,
  "verifiedBy": null, "verifiedAt": null, "uploadedBy": "uuid", "uploadedAt": "..." }
```

---

## 10. Référence des énumérations

| Enum | Valeurs |
|---|---|
| **StructureType** | `SARL`, `SA`, `ENTREPRISE_INDIVIDUELLE`, `GIE`, `AUTRE` |
| **DepartureFrequency** | `HEBDOMADAIRE`, `BIMENSUEL`, `MENSUEL` |
| **TransportMode** | `FRET_MARITIME_FCL`, `FRET_MARITIME_LCL`, `FRET_AERIEN` |
| **VerificationStatus** (forwarder) | `DRAFT`, `SUBMITTED`, `VERIFIED`, `REJECTED` |
| **MemberRole** (équipe) | `OWNER`, `AGENT` |
| **DocumentType** | `REPRESENTATIVE_ID`, `RCCM`, `TAX_CLEARANCE`, `CUSTOMS_LICENSE`, `OTHER` |
| **DocumentStatus** | `PENDING`, `VERIFIED`, `REJECTED` |
| **ImporterType** | `PARTICULIER`, `COMMERCANT`, `PME` |
| **ImportFrequency** | `MULTIPLE_TIMES_PER_MONTH`, `ONCE_PER_MONTH`, `QUARTERLY`, `RARELY` |
| **RefundMethod** | `MOBILE_MONEY`, `BANK_TRANSFER`, `NEXT_SHIPMENT_CREDIT` |
| **SelectionCriterion** | `PRICE`, `SECURITY`, `SPEED` |

### Codes d'erreur métier notables

| Code | HTTP | Signification |
|---|---|---|
| `ERR_VALIDATION` | 400 | Champs invalides (voir `details`) |
| `ERR_FORWARDER_NOT_A_TRANSITAIRE` | 403 | Création d'entreprise réservée à ADMIN_TRANSITAIRE |
| `ERR_FORWARDER_ALREADY_EXISTS` | 409 | Le compte a déjà une entreprise |
| `ERR_FORWARDER_NOT_OWNER` | 403 | Modification réservée au OWNER |
| `ERR_FORWARDER_INCOMPLETE` | 400 | Dossier incomplet au `submit` |
| `ERR_FORWARDER_DOCUMENTS_NOT_VERIFIED` | 400 | RCCM/agrément pas encore `VERIFIED` au `submit` |
| `ERR_FORWARDER_INVALID_STATUS` | 400 | Transition de statut non permise |
| `ERR_FORWARDER_MEMBER_NOT_FOUND` | 404 | Agent introuvable / hors de mon entreprise (retrait) |
| `ERR_FORWARDER_CANNOT_REMOVE_OWNER` | 400 | Impossible de retirer le OWNER de l'entreprise |
| `ERR_ACCOUNT_ALREADY_EXISTS` | 409 | Email déjà utilisé (ajout d'un agent) |
| `ERR_IMPORTER_NOT_A_CLIENT` | 403 | Endpoint importateur réservé à CLIENT |
| `ERR_CATEGORY_INVALID` | 400 | Catégorie inexistante/inactive |
| `ERR_STORAGE_INVALID_FORMAT` | 400 | Type de fichier non autorisé |
| `ERR_STORAGE_FILE_TOO_LARGE` | 400 | Fichier trop volumineux |
| `ERR_GEO_UNAVAILABLE` | 502 | Référentiel géographique indisponible |

---

## 11. Conseils d'intégration (gagner du temps)

- **Ordre des appels** : identité (`/profile`) → profil de rôle (`/importer` ou `/forwarders`). Les deux sont indépendants.
- **PATCH partiel** partout : n'envoyez que les champs modifiés. Une **collection envoyée remplace** l'ensemble (envoyer `[]` vide la liste ; ne pas envoyer la clé = inchangé).
- **Adresses** : ne jamais gérer d'ID d'adresse côté front — envoyez toujours l'objet `address` complet, le backend upsert et renvoie l'`AddressView` avec son `id`.
- **Uploads** : `multipart/form-data`. Images (logo/cover/photo) ≤ 5 Mo ; documents (PDF+image) ≤ 10 Mo.
- **Formulaire multi-étapes transitaire** : chaque étape = un `PATCH /forwarders/me`. Le bouton « Soumettre » n'appelle `submit` qu'à la fin.
- **Statut & badges** : pilotez l'UI transitaire sur `verificationStatus` ; pilotez la complétude sur `complete` (importateur).
- **Écran équipe (agents)** : n'affichez les actions ajouter/retirer que si le compte courant est OWNER. Après un `POST` réussi, informez que l'agent recevra ses identifiants par email — aucun mot de passe n'est renvoyé dans la réponse. Pour le `DELETE`, utilisez `member.id` (et non `accountId`).
- **Pagination admin** : paramètres Spring standard `?page=0&size=20&sort=createdAt,desc`.

---

## 12. Limitations connues (à date)

- **Retrait d'un agent** : le `DELETE /forwarders/me/members/{id}` détache l'agent de l'entreprise mais **ne supprime/suspend pas** son compte IAM (à faire séparément côté administration si nécessaire).
- **Matching** transitaire ↔ importateur : pas encore implémenté (les données nécessaires — origines, destinations, modes, catégories partagées — sont déjà en place).
- Modules **Order / Shipment / Proof / Payment (Escrow)** : à venir.
