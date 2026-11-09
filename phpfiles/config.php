<?php

$lochost = getenv('DB_HOST') ?: 'localhost';
$root = getenv('DB_USER') ?: 'root';
$pswrd = getenv('DB_PASSWORD') ?: '';
$prefix = getenv('DB_PREFIX') ?: '';
$databaseName = getenv('DB_NAME') ?: $prefix . 'dtypes';

function databaseConnection(): PDO
{
    global $lochost, $root, $pswrd, $databaseName;

    return new PDO(
        "mysql:host={$lochost};dbname={$databaseName};charset=utf8mb4",
        $root,
        $pswrd,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ]
    );
}
