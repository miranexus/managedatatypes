# Manage Data Types

An Android sample app for learning CRUD operations (create, read, update, and
delete) with three storage options:

| Example | Storage / service | Needs a server? |
| --- | --- | --- |
| SQLite | Local database on the device | No |
| MongoDB | Node.js API using Retrofit | Yes |
| MySQL | PHP API using Retrofit | Yes |

The app also includes a calendar view and exports displayed data as `.xls`.

## Get started

1. Open this repository in Android Studio and let it install Android SDK
   Platform 36.
2. Use JDK 17 or newer (the project uses Gradle 9.8.2 and Android Gradle Plugin
   9.5.1).
3. Run the app on an Android 6.0 (API 23) or newer device or emulator.
4. Choose **SQLite** to try the app without configuring a backend.

The MongoDB and PHP/MySQL examples need their respective services for remote
operations. Their URLs are configured in
`app/src/main/res/values/strings.xml`. The Android emulator can reach services
running on the host computer at `10.0.2.2`. Use HTTPS for deployed services;
cleartext HTTP is enabled only in the Android debug build.

## MongoDB API (Node.js)

The API requires Node.js 22.12 or newer (Node.js 24 LTS recommended) and
MongoDB. For a local MongoDB server, create `nodejsfiles/.env`:

```text
MONGODB_URI=mongodb://localhost:27017
PORT=3003
```

For MongoDB Atlas, use its connection string for `MONGODB_URI`. Keep `.env`
private; it is excluded from Git. In a terminal, run:

```sh
cd nodejsfiles
npm ci
npm start
```

To run the API tests, use `npm test` from `nodejsfiles`. For a hosted API, set
`MONGODB_URI` in the deployment platform's environment settings instead of
committing it. The legacy `url` environment variable is also supported.

Set `mongonodeurl` in `strings.xml` to `http://10.0.2.2:3003/` for local
emulator development. The optional Heroku wake-up check runs only when this
URL uses HTTPS and a `*.herokuapp.com` host.

## MySQL API (PHP)

The endpoints require PHP 8.3 or newer with PDO MySQL enabled, plus a MySQL
database. Deploy `phpfiles/config.php` alongside the `phpmanagedatatypes`
directory. Create a database named `dtypes`, then import the provided
[`dtypes.sql`](dtypes.sql) schema to create and populate the
`textletsretrofit` table.

Configure the connection with `DB_HOST`, `DB_USER`, and `DB_PASSWORD`.
`DB_NAME` is optional and defaults to `dtypes`; `DB_PREFIX` can provide a
prefix when `DB_NAME` is not set. Keep credentials outside version control.
Set `mysqlphpurl` in `strings.xml` to the base URL that contains the
`phpmanagedatatypes` directory (for example, `https://example.com/`).

## Notes

This educational app is not a production authentication service: account
passwords are stored and exported as plain text. The calendar widget and
Apache POI `.xls` export use legacy Android-compatible libraries; evaluate
them separately before upgrading to avoid changing those features.
