# CeylonVillas — Selenium (Java + TestNG) — Signup/Login Suite

Automates the signup/login flow of the CeylonVillas villa-stay app
(`Login.jsx` + navbar sign-in/out in `Home.jsx`).

## Prerequisites

- Java 17+
- Maven 3.8+ (or just use IntelliJ, which bundles Maven)
- Google Chrome installed (WebDriverManager downloads the matching driver automatically)
- The frontend running locally: `npm run dev` in your `frontend/` folder (default `http://localhost:5173`)
- **At least one villa in the database.** Booking tests click the first villa
  card on the homepage — if none exist, they'll skip themselves with a clear
  message telling you to add one via `/admin` first, rather than failing
  confusingly.

## Important context

- **No test/staging backend.** Signup and login hit the real deployed backend
  (`villa-backend-1gzn.onrender.com`). Every signup test therefore generates a
  brand-new random email (`TestDataGenerator.uniqueEmail()`) so re-running the
  suite never collides with an already-registered account.
- **No `id`/`data-testid` attributes in the source.** Locators fall back to
  `type`, `placeholder`, and DOM structure (see comments in `LoginPage.java`
  and `VillaDetailsPage.java`). If you have write access to the frontend
  repo, adding `data-testid` to the key form fields would make these
  locators much less brittle against future redesigns — happy to draft
  that patch if you want it.

## Configure

Edit `src/test/resources/config.properties`:

```properties
base.url=http://localhost:5173   # or your deployed frontend URL
browser=chrome                   # chrome | firefox
headless=false
timeout.seconds=10
```

## Run

```bash
mvn clean test
```

Or run a single class:

```bash
mvn test -Dtest=LoginSignupTest
```

## What's covered

| Test | Scenario |
|---|---|
| `testSuccessfulSignup` | New user signs up → redirected to homepage, signed in |
| `testDuplicateSignupShowsError` | Re-signing up with the same email shows "Email already registered" |
| `testSuccessfulLoginWithValidCredentials` | Existing user logs in successfully |
| `testLoginWithWrongPasswordShowsError` | Correct email + wrong password → "Invalid email or password" |
| `testLoginWithUnregisteredEmailShowsError` | Never-registered email → same invalid-credentials error |
| `testToggleBetweenSignInAndSignUpModes` | Form heading/name-field toggle correctly between modes |
| `testSignOutAfterLogin` | Signed-in user can sign out from the navbar |

**Booking flow (`BookingFlowTest`):**

| Test | Scenario |
|---|---|
| `testSearchWithNoMatchesShowsEmptyState` | Searching a nonsense term shows "No villas found" |
| `testClearingSearchRestoresAllVillas` | Clearing the search brings back the full villa grid |
| `testClickingVillaCardOpensDetailsPage` | Clicking a villa card navigates to `/villa/:id` with matching name |
| `testBookingTotalPreviewCalculatesCorrectly` | Live total preview = nights × price per night |
| `testCompleteBookingFlowHappyPath` | Guest fills the full booking form and gets a confirmation with correct name/villa/dates |

**Reviews flow (`ReviewsTest`):**

| Test | Scenario |
|---|---|
| `testLoggedOutGuestSeesSignInPromptNotForm` | Logged-out guest sees "Sign in to leave a review", no form rendered |
| `testSignInPromptLinksToLoginPage` | Clicking that prompt navigates to `/login` |
| `testReviewFormDefaultsToFiveStars` | Logged-in user's rating field defaults to 5 stars |
| `testLoggedInUserCanSubmitReview` | Logged-in user submits a review; count increases, comment appears, thank-you message shown |

**Admin CRUD (`AdminCrudTest`):**

| Test | Scenario |
|---|---|
| `testWrongPasswordShowsError` | Wrong admin password shows an error, panel stays locked |
| `testCorrectPasswordUnlocksPanel` | Correct password unlocks the Admin Panel |
| `testAddEditDeleteVillaLifecycle` | Full villa create → edit → delete lifecycle, count checked at each step |
| `testAddDeletePackageLifecycle` | Creates a throwaway villa, adds/deletes a package for it, cleans up both |
| `testBookingsTabRenders` | Bookings tab renders with a valid count (smoke check) |

## Project structure

```
selenium-villa-tests/
├── pom.xml
├── testng.xml
├── README.md
└── src/test/
    ├── java/com/villastay/
    │   ├── base/BaseTest.java          # WebDriver lifecycle (setup/teardown)
    │   ├── pages/BasePage.java         # shared explicit-wait helpers
    │   ├── pages/HomePage.java         # navbar, search, villa grid
    │   ├── pages/LoginPage.java        # combined sign-in/sign-up form
    │   ├── pages/VillaDetailsPage.java # villa info + booking form
    │   ├── pages/AdminPage.java        # login gate, villas/packages/bookings tabs
    │   ├── tests/LoginSignupTest.java
    │   ├── tests/BookingFlowTest.java
    │   ├── tests/ReviewsTest.java
    │   ├── tests/AdminCrudTest.java
    │   ├── utils/ConfigReader.java
    │   ├── utils/DateUtils.java
    │   └── utils/TestDataGenerator.java
    └── resources/config.properties
```

## CI (GitHub Actions) — manual trigger only

A workflow at `.github/workflows/selenium-tests.yml` can run this suite on
GitHub's servers instead of your machine. **It is manual-trigger only, not
on every push/PR** — several tests (Admin CRUD, booking, signup) mutate your
real production database, since there's no staging environment, and running
that automatically on every commit felt like the wrong default.

**Where this project goes:** since these tests only need your frontend
running (they talk to the deployed Render backend directly, not a local
one), this `selenium-villa-tests/` folder — and the `.github/workflows/`
folder — belong inside your **frontend's own GitHub repo**, as a sibling to
`src/`, `package.json`, etc. at the repo root. Your backend repo isn't
involved here at all.

**To run it:** push this into your frontend repo, then go to that repo's
**Actions** tab → "Selenium Villa Tests" → **Run workflow**. Optionally type
a single test class name (e.g. `LoginSignupTest`) to run just that one
instead of the full suite.

What it does: checks out your code, starts the frontend (`npm run dev`)
in the background, waits for it to respond, installs Java/Maven/Chrome,
runs the suite in headless mode (`-Dheadless=true`, since GitHub's runners
have no visible display), and uploads the test reports as a downloadable
artifact regardless of pass/fail.

## Next steps

Every major flow is covered (signup/login, booking, reviews, admin CRUD)
and CI is wired up. Natural directions from here: parallel/cross-browser
runs, or an Allure/ExtentReports HTML report layer instead of raw Surefire
XML.

## Known issues

- **`testBookingTotalPreviewCalculatesCorrectly` (in `BookingFlowTest`) currently
  fails/errors and the root cause hasn't been confirmed.** Two related fixes
  were applied along the way (a search-clear race condition in `HomePage`,
  and a comma-unsafe price regex in `VillaDetailsPage`), but neither was
  verified as *the* cause of this specific test's failure — left as a known
  gap rather than guessed away. If you pick this back up, grab the actual
  stack trace from the IntelliJ Run panel first before changing anything
  else in this test.
