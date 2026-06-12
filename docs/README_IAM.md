# Module IAM — CargoTrust Backend

Documentation destinée aux **développeurs frontend (Angular)**.  
Couvre l'intégralité du module IAM : authentification, profil utilisateur, et administration RBAC.

---

## Sommaire

1. [Informations générales](#1-informations-générales)
2. [Flux d'authentification classique](#2-flux-dauthentification-classique)
3. [Authentification Google OAuth2](#3-authentification-google-oauth2)
4. [Gestion des tokens JWT](#4-gestion-des-tokens-jwt)
5. [Mot de passe oublié / réinitialisation](#5-mot-de-passe-oublié--réinitialisation)
6. [Profil utilisateur (user normal)](#6-profil-utilisateur-user-normal)
7. [Administration — Gestion des utilisateurs](#7-administration--gestion-des-utilisateurs)
8. [Administration — Rôles et permissions](#8-administration--rôles-et-permissions)
9. [Système de rôles et permissions (RBAC)](#9-système-de-rôles-et-permissions-rbac)
10. [Codes d'erreur](#10-codes-derreur)
11. [Génération automatique des services Angular](#11-génération-automatique-des-services-angular)

---

## 1. Informations générales

| Paramètre | Valeur |
|---|---|
| Base URL (dev) | `http://localhost:8081/cargo-trust-api` |
| Swagger UI | `http://localhost:8081/cargo-trust-api/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8081/cargo-trust-api/v3/api-docs` |
| Format des IDs | UUID v4 (string, ex: `"a1b2c3d4-..."`) |
| Format des dates | ISO-8601 UTC (ex: `"2026-06-12T10:30:00Z"`) |
| Authentification | `Authorization: Bearer <accessToken>` |

### Structure d'une réponse d'erreur

Toutes les erreurs retournent le format suivant :

```json
{
  "errorCode": "ERR_INVALID_CREDENTIALS",
  "status": 401,
  "timestamp": "2026-06-12T10:30:00Z"
}
```

En cas d'erreur de validation (400), le champ `errors` est présent :

```json
{
  "errorCode": "ERR_VALIDATION",
  "status": 400,
  "errors": {
    "email": "must not be blank",
    "password": "Le mot de passe doit contenir au moins 8 caractères"
  }
}
```

---

## 2. Flux d'authentification classique

### Diagramme de séquence

```
Frontend                          Backend
   |                                 |
   |-- POST /auth/register --------> |  Crée le compte, envoie un OTP par email
   |                                 |
   |-- POST /auth/login ------------> |  Vérifie identifiants, envoie OTP si nécessaire
   |<- { requiresOtp: true } -------- |
   |                                 |
   |-- POST /auth/verify-otp -------> |  Valide l'OTP
   |<- { accessToken, refreshToken } |
   |                                 |
   |   [Requêtes authentifiées]       |
   |-- GET /api/... + Bearer token -> |
   |                                 |
   |-- POST /auth/refresh ----------> |  Renouvelle l'access token
   |<- { accessToken, refreshToken } |
   |                                 |
   |-- POST /auth/logout -----------> |  Révoque le refresh token
```

---

### 2.1 — Inscription

```
POST /api/v1/auth/register
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "email": "user@example.com",
  "password": "MonMotDePasse123"
}
```

**Réponse 201 Created :**
```json
{
  "message": "Compte créé. Vérifiez votre email pour le code OTP.",
  "success": true
}
```

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 409 | `ERR_ACCOUNT_ALREADY_EXISTS` | Email déjà utilisé |
| 400 | `ERR_VALIDATION` | Email invalide ou mot de passe trop court |

> Après l'inscription, un OTP à 6 chiffres est envoyé par email. L'utilisateur doit le valider via `/auth/verify-otp` pour activer son compte.

---

### 2.2 — Connexion

```
POST /api/v1/auth/login
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "email": "user@example.com",
  "password": "MonMotDePasse123"
}
```

**Réponse 200 — OTP requis (nouveau compte ou première connexion du jour) :**
```json
{
  "requiresOtp": true,
  "message": "Code OTP envoyé par email."
}
```

**Réponse 200 — Connexion directe (OTP déjà validé) :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a3f8d2e1-9b4c-...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "requiresOtp": false
}
```

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 401 | `ERR_INVALID_CREDENTIALS` | Email ou mot de passe incorrect |
| 403 | `ERR_ACCOUNT_SUSPENDED` | Compte suspendu par un admin |
| 403 | `ERR_ACCOUNT_PENDING_VERIFICATION` | Compte non vérifié (OTP jamais validé) |

---

### 2.3 — Vérification OTP

```
POST /api/v1/auth/verify-otp
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "email": "user@example.com",
  "otp": "482917"
}
```

**Réponse 200 :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a3f8d2e1-9b4c-...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "requiresOtp": false
}
```

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 404 | `ERR_OTP_INVALID` | OTP incorrect |
| 401 | `ERR_OTP_EXPIRED` | OTP expiré (valable 10 min) |
| 401 | `ERR_OTP_ALREADY_CONSUMED` | OTP déjà utilisé |

---

## 3. Authentification Google OAuth2

Deux flux disponibles selon la plateforme cliente :

### 3.1 — ID Token (Android / Web One-Tap)

Utilisé quand le client possède déjà un ID Token Google (ex: `@react-oauth/google`, Google Sign-In Android SDK).

```
POST /api/v1/auth/google/token
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

**Réponse 200 :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "b7c9e3a2-...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "requiresOtp": false
}
```

---

### 3.2 — Code d'autorisation (Web redirect flow)

Utilisé dans le flow OAuth2 complet où Google redirige l'utilisateur avec un `code`.

```
POST /api/v1/auth/google/callback
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "code": "4/0AX4XfWi...",
  "redirectUri": "http://localhost:4200/auth/google/callback"
}
```

**Réponse 200 :** identique à 3.1

**Erreurs possibles pour Google :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 401 | `ERR_GOOGLE_TOKEN_INVALID` | Token/code Google invalide ou expiré |
| 400 | `ERR_GOOGLE_EMAIL_NOT_VERIFIED` | Email Google non vérifié |
| 403 | `ERR_ACCOUNT_SUSPENDED` | Compte existant suspendu |

> **Option A — Fusion de comptes :** si l'email Google correspond à un compte CargoTrust existant (créé avec email/mot de passe), les deux sont automatiquement fusionnés. Les nouveaux comptes Google reçoivent le rôle `ROLE_IMPORTER` par défaut.

---

## 4. Gestion des tokens JWT

### Structure du Access Token (décodé)

```json
{
  "sub": "a1b2c3d4-...",
  "email": "user@example.com",
  "role": "ROLE_IMPORTER",
  "permissions": ["orders:create", "orders:read", "shipments:read"],
  "iat": 1718100000,
  "exp": 1718100900
}
```

| Champ | Type | Description |
|---|---|---|
| `sub` | UUID | `accountId` de l'utilisateur |
| `role` | string | Rôle unique de l'utilisateur |
| `permissions` | string[] | Liste des permissions accordées |
| `exp` | number | Timestamp d'expiration (15 min par défaut) |

### Utilisation dans les requêtes

```http
GET /api/v1/profile/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 4.1 — Rafraîchir le token

À appeler **avant** l'expiration du access token (les deux tokens sont régénérés).

```
POST /api/v1/auth/refresh
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "refreshToken": "a3f8d2e1-9b4c-..."
}
```

**Réponse 200 :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "nouveau-refresh-token-...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "requiresOtp": false
}
```

> **Important :** le refresh token retourné est différent du précédent. Mettez toujours à jour le token stocké (rotation des tokens).

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 401 | `ERR_REFRESH_TOKEN_INVALID` | Token inconnu |
| 401 | `ERR_REFRESH_TOKEN_EXPIRED` | Token expiré (30 jours) |
| 401 | `ERR_REFRESH_TOKEN_REVOKED` | Token révoqué (logout) |
| 403 | `ERR_ACCOUNT_SUSPENDED` | Compte suspendu entre-temps |

---

### 4.2 — Déconnexion

```
POST /api/v1/auth/logout
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "refreshToken": "a3f8d2e1-9b4c-..."
}
```

**Réponse 200 :**
```json
{
  "message": "Déconnexion réussie.",
  "success": true
}
```

---

### 4.3 — Informations du compte connecté

```
GET /api/v1/auth/me
Authorization: Bearer <token>
```

**Réponse 200 :**
```json
{
  "accountId": "a1b2c3d4-...",
  "email": "user@example.com",
  "role": "ROLE_IMPORTER"
}
```

---

## 5. Mot de passe oublié / réinitialisation

### 5.1 — Demander un lien de réinitialisation

```
POST /api/v1/auth/forgot-password
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "email": "user@example.com"
}
```

**Réponse 200 :** (toujours 200 même si l'email n'existe pas, pour ne pas exposer les comptes)
```json
{
  "message": "Si ce compte existe, un email a été envoyé.",
  "success": true
}
```

---

### 5.2 — Réinitialiser le mot de passe

```
POST /api/v1/auth/reset-password?token=<reset-token>
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "newPassword": "NouveauMotDePasse123"
}
```

**Réponse 200 :**
```json
{
  "message": "Mot de passe réinitialisé avec succès.",
  "success": true
}
```

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 404 | `ERR_PASSWORD_RESET_TOKEN_INVALID` | Token inconnu |
| 401 | `ERR_PASSWORD_RESET_TOKEN_EXPIRED` | Token expiré (1h) |

---

## 6. Profil utilisateur (user normal)

Tous les endpoints requièrent `Authorization: Bearer <token>`.

### 6.1 — Mon profil

```
GET /api/v1/profile/me
Authorization: Bearer <token>
```

**Réponse 200 :**
```json
{
  "id": "prof-uuid-...",
  "accountId": "a1b2c3d4-...",
  "firstName": "Jean",
  "lastName": "Dupont",
  "phoneNumber": "+237612345678",
  "country": "Cameroun",
  "city": "Douala",
  "profilePhotoUrl": "http://localhost:8081/cargo-trust-api/uploads/photos/a1b2c3d4.jpg",
  "bio": "Importateur depuis 5 ans.",
  "complete": true,
  "roleMetadata": {
    "importerType": "SME"
  },
  "updatedAt": "2026-06-12T10:00:00Z"
}
```

---

### 6.2 — Mettre à jour mon profil

```
PATCH /api/v1/profile/me
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de la requête** (tous les champs sont optionnels) :
```json
{
  "firstName": "Jean",
  "lastName": "Dupont",
  "phoneNumber": "+237612345678",
  "country": "Cameroun",
  "city": "Douala",
  "bio": "Importateur depuis 5 ans.",
  "roleMetadata": {
    "importerType": "SME"
  }
}
```

**Valeurs `roleMetadata` selon le rôle :**

| Rôle | Clés acceptées |
|---|---|
| `ROLE_IMPORTER` | `importerType`: `"SME"` \| `"BEGINNER"` \| `"ECOMMERCE"` \| `"LARGE_IMPORTER"` |
| `ROLE_AGENT` | `position`, `forwarderId` (UUID) |
| `ROLE_ADMIN_FORWARDER` | `operationalPosition`, `forwarderId` (UUID), `additionalPermissions` (array) |
| `ROLE_SUPER_*` | `superRole`: `"DEVELOPER"` \| `"COMMERCIAL"` \| `"ACCOUNTANT"` \| `"SUPER_ADMIN"` |

**Réponse 200 :** `ProfileResponse` (même structure que 6.1)

---

### 6.3 — Uploader une photo de profil

```
POST /api/v1/profile/me/photo
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Form data :**
- `file` : fichier image (JPEG, PNG, WEBP — max 5 Mo)

**Réponse 200 :** `ProfileResponse` avec le nouveau `profilePhotoUrl`

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 400 | `ERR_PROFILE_PHOTO_INVALID` | Format non supporté |
| 400 | `ERR_PROFILE_PHOTO_TOO_LARGE` | Fichier > 5 Mo |

---

### 6.4 — Supprimer la photo de profil

```
DELETE /api/v1/profile/me/photo
Authorization: Bearer <token>
```

**Réponse 204 No Content**

---

### 6.5 — Voir le profil d'un autre utilisateur

Nécessite la permission `users:read` (accordée aux rôles admin).

```
GET /api/v1/profile/{accountId}
Authorization: Bearer <token>
```

**Réponse 200 :** `ProfileResponse` de l'utilisateur cible

---

## 7. Administration — Gestion des utilisateurs

> **Accès réservé aux rôles super :**  
> `ROLE_SUPER_RESPONSIBLE`, `ROLE_SUPER_COMMERCIAL`, `ROLE_SUPER_FINANCIAL`, `ROLE_SUPER_PACKAGE`

Compte super admin par défaut créé au démarrage :
- **Email :** `cargotrust237@gmail.com`
- **Password :** `Admin.123`

---

### 7.1 — Lister / rechercher des utilisateurs

```
GET /api/v1/admin/users?email=dupont&status=ACTIVE&role=ROLE_IMPORTER&page=0&size=20
Authorization: Bearer <token>
```

**Query parameters :**

| Paramètre | Type | Description | Valeurs |
|---|---|---|---|
| `email` | string | Recherche partielle (LIKE %email%) | optionnel |
| `status` | string | Filtre par statut | `ACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION` |
| `role` | string | Filtre par rôle | `ROLE_IMPORTER`, `ROLE_AGENT`, etc. |
| `page` | int | Page (0-based) | défaut : `0` |
| `size` | int | Éléments par page | `1–100`, défaut : `20` |

**Réponse 200 — Page paginée :**
```json
{
  "content": [
    {
      "id": "a1b2c3d4-...",
      "email": "jean.dupont@example.com",
      "status": "ACTIVE",
      "role": "ROLE_IMPORTER",
      "firstName": "Jean",
      "lastName": "Dupont",
      "profileComplete": true,
      "createdAt": "2026-06-01T08:00:00Z"
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

---

### 7.2 — Détail complet d'un utilisateur

```
GET /api/v1/admin/users/{accountId}
Authorization: Bearer <token>
```

**Réponse 200 :**
```json
{
  "id": "a1b2c3d4-...",
  "email": "jean.dupont@example.com",
  "status": "ACTIVE",
  "role": "ROLE_IMPORTER",
  "createdAt": "2026-06-01T08:00:00Z",
  "updatedAt": "2026-06-10T14:30:00Z",
  "profile": {
    "id": "prof-uuid-...",
    "accountId": "a1b2c3d4-...",
    "firstName": "Jean",
    "lastName": "Dupont",
    "phoneNumber": "+237612345678",
    "country": "Cameroun",
    "city": "Douala",
    "profilePhotoUrl": "http://...",
    "bio": "...",
    "complete": true,
    "roleMetadata": { "importerType": "SME" },
    "updatedAt": "2026-06-10T14:30:00Z"
  }
}
```

---

### 7.3 — Changer le statut d'un utilisateur

```
PATCH /api/v1/admin/users/{accountId}/status
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "status": "SUSPENDED",
  "reason": "Non-respect des conditions d'utilisation"
}
```

**Valeurs acceptées :** `"ACTIVE"` | `"SUSPENDED"`

**Réponse 200 :** `UserDetailResponse` mis à jour

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 404 | `ERR_ACCOUNT_NOT_FOUND` | Utilisateur introuvable |
| 400 | `ERR_INVALID_STATUS` | Valeur de statut invalide |

---

### 7.4 — Changer le rôle d'un utilisateur

```
PATCH /api/v1/admin/users/{accountId}/role
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "roleName": "ROLE_AGENT"
}
```

**Valeurs acceptées :** voir [tableau des rôles section 9](#91--rôles-disponibles)

**Réponse 200 :** `UserDetailResponse` mis à jour

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 404 | `ERR_ROLE_NOT_FOUND` | Rôle inexistant |
| 403 | `ERR_CANNOT_CHANGE_OWN_ROLE` | Un admin ne peut pas changer son propre rôle |

---

## 8. Administration — Rôles et permissions

> **Accès réservé aux rôles super** (mêmes que section 7)

---

### 8.1 — Lister tous les rôles

```
GET /api/v1/admin/roles
Authorization: Bearer <token>
```

**Réponse 200 :**
```json
[
  {
    "id": 1,
    "name": "ROLE_IMPORTER",
    "displayName": "Importateur",
    "description": "Utilisateur final qui importe des marchandises",
    "system": true,
    "permissions": ["orders:create", "orders:read", "shipments:read", "payments:initiate"]
  }
]
```

---

### 8.2 — Créer un rôle personnalisé

```
POST /api/v1/admin/roles
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "name": "ROLE_CUSTOM_VIEWER",
  "displayName": "Observateur personnalisé",
  "description": "Accès lecture seule pour les auditeurs"
}
```

**Réponse 201 :** `RoleDetailResponse`

---

### 8.3 — Modifier un rôle

```
PATCH /api/v1/admin/roles/{roleId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de la requête :** (mêmes champs que la création)

**Réponse 200 :** `RoleDetailResponse`

---

### 8.4 — Supprimer un rôle

```
DELETE /api/v1/admin/roles/{roleId}
Authorization: Bearer <token>
```

**Réponse 204 No Content**

**Erreurs possibles :**
| Code HTTP | errorCode | Signification |
|---|---|---|
| 400 | `ERR_ROLE_IS_SYSTEM` | Impossible de supprimer un rôle système |
| 404 | `ERR_ROLE_NOT_FOUND` | Rôle introuvable |

---

### 8.5 — Assigner des permissions à un rôle

```
POST /api/v1/admin/roles/{roleId}/permissions
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de la requête :**
```json
{
  "permissionIds": [1, 3, 7, 12]
}
```

**Réponse 200 :** `RoleDetailResponse` avec la liste mise à jour des permissions

---

### 8.6 — Révoquer une permission d'un rôle

```
DELETE /api/v1/admin/roles/{roleId}/permissions/{permissionId}
Authorization: Bearer <token>
```

**Réponse 204 No Content**

---

### 8.7 — Lister toutes les permissions

```
GET /api/v1/admin/permissions
Authorization: Bearer <token>
```

**Réponse 200 :**
```json
[
  {
    "id": 1,
    "name": "orders:create",
    "resource": "orders",
    "action": "create",
    "description": "Créer une commande"
  }
]
```

---

### 8.8 — Créer / Modifier / Supprimer une permission

```
POST   /api/v1/admin/permissions              → 201
PATCH  /api/v1/admin/permissions/{permissionId} → 200
DELETE /api/v1/admin/permissions/{permissionId} → 204
```

**Corps pour création/modification :**
```json
{
  "name": "reports:export",
  "resource": "reports",
  "action": "export",
  "description": "Exporter les rapports"
}
```

---

## 9. Système de rôles et permissions (RBAC)

### 9.1 — Rôles disponibles

| Rôle | Cible | Description |
|---|---|---|
| `ROLE_IMPORTER` | Utilisateurs finaux | Importateurs de marchandises |
| `ROLE_AGENT` | Employés transitaires | Agents de terrain |
| `ROLE_ADMIN_FORWARDER` | Admins transitaires | Gestion d'une entreprise de transit |
| `ROLE_SUPER_RESPONSIBLE` | Super admin principal | Accès total + gestion des comptes |
| `ROLE_SUPER_COMMERCIAL` | Admin commercial | Vue commerciale de la plateforme |
| `ROLE_SUPER_FINANCIAL` | Admin financier | Vue financière de la plateforme |
| `ROLE_SUPER_PACKAGE` | Admin packages | Gestion des forfaits |

---

### 9.2 — Permissions disponibles

| Permission | Ressource | Action | Description |
|---|---|---|---|
| `orders:create` | orders | create | Créer une commande |
| `orders:read` | orders | read | Voir ses propres commandes |
| `orders:read_all` | orders | read_all | Voir toutes les commandes |
| `orders:update` | orders | update | Modifier une commande |
| `orders:cancel` | orders | cancel | Annuler une commande |
| `shipments:read` | shipments | read | Voir les expéditions |
| `shipments:update` | shipments | update | Modifier une expédition |
| `proofs:create` | proofs | create | Ajouter une preuve |
| `proofs:read` | proofs | read | Voir les preuves |
| `payments:read` | payments | read | Voir les paiements |
| `payments:initiate` | payments | initiate | Initier un paiement |
| `payments:validate` | payments | validate | Valider un paiement |
| `users:read` | users | read | Voir les profils utilisateurs |
| `users:manage` | users | manage | Gérer les comptes utilisateurs |
| `forwarders:read` | forwarders | read | Voir les transitaires |
| `forwarders:manage` | forwarders | manage | Gérer les transitaires |
| `analytics:read` | analytics | read | Voir les analyses |
| `incidents:read` | incidents | read | Voir les incidents |
| `incidents:manage` | incidents | manage | Gérer les incidents |

---

### 9.3 — Comment utiliser les permissions côté frontend

Le access token JWT contient directement les permissions de l'utilisateur. Pas besoin d'appel API supplémentaire.

**Exemple en Angular — guard de permission :**
```typescript
// Décoder le JWT et lire les permissions
import { jwtDecode } from 'jwt-decode';

interface TokenPayload {
  sub: string;
  email: string;
  role: string;
  permissions: string[];
  exp: number;
}

export class AuthService {
  hasPermission(permission: string): boolean {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;
    const payload = jwtDecode<TokenPayload>(token);
    return payload.permissions.includes(permission);
  }

  hasRole(role: string): boolean {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;
    const payload = jwtDecode<TokenPayload>(token);
    return payload.role === role;
  }
}
```

**Utilisation dans un template :**
```typescript
// Afficher un bouton seulement si l'utilisateur peut créer des commandes
*ngIf="authService.hasPermission('orders:create')"
```

---

## 10. Codes d'erreur

Référence complète de tous les codes d'erreur retournés par le module IAM :

| errorCode | HTTP | Contexte |
|---|---|---|
| `ERR_ACCOUNT_NOT_FOUND` | 404 | Compte inexistant |
| `ERR_ACCOUNT_ALREADY_EXISTS` | 409 | Email déjà utilisé |
| `ERR_ACCOUNT_SUSPENDED` | 403 | Compte suspendu |
| `ERR_ACCOUNT_PENDING_VERIFICATION` | 403 | OTP jamais validé |
| `ERR_INVALID_CREDENTIALS` | 401 | Email/mot de passe incorrect |
| `ERR_OTP_INVALID` | 404 | Code OTP incorrect |
| `ERR_OTP_EXPIRED` | 401 | Code OTP expiré (10 min) |
| `ERR_OTP_ALREADY_CONSUMED` | 401 | Code OTP déjà utilisé |
| `ERR_REFRESH_TOKEN_INVALID` | 401 | Refresh token inconnu |
| `ERR_REFRESH_TOKEN_EXPIRED` | 401 | Refresh token expiré (30 jours) |
| `ERR_REFRESH_TOKEN_REVOKED` | 401 | Refresh token révoqué |
| `ERR_PASSWORD_RESET_TOKEN_INVALID` | 404 | Token reset inconnu |
| `ERR_PASSWORD_RESET_TOKEN_EXPIRED` | 401 | Token reset expiré (1h) |
| `ERR_PROFILE_NOT_FOUND` | 404 | Profil inexistant |
| `ERR_PROFILE_PHOTO_INVALID` | 400 | Format photo invalide |
| `ERR_PROFILE_PHOTO_TOO_LARGE` | 400 | Photo > 5 Mo |
| `ERR_GOOGLE_TOKEN_INVALID` | 401 | Token Google invalide |
| `ERR_GOOGLE_EMAIL_NOT_VERIFIED` | 400 | Email Google non vérifié |
| `ERR_INVALID_STATUS` | 400 | Valeur de statut inconnue |
| `ERR_ROLE_NOT_FOUND` | 404 | Rôle inexistant |
| `ERR_ROLE_ALREADY_EXISTS` | 409 | Nom de rôle déjà pris |
| `ERR_ROLE_IS_SYSTEM` | 400 | Impossible de supprimer un rôle système |
| `ERR_PERMISSION_NOT_FOUND` | 404 | Permission inexistante |
| `ERR_PERMISSION_ALREADY_EXISTS` | 409 | Nom de permission déjà pris |
| `ERR_CANNOT_CHANGE_OWN_ROLE` | 403 | Un admin ne peut pas modifier son propre rôle |
| `ERR_FORBIDDEN` | 403 | Accès interdit (droits insuffisants) |
| `ERR_VALIDATION` | 400 | Erreur de validation du corps de la requête |
| `ERR_NOT_FOUND` | 404 | Ressource introuvable |
| `ERR_INTERNAL` | 500 | Erreur serveur inattendue |

---

## 11. Génération automatique des services Angular

Le backend expose une spécification **OpenAPI 3.0** complète. Vous pouvez générer automatiquement tous les services Angular (`*.service.ts`) et les modèles TypeScript à partir de cette spec.

### Prérequis

- Node.js ≥ 18
- Java 17 (nécessaire pour le générateur)
- L'application backend doit être démarrée

### Installation

Dans votre projet Angular :

```bash
npm install @openapitools/openapi-generator-cli --save-dev
```

### Script npm

Ajoutez ce script dans votre `package.json` :

```json
{
  "scripts": {
    "api:generate": "openapi-generator-cli generate -i http://localhost:8081/cargo-trust-api/v3/api-docs -g typescript-angular -o src/app/core/api --additional-properties=apiModulePrefix=CargoTrust,fileNaming=kebab-case,stringEnums=true,withInterfaces=true,ngVersion=17"
  }
}
```

**Puis exécuter :**
```bash
npm run api:generate
```

### Options expliquées

| Option | Valeur | Description |
|---|---|---|
| `-i` | URL de la spec OpenAPI | Source de la spec (backend doit être démarré) |
| `-g` | `typescript-angular` | Générateur pour Angular |
| `-o` | `src/app/core/api` | Dossier de destination |
| `apiModulePrefix` | `CargoTrust` | Préfixe de tous les services générés |
| `fileNaming` | `kebab-case` | Convention de nommage des fichiers |
| `stringEnums` | `true` | Génère des string enums TypeScript |
| `withInterfaces` | `true` | Génère aussi les interfaces pour les modèles |
| `ngVersion` | `17` | Version cible d'Angular |

### Utiliser depuis un fichier local (sans serveur)

Si vous préférez travailler depuis le fichier JSON téléchargé :

```bash
# 1. Télécharger la spec
curl http://localhost:8081/cargo-trust-api/v3/api-docs -o openapi-spec.json

# 2. Générer depuis le fichier
openapi-generator-cli generate \
  -i ./openapi-spec.json \
  -g typescript-angular \
  -o src/app/core/api \
  --additional-properties=apiModulePrefix=CargoTrust,fileNaming=kebab-case,stringEnums=true,withInterfaces=true,ngVersion=17
```

**Script package.json avec fichier local :**
```json
{
  "scripts": {
    "api:fetch-spec": "curl http://localhost:8081/cargo-trust-api/v3/api-docs -o openapi-spec.json",
    "api:generate": "openapi-generator-cli generate -i ./openapi-spec.json -g typescript-angular -o src/app/core/api --additional-properties=apiModulePrefix=CargoTrust,fileNaming=kebab-case,stringEnums=true,withInterfaces=true,ngVersion=17",
    "api:update": "npm run api:fetch-spec && npm run api:generate"
  }
}
```

**La commande `npm run api:update` télécharge la dernière spec ET régénère les services en une seule étape.**

### Ce qui est généré

```
src/app/core/api/
├── api/
│   ├── authentication.service.ts      ← /auth/* endpoints
│   ├── admin-users.service.ts         ← /admin/users/* endpoints
│   ├── admin-roles-permissions.service.ts ← /admin/roles|permissions/* endpoints
│   └── profile.service.ts             ← /profile/* endpoints
├── model/
│   ├── auth-response.ts
│   ├── user-summary-response.ts
│   ├── user-detail-response.ts
│   ├── profile-response.ts
│   ├── role-detail-response.ts
│   ├── permission-response.ts
│   └── ...
├── api.module.ts                       ← Module Angular à importer
└── index.ts                            ← Exports centralisés
```

### Intégrer dans votre module Angular

```typescript
// app.module.ts
import { ApiModule, Configuration } from './core/api';

@NgModule({
  imports: [
    ApiModule.forRoot(() => new Configuration({
      basePath: 'http://localhost:8081/cargo-trust-api',
    })),
  ],
})
export class AppModule {}
```

### Exemple d'utilisation d'un service généré

```typescript
import { AuthenticationService, LoginRequest } from '../core/api';

@Injectable({ providedIn: 'root' })
export class LoginComponent {
  constructor(private authApi: AuthenticationService) {}

  login(email: string, password: string) {
    const req: LoginRequest = { email, password };
    return this.authApi.login(req).subscribe(response => {
      if (response.requiresOtp) {
        // Rediriger vers la page OTP
      } else {
        localStorage.setItem('accessToken', response.accessToken!);
        localStorage.setItem('refreshToken', response.refreshToken!);
        // Rediriger vers le dashboard
      }
    });
  }
}
```

### Intercepteur HTTP pour le token JWT (à créer manuellement)

```typescript
// jwt.interceptor.ts
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
```

```typescript
// app.config.ts
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([jwtInterceptor])),
  ],
};
```

---

*Dernière mise à jour : 2026-06-12 — Module IAM v1.0*
