# GameForge
 
A desktop storefront built around a SQL Server database, with two custom Trie-based search engines, transactional purchases mediated by database triggers, and a curated browsing experience.
 
Built as a coursework project on database systems, with an emphasis on letting the database do real work - joins for personalised recommendations, triggers for purchase integrity, role-based access control via DCL - rather than treating it as a passive store of records.
 
---
 
## What it does
 
GameForge is the customer-facing client of an online game platform. Users sign in, browse games organised into curated sections, search and filter the catalog, buy games using an in-app balance, manage a wishlist, write reviews, befriend other users, and track their playtime and favorites.
 
Every action that mutates state goes through the database with proper transactions, foreign-key integrity, and trigger-enforced business rules.
 
### Highlights
 
- **Steam-style storefront** with curated sections (Top Rated, Recommended For You, New Arrivals, Budget Picks)
- **Personalised recommendations** computed by SQL joins on the user's library genres
- **Two Trie-based search engines** - one for games, one for usernames - both with prefix matching, fuzzy fallback (1-edit tolerance), and recency-based ranking boosts
- **Trigger-mediated purchases** - the application inserts into `Order_Game` and the database itself handles balance verification, balance deduction, and Library auto-population
- **Mutual friendships** stored as paired rows with transactional add/remove guarantees
- **Profile-style Library tab** with stats (games owned, favorites, total playtime, top genre)
- **Custom dark theme** with rounded chips, themed scroll bars, hover-aware buttons
---
 
## Tech stack
 
| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | Swing (with custom UI subclasses for theming) |
| Database | Microsoft SQL Server (SQLEXPRESS) |
| Connectivity | JDBC via `mssql-jdbc` |
| Build | Maven |
| Search | Custom Trie + 1-edit fuzzy DFS (no external library) |
 
No web framework, no ORM, no front-end build pipeline. Everything is JDBC, Swing, and ~50 source files.
 
---
 
## Architecture
 
The project is organised into clear layers, with each layer depending only on what's beneath it.
 
```
┌──────────────────────────────────────────────────────────┐
│  ui (Swing)                                              │
│    LoginFrame, MainFrame                                 │
│    panels: StorePanel, LibraryPanel, WishlistPanel,      │
│            GameDetailDialog, FriendsDialog, SectionRow,  │
│            GameCard, LibraryStatsPanel, ...              │
│    theme: GameForgeTheme                                 │
│    util: ImageLoader, DarkScrollBarUI, WrapLayout, Chip  │
└────────────────────────────┬─────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────┐
│  search                                                  │
│    SearchService          (game-name Trie)               │
│    UserSearchService      (username Trie)                │
│    StoreSectionsService   (curated SQL projections)      │
└────────────────────────────┬─────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────┐
│  db (DAO layer - pure JDBC)                              │
│    DBConnection, GameDAO, LibraryDAO, WishlistDAO,       │
│    OrderDAO, ReviewDAO, FriendDAO, GenreDAO, UserDAO     │
└────────────────────────────┬─────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────┐
│  Microsoft SQL Server                                    │
│    Tables, FKs, CHECK constraints                        │
│    Triggers: trg_CheckUserBalance, add_game_to_library,  │
│              prevent_review_for_unowned_game, ...        │
│    DCL: UserRole, AnalystRole, db_owner                  │
└──────────────────────────────────────────────────────────┘
```
 
A separate `model` package holds simple data carriers (`Game`, `Review`) and a `gamedb.trie` package contains the reusable Trie/FuzzySearch implementations.
 
---
 
## The two search engines
 
Both engines share the same underlying `Trie` implementation; they differ only in what's indexed and how frequencies are normalised.
 
### `SearchService` - game search
 
- Indexes all game names from the database
- Frequency normalised from average review rating (0–10) to 1–1000
- On every keystroke in the Store search bar, returns the top 5 prefix matches
- Falls back to fuzzy search (edit distance ≤ 1) if the prefix yields no exact matches
- Selecting a result adds +200 to that name's frequency, so recently-clicked games surface first on subsequent searches
### `UserSearchService` - friend search
 
- Indexes all customer usernames from `[User]`
- Every user starts at frequency 1 (no rating signal to draw from)
- Same prefix + fuzzy mechanics
- Same +200 boost when a username is selected (so frequently-friended users surface first)
Both indexes are built once on login and live in memory thereafter. No keystroke ever hits the database.
 
---
 
## Database design highlights
 
### Transactional purchase flow
 
The application **never** writes to `Library` directly. Purchasing is initiated by inserting into `Order_Game`, after which the database takes over via two triggers:
 
```
INSERT INTO Order_Game
        │
        ▼
trg_CheckUserBalance (INSTEAD OF INSERT):
  - verifies User.balance ≥ Game.gamePrice × quantity
  - if insufficient, RAISERROR + ROLLBACK
  - otherwise inserts the row and deducts balance
        │
        ▼
add_game_to_library (AFTER INSERT):
  - inserts (userID, gameID) into Library
        │
        ▼
Application sees one transactional outcome:
  success (balance reduced, game owned) or failure (everything rolled back)
```
 
This means a partial state is impossible. Even if the application crashes between the `Order` insert and the `Order_Game` insert, the failure is contained.
 
### Mutual friendships
 
Every friendship is stored as **two rows**, one in each direction. `FriendDAO.add` and `FriendDAO.remove` execute both inserts/deletes inside a single transaction, so the symmetric invariant always holds. As a result, "list my friends" is a simple `WHERE userID_1 = ?` query - no `OR userID_2 = ?` needed.
 
A `CHECK (userID_1 <> userID_2)` constraint prevents self-friendship at the database level even if the DAO is bypassed.
 
### Role-based access control via DCL
 
Three logins, three permission scopes:
 
| Login | Role | What they can do |
|---|---|---|
| `user1` | `UserRole` | SELECT on the catalog, INSERT/DELETE on personal data (Library, Wishlist, Review, Friend, Order, Order_Game) |
| `analyst1` | `AnalystRole` | SELECT only, across all tables |
| `admin1` | `db_owner` | Full control |
 
The customer-facing GUI always connects as `user1` internally, regardless of which `[User]` row the customer logs in as. The customer's identity is tracked separately at the application layer (`DBConnection.getAppUsername()`), but every query is constrained by `UserRole`'s grants.
 
This means the application **cannot** issue queries the customer shouldn't be able to issue - the database refuses them outright.
 
---
 
## How to run
 
### Prerequisites
 
- **Java 17 or newer** (the codebase uses pattern-matching `instanceof`, switch expressions)
- **Microsoft SQL Server Express** (or full edition)
- **SQL Server Management Studio (SSMS)** for running setup scripts
- **IntelliJ IDEA** (recommended; any IDE with Maven support will do)
- **Maven** (bundled with IntelliJ)
### Setup
 
1. **Clone the project** and open in IntelliJ as a Maven project. The `pom.xml` declares the only external dependency (`mssql-jdbc`).
2. **Create the database**. In SSMS, run the scripts in this order:
   - `SQLcreate_tables.sql` - schema
   - `DCL_Statements.sql` - logins, users, roles, grants
   - `SQLInsert_Data.sql` - seed data (games, users, library entries, reviews, etc.)
   - All trigger scripts (`CheckUserBalance.sql`, `add_game_to_library.sql`, `prevent_review_for_unowned_game.sql`, `prevent_game_deletion.sql`, `update_playtime.sql`)
3. **Update the JDBC URL**. In `com/gameplatform/db/DBConnection.java`, the `BASE_URL` constant points to `DESKTOP-7OVINNS\SQLEXPRESS`. Change this to your SQL Server hostname (run `hostname` in cmd to find it).
4. **Verify SQL Server config**. In SQL Server Configuration Manager, ensure TCP/IP is enabled on `SQLEXPRESS`. In SSMS server properties, "Mixed mode authentication" must be enabled.
5. **Run** `com.gameplatform.Main`. The login window opens.
### Login credentials
 
The seed data populates customers with passwords matching the pattern `<nickname>123`. For example:
 
- Username `alex_k` → password `Alex123`
- Username `armin_kh` → password `Armin123`
- Username `pixelking` → password `PixelKing123`
The full list is in `[User].password`. SQL Server logins (`user1`, `admin1`, `analyst1`) are an internal implementation detail - the customer never sees them.
 
---
 
## Project structure
 
```
src/main/java/
├── com.gameplatform/
│   └── Main.java                     # Entry point
│
├── com.gameplatform.db/              # JDBC layer
│   ├── DBConnection.java             # Singleton connection + customer auth
│   ├── GameDAO.java                  # Catalog reads
│   ├── LibraryDAO.java               # Library + stats + favorite/playtime
│   ├── WishlistDAO.java              # Wishlist CRUD
│   ├── OrderDAO.java                 # Transactional purchases
│   ├── ReviewDAO.java                # Reviews
│   ├── FriendDAO.java                # Mutual friendships
│   ├── GenreDAO.java                 # Genre lookups
│   └── UserDAO.java                  # User profile data
│
├── com.gameplatform.model/           # DTOs
│   ├── Game.java
│   └── Review.java
│
├── com.gameplatform.search/          # Search services
│   ├── SearchService.java            # Game-name Trie
│   ├── UserSearchService.java        # Username Trie
│   └── StoreSectionsService.java     # Curated SQL projections
│
├── com.gameplatform.ui/              # Swing UI
│   ├── LoginFrame.java
│   ├── MainFrame.java
│   ├── theme/
│   │   └── GameForgeTheme.java       # Colors and fonts
│   ├── panels/
│   │   ├── StorePanel.java           # Sections + search/filter modes
│   │   ├── LibraryPanel.java         # Owned games + stats strip
│   │   ├── WishlistPanel.java        # Saved games
│   │   ├── GameDetailDialog.java     # Per-game detail view
│   │   ├── FriendsDialog.java        # Friends list + user search
│   │   ├── WriteReviewDialog.java    # New-review form
│   │   ├── GameCard.java             # Reusable card component
│   │   ├── SectionRow.java           # Sliding-window curated row
│   │   ├── LibraryStatsPanel.java    # Profile-style stats header
│   │   └── SortOption.java           # Sort dropdown enum
│   └── util/
│       ├── ImageLoader.java          # Cached image loading
│       ├── DarkScrollBarUI.java      # Themed scroll bars
│       ├── WrapLayout.java           # FlowLayout that works in JScrollPane
│       └── Chip.java                 # Rounded tag component
│
└── com.gamedb.trie/                  # Reusable search index
    ├── Trie.java
    ├── TrieNode.java
    └── FuzzySearch.java
```
 
---
 
## Notable design decisions
 
### Why two search engines instead of a single generic one?
 
The Trie itself is generic - it indexes `(word, frequency)` pairs and doesn't care what the words are. But the *normalisation logic* differs: game frequencies derive from review ratings, while username frequencies are flat (no rating signal). Encoding that difference in two thin services keeps the responsibilities clean and makes future tuning (e.g. weighting friend search by mutual-friend count) trivial to add.
 
### Why does the application connect as `user1` internally?
 
Customer authentication and SQL Server authentication are two different problems. SQL Server logins exist to enforce DCL grants; they're not user-facing identities. By always connecting as `user1`, every query is automatically constrained by `UserRole`, regardless of which customer is logged in. The customer's actual identity (`alex_k`, `pixelking`, …) is tracked at the application layer for queries like "list my games."
 
### Why not just put `LIKE '%term%'` searches on the database?
 
For a 50-game catalog, `LIKE` would be fine. But the project's design constraint was to demonstrate a Trie-based autocomplete, which gives O(prefix length) lookups regardless of catalog size, plus fuzzy matching, plus the +200 recency-boost ranking that would be awkward to express in pure SQL. The Trie also means search is fully decoupled from the database's load - no DB roundtrips per keystroke.
 
### Why is `Game` mutable for `playTime` and `favorite`?
 
In nearly every context, `Game` is an immutable view object. But the Library context needs to carry the user's per-game state (playtime, favorite flag) alongside the catalog data, and the natural place for those values is on the `Game` itself rather than in a parallel data structure. The setters are only ever called from `LibraryDAO.listForUser` - a single, well-known mutation point.
 
### Why curated sections + a flat results mode, instead of one or the other?
 
Sections answer "what should I look at?"; flat results answer "did this match what I typed?". Steam, Netflix, and most discovery-oriented apps default to the former and switch to the latter when the user expresses intent. Trying to do both in one view (e.g. sectioning the search results) muddies the visual hierarchy and makes the page feel disorganised.
