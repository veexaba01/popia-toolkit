# POPIA Toolkit

A secure web application that helps small South African businesses keep a record of the personal information they hold, so they can understand and manage their responsibilities under POPIA (the Protection of Personal Information Act).

It is a learning project, built while studying secure software development and penetration testing, with security designed in from the start rather than added at the end.

> **Status:** working application, penetration test in progress. It has not been audited and is not ready to hold real personal information. This project is not legal advice, and using it does not guarantee POPIA compliance.

## The problem

Clinics, schools, estate agencies, accountants and many other small businesses collect personal information every day: names, contact details, health details, ID numbers. POPIA requires them to know what they hold, why they hold it, where it is kept, and to protect it.

Most of these businesses have no security or compliance staff. In practice that means:

- Nobody has a clear list of what personal information the business holds or where it lives.
- Information is spread across software, spreadsheets, email and filing cabinets, and nobody can say who changed what.
- Every staff member can often see and change everything, because there is no simple way to limit access.
- Existing compliance tools are built for large companies, and are too expensive and too complicated for a five-person practice.

The POPIA Toolkit aims to be a small, simple, secure tool that a non-expert can use to keep that inventory, control who can change it, and see a trustworthy record of what happened.

## What it does

- Lets a business register an organisation and its first administrator
- Lets the administrator add staff members
- Keeps a **personal data inventory**: for each kind of information, what it is, why it is held, where it is stored, and whether it is special personal information (for example health details)
- Keeps an **audit log** of logins, failed logins, staff changes, inventory changes and password changes
- Keeps every organisation's data completely separate from every other organisation's

The application stores *descriptions* of the personal information a business holds, not the personal information itself. That limits the damage if the data is ever exposed.

### Who can do what

| Action | Admin | Staff |
|---|---|---|
| Log in, change own password | Yes | Yes |
| View the inventory | Yes | Yes |
| Add and edit inventory items | Yes | Yes |
| Delete inventory items | Yes | No |
| Add staff members | Yes | No |
| View the audit log | Yes | No |

## How it works (pseudocode)

This is a plain description of the main flows, not the actual code.

### Registration

```
REGISTER(organisationName, email, password)
    validate every field on the server:
        organisationName is required, at most 200 characters
        email is a valid address, at most 254 characters
        password is between 12 and 72 characters
    if validation fails:
        show the form again with the messages
    normalise the email: trim spaces, lower case
    if an account with this email already exists:
        show a general failure message (does not say why)
    create the organisation
    create the user as ADMIN of that organisation
        store only a salted BCrypt hash of the password
    send the user to the login page
```

### Login

```
LOGIN(email, password)
    if this email has 5 failed attempts in the last 15 minutes:
        refuse, and look exactly like a normal failed login
    look up the account and check the password hash
    if the check succeeds:
        issue a new session id
        clear the failed attempts for this email
        record LOGIN_SUCCESS in the audit log
    otherwise:
        count a failed attempt for this email, whether or not the account exists
        if the account exists:
            record LOGIN_FAILURE in the audit log
        show the same error whatever the reason
```

### Every inventory action

```
ANY_INVENTORY_ACTION(request)
    organisationId = taken from the logged-in user
                     (never from the form, the address or any request value)
    for a single item:
        item = find the item by its id AND organisationId together
        if no item is found:
            answer "not found"
            (the same answer for an unknown id and for another organisation's id)
    LIST   -> items of this organisation only, ordered by name
    CREATE -> validate, save under this organisation, record INVENTORY_CREATED
    UPDATE -> find the item as above, validate, save, record INVENTORY_UPDATED
    DELETE -> admin only, find the item as above, remove it, record INVENTORY_DELETED
```

### Adding staff, changing a password, reading the audit log

```
ADD_STAFF(email, password)                      -- admin only
    organisationId = the logged-in admin's organisation
    refuse if the email already belongs to any account
    create the user with the role STAFF (the role cannot be chosen by the request)
    record STAFF_CREATED

CHANGE_PASSWORD(currentPassword, newPassword, confirmation)
    userId = the logged-in user (a user can only change their own password)
    refuse unless: new password is 12 to 72 characters,
                   it matches the confirmation,
                   it differs from the current password,
                   the current password is correct
    store a new salted hash, record PASSWORD_CHANGED

SHOW_AUDIT_LOG()                                -- admin only
    show the 100 most recent events of the logged-in admin's organisation
    events can be added but never edited or deleted through the application
```

## Security

Security is the point of this project. A plain-language summary of what is in place, what is planned and what the known limits are is in [docs/security.md](docs/security.md). In short:

- Passwords stored only as salted BCrypt hashes, with length limits and a login rate limit
- Every page denied by default, with role checks on the server for admin pages and deletion
- Strict separation between organisations, enforced in every query and covered by automated tests
- CSRF protection on every form, escaped output, a content security policy, and hardened session cookies
- Secrets kept out of the code, secret scanning, weekly dependency vulnerability scans and automatic update proposals
- An append-only audit log, visible only to administrators

The threat model is in [docs/threat-model.md](docs/threat-model.md), the test plan in [docs/pentest-plan.md](docs/pentest-plan.md), and results as they are recorded in [docs/pentest-findings.md](docs/pentest-findings.md).

## Tech stack

Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA, PostgreSQL 17, Flyway, Thymeleaf, JUnit 5, Mockito, Docker Compose, GitHub Actions, Dependabot, gitleaks and OSV-Scanner.

## Getting started

You need Java 21 and Docker with the Compose plugin.

```bash
# 1. Get the code
git clone https://github.com/<your-username>/popia-toolkit.git
cd popia-toolkit

# 2. Create your local settings (this file is never committed)
cp .env.example .env
# open .env and set your own database password

# 3. Start the database
docker compose up -d

# 4. Run the application
set -a; source .env; set +a
./mvnw spring-boot:run
```

Then open `http://localhost:8080/register`. To use a different port, start the application with `SERVER_PORT=8081 ./mvnw spring-boot:run`.

The first time it runs, Flyway builds the database tables from the migration files, so there is nothing to set up by hand. To stop the database and keep its data, use `docker compose down`. To wipe it completely, use `docker compose down -v`.

## Running the tests

```bash
./mvnw test
```

The tests use an in-memory database, so they need neither Docker nor the `.env` file. They cover validation, password hashing, login, rate limiting, role rules, session and header settings, every inventory action, the audit log, and, most importantly, attempts by one organisation to read, change or delete another organisation's data.

## Project structure

```
src/main/java/com/veex/popia_toolkit
├── config         Security rules, headers, password hashing
├── auth           Registration, login, staff, password change, login rate limit
├── organisation   Organisations (the tenants)
├── inventory      The personal data inventory
└── audit          The audit log

src/main/resources
├── db/migration   Database migrations (Flyway)
└── templates      Pages (Thymeleaf)

docs/              Security overview, threat model, penetration test plan and results
.github/workflows  Build, secret scan and dependency scan
```

## Improvements

### Known limitations in the current version

These are listed honestly, and each one is tracked in the threat model.

- Registration still reveals, through a different response, whether an email already has an account
- The login limit is per email address and held in memory, so it resets when the application restarts, and it lets someone lock a known account out for 15 minutes
- Blocked login attempts, and failed logins for emails that do not exist, are not recorded in the audit log
- Changing a password does not end the user's other sessions
- There is no way for a user to recover a forgotten password
- There is no way to disable or remove a staff member
- Anyone can register an organisation, with no email check
- The inventory list has no paging
- The audit log lives in the same database as everything else, so someone with direct database access could alter it
- The Secure cookie flag only applies when the application runs with the production profile, behind HTTPS

### Planned next

- Fix the findings from the penetration test and retest them
- Password reset by email, and email verification at registration
- A screen to disable or remove staff members
- End all of a user's sessions when their password changes
- Record blocked login attempts in the audit log
- Paging and search for the inventory

### Future ideas

- An **Information Officer** role and a read-only **Auditor** role (both already exist in the data model but are not used yet)
- An incident and breach log with a response checklist
- A ready-made starting inventory for common kinds of business, such as a clinic, a school or an estate agency
- Reminders to review inventory items regularly
- Export of the inventory and audit log as a report
- Two-factor sign-in
- Rate limiting by network address as well as by email, shared across several servers
- Tamper-evident audit records, for example by chaining each event to the one before it
- Deployment guide covering HTTPS, backups, monitoring and database access rules
- A mobile-friendly layout and an accessibility review

## Disclaimer

This is a student project. It has not been audited, and it should not be used to store real personal information yet. It helps organise information about POPIA responsibilities, but it does not give legal advice and does not make a business compliant.

## License

To be decided.

## Tracking code

WTC-AWN7NMCX
