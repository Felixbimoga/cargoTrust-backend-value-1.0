# CargoTrust — Backend

API REST Spring Boot pour la plateforme CargoTrust de gestion des achats en ligne et du transit de marchandises.

---

## Prérequis

Assurez-vous d'avoir installé sur votre machine :

| Outil | Version minimale | Vérification |
|---|---|---|
| Java (JDK) | 17 | `java -version` |
| Maven | 3.9 *(ou utiliser `mvnw`)* | `mvn -version` |
| MySQL | 8.0 | `mysql --version` |
| Git | toute version récente | `git --version` |

> Maven n'est **pas obligatoire** si vous utilisez le wrapper inclus dans le projet (`./mvnw` sous Linux/macOS, `mvnw.cmd` sous Windows).

---

## Installation et démarrage

### 1 — Cloner le projet

```bash
git clone https://github.com/Felixbimoga/cargoTrust-backend-value-1.0.git
cd cargoTrust-backend-value-1.0
```

---

### 2 — Créer la base de données MySQL

Connectez-vous à MySQL puis exécutez :

```sql
CREATE DATABASE cargotrust_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> Le schéma complet (tables, index, données initiales) est créé **automatiquement** par Flyway au premier démarrage de l'application. Vous n'avez aucun autre script SQL à exécuter.

---

### 3 — Configurer le fichier `.env`

Copiez le fichier d'exemple fourni :

```bash
# Linux / macOS / Git Bash
cp .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env
```

Ouvrez `.env` et renseignez les valeurs **obligatoires** marquées `← à remplir` :

```env
# ── Base de données ─────────────────────────────────────────────────────
DB_URL=jdbc:mysql://localhost:3306/cargotrust_dev?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=                          # ← votre mot de passe MySQL

# ── Sécurité JWT ────────────────────────────────────────────────────────
# Générez une clé aléatoire : openssl rand -base64 64
JWT_SECRET=change-this-secret-key-minimum-256-bits-in-production-mandatory

# ── CORS ────────────────────────────────────────────────────────────────
CORS_ALLOWED_ORIGINS=http://localhost:4200   # URL de votre app Angular

# ── Email (Gmail SMTP) ──────────────────────────────────────────────────
MAIL_USERNAME=                        # ← votre adresse Gmail
MAIL_PASSWORD=                        # ← mot de passe d'application Gmail (voir note ci-dessous)

# ── Compte super admin ──────────────────────────────────────────────────
# Ce compte est créé automatiquement au premier démarrage
SUPER_ADMIN_EMAIL=cargotrust237@gmail.com
SUPER_ADMIN_PASSWORD=Admin.123

# ── Google OAuth2 (laisser vide si vous n'utilisez pas Google Auth) ─────
GOOGLE_CLIENT_ID_WEB=
GOOGLE_CLIENT_SECRET_WEB=
GOOGLE_CLIENT_ID_ANDROID=
```

**Note — Mot de passe d'application Gmail :**  
Ne mettez pas votre mot de passe Gmail ordinaire. Allez sur [myaccount.google.com](https://myaccount.google.com) → Sécurité → **Mots de passe des applications**, créez-en un et copiez les 16 caractères générés dans `MAIL_PASSWORD`.

---

### 4 — Démarrer l'application

**Option A — Depuis IntelliJ IDEA :**
1. `File → Open` → sélectionner le dossier du projet
2. Attendre que Maven télécharge les dépendances (barre de progression en bas)
3. Ouvrir `src/main/java/.../CargoTrustApplication.java`
4. Cliquer sur le bouton **Run** vert

**Option B — Depuis le terminal :**

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run

# Windows (PowerShell ou CMD)
mvnw.cmd spring-boot:run
```

Le premier démarrage prend environ 30–60 secondes (téléchargement des dépendances + migrations Flyway).

Quand l'application est prête vous verrez dans les logs :
```
Started CargoTrustApplication in X.XXX seconds (process running for X.XXX)
```

---

### 5 — Vérifier que tout fonctionne

Ouvrez votre navigateur et accédez à ces URLs :

| URL | Description |
|---|---|
| `http://localhost:8081/cargo-trust-api/swagger-ui.html` | Interface Swagger — explorez et testez toutes les APIs |
| `http://localhost:8081/cargo-trust-api/v3/api-docs` | Spec OpenAPI JSON |
| `http://localhost:8081/cargo-trust-api/actuator/health` | Doit retourner `{"status":"UP"}` |

---

### 6 — Se connecter avec le compte super admin

Un compte administrateur est créé **automatiquement** au premier démarrage (valeurs définies dans `.env`).

Testez la connexion depuis Swagger ou Postman :

```
POST http://localhost:8081/cargo-trust-api/api/v1/auth/login

{
  "email": "cargotrust237@gmail.com",
  "password": "Admin.123"
}
```

Si la réponse contient `"requiresOtp": true`, vérifiez la boîte mail de `MAIL_USERNAME` pour récupérer le code OTP à 6 chiffres.

---

## Résolution des problèmes courants

| Erreur | Cause probable | Solution |
|---|---|---|
| `Access denied for user 'root'@'localhost'` | Mauvais mot de passe MySQL | Vérifier `DB_PASSWORD` dans `.env` |
| `Unknown database 'cargotrust_dev'` | Base de données non créée | Exécuter le `CREATE DATABASE` de l'étape 2 |
| `Could not resolve placeholder '...'` | Fichier `.env` absent ou incomplet | Vérifier que `.env` existe avec toutes les clés |
| `Flyway migration checksum mismatch` | Script SQL modifié après migration | Supprimer la DB, la recréer, redémarrer |
| Port `8081` déjà utilisé | Autre processus sur ce port | Changer `server.port` dans `application.yml` |
| `535-5.7.8 Authentication failed` (Gmail) | Mauvais mot de passe d'application | Régénérer un nouveau mot de passe d'application Gmail |
| `ERR_ACCOUNT_PENDING_VERIFICATION` au login | OTP jamais validé | Appeler `POST /auth/verify-otp` avec le code reçu par email |

---

## Structure du projet

```
src/
├── main/
│   ├── java/.../
│   │   ├── CargoTrustApplication.java   ← Point d'entrée
│   │   ├── iam/                         ← Module IAM (auth, profil, admin RBAC)
│   │   │   ├── controller/              ← AuthController
│   │   │   ├── admin/controller/        ← AdminUserController, AdminRoleController
│   │   │   ├── profile/controller/      ← ProfileController
│   │   │   ├── service/                 ← Logique métier
│   │   │   ├── domain/                  ← Entités JPA
│   │   │   └── security/               ← JWT filter, SecurityConfig
│   │   ├── shared/                      ← Exceptions globales, email, utils
│   │   └── [order|shipment|payment|...] ← Modules futurs
│   └── resources/
│       ├── application.yml              ← Configuration principale
│       └── db/migration/                ← Scripts Flyway V1 → V7
└── test/                                ← Tests unitaires

docs/
└── README_IAM.md                        ← Documentation complète des APIs IAM
.env.example                             ← Template des variables d'environnement
```

---

## Documentation des APIs

La documentation complète de chaque endpoint (corps des requêtes, réponses, codes d'erreur) est disponible dans :

- **Swagger UI** (interactif) : `http://localhost:8081/cargo-trust-api/swagger-ui.html`
- **Fichier Markdown** : [`docs/README_IAM.md`](docs/README_IAM.md)

---

## Technologies utilisées

| Technologie | Version | Rôle |
|---|---|---|
| Java | 17 | Langage |
| Spring Boot | 3.2.0 | Framework principal |
| Spring Modulith | 1.2.4 | Architecture modulaire |
| Spring Security | 6 | Authentification et autorisation |
| JWT (jjwt) | 0.12.6 | Tokens d'accès |
| MySQL | 8.0 | Base de données |
| Flyway | 9.x | Migrations de schéma |
| Hibernate / JPA | 6.4 | ORM |
| Swagger / OpenAPI | 3.0 | Documentation API |
| Google API Client | 2.2.0 | Authentification Google OAuth2 |
